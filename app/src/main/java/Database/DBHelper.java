package Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "BoxedDB.db";
    public static final int DATABASE_VERSION = 7;

    public DBHelper(Context context){ super(context, DATABASE_NAME , null, DATABASE_VERSION); }

    /**
     * Moves Table:
     * Primary key for accessing all other tables. Stores an id
     * and a description for different moves that can reference
     * specific box data.
      */
    public static final String MOVES_TABLE_NAME = "moves";
    public static final String MOVES_COLUMN_MOVE_ID = "move_id";
    public static final String MOVES_COLUMN_DESCRIPTION = "description";

    /**
     * Address Table:
     * Location values are 1 and 2 representing starting
     * and final destination addresses.
     */
    public static final String ADDRESS_TABLE_NAME = "address";
    public static final String ADDRESS_COLUMN_MOVE_ID = "move_id";
    public static final String ADDRESS_COLUMN_LOCATION = "location";
    public static final String ADDRESS_COLUMN_STREET = "street";
    public static final String ADDRESS_COLUMN_CITY_STATE_ZIP = "city_state_zip";

    /**
     * Boxes:
     * Stores an id for each box including the location (bedroom) the
     * box content belongs to, as well as heavy or fragile which is
     * represented by 0 or 1. The table also stores a blob for images
     * of a box and an RFID for NFC scanning.
     */
    public static final String BOXES_TABLE_NAME = "boxes";
    public static final String BOXES_COLUMN_MOVE_ID = "move_id";
    public static final String BOXES_COLUMN_BOX_ID = "box_id";
    public static final String BOXES_COLUMN_RFID = "rfid";
    public static final String BOXES_COLUMN_LOCATION = "location";
    public static final String BOXES_COLUMN_HEAVY = "heavy";
    public static final String BOXES_COLUMN_FRAGILE = "fragile";
    public static final String BOXES_COLUMN_IMAGE = "image";

    /**
     * Box Content:
     * References the move and box ID's. Stores a record of each
     * item in the box.
     */
    public static final String BOX_CONTENT_TABLE_NAME = "box_content";
    public static final String BOX_CONTENT_COLUMN_MOVE_ID = "move_id";
    public static final String BOX_CONTENT_COLUMN_BOX_ID = "box_id";
    public static final String BOX_CONTENT_COLUMN_ITEM = "item";
    public static final String BOX_CONTENT_COLUMN_IMAGE = "image";

    /**
     * Create each table.
     */
    @Override
    public void onCreate(SQLiteDatabase db){
        // Moves Table
        db.execSQL("create table moves (" +
                "move_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "description text NOT NULL)");

        // Address Table
        db.execSQL("create table address (" +
                "move_id INTEGER NOT NULL, " +
                "location TEXT NOT NULL," +
                "street TEXT," +
                "city_state_zip TEXT)");

        // Boxes Table
        db.execSQL("create table boxes (" +
                "move_id INTEGER NOT NULL, " +
                "box_id INTEGER NOT NULL, " +
                "rfid INTEGER," +
                "location TEXT NOT NULL," +
                "heavy INTEGER NOT NULL," +
                "fragile INTEGER NOT NULL," +
                "image BLOB)");

        // Box Content Table
        db.execSQL("create table box_content (" +
                "move_id INTEGER NOT NULL, " +
                "box_id INTEGER NOT NULL, " +
                "item TEXT NOT NULL," +
                "image BLOB)");
    }

    /**
     * When the database is upgraded, then drop all tables if it exist.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS moves");
        db.execSQL("DROP TABLE IF EXISTS address");
        db.execSQL("DROP TABLE IF EXISTS boxes");
        db.execSQL("DROP TABLE IF EXISTS box_content");
        onCreate(db);
    }

    /**
     * Insert a new Move.
     * @param description
     * @return: success
     */
    public boolean insertMove(String description){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values  = new ContentValues();
        values.put(MOVES_COLUMN_DESCRIPTION, description);
        db.insert(MOVES_TABLE_NAME, null, values);
        return true;
    }

    /**
     * Update a Move.
     * @param description
     * @return: success
     */
    public boolean updateMove(int id, String description){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values  = new ContentValues();
        values.put(MOVES_COLUMN_DESCRIPTION, description);
        db.update(MOVES_TABLE_NAME, values, "" + MOVES_COLUMN_MOVE_ID + " = " + id, null);
        return true;
    }

    /**
     * Insert a new Address.
     * @param move_id
     * @param location:(1/2) Starting and Final Addresses.
     * @param street
     * @param city_state_zip
     * @return
     */
    public boolean insertAddress(int move_id, int location, String street, String city_state_zip){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values  = new ContentValues();
        values.put(ADDRESS_COLUMN_MOVE_ID, move_id);
        values.put(ADDRESS_COLUMN_LOCATION, location);
        values.put(ADDRESS_COLUMN_STREET, street);
        values.put(ADDRESS_COLUMN_CITY_STATE_ZIP, city_state_zip);
        db.insert(ADDRESS_TABLE_NAME, null, values);
        return true;
    }

    /**
     * Update a new Address.
     * @param move_id
     * @param location:(1/2) Starting and Final Addresses.
     * @param street
     * @param city_state_zip
     * @return
     */
    public boolean updateAddress(int move_id, int location, String street, String city_state_zip){
        String statement = ADDRESS_COLUMN_MOVE_ID + " = " + move_id + " AND " +
                ADDRESS_COLUMN_LOCATION + " = " + location;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values  = new ContentValues();
        values.put(ADDRESS_COLUMN_STREET, street);
        values.put(ADDRESS_COLUMN_CITY_STATE_ZIP, city_state_zip);
        db.update(ADDRESS_TABLE_NAME, values, statement, null);
        return true;
    }

    /**
     * Insert a new Box.
     * @param move_id
     * @param box_id
     * @param rfid
     * @param location: (Bedroom, Living Room...)
     * @param heavy: (0/1)
     * @param fragile: (0/1)
     * @param image: Blob
     * @return
     */
    public boolean insertBox(int move_id, int box_id, String rfid, String location,
                             int heavy, int fragile, byte[] image){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values  = new ContentValues();
        values.put(BOXES_COLUMN_MOVE_ID, move_id);
        values.put(BOXES_COLUMN_BOX_ID, box_id);

        if(rfid != null) values.put(BOXES_COLUMN_RFID, rfid);

        values.put(BOXES_COLUMN_LOCATION, location);
        values.put(BOXES_COLUMN_HEAVY, heavy);
        values.put(BOXES_COLUMN_FRAGILE, fragile);

        if(image != null) values.put(BOXES_COLUMN_IMAGE, image);

        db.insert(BOXES_TABLE_NAME, null, values);
        return true;
    }

    /**
     * Update Box ID
     * @param move_id
     * @param old_box_id
     * @param new_box_id
     * @return
     */
    public boolean updateBox(int move_id, int old_box_id, int new_box_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BOXES_COLUMN_BOX_ID, new_box_id);

        // Which row to update
        String selection = BOXES_COLUMN_MOVE_ID + " = " + move_id + " and " +
                BOXES_COLUMN_BOX_ID + " = " + old_box_id;

        db.update(BOXES_TABLE_NAME, values, selection, null);
        return true;
    }

    /**
     * Update Box Information
     * @param move_id
     * @param box_id
     * @param rfid
     * @param location
     * @param heavy
     * @param fragile
     * @param image
     * @return
     */
    public boolean updateBox(int move_id, int box_id, String rfid, String location,
                             int heavy, int fragile, byte[] image){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BOXES_COLUMN_HEAVY, heavy);
        values.put(BOXES_COLUMN_FRAGILE, fragile);
        values.put(BOXES_COLUMN_LOCATION, location);
        values.put(BOXES_COLUMN_RFID, rfid);
        values.put(BOXES_COLUMN_IMAGE, image);

        // Which row to update
        String selection = BOXES_COLUMN_MOVE_ID + " = " + move_id + " and " +
                BOXES_COLUMN_BOX_ID + " = " + box_id;

        db.update(BOXES_TABLE_NAME, values, selection, null);
        return true;
    }

    /**
     * Insert a new Box Item.
     * @param move_id
     * @param box_id
     * @param item: description
     * @return
     */
    public boolean insertBoxItem(int move_id, int box_id, String item){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values  = new ContentValues();
        values.put(BOX_CONTENT_COLUMN_MOVE_ID, move_id);
        values.put(BOX_CONTENT_COLUMN_BOX_ID, box_id);
        values.put(BOX_CONTENT_COLUMN_ITEM, item);

        db.insert(BOX_CONTENT_TABLE_NAME, null, values);
        return true;
    }

    /**
     * Update Box Item
     * @param move_id
     * @param box_id
     * @param old_item
     * @param item
     * @return
     */
    public boolean updateBoxItem(int move_id, int box_id, String old_item, String item){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BOX_CONTENT_COLUMN_ITEM, item);

        // Which row to update
        String selection = BOX_CONTENT_COLUMN_MOVE_ID + " = " + move_id + " and " +
                BOX_CONTENT_COLUMN_BOX_ID + " = " + box_id + " and " +
                BOX_CONTENT_COLUMN_ITEM + " = '" + old_item + "'";

        db.update(BOX_CONTENT_TABLE_NAME, values, selection, null);
        return true;
    }

    /**
     * Update Box Item
     * @param move_id
     * @param box_id
     * @param item
     * @param image
     * @return
     */
    public boolean updateBoxItem(int move_id, int box_id, String item, byte[] image){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BOX_CONTENT_COLUMN_IMAGE, image);

        // Which row to update
        String selection = BOX_CONTENT_COLUMN_MOVE_ID + " = " + move_id + " and " +
                BOX_CONTENT_COLUMN_BOX_ID + " = " + box_id + " and " +
                BOX_CONTENT_COLUMN_ITEM + " = '" + item + "'";

        db.update(BOX_CONTENT_TABLE_NAME, values, selection, null);
        return true;
    }

    /**
     * Update Box Item ID
     * @param move_id
     * @param old_box_id
     * @param new_box_id
     * @return
     */
    public boolean updateBoxItem(int move_id, int old_box_id, int new_box_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BOX_CONTENT_COLUMN_BOX_ID, new_box_id);

        // Which row to update
        String selection = BOX_CONTENT_COLUMN_MOVE_ID + " = " + move_id + " and " +
                BOX_CONTENT_COLUMN_BOX_ID + " = " + old_box_id;

        db.update(BOX_CONTENT_TABLE_NAME, values, selection, null);
        return true;
    }

    /**
     * Delete Box Item
     * @param move_id
     * @param box_id
     * @param item
     */
    public void deleteBoxItem(int move_id, int box_id, String item){
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = BOX_CONTENT_COLUMN_MOVE_ID + " = " + move_id + " and " +
                BOX_CONTENT_COLUMN_BOX_ID + " = " + box_id + " and " +
                BOX_CONTENT_COLUMN_ITEM + " = '" + item + "'";
        db.delete(BOX_CONTENT_TABLE_NAME, selection, null);
    }

    /**
     * Delete Box by cascading
     * over all tables.
     * @param move_id
     * @param box_id
     */
    public void deleteBox(int move_id, int box_id){
        SQLiteDatabase db = this.getWritableDatabase();

        String statement = BOX_CONTENT_COLUMN_MOVE_ID + " = " + move_id + " AND " +
                BOX_CONTENT_COLUMN_BOX_ID + " = " + box_id;
        db.delete(BOX_CONTENT_TABLE_NAME, statement, null);

        statement = BOXES_COLUMN_MOVE_ID + " = " + move_id + " AND " +
                BOXES_COLUMN_BOX_ID + " = " + box_id;
        db.delete(BOXES_TABLE_NAME, statement, null);
    }

    /**
     * Delete Move by cascading
     * over all tables.
     * @param id
     */
    public void deleteMove(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(BOX_CONTENT_TABLE_NAME, BOX_CONTENT_COLUMN_MOVE_ID + " = " + id, null);
        db.delete(BOXES_TABLE_NAME, BOXES_COLUMN_MOVE_ID + " = " + id, null);
        db.delete(ADDRESS_TABLE_NAME, ADDRESS_COLUMN_MOVE_ID + " = " + id, null);
        db.delete(MOVES_TABLE_NAME, MOVES_COLUMN_MOVE_ID + " = " + id, null);
    }

    /**
     * Query RFID Tag
     * @param rfid
     * @return Data from multiple tables
     */
    public Object[] queryRFID(String rfid){
        // Hold the data in an Object[]
        Object[] data = new Object[14];

        SQLiteDatabase db = this.getReadableDatabase();

        // Query the Box Information
        String statement = "SELECT * FROM " + BOXES_TABLE_NAME +
                " WHERE " + BOXES_COLUMN_RFID + " = '" + rfid + "'";
        Cursor cursor =  db.rawQuery(statement, null);

        if(cursor.moveToFirst()){
            data[0] = cursor.getInt(cursor.getColumnIndexOrThrow(BOXES_COLUMN_MOVE_ID));
            data[1] = cursor.getInt(cursor.getColumnIndexOrThrow(BOXES_COLUMN_BOX_ID));
            data[2] = cursor.getString(cursor.getColumnIndexOrThrow(BOXES_COLUMN_RFID));
            data[3] = cursor.getString(cursor.getColumnIndexOrThrow(BOXES_COLUMN_LOCATION));
            data[4] = cursor.getInt(cursor.getColumnIndexOrThrow(BOXES_COLUMN_HEAVY));
            data[5] = cursor.getInt(cursor.getColumnIndexOrThrow(BOXES_COLUMN_FRAGILE));
            data[6] = cursor.getBlob(cursor.getColumnIndexOrThrow(BOXES_COLUMN_IMAGE));

            statement = "SELECT * FROM " + ADDRESS_TABLE_NAME +
                    " WHERE " + ADDRESS_COLUMN_MOVE_ID + " = " + data[0];
            cursor =  db.rawQuery(statement, null);

            // Get the address of the move
            if(cursor.moveToFirst()){
                data[7] = cursor.getInt(cursor.getColumnIndexOrThrow(ADDRESS_COLUMN_LOCATION));
                data[8] = cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS_COLUMN_STREET));
                data[9] = cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS_COLUMN_CITY_STATE_ZIP));

                if(cursor.moveToNext()){
                    data[10] = cursor.getInt(cursor.getColumnIndexOrThrow(ADDRESS_COLUMN_LOCATION));
                    data[11] = cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS_COLUMN_STREET));
                    data[12] = cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS_COLUMN_CITY_STATE_ZIP));
                }
            }

            // Get Move Description
            statement = "SELECT * FROM " + MOVES_TABLE_NAME +
                    " WHERE " + MOVES_COLUMN_MOVE_ID + " = " + data[0];
            cursor =  db.rawQuery(statement, null);
            if(cursor.moveToFirst()){
                data[13] = cursor.getString(cursor.getColumnIndexOrThrow(MOVES_COLUMN_DESCRIPTION));
            }
            return data;
        }
        else return null;
    }

    /**
     * Get Query
     * @param statement
     * @return
     */
    public Cursor getQuery(String statement){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery(statement, null);
        return cursor;
    }
}