package hu.bme.mynotes.business;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import java.util.List;

import hu.bme.mynotes.adapter.NoteAdapter;
import hu.bme.mynotes.data.Note;
import hu.bme.mynotes.data.NoteDao;

public class NoteEditor {
    private static NoteEditor instance = null;

    private NoteDao dao;
    private NoteAdapter adapter;

    private Note editedNote;

    private NoteEditor(NoteDao dao, NoteAdapter adapter) {
        this.dao = dao;
        this.adapter = adapter;
    }

    public static void initialize(NoteDao dao, NoteAdapter adapter) {
       instance = new NoteEditor(dao, adapter);
    }

    public static NoteEditor getInstance() {
        if (instance == null) {
            throw new RuntimeException("Uninitialized.");
        }

        return instance;
    }

    public void startEditing(@Nullable Note note) {
        if (note == null) {
            note = new Note();
            onNoteCreated(note);
        }
        editedNote = note;
    }

    public Note getEditedNote() {
        return editedNote;
    }

    public void saveEdited() {
        onNoteChanged(editedNote);
    }

    @SuppressLint("StaticFieldLeak")
    public void loadNotesInBackground() {
        new AsyncTask<Void, Void, List<Note>>() {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return dao.getAll();
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
                dao.update(note);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean isSuccessful) {
                loadNotesInBackground();
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void onNoteCreated(final Note note) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                note.id = dao.insert(note);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean isSuccessful) {
                adapter.add(note);
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void onNoteDeleted(final Note note) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                dao.delete(note);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean isSuccessful) {
                adapter.remove(note);
            }
        }.execute();
    }
}
