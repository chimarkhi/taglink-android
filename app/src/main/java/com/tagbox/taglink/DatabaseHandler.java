package com.tagbox.taglink;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.tagbox.taglink.Constants.KEY_NODE_BATTERY;
import static com.tagbox.taglink.Constants.KEY_NODE_HUMIDITY;
import static com.tagbox.taglink.Constants.KEY_NODE_SIGNAL_STRENGTH;
import static com.tagbox.taglink.Constants.KEY_NODE_TEMPERATURE;
import static com.tagbox.taglink.Constants.KEY_NODE_TIMESTAMP;


/**
 * Created by Suhas on 10/23/2016.
 */

public class DatabaseHandler {
    // All Static variables

    private static final String TABLE_TAG_LOG_DATA = "tagLogData";
    private static final String KEY_TAG_LOG_ID = "id";
    private static final String KEY_NODE_ID = "nodeId";
    private static final String KEY_TAG_NAME = "friendlyName";
    private static final String KEY_LAST_COUNTER = "lastCounter";
    private static final String KEY_UPLOAD_TIMESTAMP = "uploadTimestamp";
    private static final String KEY_BREACH = "breach";

    // Table name
    private static final String TABLE_SENSOR_DATA = "sensorData";
    // Table Columns names
    private static final String KEY_SENSOR_ID = "id";

    //post message data table
    private static final String TABLE_POST_MESSAGE_DATA = "postMessageData";
    private static final String KEY_POST_MESSAGE_ID = "id";
    private static final String KEY_POST_MESSAGE = "postMessage";

    private SQLiteDatabase mDb;
    private static DatabaseManager mDbManager;

    public DatabaseHandler(Context context) {
        mDbManager = new DatabaseManager(context);
        mDb = mDbManager.getWritableDatabase();
    }

    public boolean isOpen() {
        return mDb != null && mDb.isOpen();
    }

    public void close() {
        if (isOpen()) {
            mDb.close();
            mDb = null;
            if (mDbManager != null) {
                mDbManager.close();
                mDbManager = null;
            }
        }
    }

    private void checkDbState() {
        if (mDb == null || !mDb.isOpen()) {
            throw new IllegalStateException("The database has not been opened");
        }
    }

/**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    void addSenseData(SensorData senseData) {
        ContentValues values = new ContentValues();
        values.put(KEY_NODE_ID, senseData.getDeviceAddress());
        values.put(KEY_NODE_TEMPERATURE, senseData.getTempData());
        values.put(KEY_NODE_HUMIDITY, senseData.getHumidityData());
        values.put(KEY_NODE_TIMESTAMP, senseData.getTimestamp());
        values.put(KEY_NODE_BATTERY, senseData.getBatteryLevel());
        values.put(KEY_NODE_SIGNAL_STRENGTH, senseData.getRssi());

        // Inserting Row
        mDb.insert(TABLE_SENSOR_DATA, null, values);
    }

    public long getSenseDataCount() {
        long numRows = DatabaseUtils.queryNumEntries(mDb, TABLE_SENSOR_DATA);
        return numRows;
    }

    public List<SensorData> getSenseData(long limit) {
        List<SensorData> senseDataList = new LinkedList<>();        //in this application we want to remove items from list. so ll is better

        String selectQuery = "SELECT  * FROM " + TABLE_SENSOR_DATA
                + " ORDER BY " + KEY_SENSOR_ID + " LIMIT " + Long.toString(limit);

        String ids = "";

        Cursor cursor = mDb.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SensorData senseData = new SensorData();
                senseData.setDeviceAddress(cursor.getString(cursor.getColumnIndex(KEY_NODE_ID)));
                senseData.setTempData(cursor.getString(cursor.getColumnIndex(KEY_NODE_TEMPERATURE)));
                senseData.setHumidityData(cursor.getString(cursor.getColumnIndex(KEY_NODE_HUMIDITY)));
                senseData.setTimestamp(cursor.getString(cursor.getColumnIndex(KEY_NODE_TIMESTAMP)));
                senseData.setBatteryLevel(cursor.getString(cursor.getColumnIndex(KEY_NODE_BATTERY)));
                //senseData.setRssi(cursor.getString(cursor.getColumnIndex(KEY_NODE_SIGNAL_STRENGTH)));
                senseDataList.add(senseData);

                if(ids.trim() != "")
                    ids += ",";
                ids += Long.toString(cursor.getLong(cursor.getColumnIndex(KEY_POST_MESSAGE_ID)));

            } while (cursor.moveToNext());
        }
        cursor.close();

        deleteSenseData(ids);

        return senseDataList;
    }

    public void deleteSenseData(String ids) {

        String deleteQuery = "DELETE FROM " + TABLE_SENSOR_DATA +
                " WHERE "+ KEY_SENSOR_ID + " IN (" + ids + ")";

        mDb.execSQL(deleteQuery);
    }

    public List<SensorData> getAllSenseData() {
        //Map<String, SensorData> senseDataList = new HashMap<String, SensorData>();
        List<SensorData> senseDataList = new ArrayList<SensorData>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SENSOR_DATA;
        Cursor cursor = mDb.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SensorData senseData = new SensorData();
                //String id = cursor.getString(0);
                senseData.setDeviceAddress(cursor.getString(cursor.getColumnIndex(KEY_NODE_ID)));
                senseData.setTempData(cursor.getString(cursor.getColumnIndex(KEY_NODE_TEMPERATURE)));
                senseData.setHumidityData(cursor.getString(cursor.getColumnIndex(KEY_NODE_HUMIDITY)));
                senseData.setTimestamp(cursor.getString(cursor.getColumnIndex(KEY_NODE_TIMESTAMP)));
                senseData.setBatteryLevel(cursor.getString(cursor.getColumnIndex(KEY_NODE_BATTERY)));
                senseData.setRssi(cursor.getString(cursor.getColumnIndex(KEY_NODE_SIGNAL_STRENGTH)));
                senseDataList.add(senseData);
            } while (cursor.moveToNext());
        }
        cursor.close();

        deleteAllSensorData();
        return senseDataList;
    }

    public void deleteAllSensorData(){
        mDb.delete(TABLE_SENSOR_DATA, null, null);
    }

    void addTagLogData(String devAddr, String friendlyName, long lastCounter, long timestamp) {
        //check if tag with this address already exists
        String Query = "SELECT * FROM " + TABLE_TAG_LOG_DATA
                + " WHERE " + KEY_NODE_ID + " = '" + devAddr + "'"
                + " LIMIT 1";

        Cursor cursor = mDb.rawQuery(Query, null);

        if(cursor.moveToFirst()) {
            //tag with address exists. so update that particular row
            long id = cursor.getLong(cursor.getColumnIndex(KEY_TAG_LOG_ID));

            ContentValues values = new ContentValues();
            values.put(KEY_LAST_COUNTER, lastCounter);
            values.put(KEY_UPLOAD_TIMESTAMP, timestamp);
            mDb.update(TABLE_TAG_LOG_DATA, values, KEY_TAG_LOG_ID + "=" + id, null);
        } else {
            //tag with address does not exist. insert new row
            ContentValues values = new ContentValues();
            values.put(KEY_NODE_ID, devAddr);
            values.put(KEY_TAG_NAME, friendlyName);
            values.put(KEY_LAST_COUNTER, lastCounter);
            values.put(KEY_UPLOAD_TIMESTAMP, timestamp);
            mDb.insert(TABLE_TAG_LOG_DATA, null, values);
        }
        cursor.close();
    }

    public List<TagLogData> getAllTagLogData() {
        String selectQuery = "SELECT * FROM " + TABLE_TAG_LOG_DATA;

        List<TagLogData> list = null;
        TagLogData tagLogData = null;

        list = new ArrayList<>();

        Cursor cursor = mDb.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                tagLogData = new TagLogData();
                tagLogData.nodeId = cursor.getString(cursor.getColumnIndex(KEY_NODE_ID));
                tagLogData.friendlyName = cursor.getString(cursor.getColumnIndex(KEY_TAG_NAME));
                tagLogData.lastCounter = cursor.getInt(cursor.getColumnIndex(KEY_LAST_COUNTER));
                tagLogData.uploadTimestamp = cursor.getLong(cursor.getColumnIndex(KEY_UPLOAD_TIMESTAMP));
                list.add(tagLogData);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public TagLogData getTagLogData(String nodeId) {
        String selectQuery = "SELECT * FROM " + TABLE_TAG_LOG_DATA + " WHERE " + KEY_NODE_ID
                + " = '" + nodeId + "'";

        TagLogData tagLogData = null;

        Cursor cursor = mDb.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            tagLogData = new TagLogData();
            tagLogData.nodeId = cursor.getString(cursor.getColumnIndex(KEY_NODE_ID));
            tagLogData.lastCounter = cursor.getInt(cursor.getColumnIndex(KEY_LAST_COUNTER));
            tagLogData.uploadTimestamp = cursor.getLong(cursor.getColumnIndex(KEY_UPLOAD_TIMESTAMP));
        }
        cursor.close();
        return tagLogData;
    }

    public void deleteAllTagLogData() {
        mDb.delete(TABLE_TAG_LOG_DATA, null, null);
    }

    public void addPostMessage(PostMessageData postMessage) {
        ContentValues values = new ContentValues();
        values.put(KEY_POST_MESSAGE, postMessage.getPostMessage());

        // Inserting Row
        mDb.insert(TABLE_POST_MESSAGE_DATA, null, values);
    }

    public long getPostMessageCount() {

        long cnt  = DatabaseUtils.queryNumEntries(mDb, TABLE_POST_MESSAGE_DATA);

        return cnt;
    }

    // Getting All Contacts
    public List<PostMessageData> getAllPostMessageData() {
        //Map<String, SensorData> senseDataList = new HashMap<String, SensorData>();
        List<PostMessageData> dataList = new ArrayList<>();
        // Select All Query
        //String selectQuery = "SELECT  * FROM " + TABLE_POST_MESSAGE_DATA;
        String count = "100";

        String selectQuery = "SELECT  * FROM " + TABLE_POST_MESSAGE_DATA
                + " ORDER BY " + KEY_POST_MESSAGE_ID + " LIMIT " + count;

        String ids = "";

        Cursor cursor = mDb.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                PostMessageData data = new PostMessageData();
                data.setPostMessage(cursor.getString(cursor.getColumnIndex(KEY_POST_MESSAGE)));
                dataList.add(data);

                if(ids.trim() != "")
                    ids += ",";
                ids += Long.toString(cursor.getLong(cursor.getColumnIndex(KEY_POST_MESSAGE_ID)));
            } while (cursor.moveToNext());
        }
        cursor.close();

        deletePostMessageData(ids);
        //deleteAllPostMessageData();
        return dataList;
    }


    public void deletePostMessageData(String ids) {

        String deleteQuery = "DELETE FROM " + TABLE_POST_MESSAGE_DATA +
                " WHERE "+ KEY_POST_MESSAGE_ID + " IN (" + ids + ")";

        mDb.execSQL(deleteQuery);
        //mDb.rawQuery(deleteQuery, null);
    }

    private static class DatabaseManager extends SQLiteOpenHelper {
        // Database Version
        private static final int DATABASE_VERSION = 7;
        // Database Name
        private static final String DATABASE_NAME = "taglinkData";

        public DatabaseManager(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            SQLiteDatabase db = this.getWritableDatabase();
            createTagLogTable(db);
            createSensorDataTable(db);
            createPostMessageTable(db);
        }

        // Creating Tables
        @Override
        public void onCreate(SQLiteDatabase db) {
            createTagLogTable(db);
        }

        // Upgrading database
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Drop older table if existed
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAG_LOG_DATA);
            // Create tables again
            onCreate(db);
        }

        private void createTagLogTable(SQLiteDatabase database){
            String CREATE_TAG_LOG_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_TAG_LOG_DATA + "("
                    + KEY_TAG_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NODE_ID + " TEXT,"
                    + KEY_TAG_NAME + " TEXT," + KEY_LAST_COUNTER + " INTEGER,"
                    + KEY_UPLOAD_TIMESTAMP + " INTEGER" + ")";
            database.execSQL(CREATE_TAG_LOG_TABLE);
        }


        private void createSensorDataTable(SQLiteDatabase database){
            String CREATE_SENSORDATA_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SENSOR_DATA + "("
                    + KEY_SENSOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NODE_ID + " TEXT,"
                    + KEY_NODE_TEMPERATURE + " TEXT," + KEY_NODE_HUMIDITY + " TEXT," + KEY_NODE_TIMESTAMP + " TEXT,"
                    + KEY_NODE_BATTERY + " TEXT," + KEY_NODE_SIGNAL_STRENGTH + " TEXT" + ")";
            database.execSQL(CREATE_SENSORDATA_TABLE);
        }

        private void createPostMessageTable(SQLiteDatabase database){
            //SQLiteDatabase db = this.getWritableDatabase();
            String CREATE_POSTMESSAGE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_POST_MESSAGE_DATA + "("
                    + KEY_POST_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_POST_MESSAGE + " TEXT" +  ")";
            database.execSQL(CREATE_POSTMESSAGE_TABLE);
        }
    }
}
