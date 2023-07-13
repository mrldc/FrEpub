package com.folioreader.model.event;

/**
 * This event is used when web page in {@link com.folioreader.ui.fragment.FolioPageFragment}
 * is to reloaded.
 */
//写笔记事件
public class HighlightNoteEvent {
    public String content;
    public HighlightNoteEvent(String content){
        this.content = content;
    }
}
