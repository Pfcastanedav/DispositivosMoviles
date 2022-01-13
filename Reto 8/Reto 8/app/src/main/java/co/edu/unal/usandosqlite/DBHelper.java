package co.edu.unal.usandosqlite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "MyDBName.db";
    public static final String CONTACTS_TABLE_NAME = "contacts";
    public static final String CONTACTS_COLUMN_ID = "id";
    public static final String CONTACTS_COLUMN_NAME = "name";
    public static final String CONTACTS_COLUMN_URL = "url";
    public static final String CONTACTS_COLUMN_PHONE = "phone";
    public static final String CONTACTS_COLUMN_EMAIL = "email";
    public static final String CONTACTS_COLUMN_SERVICE = "service";
    public static final String CONTACTS_COLUMN_CLASIFICATION = "clasification";

    private HashMap hp;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table contacts " +
                        "(id integer primary key, name text, url text, phone text, email text, service text, clasification text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS contacts");
        onCreate(db);
    }

    public boolean insertContact (String name, String url, String phone, String email, String service,String clasification) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("url", url);
        contentValues.put("phone", phone);
        contentValues.put("email", email);
        contentValues.put("service", service);
        contentValues.put("clasification", clasification);
        db.insert("contacts", null, contentValues);
        return true;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from contacts where id="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, CONTACTS_TABLE_NAME);
        return numRows;
    }

    public boolean updateContact (Integer id, String name, String url, String phone, String email, String service, String clasification) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("url", url);
        contentValues.put("phone", phone);
        contentValues.put("email", email);
        contentValues.put("service", service);
        contentValues.put("clasification", clasification);
        db.update("contacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteContact (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("contacts",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<String>[] getAllCotacts() {
        ArrayList<String>[] arrays = new ArrayList[3];
        for (int i = 0; i < 3; i++) {
            arrays[i] = new ArrayList<String>();
        }

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from contacts", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            arrays[0].add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_ID)));
            arrays[1].add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)));
            arrays[2].add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_CLASIFICATION)));
            res.moveToNext();
        }
        return arrays;
    }

    public  ArrayList<String>[] getSomeContacts(String searchTermName, String searchTermClass)
    {
        String[] columns={CONTACTS_COLUMN_ID,CONTACTS_COLUMN_NAME};

        if((searchTermName != null && searchTermName.length() >0) || (searchTermClass != null && searchTermClass.length() >0))
        {

            String sql="SELECT * FROM "+ CONTACTS_TABLE_NAME +" WHERE ";
            if(searchTermName != null && searchTermName.length()>0){
                sql += CONTACTS_COLUMN_NAME + " LIKE '%" + searchTermName + "%'";
                if(searchTermClass != null && searchTermClass.length() >0){
                    sql += " AND ";
                }
            }

            if(searchTermClass != null && searchTermClass.length() >0){
                sql += CONTACTS_COLUMN_CLASIFICATION + " LIKE '%" + searchTermClass +"%'";
            }

            ArrayList<String>[] arrays = new ArrayList[3];
            for (int i = 0; i < 3; i++) {
                arrays[i] = new ArrayList<String>();
            }

            //hp = new HashMap();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res = db.rawQuery(sql, null);
            res.moveToFirst();

            while (res.isAfterLast() == false) {
                arrays[0].add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_ID)));
                arrays[1].add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)));
                arrays[2].add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_CLASIFICATION)));
                res.moveToNext();
            }
            return arrays;
        }

        return this.getAllCotacts();
    }

}
