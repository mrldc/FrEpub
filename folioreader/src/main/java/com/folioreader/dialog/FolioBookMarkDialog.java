package com.folioreader.dialog;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.folioreader.R;
import com.folioreader.util.InputMethodUtils;
import com.kongzue.dialogx.dialogs.CustomDialog;
import com.kongzue.dialogx.interfaces.OnBindView;

/**
 * @author： libaixing
 * time：2023/7/12 15:19
 * description：笔记编辑、删除dialog
 * updateUser：
 * updateDate：2023/7/12 15:19
 */
public class FolioBookMarkDialog extends CustomDialog {

    public void showDialog(){
        CustomDialog.build()
                .setCustomView(new OnBindView<CustomDialog>(R.layout.dialog_folio_bookmark) {
                    @Override
                    public void onBind(final CustomDialog dialog, View v) {
                        RelativeLayout rlComment;
                        TextView tvBookmarkDelete;
                        TextView tvCancel;
                        TextView tvEdit;
                        final EditText etContent;
                        rlComment = v.findViewById(R.id.rl_comment);
                        tvBookmarkDelete = v.findViewById(R.id.tv_bookmark_delete);
                        tvCancel = v.findViewById(R.id.tv_cancel);
                        tvEdit = v.findViewById(R.id.tv_edit);
                        etContent = v.findViewById(R.id.et_page_note);
                        v.findViewById(R.id.view_line).setVisibility(View.GONE);
                        tvBookmarkDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //删除
                                dialog.dismiss();
                            }
                        });
                        tvCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        tvEdit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //编辑
                                dialog.dismiss();
                            }
                        });

                    }
                })
                .setMaskColor(Color.parseColor("#80000000"))
                .show();
    }
}
