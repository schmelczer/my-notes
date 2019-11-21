package hu.bme.mynotes;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import hu.bme.mynotes.adapter.NoteAdapter;
import hu.bme.mynotes.data.Note;
import hu.bme.mynotes.data.NoteDatabase;
import hu.bme.mynotes.business.NoteEditor;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import java.util.Set;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OpenNoteListener, NoteEditor.OnTagsChanged {
    public final static String NOTE_KEY = "note";

    private ViewGroup tagsContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);

        NoteDatabase database = Room.databaseBuilder (
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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NoteEditor.getInstance().startEditing(null);
                startActivity(new Intent(MainActivity.this, NoteActivity.class));
            }
        });

        NoteEditor.getInstance().loadNotesInBackground();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
        Snackbar.make(findViewById(R.id.main), getResources().getString(R.string.sure_delete), Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NoteEditor.getInstance().onNoteCreated(note);
                    }
                })
                .setActionTextColor(Color.YELLOW)
                .show();
    }

    @Override
    public void openNote(Note note) {
        NoteEditor.getInstance().startEditing(note);
        Intent intent = new Intent(MainActivity.this, NoteActivity.class);
        intent.putExtra(NOTE_KEY, "open");
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTagsChanged(Set<String> tags) {
        tagsContainer.removeAllViews();
        for (String t : tags) {
            final String tag = t;
            View parent = getLayoutInflater().inflate(R.layout.tag, null);

            CheckBox show = parent.findViewById(R.id.show);
            show.setText(t);
            show.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        NoteEditor.getInstance().addSelectedTag(tag);
                    } else {
                        NoteEditor.getInstance().removeSelectedTag(tag);
                    }
                }
            });

            tagsContainer.addView(parent, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ));
        }
    }
}
