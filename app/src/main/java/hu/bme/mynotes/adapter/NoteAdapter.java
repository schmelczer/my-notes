package hu.bme.mynotes.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import hu.bme.mynotes.R;
import hu.bme.mynotes.data.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private final List<Note> notes;

    private OpenNoteListener listener;

    public NoteAdapter(OpenNoteListener listener) {
        this.listener = listener;
        notes = new ArrayList<>();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.note_card, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.titleView.setText(note.content == null ? "null" : note.content);
        holder.note = note;
        Log.d("alma", String.valueOf(note.id));
        // TODO tags
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void add(Note note) {
        notes.add(note);
        notifyItemInserted(notes.size() - 1);
    }

    public void remove(Note note) {
        notes.remove(note);
        notifyDataSetChanged();
    }

    public void update(List<Note> notes) {
        this.notes.clear();
        this.notes.addAll(notes);
        notifyDataSetChanged();
    }

    public interface OpenNoteListener {
        void editNote(Note note);
        void deleteNote(Note note);
        void openNote(Note note);
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        TextView tag1View;
        TextView tag2View;
        TextView tag3View;

        Note note;

        NoteViewHolder(View view) {
            super(view);
            titleView = view.findViewById(R.id.noteTitle);
            tag1View = view.findViewById(R.id.noteTag1);
            tag2View = view.findViewById(R.id.noteTag2);
            tag3View = view.findViewById(R.id.noteTag3);

            view.findViewById(R.id.noteBody).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.openNote(note);
                }
            });

            view.findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.deleteNote(note);
                }
            });

            view.findViewById(R.id.editButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.editNote(note);
                }
            });
        }
    }
}