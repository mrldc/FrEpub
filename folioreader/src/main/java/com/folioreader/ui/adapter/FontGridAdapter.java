package com.folioreader.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.folioreader.R;
import com.folioreader.callback.FontsCallback;

import java.util.List;

public class FontGridAdapter extends RecyclerView.Adapter<FontGridAdapter.FontHolder>{
    List<String> fontNameList;
    FontsCallback fontsCallback;
    public  FontGridAdapter(List<String> fontNameList, FontsCallback callback){
        this.fontNameList = fontNameList;
        this.fontsCallback = callback;
    }



    @NonNull
    @Override
    public FontHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FontHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.font_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FontHolder holder,  int position) {
        final int pPosition = position;
        holder.textView.setText(getFontShowName(fontNameList.get(position)));
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fontName = fontNameList.get(pPosition);
                fontsCallback.selectFontCallback(fontName,getFontShowName(fontName));
            }
        });
    }

    @Override
    public int getItemCount() {
        return fontNameList.size();
    }
    static class FontHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        TextView textView;
        public FontHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_font);
            linearLayout = itemView.findViewById(R.id.ll_font_item);
        }
    }
    public static String getFontShowName(String fontName){
        if (fontName == null){
            return "系统字体";
        }
        int lastIndex = fontName.lastIndexOf(".");
        if(lastIndex > 0){
           return fontName.substring(0,lastIndex);
        }
        return fontName;
    }
}
