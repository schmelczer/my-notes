package hu.bme.mynotes;

import android.content.Intent;
import android.os.Bundle;

import hu.bme.mynotes.adapter.NoteAdapter;
import hu.bme.mynotes.data.Note;
import hu.bme.mynotes.data.NoteDatabase;
import hu.bme.mynotes.business.NoteEditor;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OpenNoteListener {
    public final static String NOTE_KEY = "note";


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

        NoteAdapter adapter = new NoteAdapter(this);
        NoteEditor.initialize(database.getNoteDao(), adapter);

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

    public void editNote(Note note) {
        NoteEditor.getInstance().startEditing(note);
        startActivity(new Intent(MainActivity.this, NoteActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
