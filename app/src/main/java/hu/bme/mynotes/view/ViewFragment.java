package hu.bme.mynotes.view;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hu.bme.mynotes.R;
import io.noties.markwon.Markwon;

public class ViewFragment extends Fragment {
    private String text;
    private Markwon markwon;
    private TextView textContainer;

    public ViewFragment() {
    }


    @Override
    public View onCreateView (
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
    ) {
        markwon = Markwon.create(container.getContext());
        return inflater.inflate(R.layout.fragment_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        textContainer = view.findViewById(R.id.textContainer);
        if (text != null) {
            markwon.setMarkdown(textContainer, text);
        }
    }

    public void setText(String text) {
        this.text = text;
        if (textContainer != null) {
            markwon.setMarkdown(textContainer, text);
        }
    }
}
