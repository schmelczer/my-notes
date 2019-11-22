package hu.bme.mynotes.editor;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.concurrent.Executors;

import hu.bme.mynotes.R;
import hu.bme.mynotes.business.NoteEditor;
import io.noties.markwon.Markwon;
import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.MarkwonEditorTextWatcher;

public class EditFragment extends Fragment {
    private EditText input;
    private String initialText = null;

    @Override
    public View onCreateView (
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        input = getView().findViewById(R.id.textInput);

        if (initialText != null) {
            input.setText(initialText);
            initialText = null;
        }

        final MarkwonEditor editor = MarkwonEditor.builder(Markwon.create(view.getContext()))
                .punctuationSpan(CustomPunctuationSpan.class, CustomPunctuationSpan::new)
                .build();

        input.addTextChangedListener(MarkwonEditorTextWatcher.withPreRender(
                editor,
                Executors.newCachedThreadPool(),
                input));

        editor.process(input.getText());
    }

    public String getText() {
        if (input != null) {
            Log.d("alma", input.getText().toString());
            return input.getText().toString();
        }

        return NoteEditor.getInstance().getEditedNote().content;
    }

    public void setText(String text) {
        if (input == null) {
            initialText = text;
        } else {
            input.setText(text);
        }
    }
}
