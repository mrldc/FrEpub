package com.folioreader.model.sqlite;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.folioreader.Constants;
import com.folioreader.model.MarkVo;
import com.folioreader.model.locators.ReadLocator;
import com.folioreader.ui.activity.FolioActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author syed afshan on 24/12/20.
 */

public class BookmarkTable {

    public static final String TABLE_NAME = "bookmark_table";

    public static final String ID = "_id";
    public static final String bookID = "bookID";
    public static final String date = "date";
    public static final String content = "content";
    //笔记内容
    public static final String note = "note";
    public static final String readlocator = "readlocator";
    public static final String cfi = "cfi";
    public static final String pageNumber = "page_number";
    /**1：书签，2:页笔记**/
    public static final String type = "type";

    public static final String MARK_TYPE = "1";
    public static final String NOTE_TYPE = "2";
    public static SQLiteDatabase Bookmarkdatabase;

    public BookmarkTable(Context context) {
        FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
        Bookmarkdatabase = dbHelper.getWritableDatabase();
    }

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
            + bookID + " TEXT" + ","
            + date + " TEXT" + ","
            + content + " TEXT" + ","
            + note + " TEXT" + ","
            + readlocator + " TEXT" + ","
            + type + " TEXT" + ","
            + pageNumber + " INTEGER" + ","
            + cfi + " TEXT" + ")";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public final boolean insertBookmark(String new_bookID, String new_content,String new_note,int new_pageNumber, String new_locator,String new_cfi,String new_type) {
        ContentValues values = new ContentValues();
        values.put(bookID, new_bookID);
        values.put(date, getDateTimeString(new Date()));
        values.put(readlocator, new_locator);
        values.put(content, new_content);
        values.put(cfi,new_cfi);
        values.put(type,new_type);
        values.put(note,new_note);
        values.put(pageNumber,new_pageNumber);

        return Bookmarkdatabase.insert(TABLE_NAME, null, values) > 0;

    }

    public static String getReadLocatorString(ReadLocator readLocator){
        if(readLocator != null){
            return readLocator.getHref();
        }
        return null;
    }
    @SuppressLint("Range")
    public static final ArrayList<HashMap> getBookmarksForID(String id, Context context) {
        if(Bookmarkdatabase == null){
            FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
            Bookmarkdatabase = dbHelper.getWritableDatabase();
        }
        ArrayList<HashMap> bookmarks = new ArrayList<>();
        Cursor c = Bookmarkdatabase.rawQuery("SELECT * FROM "
                + TABLE_NAME + " WHERE " + bookID + " = \"" + id + "\"", null);
        while (c.moveToNext()) {
            HashMap<String, String> name_value = new HashMap<String, String>();
            name_value.put("name", c.getString(c.getColumnIndex(content)));
            name_value.put("readlocator", c.getString(c.getColumnIndex(readlocator)));
            name_value.put("date", c.getString(c.getColumnIndex(date)));
            name_value.put("cfi", c.getString(c.getColumnIndex(cfi)));
            name_value.put("content", c.getString(c.getColumnIndex(content)));
            name_value.put("note", c.getString(c.getColumnIndex(note)));
            name_value.put("pageNumber", c.getString(c.getColumnIndex(pageNumber)));


            bookmarks.add(name_value);
        };
        c.close();
        return bookmarks;
    }

    @SuppressLint("Range")
    public static final boolean deleteBookmark(String arg_date, String arg_name, Context context){
        if(Bookmarkdatabase == null){
            FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
            Bookmarkdatabase = dbHelper.getWritableDatabase();
        }
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + date + " = \"" + arg_date + "\"" + "AND " + content + " = \"" + arg_name + "\"";
        Cursor c = Bookmarkdatabase.rawQuery(query, null);

        int id = -1;
        while (c.moveToNext()) {
            id = c.getInt(c.getColumnIndex(BookmarkTable.ID));
        }
        c.close();
        return DbAdapter.deleteById(TABLE_NAME, ID, String.valueOf(id));
    }
    @SuppressLint("Range")
    public static final boolean deleteBookmarkByCfi(String arg_cfi, String arg_bookID, String arg_type, Context context){
        if(Bookmarkdatabase == null){
            FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
            Bookmarkdatabase = dbHelper.getWritableDatabase();
        }
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + cfi + " = \"" + arg_cfi + "\""+ " and "+bookID+" = \"" + arg_bookID+"\""+" and "+type+" = \"" + arg_type+"\"";
        Cursor c = Bookmarkdatabase.rawQuery(query, null);

        int id = -1;
        while (c.moveToNext()) {
            id = c.getInt(c.getColumnIndex(BookmarkTable.ID));
        }
        c.close();
        return DbAdapter.deleteById(TABLE_NAME, ID, String.valueOf(id));
    }
    @SuppressLint("Range")
    public static int getBookmarkIdByCfi(String arg_cfi, String arg_bookID,String arg_type, Context context){
        if(Bookmarkdatabase == null){
            FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
            Bookmarkdatabase = dbHelper.getWritableDatabase();
        }
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + cfi + " = \"" + arg_cfi + "\""+ " and "+bookID+" = \"" + arg_bookID+"\""+" and "+type+" = \"" + arg_type+"\"";
        Cursor c = Bookmarkdatabase.rawQuery(query, null);

        int id = -1;
        while (c.moveToNext()) {
            id = c.getInt(c.getColumnIndex(BookmarkTable.ID));
        }
        c.close();
        return id;
    }
    public static boolean deleteBookmarkById(int arg_id,Context context){
        if(Bookmarkdatabase == null){
            FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
            Bookmarkdatabase = dbHelper.getWritableDatabase();
        }

        return DbAdapter.deleteById(TABLE_NAME, ID, String.valueOf(arg_id));
    }
    public static String getDateTimeString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constants.DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(date);
    }
    public static boolean updateNote(String note,Integer id,Context context){
        if(Bookmarkdatabase == null){
            FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
            Bookmarkdatabase = dbHelper.getWritableDatabase();
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(BookmarkTable.note,note);
        contentValues.put(date, getDateTimeString(new Date()));
        return Bookmarkdatabase.update(TABLE_NAME, contentValues, ID + " = " + id, null) > 0;

    }

    @SuppressLint("Range")
    public static MarkVo getPageNote(@Nullable String mBookId, @Nullable String readLocatorString,String arg_cfi) {

        StringBuilder sb = new StringBuilder();
        //查询书签与页笔记
        sb.append("select * from (");
        sb.append("select _id as id,bookID as bookId,  content,  note, type as kind,null as highLightType,cfi,null as rangy,readlocator as href,date, page_number as pageNumber from bookmark_table");
        sb.append(" where bookID='").append(mBookId).append("' ")
                .append(" and ").append(readlocator).append(" ='").append(readLocatorString).append("'")
                .append(" and ").append(cfi).append(" ='").append(arg_cfi).append("'")
                .append(" and ").append(type).append(" ='").append(NOTE_TYPE).append("'");

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
        if(markVoList.size() > 0){
            return markVoList.get(0);
        }
        return null;
    }
}
