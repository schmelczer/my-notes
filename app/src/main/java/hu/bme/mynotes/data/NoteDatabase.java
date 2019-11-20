package hu.bme.mynotes.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(
    entities = {Note.class},
    version = 6
)
public abstract class NoteDatabase extends RoomDatabase {
    public abstract NoteDao getNoteDao();
}