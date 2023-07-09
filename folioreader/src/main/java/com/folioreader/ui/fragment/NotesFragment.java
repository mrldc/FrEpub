package com.folioreader.ui.fragment;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.FolioReader;
import com.folioreader.R;
import com.folioreader.model.sqlite.BookmarkTable;
import com.folioreader.ui.adapter.BookmarkAdapter;
import com.folioreader.util.AppUtil;

import java.util.HashMap;

/**
 * 笔记fragment主页面
 */
public class NotesFragment extends Fragment {
    private String mBookId;
    private View mRootView;

    public static NotesFragment newInstance(String bookId, String epubTitle) {
        NotesFragment bookmarkFragment = new NotesFragment();
        Bundle args = new Bundle();
        args.putString(FolioReader.EXTRA_BOOK_ID, bookId);
        args.putString(Constants.BOOK_TITLE, epubTitle);
        bookmarkFragment.setArguments(args);
        return bookmarkFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_notes, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.i("Bookmark fragment", "onViewCreated: inside onViewCreated ");
        super.onViewCreated(view, savedInstanceState);
        Config config = AppUtil.getSavedConfig(getActivity());

        mBookId = getArguments().getString(FolioReader.EXTRA_BOOK_ID);
        Log.i("Bookmark fragment", "onViewCreated: mbookID " + mBookId);
        loadHighlightsFragment();

    }
    private void loadHighlightsFragment() {
        String bookId = getActivity().getIntent().getStringExtra(FolioReader.EXTRA_BOOK_ID);
        String bookTitle = getActivity().getIntent().getStringExtra(Constants.BOOK_TITLE);
        HighlightFragment highlightFragment = HighlightFragment.newInstance(bookId, bookTitle);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.parent_notes, highlightFragment);
        ft.commit();
    }

}
