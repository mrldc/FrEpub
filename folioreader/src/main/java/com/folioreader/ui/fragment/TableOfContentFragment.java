package com.folioreader.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.event.MessageEvent;
import com.folioreader.model.TOCLinkWrapper;
import com.folioreader.ui.activity.FolioActivityCallback;
import com.folioreader.ui.adapter.TOCAdapter;
import com.folioreader.util.AppUtil;

import org.greenrobot.eventbus.EventBus;
import org.readium.r2.shared.Link;
import org.readium.r2.shared.Publication;

import java.util.ArrayList;
import java.util.List;

import static com.folioreader.Constants.BOOK_TITLE;
import static com.folioreader.Constants.CFI;
import static com.folioreader.Constants.CHAPTER_SELECTED;
import static com.folioreader.Constants.PUBLICATION;
import static com.folioreader.Constants.SELECTED_CHAPTER_POSITION;
import static com.folioreader.Constants.TYPE;

public class TableOfContentFragment extends Fragment implements TOCAdapter.TOCCallback {
    private TOCAdapter mTOCAdapter;
    private RecyclerView mTableOfContentsRecyclerView;
    private TextView errorView;
    private ImageView deleteView;
    private Config mConfig;
    private String mBookTitle;
    private Publication publication;
    private String cfi;
    private FolioActivityCallback activityCallback;

    public static TableOfContentFragment newInstance(Publication publication,
                                                     String selectedChapterHref, String bookTitle,String cfi) {
        TableOfContentFragment tableOfContentFragment = new TableOfContentFragment();
        Bundle args = new Bundle();
        args.putSerializable(PUBLICATION, publication);
        args.putString(SELECTED_CHAPTER_POSITION, selectedChapterHref);
        args.putString(BOOK_TITLE, bookTitle);
        args.putString(CFI, cfi);
        tableOfContentFragment.setArguments(args);



        return tableOfContentFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        publication = (Publication) getArguments().getSerializable(PUBLICATION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_contents, container, false);
        mConfig = AppUtil.getSavedConfig(getActivity());
        mBookTitle = getArguments().getString(BOOK_TITLE);
        if (mConfig.isNightMode()) {
            mRootView.findViewById(R.id.recycler_view_menu).
                    setBackgroundColor(ContextCompat.getColor(getActivity(),
                            R.color.black));
        }
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTableOfContentsRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_menu);
        errorView = (TextView) view.findViewById(R.id.tv_error);
        deleteView = (ImageView) view.findViewById(R.id.iv_item_delete);
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new MessageEvent("接收到TwoActivity发送过来的事件啦"));
            }
        });
        configRecyclerViews();
        initAdapter();
        if (getActivity() instanceof FolioActivityCallback){

        }
    }

    public void configRecyclerViews() {
        mTableOfContentsRecyclerView.setHasFixedSize(true);
        mTableOfContentsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mTableOfContentsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
    }

    private void initAdapter() {
        if (publication != null) {
            if (!publication.getTableOfContents().isEmpty()) {
                ArrayList<TOCLinkWrapper> tocLinkWrappers = new ArrayList<>();
                for (Link tocLink : publication.getTableOfContents()) {
                    TOCLinkWrapper tocLinkWrapper = createTocLinkWrapper(tocLink, 0);
                    tocLinkWrappers.add(tocLinkWrapper);
                }
                onLoadTOC(tocLinkWrappers);
            } else {
                onLoadTOC(createTOCFromSpine(publication.getReadingOrder()));
            }
        } else {
            onError();
        }
    }

    /**
     * [RECURSIVE]
     * <p>
     * function generates list of {@link TOCLinkWrapper} of TOC list from publication manifest
     *
     * @param tocLink     table of content elements
     * @param indentation level of hierarchy of the child elements
     * @return generated {@link TOCLinkWrapper} list
     */
    private static TOCLinkWrapper createTocLinkWrapper(Link tocLink, int indentation) {
        TOCLinkWrapper tocLinkWrapper = new TOCLinkWrapper(tocLink, indentation);
        for (Link tocLink1 : tocLink.getChildren()) {
            TOCLinkWrapper tocLinkWrapper1 = createTocLinkWrapper(tocLink1, indentation + 1);
            if (tocLinkWrapper1.getIndentation() != 3) {
                tocLinkWrapper.addChild(tocLinkWrapper1);
            }
        }
        return tocLinkWrapper;
    }

    private static ArrayList<TOCLinkWrapper> createTOCFromSpine(List<Link> spine) {
        ArrayList<TOCLinkWrapper> tocLinkWrappers = new ArrayList<>();
        for (Link link : spine) {
            Link tocLink = new Link();
            tocLink.setTitle(link.getTitle());
            tocLink.setHref(link.getHref());
            tocLinkWrappers.add(new TOCLinkWrapper(tocLink, 0));
        }
        return tocLinkWrappers;
    }

    public void onLoadTOC(ArrayList<TOCLinkWrapper> tocLinkWrapperList) {
        mTOCAdapter = new TOCAdapter(getActivity(), tocLinkWrapperList,
                getArguments().getString(SELECTED_CHAPTER_POSITION), mConfig,getArguments().getString(CFI));
        mTOCAdapter.setCallback(this);
        mTableOfContentsRecyclerView.setAdapter(mTOCAdapter);
    }

    public void onError() {
        errorView.setVisibility(View.VISIBLE);
        mTableOfContentsRecyclerView.setVisibility(View.GONE);
        errorView.setText("Table of content \n not found");
    }

    @Override
    public void onTocClicked(int position) {
        TOCLinkWrapper tocLinkWrapper = (TOCLinkWrapper) mTOCAdapter.getItemAt(position);

        activityCallback.goToChapter(tocLinkWrapper.getTocLink().getHref(),cfi);

    }

    @Override
    public void onExpanded(int position) {
        TOCLinkWrapper tocLinkWrapper = (TOCLinkWrapper) mTOCAdapter.getItemAt(position);
        if (tocLinkWrapper.getChildren() != null && tocLinkWrapper.getChildren().size() > 0) {
            mTOCAdapter.toggleGroup(position);
        }
    }

    public void setActivityCallback(FolioActivityCallback activityCallback) {
        this.activityCallback = activityCallback;
    }
}