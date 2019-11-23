package hu.bme.mynotes.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.Objects;

import hu.bme.mynotes.business.NoteEditor;


@Database(
        entities = {Note.class},
        version = 16,
        exportSchema = false
)
public abstract class NoteDatabase extends RoomDatabase {
    private static final String DB_NAME = "notes";

    private static NoteDatabase instance;
    private static RoomDatabase.Callback seedCallback = new RoomDatabase.Callback() {
        @SuppressLint("StaticFieldLeak")
        public void onCreate(@NonNull SupportSQLiteDatabase db) {

            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... voids) {
                    getInstance(null).getNoteDao().insert(getSeed());
                    return true;
                }

                @Override
                protected void onPostExecute(Boolean success) {
                    // Not the nicest solution.
                    NoteEditor.getInstance().loadNotesInBackground();
                }

            }.execute();
        }
    };

    public static synchronized NoteDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    Objects.requireNonNull(context), NoteDatabase.class, DB_NAME
            )
                    .fallbackToDestructiveMigration()
                    .addCallback(seedCallback)
                    .build();

        }

        return instance;
    }

    private static Note getSeed() {
        Note noteWelcome = new Note();
        noteWelcome.content =
                "# Welcome to **My Notes**!\n\n" +
                "With the help of [Markwon](https://github.com/noties/Markwon) you are able to use " +
                "markdown in your notes.\n" +
                "There is even support for pictures, how cool is that?\n" +
                "![some notes](https://i.imgur.com/3iG7fsQ.jpg)\n\n" +
                "With the help of #hashtags, you can **tag and filter** your #notes.\n\n" +
                "Hope you'll enjoy using it:\n" +
                "- [ ] Thank you!\n" +
                "- [ ] Of course!\n\n" +
                "> _Created by András Schmelczer_";
        return noteWelcome;
    }

    public abstract NoteDao getNoteDao();
}