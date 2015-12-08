package crittercism.com.critterretail;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;

/**
 * Created by chrisbeauchamp on 11/28/15.
 */
public class CartFragment extends Fragment {

    private ListView mListView;
    private View mRootView;
    private TextView mTotalPrice;
    DatabaseHelper mDbHelper;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static CartFragment newInstance(int sectionNumber) {
        CartFragment fragment = new CartFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CartFragment() {
    }

    public void reloadCart() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseHelper.CartEntry._ID,
                DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_ID,
                DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_DESCRIPTION,
                DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_NAME,
                DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_PRICE,
                DatabaseHelper.CartEntry.COLUMN_NAME_IMAGE_URL,
        };

        Cursor c = db.query(
                DatabaseHelper.CartEntry.TABLE_NAME, projection, null, null, null, null, null);

        final CartAdapter adapter = new CartAdapter(getActivity().getApplicationContext(), c, 0);

        mListView.setAdapter(adapter);

        Cursor cursor = db.rawQuery("SELECT sum(" + DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_PRICE + ") FROM " + DatabaseHelper.CartEntry.TABLE_NAME, null);
        cursor.moveToFirst();
        double cnt =  cursor.getDouble(0);
        cursor.close();

        mTotalPrice.setText("$" + String.valueOf(cnt));
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser) {
            this.reloadCart();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_cart, container, false);
        final CartFragment mThis = this;

        mDbHelper = new DatabaseHelper(getActivity().getApplicationContext());
        mListView = (ListView) mRootView.findViewById(R.id.cart_listview);

        View footerView = inflater.inflate(R.layout.footer_layout, null, false);
        mListView.addFooterView(footerView);

        mTotalPrice = (TextView) mRootView.findViewById(R.id.footer_price);

        this.reloadCart();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                System.out.println("Clicked: " + position);

                //Ask the user if they want to quit
                new AlertDialog.Builder(mRootView.getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Delete?")
                        .setMessage("Are you sure you wish to remove this item from your cart?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Cursor c = (Cursor) mListView.getAdapter().getItem(position);
                                String prodID = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_ID));
                                System.out.println(prodID);

                                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                                db.delete(DatabaseHelper.CartEntry.TABLE_NAME, DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_ID + "=" + prodID, null);

                                mThis.reloadCart();
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();


            }
        });

        return mRootView;
    }


    class CartAdapter extends CursorAdapter {

        Context context;
        private LayoutInflater inflater = null;

        public CartAdapter(Context context, Cursor cursor, int flags) {
            super(context, cursor, flags);
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        // The newView method is used to inflate a new view and return it,
        // you don't bind any data to the view at this point.
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.row, parent, false);
        }

        // The bindView method is used to bind all data to a given view
        // such as setting the text on a TextView.
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Find fields to populate in inflated template
            TextView rowHeader = (TextView) view.findViewById(R.id.text_header);
            TextView rowDesc = (TextView) view.findViewById(R.id.text);
            TextView rowPrice = (TextView) view.findViewById(R.id.text_price);
            ImageView prodImage = (ImageView) view.findViewById(R.id.imageView);

            String headerText = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_NAME));
            rowHeader.setText(headerText);

            String descText = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_DESCRIPTION));
            rowDesc.setText(descText);

            double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_PRICE));
            String priceString = String.format("$%.2f", price);
            rowPrice.setText(priceString);

            String prodImageURL = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CartEntry.COLUMN_NAME_IMAGE_URL));

            new DownloadImageTask(prodImage).execute(prodImageURL);
        }

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            System.out.println(urldisplay);
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
