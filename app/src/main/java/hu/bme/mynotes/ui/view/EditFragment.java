package hu.bme.mynotes.ui.view;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import hu.bme.mynotes.R;

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
    }

    public String getText() {
        return input.getText().toString();
    }
}
