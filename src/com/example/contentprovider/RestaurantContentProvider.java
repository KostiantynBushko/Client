package com.example.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by kbushko on 12/16/13.
 */
public class RestaurantContentProvider extends ContentProvider{

    static final String DB_NAME = "restaurants_db";
    static final String R_TABLE = "restaurants";
    static final int DB_VERSION = 1;

    static final String R_ID      = "_id";
    static final String R_NAME    = "name";
    static final String R_EMAIL   = "email";
    static final String R_URL     = "url";
    static final String R_COUNTRY = "country";
    static final String R_CITY    = "city";
    static final String R_STREET  = "street";
    static final String R_STREET_NUMBER = "street_number";

    private static final int fieldCount = 8;
    static final String CREATE_DB = "create table " + R_TABLE + " ("
            + R_ID + " integer primary key autoincrement, "
            + R_NAME + " text, "
            + R_EMAIL + " text, "
            + R_URL + " text, "
            + R_COUNTRY + " text, "
            + R_CITY + " text, "
            + R_STREET + " text, "
            + R_STREET_NUMBER + " text" + ");";

    /* URI */
    static final String AUTHORITY = "com.example.client.object";
    static final String PATH = "restaurants";
    static final Uri URI_CONTENT = Uri.parse("content://" + AUTHORITY + "/" + PATH);

    static final String RESTAURANT_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.";
    static final String RESTAURANT_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.";

    /* Uri Matcher */
    static final int URI_RESTAURANT = 1;
    static final int URI_RESTAURANT_ID = 2;

    private static UriMatcher uriMatcher = null;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, PATH, URI_RESTAURANT);
        uriMatcher.addURI(AUTHORITY, PATH + "/#", URI_RESTAURANT_ID);
    }

    DatabaseHelper databaseHelper;
    SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case URI_RESTAURANT:
                break;
            case URI_RESTAURANT_ID:
                break;
            default:
                throw new IllegalArgumentException("wrong uri: " + uri);
        }
        db = databaseHelper.getWritableDatabase();
        Cursor cursor = db.query(R_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), URI_CONTENT);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case URI_RESTAURANT:
                return RESTAURANT_CONTENT_TYPE;
            case URI_RESTAURANT_ID:
                return RESTAURANT_CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        if (uriMatcher.match(uri) != URI_RESTAURANT)
            throw new IllegalArgumentException("wrong uri: " + uri);
        db = databaseHelper.getWritableDatabase();
        long rowId = db.insert(R_TABLE, null, contentValues);
        Uri resultUri = ContentUris.withAppendedId(URI_CONTENT, rowId);
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch(uriMatcher.match(uri)) {
            case URI_RESTAURANT:
                break;
            case URI_RESTAURANT_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                    selection = R_ID + "=" + id;
                }else {
                    selection = selection + "AND" + R_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("wrong uri: " + uri);
        }
        db = databaseHelper.getWritableDatabase();
        int countRow = db.delete(R_TABLE, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return countRow;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArg) {
        switch(uriMatcher.match(uri)) {
            case URI_RESTAURANT:
                break;
            case URI_RESTAURANT_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                    selection = R_ID + " = " + id;
                } else {
                    selection = selection + " AND " + R_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("wrong uri: " + uri);
        }
        db = databaseHelper.getWritableDatabase();
        int countRow = db.update(R_TABLE, contentValues, selection, selectionArg);
        getContext().getContentResolver().notifyChange(uri, null);
        return countRow;
    }

    /* SQLite helper */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_DB);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) { /* */ }
    }
}
