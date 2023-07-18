package com.folioreader.model.sqlite;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.folioreader.Constants;
import com.folioreader.model.db.Book;
import com.folioreader.model.db.PageProgress;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author syed afshan on 24/12/20.
 */

public class PageProgressTable {

    public static final String TABLE_NAME = "page_progress";

    public static final String ID = "_id";
    public static final String  HREF = "href";
    public static final String  BOOK_ID = "book_id";
    public static final String  START = "start";
    public static final String  END = "end";

    public static final String  PAGE_NUMBER = "page_number";
    public static final String  CONTENT_SIZE = "content_size";

    public static SQLiteDatabase database;

    public PageProgressTable(Context context) {
        FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
            + BOOK_ID + " TEXT" + ","
            + HREF + " TEXT" + ","
            + START + " NUMERIC" + ","
            + END + " NUMERIC" + ","
            + PAGE_NUMBER + " INTEGER" + ","
            + CONTENT_SIZE + " INTEGER" +")";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public final boolean insertBook(PageProgress pageProgress) {
        ContentValues values = new ContentValues();
        values.put(BOOK_ID, pageProgress.bookId);
        values.put(HREF, pageProgress.href);
        values.put(START, pageProgress.start);
        values.put(END,pageProgress.end);
        values.put(PAGE_NUMBER,pageProgress.pageNumber);
        values.put(CONTENT_SIZE,pageProgress.contentSize);


        return database.insert(TABLE_NAME, null, values) > 0;
    }



    public static String getDateTimeString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constants.DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(date);
    }

    @SuppressLint("Range")
    public static PageProgress getPageProgress(String bookId,String href,Context context){
        if(database == null){
            FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
            database = dbHelper.getWritableDatabase();
        }
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + BOOK_ID + " = \"" + bookId + "\""+" and " + HREF + " = \""+href+ "\"", null);
        PageProgress pageProgress =  null;
        while (cursor.moveToNext()) {
            pageProgress =  new PageProgress();
            pageProgress.id = cursor.getInt(cursor.getColumnIndex(ID));
            pageProgress.bookId = cursor.getString(cursor.getColumnIndex(BOOK_ID));
            pageProgress.href = cursor.getString(cursor.getColumnIndex(HREF));
            pageProgress.pageNumber = cursor.getInt(cursor.getColumnIndex(PAGE_NUMBER));
            pageProgress.start = cursor.getFloat(cursor.getColumnIndex(START));
            pageProgress.end = cursor.getFloat(cursor.getColumnIndex(END));
            pageProgress.contentSize = cursor.getInt(cursor.getColumnIndex(CONTENT_SIZE));
        }
        return pageProgress;
    }
    @SuppressLint("Range")
    public static PageProgress getPageProgressByProgress(String bookId,float progress,Context context){
        if(database == null){
            FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
            database = dbHelper.getWritableDatabase();
        }
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + BOOK_ID + " = \"" + bookId + "\""+
                " and " + START + " > "+progress + " and " + END + " <= "+progress,
                null);
        PageProgress pageProgress =  null;
        while (cursor.moveToNext()) {
            pageProgress =  new PageProgress();
            pageProgress.id = cursor.getInt(cursor.getColumnIndex(ID));
            pageProgress.bookId = cursor.getString(cursor.getColumnIndex(BOOK_ID));
            pageProgress.href = cursor.getString(cursor.getColumnIndex(HREF));
            pageProgress.pageNumber = cursor.getInt(cursor.getColumnIndex(PAGE_NUMBER));
            pageProgress.start = cursor.getFloat(cursor.getColumnIndex(START));
            pageProgress.end = cursor.getFloat(cursor.getColumnIndex(END));
            pageProgress.contentSize = cursor.getInt(cursor.getColumnIndex(CONTENT_SIZE));
        }
        return pageProgress;
    }
}
