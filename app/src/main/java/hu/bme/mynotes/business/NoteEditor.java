package hu.bme.mynotes.business;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hu.bme.mynotes.adapter.NoteAdapter;
import hu.bme.mynotes.data.Note;
import hu.bme.mynotes.data.NoteDao;

public class NoteEditor {
    private static NoteEditor instance = null;

    private List<Note> notes;
    private Set<String> selectedTags;
    private Set<String> tags;

    private NoteDao dao;
    private NoteAdapter adapter;

    private Note editedNote;

    private NoteEditor(NoteDao dao, NoteAdapter adapter) {
        this.dao = dao;
        this.adapter = adapter;
        this.selectedTags = new HashSet<>();
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


    public Set<String> getTags() {
        return tags;
    }

    public void setSelectedTags(Set<String> selectedTags) {
        this.selectedTags = selectedTags;
        showNotes();
    }

    private void setNotes(List<Note> notes) {
        this.notes = notes;
        showNotes();
    }

    private void showNotes() {
        this.tags = new HashSet<>();

        for (Note n : notes) {
            this.tags.addAll(n.getTags());
        }
        Collections.sort(notes, new Comparator<Note>() {
            @Override
            public int compare(Note n1, Note n2) {
                return n1.getTitle().compareToIgnoreCase(n2.getTitle());
            }
        });

        List<Note> shownNotes = new ArrayList<>();
        for (Note n : notes) {
            for (String tag: n.getTags()) {
                if (selectedTags.contains(tag)) {
                    shownNotes.add(n);
                    break;
                }
            }
        }
        adapter.update(shownNotes);
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
                setNotes(notes);
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
                loadNotesInBackground();
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
                loadNotesInBackground();
            }
        }.execute();
    }
}
