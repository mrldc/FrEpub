package com.folioreader.model.event;

/**
 * This event is used when web page in {@link com.folioreader.ui.fragment.FolioPageFragment}
 * is to reloaded.
 */
public class ChangeBackgroundEvent {
    private String color;
    public ChangeBackgroundEvent(String color){
        //去除透明属性
        this.color = color.charAt(0)+color.substring(3);
    }

    public String getColor() {
        return color;
    }
}
