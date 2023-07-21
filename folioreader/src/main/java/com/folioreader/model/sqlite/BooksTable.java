package com.folioreader.model.sqlite;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.folioreader.Constants;
import com.folioreader.model.db.Book;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * @author syed afshan on 24/12/20.
 */

public class BooksTable {

    public static final String TABLE_NAME = "books";

    public static final String ID = "_id";
    public static final String  CREATION_DATE = "creation_date";
    public static final String  HREF = "href";
    public static final String  TITLE = "title";
    public static final String  AUTHOR = "author";
    public static final String  IDENTIFIER = "identifier";

    public static final String  PROGRESSION = "progression";
    public static final String  TYPE = "type";
    public static final String  CFI = "cfi";
    //章节所在页数
    public static final String  PAGE_NUMBER = "page_number";
    //章节
    public static final String  CHAPTER_NUMBER = "chapter_number";

    public static SQLiteDatabase database;

    public BooksTable(Context context) {
        FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
            + CREATION_DATE + " TEXT" + ","
            + HREF + " TEXT" + ","
            + TITLE + " TEXT" + ","
            + AUTHOR + " TEXT" + ","
            + PROGRESSION + " TEXT" + ","
            + TYPE + " TEXT" + ","
            + CFI + " TEXT" + ","
            + CHAPTER_NUMBER + " INTEGER" + ","
            + PAGE_NUMBER + " INTEGER" + ","
            + IDENTIFIER + " TEXT" + ")";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public final boolean insertBook(String new_bookID,  String href, String title,String author,String progression,String type,String cfi,int chapterNumber,int pageNumber) {
        ContentValues values = new ContentValues();
        values.put(IDENTIFIER, new_bookID);
        values.put(CREATION_DATE, getDateTimeString(new Date()));
        values.put(HREF, href);
        values.put(TITLE, title);
        values.put(AUTHOR,author);
        values.put(PROGRESSION,progression);
        values.put(TYPE,type);
        values.put(CFI,cfi);
        values.put(CHAPTER_NUMBER,chapterNumber);
        values.put(PAGE_NUMBER,pageNumber);


        return database.insert(TABLE_NAME, null, values) > 0;
    }
    public static boolean updateBook(String new_bookID,String href,String cfi,Context context){
        if(database == null){
            FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
            database = dbHelper.getWritableDatabase();
        }
        ContentValues values = new ContentValues();
        values.put(CREATION_DATE, getDateTimeString(new Date()));
        values.put(HREF, href);
        values.put(CFI,cfi);
        return database.update(TABLE_NAME,values,IDENTIFIER+"=?",new String[]{new_bookID}) > 0;
    }
    public static boolean updateBookPage(String new_bookID,int chapterNumber,int pageNumber,Context context){
        if(database == null){
            FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
            database = dbHelper.getWritableDatabase();
        }
        ContentValues values = new ContentValues();
        values.put(CREATION_DATE, getDateTimeString(new Date()));
        values.put(CHAPTER_NUMBER, chapterNumber);
        values.put(PAGE_NUMBER,pageNumber);
        return database.update(TABLE_NAME,values,IDENTIFIER+"=?",new String[]{new_bookID}) > 0;
    }
    @SuppressLint("Range")
    public final static String getReadHref(String bookId, Context context){
        if(database == null){
            FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
            database = dbHelper.getWritableDatabase();
         }
        String query = "SELECT " + HREF + " FROM " + TABLE_NAME + " WHERE " + IDENTIFIER + " = \"" + bookId + "\"";
        Cursor c = database.rawQuery(query, null);

        String href = null;
        while (c.moveToNext()) {
            href = c.getString(c.getColumnIndex(HREF));
        }
        c.close();
        return href;
    }
    public static String getDateTimeString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constants.DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(date);
    }

    @SuppressLint("Range")
    public static Book getBookByBooKId(String bookId,Context context){
        if(database == null){
            FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
            database = dbHelper.getWritableDatabase();
        }
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + IDENTIFIER + " = \"" + bookId + "\"", null);
        Book book = null;
        while (cursor.moveToNext()) {
            book = new Book(cursor.getInt(cursor.getColumnIndex(ID)),
                    cursor.getString(cursor.getColumnIndex(CREATION_DATE)),
                    cursor.getString(cursor.getColumnIndex(HREF)),
                    cursor.getString(cursor.getColumnIndex(TITLE)),
                    cursor.getString(cursor.getColumnIndex(AUTHOR)),
                    cursor.getString(cursor.getColumnIndex(IDENTIFIER)),
                    cursor.getString(cursor.getColumnIndex(PROGRESSION)),
                    cursor.getString(cursor.getColumnIndex(TYPE)),
                    cursor.getString(cursor.getColumnIndex(CFI)),
                    cursor.getInt(cursor.getColumnIndex(CHAPTER_NUMBER)),
                    cursor.getInt(cursor.getColumnIndex(PAGE_NUMBER))
            );

        }
        cursor.close();
        return book;
    }
}
