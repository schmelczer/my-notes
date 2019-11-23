package hu.bme.mynotes.editor;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hu.bme.mynotes.R;
import hu.bme.mynotes.helper.ColorHelpers;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.image.glide.GlideImagesPlugin;

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
        markwon =  Markwon.builder(container.getContext())
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                        builder.linkColor(ColorHelpers.getBrightColor())
                                .blockQuoteColor(ColorHelpers.getBrightColor())
                                .listItemColor(ColorHelpers.getBrightColor())
                                .headingBreakHeight(0);

                    }
                })
                .usePlugin(GlideImagesPlugin.create(container.getContext()))
                .build();

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
