package com.folioreader.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * 划线、高亮、书签等显示类
 */
public class MarkVo implements Parcelable {
    //：书签
    public static String BookMarkType = "1";
    //：页笔记
    public static String PageNoteType = "2";
    //：划线
    public static String HighlightType = "3";
    //：段落笔记
    public static String HighlightMarkType = "4";
    private int id;
    private String bookId;
    //选中的文字
    private String content;
    //笔记
    private String note;
    //标记的种类：1：书签 2：页笔记 3：划线, 4 （段落笔记）
    private String kind;
    //高亮类型
    private String highlightType;
    //所处位置信息
    private String cfi;
    //控件id
    private String rangy;
    //所在页面地址
    private String href;
    private String date;

    private int pageNumber;
    public MarkVo(){}
    public MarkVo(Parcel in) {
        id = in.readInt();
        bookId = in.readString();
        content = in.readString();
        note = in.readString();
        kind = in.readString();
        highlightType = in.readString();
        cfi = in.readString();
        rangy = in.readString();
        href = in.readString();
        date = in.readString();
        pageNumber = in.readInt();
    }

    public static final Creator<MarkVo> CREATOR = new Creator<MarkVo>() {
        @Override
        public MarkVo createFromParcel(Parcel in) {
            return new MarkVo(in);
        }

        @Override
        public MarkVo[] newArray(int size) {
            return new MarkVo[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getHighlightType() {
        return highlightType;
    }

    public void setHighlightType(String highlightType) {
        this.highlightType = highlightType;
    }

    public String getCfi() {
        return cfi;
    }

    public void setCfi(String cfi) {
        this.cfi = cfi;
    }

    public String getRangy() {
        return rangy;
    }

    public void setRangy(String rangy) {
        this.rangy = rangy;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {

        dest.writeInt(id);
        dest.writeString(bookId);
        dest.writeString(content);
        dest.writeString(rangy);
        dest.writeString(content);
        dest.writeSerializable(note);
        dest.writeString(kind);
        dest.writeString(highlightType);
        dest.writeString(note);
        dest.writeString(cfi);
        dest.writeString(rangy);
        dest.writeString(href);
        dest.writeString(date);
        dest.writeInt(pageNumber);
    }
}
