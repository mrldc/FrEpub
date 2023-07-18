package com.folioreader.model.db;

public class PageProgress {
    public int id;
    public String bookId;
    public String href;
    /**开始百分比**/
    public float start;
    /**结束百分比**/
    public float end;
    /**所在页数**/
    public int pageNumber;
    /**内容大小**/
    public int contentSize;
}
