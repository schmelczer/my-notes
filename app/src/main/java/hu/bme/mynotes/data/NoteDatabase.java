package hu.bme.mynotes.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;


@Database(
        entities = {Note.class},
        version = 6,
        exportSchema = false
)
public abstract class NoteDatabase extends RoomDatabase {
    public abstract NoteDao getNoteDao();
}