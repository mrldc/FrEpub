package com.folioreader.model;

/**
 * 划线、高亮、书签等显示类
 */
public class MarkVo {
    private int id;
    private String bookId;
    //选中的文字
    private String content;
    //笔记
    private String node;
    //标记的种类：1：书签 2：页笔记 3：划线, 4 （段落笔记）
    private String kind;
    //高亮类型
    private String highlightType;
    //所处位置信息
    private String cfi;
}
