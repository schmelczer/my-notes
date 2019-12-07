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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import hu.bme.mynotes.helper.ColorHelper;


public class MainActivity extends AppCompatActivity implements NoteAdapter.OpenNoteListener, NoteEditor.OnTagsChanged {
    public final static String NOTE_KEY = "note";
    public final static String NOTE_VALUE_OPEN = "open";
    private final static int IMPORT_LOCATION_REQUEST = 8777;
    private final static int EXPORT_LOCATION_REQUEST = 9999;

    private Set<String> knownTags = new HashSet<>();
    private ViewGroup tagsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tagsContainer = findViewById(R.id.tags);

        setSupportActionBar(findViewById(R.id.toolbar));

        ColorHelper.init(this);

        NoteAdapter adapter = new NoteAdapter(this);

        initializeNoteEditor(adapter);
        initializeRecyclerView(adapter);
        initializeFab();

        NoteEditor.getInstance().loadNotesInBackground();
    }

    private void initializeNoteEditor(NoteAdapter adapter) {
        NoteEditor.initialize(NoteDatabase.getInstance(this).getNoteDao(), adapter, this);
    }

    private void initializeRecyclerView(NoteAdapter adapter) {
        RecyclerView recyclerView = findViewById(R.id.MainRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void initializeFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            NoteEditor.getInstance().startEditing(null);
            startActivity(new Intent(MainActivity.this, NoteActivity.class));
        });
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
    public void openNote(Note note) {
        NoteEditor.getInstance().startEditing(note);

        Intent intent = new Intent(MainActivity.this, NoteActivity.class);
        intent.putExtra(NOTE_KEY, NOTE_VALUE_OPEN);
        startActivity(intent);
    }

    @Override
    public void deleteNote(final Note note) {
        NoteEditor.getInstance().onNoteDeleted(note);

        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.main), getResources().getString(R.string.sure_delete), Snackbar.LENGTH_LONG)
                .setAction(
                        getResources().getText(R.string.undo), v -> NoteEditor.getInstance().onNoteCreated(note)
                )
                .setActionTextColor(getResources().getColor(R.color.colorPrimaryBright));

        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.colorBackground));
        snackbar.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_import:
                Intent intentImport = new Intent(Intent.ACTION_GET_CONTENT);
                intentImport.addCategory(Intent.CATEGORY_OPENABLE);
                intentImport.setType("application/json");
                startActivityForResult(intentImport, IMPORT_LOCATION_REQUEST);
                return true;
            case R.id.action_export:
                Intent intentExport = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intentExport.addCategory(Intent.CATEGORY_OPENABLE);
                intentExport.setType("application/json");
                intentExport.putExtra(Intent.EXTRA_TITLE, "notes.json");
                startActivityForResult(intentExport, EXPORT_LOCATION_REQUEST);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportNotes(Uri uri) {
        Gson gson = new GsonBuilder().create();
        String exported = gson.toJson(NoteEditor.getInstance().getNotes());

        try (
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
                FileOutputStream fileOutputStream = new FileOutputStream(
                        Objects.requireNonNull(pfd).getFileDescriptor()
                )
        ) {
            fileOutputStream.write(exported.getBytes());
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show();

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
            case IMPORT_LOCATION_REQUEST:
                importNotes(data.getData());
                break;
            case EXPORT_LOCATION_REQUEST:
                exportNotes(data.getData());
                break;
        }
    }

    @Override
    public void onTagsChanged(Set<String> tags) {
        tagsContainer.removeAllViews();
        selectNewTags(tags);

        Set<String> selectedTags = NoteEditor.getInstance().getSelectedTags();
        for (String tag : tags) {
            drawCheckboxForTag(tag, selectedTags.contains(tag));
        }
    }

    private void selectNewTags(Set<String> tags) {
        for (String t : tags) {
            if (!knownTags.contains(t)) {
                knownTags.add(t);
                NoteEditor.getInstance().addSelectedTag(t);
            }
        }
    }

    private void drawCheckboxForTag(final String tag, boolean isInitiallyChecked) {
        View parent = getLayoutInflater().inflate(
                R.layout.checkable_tag, tagsContainer, false
        );

        CheckBox show = parent.findViewById(R.id.show);
        show.setText(ColorHelper.formatTag(tag));
        show.setChecked(isInitiallyChecked);

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
