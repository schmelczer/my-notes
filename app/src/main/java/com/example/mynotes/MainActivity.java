package com.example.mynotes;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.mynotes.adapter.NoteAdapter;
import com.example.mynotes.data.Note;
import com.example.mynotes.data.NoteDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OpenNoteListener {
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private NoteDatabase database;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                startActivity(intent);
            }
        });

        database = Room.databaseBuilder(
                getApplicationContext(),
                NoteDatabase.class,
                "notes"
        ).build();

        initRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.MainRecyclerView);
        adapter = new NoteAdapter(this);
        loadNotesInBackground();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadNotesInBackground() {
        new AsyncTask<Void, Void, List<Note>>() {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return database.getNoteDao().getAll();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                adapter.update(notes);
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void onNoteChanged(final Note note) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                database.getNoteDao().update(note);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean isSuccessful) {
                Log.d("MainActivity", "Note update was successful");
            }
        }.execute();
    }
}
