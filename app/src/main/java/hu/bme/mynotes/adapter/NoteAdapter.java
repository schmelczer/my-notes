package hu.bme.mynotes.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import hu.bme.mynotes.R;
import hu.bme.mynotes.data.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

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
        holder.titleView.setText(note.getTitle());
        holder.note = note;
        holder.drawTags();
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
        private ViewGroup tagContainer;

        TextView titleView;
        Note note;

        NoteViewHolder(View view) {
            super(view);
            titleView = view.findViewById(R.id.noteTitle);
            tagContainer = view.findViewById(R.id.tags);

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

        public void drawTags() {
            tagContainer.removeAllViews();
            Context ctx = tagContainer.getContext();

            int i = 0;
            for (String tag : note.getTags()) {
                if(++i > 3) {
                    break;
                }

                View parent = (
                    (LayoutInflater)(ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                ).inflate(
                    R.layout.tag, tagContainer, false
                );

                TextView tagView = parent.findViewById(R.id.tag);
                tagView.setText(Html.fromHtml(String.format(
                    "<font color=\"#%s\">#</font><i>%s</i>",
                    Integer.toHexString(
                        ContextCompat.getColor(ctx, R.color.colorPrimaryBright) & 0x00ffffff
                    ),
                    tag.substring(1)
                )));

                tagContainer.addView(parent);
            }
        }
    }
}