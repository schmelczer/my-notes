package hu.bme.mynotes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import hu.bme.mynotes.adapter.NoteAdapter;
import hu.bme.mynotes.business.NoteEditor;
import hu.bme.mynotes.data.Note;
import hu.bme.mynotes.data.NoteDatabase;
import hu.bme.mynotes.helper.ColorHelpers;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OpenNoteListener, NoteEditor.OnTagsChanged {
    public final static String NOTE_KEY = "note";
    public final static String NOTE_VALUE_OPEN = "open";
    private static final int CHOOSE_FILE_REQUEST_CODE = 8777;
    private static final int CHOOSE_DIRECTORY_REQUEST_CODE = 9999;
    private Set<String> knownTags;

    private ViewGroup tagsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        knownTags = new HashSet<>();

        ColorHelpers.init(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);

        NoteDatabase database = Room.databaseBuilder(
                getApplicationContext(),
                NoteDatabase.class,
                "notes"
        )
                .fallbackToDestructiveMigration()
                .build();

        tagsContainer = findViewById(R.id.tags);

        NoteAdapter adapter = new NoteAdapter(this);
        NoteEditor.initialize(database.getNoteDao(), adapter, this);

        RecyclerView recyclerView = findViewById(R.id.MainRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(view -> {
            NoteEditor.getInstance().startEditing(null);
            startActivity(new Intent(MainActivity.this, NoteActivity.class));
        });

        NoteEditor.getInstance().loadNotesInBackground();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void editNote(Note note) {
        NoteEditor.getInstance().startEditing(note);
        startActivity(new Intent(MainActivity.this, NoteActivity.class));
    }

    @Override
    public void deleteNote(final Note note) {
        NoteEditor.getInstance().onNoteDeleted(note);
        Snackbar snackbar = Snackbar.make(findViewById(R.id.main), getResources().getString(R.string.sure_delete), Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> NoteEditor.getInstance().onNoteCreated(note))
                .setActionTextColor(getResources().getColor(R.color.colorPrimaryBright));

        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.colorBackground));
        snackbar.show();
    }

    @Override
    public void openNote(Note note) {
        NoteEditor.getInstance().startEditing(note);
        Intent intent = new Intent(MainActivity.this, NoteActivity.class);
        intent.putExtra(NOTE_KEY, NOTE_VALUE_OPEN);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_import) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            startActivityForResult(intent, CHOOSE_FILE_REQUEST_CODE);
            return true;
        } else if (id == R.id.action_export) {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, "notes.json");
            startActivityForResult(intent, CHOOSE_DIRECTORY_REQUEST_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void exportNotes(Uri uri) {
        Gson gson = new GsonBuilder().create();
        String exported = gson.toJson(NoteEditor.getInstance().getNotes());

        try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
             FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor())
        ) {
            fileOutputStream.write(exported.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void importNotes(Uri uri) {
        StringBuilder stringBuilder = new StringBuilder();
        try (
                InputStream inputStream = getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(Objects.requireNonNull(inputStream))
                )
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String json = stringBuilder.toString();
        Gson gson = new GsonBuilder().create();
        List<Note> notes = gson.fromJson(json, new TypeToken<List<Note>>() {
        }.getType());
        for (Note note : notes) {
            NoteEditor.getInstance().onNoteCreated(note);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CHOOSE_FILE_REQUEST_CODE:
                importNotes(data.getData());
                break;
            case CHOOSE_DIRECTORY_REQUEST_CODE:
                exportNotes(data.getData());
                break;
        }
    }

    @Override
    public void onTagsChanged(Set<String> tags) {
        tagsContainer.removeAllViews();

        for (String t : tags) {
            if (!knownTags.contains(t)) {
                knownTags.add(t);
                NoteEditor.getInstance().addSelectedTag(t);
            }
        }

        Set<String> selectedTags = NoteEditor.getInstance().getSelectedTags();

        for (String t : tags) {
            final String tag = t;
            View parent = getLayoutInflater().inflate(R.layout.checkable_tag, tagsContainer, false);

            CheckBox show = parent.findViewById(R.id.show);
            show.setText(ColorHelpers.formatTag(this, tag));
            show.setChecked(selectedTags.contains(tag));

            show.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    NoteEditor.getInstance().addSelectedTag(tag);
                } else {
                    NoteEditor.getInstance().removeSelectedTag(tag);
                }
            });

            tagsContainer.addView(parent, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ));
        }
    }
}
