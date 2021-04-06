package data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Samir KHan on 7/17/2016.
 */

/*  THIS CLASS HANDLE ALL INTERACTION WITH DATABASE */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE = "autosender";
    private static final int VERSION = 1;
    private String query;

    public DBHelper(Context context) {
        super(context, DATABASE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //  create just sms table
        query = "CREATE TABLE sms (id INTEGER PRIMARY KEY, contact TEXT, message TEXT, jenis TEXT, status int);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // on update, delete previous table, including data
        query = "DELETE IF EXISTS TABLE sms";
        db.execSQL(query);

        // create new one
        onCreate(db);
    }

    // insert a single record into table sms
    public void insert(String id, String contact, String message, String jenis, String status) {

        SQLiteDatabase database = getWritableDatabase();

        //  design query and execute it..
        query = "insert into sms values(" + id + ", \'" + contact + "\', \'" + message + "\', \'" + jenis + "\', " + status + ");";
        database.execSQL(query);
    }

    /*
    public void insert(String contact, String message, String status) {

        SQLiteDatabase database = getWritableDatabase();

        //  design query and execute it..
        query = "insert into sms(contact,message,status) values(" + "\'" + contact + "\', \'" + message + "\', " + status + ");";
        database.execSQL(query);
    }*/

    // get top sms sms having status = 0, not sended yet,
    public String[] topSMS() {

        int id = -1;
        SQLiteDatabase database = null;
        database = getWritableDatabase();

        // array to send id, contact_no and message
        String[] data = new String[5];

        query = "select * from sms where status = 0 LIMIT 1;";

        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
            data[0] = id + "";
            data[1] = cursor.getString(1);
            data[2] = cursor.getString(2);
            data[3] = cursor.getString(3);
            data[4] = cursor.getString(4);
        }
        cursor.close();

        // if update status to 1
        if (id != -1) {
            query = "update sms set status = 1 where id = " + id;
            database.execSQL(query);
        }
        return data;
    }

    public void execute(String query) {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(query);
    }

    public void deleteItem(String id) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = "id=?";
        String whereArgs[] = {id};
        db.delete("sms", whereClause, whereArgs);
    }
}
