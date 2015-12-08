package crittercism.com.critterretail;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DatabaseHelper extends SQLiteOpenHelper {

    /* Inner class that defines the table contents */
    public static abstract class CartEntry implements BaseColumns {
        public static final String TABLE_NAME = "cartItem";
        public static final String COLUMN_NAME_PRODUCT_ID = "productID";
        public static final String COLUMN_NAME_PRODUCT_NAME = "productName";
        public static final String COLUMN_NAME_PRODUCT_PRICE = "productPrice";
        public static final String COLUMN_NAME_PRODUCT_DESCRIPTION = "productDescription";
        public static final String COLUMN_NAME_IMAGE_URL = "imageURL";
        public static final String COLUMN_NAME_QUANTITY = "quantity";
    }

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "critterretail.sqlite";
    private static final String DICTIONARY_TABLE_CREATE =
            "CREATE TABLE " + CartEntry.TABLE_NAME + " (" +
                    CartEntry._ID + " INTEGER PRIMARY KEY, " +
                    CartEntry.COLUMN_NAME_PRODUCT_ID + " TEXT, " +
                    CartEntry.COLUMN_NAME_PRODUCT_NAME + " TEXT, " +
                    CartEntry.COLUMN_NAME_PRODUCT_PRICE + " REAL, " +
                    CartEntry.COLUMN_NAME_PRODUCT_DESCRIPTION + " TEXT, " +
                    CartEntry.COLUMN_NAME_IMAGE_URL + " TEXT, " +
                    CartEntry.COLUMN_NAME_QUANTITY + " INTEGER" +
                    ");";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DICTIONARY_TABLE_CREATE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}