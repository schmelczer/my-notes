package hu.bme.mynotes.editor;


import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Objects;
import java.util.concurrent.Executors;

import hu.bme.mynotes.R;
import hu.bme.mynotes.helper.ColorHelper;
import io.noties.markwon.Markwon;
import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.MarkwonEditorTextWatcher;


public class EditFragment extends Fragment {
    private EditText input;
    private String initialText;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        input = Objects.requireNonNull(getView()).findViewById(R.id.textInput);

        if (initialText != null) {
            input.setText(initialText);
        }

        final MarkwonEditor editor = MarkwonEditor
                .builder(Markwon.create(view.getContext()))
                .punctuationSpan(CustomPunctuationSpan.class, CustomPunctuationSpan::new)
                .build();

        input.addTextChangedListener(
                MarkwonEditorTextWatcher.withPreRender(
                        editor,
                        Executors.newCachedThreadPool(),
                        input
                )
        );

        editor.process(input.getText());
    }

    String getText() {
        return input != null ? input.getText().toString() : initialText;
    }

    void setText(String text) {
        if (input == null) {
            initialText = text;
        } else {
            input.setText(text);
        }
    }

    class CustomPunctuationSpan extends ForegroundColorSpan {
        CustomPunctuationSpan() {
            super(ColorHelper.getBrightColor());
        }
    }
}
