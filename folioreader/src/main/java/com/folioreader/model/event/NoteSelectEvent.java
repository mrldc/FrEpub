package com.folioreader.model.event;

import com.folioreader.model.MarkVo;

/**
 * This event is used when web page in {@link com.folioreader.ui.fragment.FolioPageFragment}
 * is to reloaded.
 */
public class NoteSelectEvent {
    MarkVo markVo;
    public NoteSelectEvent(MarkVo markVo){
       this.markVo = markVo;

    }

    public MarkVo getMarkVo() {
        return markVo;
    }
}
