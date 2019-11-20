package hu.bme.mynotes.view;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.concurrent.Executors;

import hu.bme.mynotes.R;
import io.noties.markwon.Markwon;
import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.MarkwonEditorTextWatcher;

public class EditFragment extends Fragment {
    private EditText input;

    public EditFragment() {
    }


    @Override
    public View onCreateView (
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        input = getView().findViewById(R.id.textInput);

        final Markwon markwon = Markwon.create(view.getContext());

        final MarkwonEditor editor = MarkwonEditor.create(markwon);

        input.addTextChangedListener(MarkwonEditorTextWatcher.withPreRender(
                editor,
                Executors.newCachedThreadPool(),
                input));

        editor.process(input.getText());
    }

    public String getText() {
        return input.getText().toString();
    }

    public void setText(String text) {
        input.setText(text);
    }
}
