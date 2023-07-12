package com.folioreader.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.FolioReader;
import com.folioreader.R;
import com.folioreader.dialog.FolioBookMarkDialog;
import com.folioreader.model.HighLight;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.MarkVo;
import com.folioreader.model.event.UpdateHighlightEvent;
import com.folioreader.model.sqlite.HighLightTable;
import com.folioreader.ui.adapter.HighlightAdapter;
import com.folioreader.ui.adapter.NoteAdapter;
import com.folioreader.util.AppUtil;
import com.folioreader.util.HighlightUtil;

import org.greenrobot.eventbus.EventBus;

public class NoteFragment extends Fragment implements NoteAdapter.NoteAdapterCallback {
    private static final String NODE_ITEM = "note_item";
    private View mRootView;
    private NoteAdapter adapter;
    private String mBookId;


    public static NoteFragment newInstance(String bookId, String epubTitle) {
        NoteFragment highlightFragment = new NoteFragment();
        Bundle args = new Bundle();
        args.putString(FolioReader.EXTRA_BOOK_ID, bookId);
        args.putString(Constants.BOOK_TITLE, epubTitle);
        highlightFragment.setArguments(args);
        return highlightFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_highlight_list, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView highlightsView = (RecyclerView) mRootView.findViewById(R.id.rv_highlights);
        Config config = AppUtil.getSavedConfig(getActivity());
        mBookId = getArguments().getString(FolioReader.EXTRA_BOOK_ID);
        initTopUi();
        if (config.isNightMode()) {
            mRootView.findViewById(R.id.rv_highlights).
                    setBackgroundColor(ContextCompat.getColor(getActivity(),
                            R.color.black));
        }
        highlightsView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        highlightsView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        adapter = new NoteAdapter(getActivity(), HighLightTable.getAllNotes(mBookId), this, config);
        highlightsView.setAdapter(adapter);
    }

    /**
     * 初始化头部按钮
     */
    private void initTopUi() {
        mRootView.findViewById(R.id.tv_all).setEnabled(true);
        mRootView.findViewById(R.id.tv_all).setSelected(true);
        //全部
        mRootView.findViewById(R.id.tv_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statuTop(true, false, false, false);
            }
        });
        //划线
        mRootView.findViewById(R.id.tv_line).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statuTop(false, true, false, false);
            }
        });
        //笔记
        mRootView.findViewById(R.id.tv_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statuTop(false, false, true, false);
            }
        });
        //书签
        mRootView.findViewById(R.id.tv_bookmark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statuTop(false, false, false, true);
            }
        });
    }

    /**
     * 头部状态
     */
    private void statuTop(Boolean all, Boolean line, Boolean note, Boolean bookmark) {
        int textColor = ContextCompat.getColor(getContext(), R.color.red_DD4F0F);
        int textColorNormal = ContextCompat.getColor(getContext(), R.color.black_333333);
        TextView tv_all = (TextView)mRootView.findViewById(R.id.tv_all);
        TextView tv_line = (TextView)mRootView.findViewById(R.id.tv_line);
        TextView tv_note = (TextView)mRootView.findViewById(R.id.tv_note);
        TextView tv_bookmark = (TextView)mRootView.findViewById(R.id.tv_bookmark);
        tv_all.setSelected(all);
        tv_line.setSelected(line);
        tv_note.setSelected(note);
        tv_bookmark.setSelected(bookmark);
        tv_all.setTextColor(all ? textColor : textColorNormal);
        tv_line.setTextColor(line ? textColor : textColorNormal);
        tv_note.setTextColor(note ? textColor : textColorNormal);
        tv_bookmark.setTextColor(bookmark ? textColor : textColorNormal);

    }

    @Override
    public void onItemClick(MarkVo markVo) {
//        Intent intent = new Intent();
//        intent.putExtra(NODE_ITEM, markVo);
//        intent.putExtra(Constants.TYPE, Constants.HIGHLIGHT_SELECTED);
//        getActivity().setResult(Activity.RESULT_OK, intent);
//        getActivity().finish();
        new FolioBookMarkDialog().showDialog();
    }

    @Override
    public void deleteHighlight(int id) {
        if (HighLightTable.deleteHighlight(id)) {
            EventBus.getDefault().post(new UpdateHighlightEvent());
        }
    }

    @Override
    public void editNote(final MarkVo markVo, final int position) {
        final Dialog dialog = new Dialog(getActivity(), R.style.DialogCustomTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_notes);
        dialog.show();
        String noteText = markVo.getNote();
        ((EditText) dialog.findViewById(R.id.edit_note)).setText(noteText);

        dialog.findViewById(R.id.btn_save_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String note =
                        ((EditText) dialog.findViewById(R.id.edit_note)).getText().toString();
                if (!TextUtils.isEmpty(note)) {
                    markVo.setNote(note);
                   /* if (HighLightTable.updateHighlight(highlightImpl)) {
                        HighlightUtil.sendHighlightBroadcastEvent(
                                NoteFragment.this.getActivity().getApplicationContext(),
                                highlightImpl,
                                HighLight.HighLightAction.MODIFY);
                        adapter.editNote(note, position);
                    }*/
                    dialog.dismiss();
                } else {
                    Toast.makeText(getActivity(),
                            getString(R.string.please_enter_note),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}


