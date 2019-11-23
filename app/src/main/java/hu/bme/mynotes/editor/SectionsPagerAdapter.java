package hu.bme.mynotes.editor;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import hu.bme.mynotes.R;

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    public static final int EDIT_PAGE = 0;
    public static final int VIEW_PAGE = 1;
    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2};
    private final Context mContext;
    private EditFragment edit;
    private ViewFragment view;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        edit = new EditFragment();
        view = new ViewFragment();
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return edit;
        } else if (position == 1) {
            return view;
        }

        throw new RuntimeException("No appropriate fragment found.");
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }

    public String getText() {
        return edit.getText();
    }

    public void setText(String text) {
        view.setText(text);
    }

    public void setTextForEditor(String text) {
        edit.setText(text);
    }
}