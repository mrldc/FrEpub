package com.folioreader.model.sqlite;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.folioreader.Constants;
import com.folioreader.model.HighLight;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.MarkVo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class HighLightTable {
    public static final String TABLE_NAME = "highlight_table";
    public static final String MARK_TYPE = "mark";

    public static final String ID = "_id";
    public static final String COL_BOOK_ID = "bookId";
    private static final String COL_CONTENT = "content";
    private static final String COL_DATE = "date";
    private static final String COL_TYPE = "type";
    private static final String COL_PAGE_NUMBER = "page_number";
    private static final String COL_PAGE_ID = "pageId";
    private static final String COL_RANGY = "rangy";
    private static final String COL_NOTE = "note";
    private static final String COL_UUID = "uuid";

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
            + COL_BOOK_ID + " TEXT" + ","
            + COL_CONTENT + " TEXT" + ","
            + COL_DATE + " TEXT" + ","
            + COL_TYPE + " TEXT" + ","
            + COL_PAGE_NUMBER + " INTEGER" + ","
            + COL_PAGE_ID + " TEXT" + ","
            + COL_RANGY + " TEXT" + ","
            + COL_UUID + " TEXT" + ","
            + COL_NOTE + " TEXT" + ")";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String TAG = HighLightTable.class.getSimpleName();

    public static ContentValues getHighlightContentValues(HighLight highLight) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_BOOK_ID, highLight.getBookId());
        contentValues.put(COL_CONTENT, highLight.getContent());
        contentValues.put(COL_DATE, getDateTimeString(highLight.getDate()));
        contentValues.put(COL_TYPE, highLight.getType());
        contentValues.put(COL_PAGE_NUMBER, highLight.getPageNumber());
        contentValues.put(COL_PAGE_ID, highLight.getPageId());
        contentValues.put(COL_RANGY, highLight.getRangy());
        contentValues.put(COL_NOTE, highLight.getNote());
        contentValues.put(COL_UUID, highLight.getUUID());
        return contentValues;
    }


    @SuppressLint("Range")
    public static ArrayList<HighlightImpl> getAllHighlights(String bookId) {
        ArrayList<HighlightImpl> highlights = new ArrayList<>();
        Cursor highlightCursor = DbAdapter.getHighLightsForBookId(bookId);
        while (highlightCursor.moveToNext()) {
            highlights.add(new HighlightImpl(highlightCursor.getInt(highlightCursor.getColumnIndex(ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_BOOK_ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_CONTENT)),
                    getDateTime(highlightCursor.getString(highlightCursor.getColumnIndex(COL_DATE))),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_TYPE)),
                    highlightCursor.getInt(highlightCursor.getColumnIndex(COL_PAGE_NUMBER)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_PAGE_ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_RANGY)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_NOTE)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_UUID))));
        }
        return highlights;
    }

    @SuppressLint("Range")
    public static HighlightImpl getHighlightId(int id) {
        Cursor highlightCursor = DbAdapter.getHighlightsForId(id);
        HighlightImpl highlightImpl = new HighlightImpl();
        while (highlightCursor.moveToNext()) {
            highlightImpl = new HighlightImpl(highlightCursor.getInt(highlightCursor.getColumnIndex(ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_BOOK_ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_CONTENT)),
                    getDateTime(highlightCursor.getString(highlightCursor.getColumnIndex(COL_DATE))),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_TYPE)),
                    highlightCursor.getInt(highlightCursor.getColumnIndex(COL_PAGE_NUMBER)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_PAGE_ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_RANGY)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_NOTE)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_UUID)));

        }
        return highlightImpl;
    }

    public static long insertHighlight(HighlightImpl highlightImpl) {
        highlightImpl.setUUID(UUID.randomUUID().toString());
        return DbAdapter.saveHighLight(getHighlightContentValues(highlightImpl));
    }

    public static boolean deleteHighlight(String rangy) {
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_RANGY + " = \"" + rangy + "\"";
        int id = DbAdapter.getIdForQuery(query);
        return id != -1 && deleteHighlight(id);
    }

    public static boolean deleteHighlight(int highlightId) {
        return DbAdapter.deleteById(TABLE_NAME, ID, String.valueOf(highlightId));
    }

    @SuppressLint("Range")
    public static List<String> getHighlightsForPageId(String pageId) {
        String query = "SELECT " + COL_RANGY + " FROM " + TABLE_NAME + " WHERE " + COL_PAGE_ID + " = \"" + pageId + "\"";
        Cursor c = DbAdapter.getHighlightsForPageId(query, pageId);
        List<String> rangyList = new ArrayList<>();
        while (c.moveToNext()) {
            rangyList.add(c.getString(c.getColumnIndex(COL_RANGY)));
        }
        c.close();
        return rangyList;
    }
    @SuppressLint("Range")
    public static List<String> getHighlightsForRangy(String rangy) {
        String query = "SELECT " + COL_RANGY + " FROM " + TABLE_NAME + " WHERE " + COL_RANGY + " = '" + rangy + "'";
        Cursor c = DbAdapter.getHighlightsBySql(query);
        List<String> rangyList = new ArrayList<>();
        while (c.moveToNext()) {
            rangyList.add(c.getString(c.getColumnIndex(COL_RANGY)));
        }
        c.close();
        return rangyList;
    }
    public static boolean updateHighlight(HighlightImpl highlightImpl) {
        return DbAdapter.updateHighLight(getHighlightContentValues(highlightImpl), String.valueOf(highlightImpl.getId()));
    }

    public static String getDateTimeString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constants.DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(date);
    }

    public static Date getDateTime(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constants.DATE_FORMAT, Locale.getDefault());
        Date date1 = new Date();
        try {
            date1 = dateFormat.parse(date);
        } catch (ParseException e) {
            Log.e(TAG, "Date parsing failed", e);
        }
        return date1;
    }

    public static HighlightImpl updateHighlightStyle(String rangy, String style) {
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_RANGY + " = \"" + rangy + "\"";
        int id = DbAdapter.getIdForQuery(query);
        if (id != -1 && update(id, updateRangy(rangy, style), style.replace("highlight_", ""))) {
            return getHighlightId(id);
        }
        return null;
    }

    public static HighlightImpl getHighlightForRangy(String rangy) {
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_RANGY + " = \"" + rangy + "\"";
        return getHighlightId(DbAdapter.getIdForQuery(query));
    }

    private static String updateRangy(String rangy, String style) {
        /*Pattern p = Pattern.compile("\\highlight_\\w+");
        Matcher m = p.matcher(rangy);
        return m.replaceAll(style);*/
        String[] s = rangy.split("\\$");
        StringBuilder builder = new StringBuilder();
        for (String p : s) {
            if (TextUtils.isDigitsOnly(p)) {
                builder.append(p);
                builder.append('$');
            } else {
                builder.append(style);
                builder.append('$');
            }
        }
        return builder.toString();
    }

    private static boolean update(int id, String s, String color) {
        HighlightImpl highlightImpl = getHighlightId(id);
        highlightImpl.setRangy(s);
        highlightImpl.setType(color);
        return DbAdapter.updateHighLight(getHighlightContentValues(highlightImpl), String.valueOf(id));
    }

    public static void saveHighlightIfNotExists(HighLight highLight) {
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_UUID + " = \"" + highLight.getUUID() + "\"";
        int id = DbAdapter.getIdForQuery(query);
        if (id == -1) {
            DbAdapter.saveHighLight(getHighlightContentValues(highLight));
        }
    }
        //获取全部

    @SuppressLint("Range")
    public static List<MarkVo> getAllNotes(String mBookId) {
        StringBuilder sb = new StringBuilder();
        //查询书签与页笔记
        sb.append("select * from (");
        sb.append("select _id as id,bookID as bookId,  content,  note, type as kind,null as highLightType,cfi,null as rangy,readlocator as href,date, page_number as pageNumber from bookmark_table");
        sb.append(" where bookID='").append(mBookId).append("' ");
        sb.append(" union all ");
        sb.append(" select _id as id,bookId,content,note,case when type='mark' then '4' else '3'end as kind,type as highLightType, null as cfi ,rangy,pageId as href, date,page_number as pageNumber  from highlight_table ");
        sb.append(" where bookId='").append(mBookId).append("' ");
        sb.append(") t order by t.date");
        Cursor cursor = DbAdapter.getHighlightsBySql(sb.toString());
        List<MarkVo> markVoList = new ArrayList<>();
        while (cursor.moveToNext()){
            MarkVo markVo = new MarkVo();
            markVo.setId(cursor.getInt(cursor.getColumnIndex("id")));
            markVo.setBookId(cursor.getString(cursor.getColumnIndex("bookId")));
            markVo.setContent(cursor.getString(cursor.getColumnIndex("content")));
            markVo.setNote(cursor.getString(cursor.getColumnIndex("note")));
            markVo.setKind(cursor.getString(cursor.getColumnIndex("kind")));
            markVo.setHighlightType(cursor.getString(cursor.getColumnIndex("highLightType")));
            markVo.setCfi(cursor.getString(cursor.getColumnIndex("cfi")));
            markVo.setRangy(cursor.getString(cursor.getColumnIndex("rangy")));
            markVo.setHref(cursor.getString(cursor.getColumnIndex("href")));
            markVo.setDate(cursor.getString(cursor.getColumnIndex("date")));
            markVo.setPageNumber(cursor.getInt(cursor.getColumnIndex("pageNumber")));
            markVoList.add(markVo);
        }
        cursor.close();
        return markVoList;
    }
    //获取书签
    @SuppressLint("Range")
    public static List<MarkVo> getAllBookMark(String mBookId) {
        StringBuilder sb = new StringBuilder();
        //查询书签与页笔记
        sb.append("select * from (");
        sb.append("select _id as id,bookID as bookId,  content,  note, type as kind,null as highLightType,cfi,null as rangy,readlocator as href,date, page_number as pageNumber from bookmark_table");
        sb.append(" where bookID='").append(mBookId).append("' ").append(" and ").append(" type = '1'");
        sb.append(") t order by t.date");
        Cursor cursor = DbAdapter.getHighlightsBySql(sb.toString());
        List<MarkVo> markVoList = new ArrayList<>();
        while (cursor.moveToNext()){
            MarkVo markVo = new MarkVo();
            markVo.setId(cursor.getInt(cursor.getColumnIndex("id")));
            markVo.setBookId(cursor.getString(cursor.getColumnIndex("bookId")));
            markVo.setContent(cursor.getString(cursor.getColumnIndex("content")));
            markVo.setNote(cursor.getString(cursor.getColumnIndex("note")));
            markVo.setKind(cursor.getString(cursor.getColumnIndex("kind")));
            markVo.setHighlightType(cursor.getString(cursor.getColumnIndex("highLightType")));
            markVo.setCfi(cursor.getString(cursor.getColumnIndex("cfi")));
            markVo.setRangy(cursor.getString(cursor.getColumnIndex("rangy")));
            markVo.setHref(cursor.getString(cursor.getColumnIndex("href")));
            markVo.setDate(cursor.getString(cursor.getColumnIndex("date")));
            markVo.setPageNumber(cursor.getInt(cursor.getColumnIndex("pageNumber")));
            markVoList.add(markVo);
        }
        cursor.close();
        return markVoList;
    }
    //获取笔记

    @SuppressLint("Range")
    public static List<MarkVo> getAllNoteList(String mBookId) {
        StringBuilder sb = new StringBuilder();
        //查询书签与页笔记
        sb.append("select * from (");
        sb.append("select _id as id,bookID as bookId,  content,  note, type as kind,null as highLightType,cfi,null as rangy,readlocator as href,date, page_number as pageNumber from bookmark_table");
        sb.append(" where bookID='").append(mBookId).append("' ").append(" and ").append(" type = '2'");
        sb.append(" union all ");
        sb.append(" select _id as id,bookId,content,note,case when type='mark' then '4' else '3'end as kind,type as highLightType, null as cfi ,rangy,pageId as href, date,page_number as pageNumber  from highlight_table ");
        sb.append(" where bookId='").append(mBookId).append("' ").append(" and ").append(" type = '").append(MARK_TYPE).append("'");
        sb.append(") t order by t.date");
        Cursor cursor = DbAdapter.getHighlightsBySql(sb.toString());
        List<MarkVo> markVoList = new ArrayList<>();
        while (cursor.moveToNext()){
            MarkVo markVo = new MarkVo();
            markVo.setId(cursor.getInt(cursor.getColumnIndex("id")));
            markVo.setBookId(cursor.getString(cursor.getColumnIndex("bookId")));
            markVo.setContent(cursor.getString(cursor.getColumnIndex("content")));
            markVo.setNote(cursor.getString(cursor.getColumnIndex("note")));
            markVo.setKind(cursor.getString(cursor.getColumnIndex("kind")));
            markVo.setHighlightType(cursor.getString(cursor.getColumnIndex("highLightType")));
            markVo.setCfi(cursor.getString(cursor.getColumnIndex("cfi")));
            markVo.setRangy(cursor.getString(cursor.getColumnIndex("rangy")));
            markVo.setHref(cursor.getString(cursor.getColumnIndex("href")));
            markVo.setDate(cursor.getString(cursor.getColumnIndex("date")));
            markVo.setPageNumber(cursor.getInt(cursor.getColumnIndex("pageNumber")));
            markVoList.add(markVo);
        }
        cursor.close();
        return markVoList;
    }
    //获取全部

    @SuppressLint("Range")
    public static List<MarkVo> getAllHighlight(String mBookId) {
        StringBuilder sb = new StringBuilder();
        //查询书签与页笔记
        sb.append("select * from (");
        sb.append(" select _id as id,bookId,content,note,case when type='mark' then '4' else '3'end as kind,type as highLightType, null as cfi ,rangy,pageId as href, date,page_number as pageNumber  from highlight_table ");
        sb.append(" where bookId='").append(mBookId).append("' ").append(" and ").append(" type != '").append(MARK_TYPE).append("'");;
        sb.append(") t order by t.date");
        Cursor cursor = DbAdapter.getHighlightsBySql(sb.toString());
        List<MarkVo> markVoList = new ArrayList<>();
        while (cursor.moveToNext()){
            MarkVo markVo = new MarkVo();
            markVo.setId(cursor.getInt(cursor.getColumnIndex("id")));
            markVo.setBookId(cursor.getString(cursor.getColumnIndex("bookId")));
            markVo.setContent(cursor.getString(cursor.getColumnIndex("content")));
            markVo.setNote(cursor.getString(cursor.getColumnIndex("note")));
            markVo.setKind(cursor.getString(cursor.getColumnIndex("kind")));
            markVo.setHighlightType(cursor.getString(cursor.getColumnIndex("highLightType")));
            markVo.setCfi(cursor.getString(cursor.getColumnIndex("cfi")));
            markVo.setRangy(cursor.getString(cursor.getColumnIndex("rangy")));
            markVo.setHref(cursor.getString(cursor.getColumnIndex("href")));
            markVo.setDate(cursor.getString(cursor.getColumnIndex("date")));
            markVo.setPageNumber(cursor.getInt(cursor.getColumnIndex("pageNumber")));
            markVoList.add(markVo);
        }
        cursor.close();
        return markVoList;
    }
}



