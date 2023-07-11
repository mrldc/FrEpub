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


        holder.content.setText(Html.fromHtml(getItem(position).getContent()));
        UiUtil.setBackColorToTextView(holder.content,
                getItem(position).getHighlightType());

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

    static class NoteHolder extends RecyclerView.ViewHolder {
        private UnderlinedTextView content;
        private ImageView delete, editNote;
        private TextView date;
        private LinearLayout swipeLinearLayout;
        private RelativeLayout rlHighlightBottom;
        private TextView tvHighlightContent;

        NoteHolder(View itemView) {
            super(itemView);
            swipeLinearLayout = (LinearLayout) itemView.findViewById(R.id.swipe_linear_layout);
            rlHighlightBottom = (RelativeLayout) itemView.findViewById(R.id.rl_highlight_bottom);
            tvHighlightContent = (TextView) itemView.findViewById(R.id.tv_content);
            content = (UnderlinedTextView) itemView.findViewById(R.id.utv_highlight_content);
            delete = (ImageView) itemView.findViewById(R.id.iv_delete);
            editNote = (ImageView) itemView.findViewById(R.id.iv_edit_note);
            date = (TextView) itemView.findViewById(R.id.tv_highlight_date);
        }
    }

    public interface NoteAdapterCallback {
        void onItemClick(MarkVo markVo);

        void deleteHighlight(int id);

        void editNote(MarkVo markVo, int position);
    }
}
