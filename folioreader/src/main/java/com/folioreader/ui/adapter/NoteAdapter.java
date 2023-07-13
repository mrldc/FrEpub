package com.folioreader.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.MarkVo;
import com.folioreader.ui.view.UnderlinedTextView;
import com.folioreader.util.AppUtil;
import com.folioreader.util.UiUtil;

import java.util.List;

/**
 * @author gautam chibde on 16/6/17.
 */

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {
    private List<MarkVo> markVos;
    private NoteAdapterCallback callback;
    private Context context;
    private Config config;

    public NoteAdapter(Context context, List<MarkVo> markVos, NoteAdapterCallback callback, Config config) {
        this.context = context;
        this.markVos = markVos;
        this.callback = callback;
        this.config = config;
    }

    @Override
    public NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NoteHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_highlight, parent, false));
    }

    @Override
    public void onBindViewHolder(final NoteHolder holder, @SuppressLint("RecyclerView") final int position) {

        final int pPosition = position;
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onItemClick(markVos.get(pPosition));
            }
        });
        holder.rlHighlightBottom.setVisibility(View.GONE);
        MarkVo markVo = getItem(position);
        if(MarkVo.PageNoteType.equals(markVo.getKind())){
            holder.tvHighlightContent.setText(getItem(position).getContent());
            holder.rlHighlightBottom.setVisibility(View.VISIBLE);
            holder.content.setText(Html.fromHtml(getItem(position).getNote()));
        }else if( MarkVo.HighlightMarkType.equals(markVo.getKind())){
            holder.tvHighlightContent.setText(getItem(position).getContent());
            holder.rlHighlightBottom.setVisibility(View.VISIBLE);
            holder.content.setText(Html.fromHtml(getItem(position).getNote()));
            holder.content.setUnderline(true);
        }else if(MarkVo.HighlightType.equals(markVo.getKind())){
            UiUtil.setBackColorToTextView(holder.content,
                    getItem(position).getHighlightType());
            holder.content.setText(Html.fromHtml(getItem(position).getContent()));
            holder.content.setUnderline(true);
        }else{
            holder.content.setText(Html.fromHtml(getItem(position).getContent()));
        }

        if("1".equals(markVo.getKind())){//书签
            holder.imageView.setImageResource(R.mipmap.ic_bookmark_item);
        }else  if("2".equals(markVo.getKind())) {//页笔记
            holder.imageView.setImageResource(R.mipmap.ic_write_item);
        }else  if("3".equals(markVo.getKind())) {//高亮
            holder.imageView.setImageResource(R.mipmap.ic_highline_item);
        }else  if("4".equals(markVo.getKind())) {//段落笔记
            holder.imageView.setImageResource(R.mipmap.ic_write_item);
        }

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.deleteHighlight(getItem(position).getId());
                markVos.remove(position);
                notifyDataSetChanged();

            }
        });
        holder.editNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.editNote(getItem(position), position);
            }
        });
        if (config.isNightMode()) {
            holder.date.setTextColor(ContextCompat.getColor(context,
                    R.color.white));
            holder.content.setTextColor(ContextCompat.getColor(context,
                    R.color.white));
        } else {
            holder.date.setTextColor(ContextCompat.getColor(context,
                    R.color.black));
            holder.content.setTextColor(ContextCompat.getColor(context,
                    R.color.black));
        }
    }

    private MarkVo getItem(int position) {
        return markVos.get(position);
    }

    @Override
    public int getItemCount() {
        return markVos.size();
    }
    public void setData(List<MarkVo> markVoList){
        this.markVos = markVoList;
    }

    static class NoteHolder extends RecyclerView.ViewHolder {
        private UnderlinedTextView content;
        private ImageView delete, editNote,imageView;
        private TextView date;
        private LinearLayout swipeLinearLayout;
        private RelativeLayout rlHighlightBottom;
        private TextView tvHighlightContent;

        private LinearLayout itemLayout;

        NoteHolder(View itemView) {
            super(itemView);
            swipeLinearLayout = (LinearLayout) itemView.findViewById(R.id.swipe_linear_layout);
            itemLayout = (LinearLayout) itemView.findViewById(R.id.rl_note_item);
            rlHighlightBottom = (RelativeLayout) itemView.findViewById(R.id.rl_highlight_bottom);
            tvHighlightContent = (TextView) itemView.findViewById(R.id.tv_content);
            content = (UnderlinedTextView) itemView.findViewById(R.id.utv_highlight_content);
            delete = (ImageView) itemView.findViewById(R.id.iv_delete);
            editNote = (ImageView) itemView.findViewById(R.id.iv_edit_note);
            date = (TextView) itemView.findViewById(R.id.tv_highlight_date);
            imageView = itemView.findViewById(R.id.iv_item);
        }
    }

    public interface NoteAdapterCallback {
        void onItemClick(MarkVo markVo);

        void deleteHighlight(int id);

        void editNote(MarkVo markVo, int position);
    }
}
