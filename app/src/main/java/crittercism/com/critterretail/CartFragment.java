package crittercism.com.critterretail;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.crittercism.app.Crittercism;

public class CartFragment extends Fragment {

    // class variables
    private DatabaseHelper mDbHelper;
    private int mCartTotal;

    // UI elements
    private ListView mListView;
    private View mRootView;
    private TextView mTotalPrice;

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

    public CartFragment() {}



    /* Load the cart from the database and update the ListView */
    public void reloadCart() {

        // get a readable database object
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database you need
        String[] projection = {
                DatabaseHelper.CartEntry._ID,
                DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_ID,
                DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_DESCRIPTION,
                DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_NAME,
                DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_PRICE,
                DatabaseHelper.CartEntry.COLUMN_NAME_IMAGE_URL,
        };

        // load all cart items into the cursor
        Cursor c = db.query(
                DatabaseHelper.CartEntry.TABLE_NAME, projection, null, null, null, null, null);

        // set the adapter with the cursor results
        final CartAdapter adapter = new CartAdapter(getActivity().getApplicationContext(), c, 0);

        // attach the adapter to the listview
        mListView.setAdapter(adapter);

        // load the sum of the cart items
        Cursor cursor = db.rawQuery("SELECT sum(" +
                DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_PRICE + ") FROM " +
                DatabaseHelper.CartEntry.TABLE_NAME, null);

        // get the first row
        cursor.moveToFirst();

        // extract the sum
        double cnt =  cursor.getDouble(0);

        // close the cursor
        cursor.close();

        // store the cart total for later and update the UI
        mCartTotal = (int) Math.round(cnt*100);
        mTotalPrice.setText(String.format("$%.2f", cnt));
    }

    /* called when the tab state changes */
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // the tab was selected and coming into view
        if(isVisibleToUser) {
            this.reloadCart();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // let Crittercism know the status
        Crittercism.leaveBreadcrumb("CartViewDisplayed");

        // inflate our views and attach to variables
        mRootView = inflater.inflate(R.layout.fragment_cart, container, false);
        mListView = (ListView) mRootView.findViewById(R.id.cart_listview);
        mListView.addFooterView(inflater.inflate(R.layout.footer_layout, null, false));
        mTotalPrice = (TextView) mRootView.findViewById(R.id.footer_price);
        Button checkout = (Button) mRootView.findViewById(R.id.checkout_button);

        // load a database helper for this class instance
        mDbHelper = new DatabaseHelper(getActivity().getApplicationContext());

        // when the checkout button is clicked
        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // start our transaction in Crittercism
                Crittercism.beginTransaction("checkout");

                // and attach the value of the cart
                Crittercism.setTransactionValue("checkout", mCartTotal);

                // show the ShippingActivity
                Intent intent = new Intent(mRootView.getContext(), ShippingActivity.class);
                startActivity(intent);
            }
        });

        // prompt to delete item from cart on listview item click
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                // first prompt the user to confirm deletion
                new AlertDialog.Builder(mRootView.getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Delete?")
                        .setMessage("Are you sure you wish to remove this item from your cart?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // get a db representation of the clicked item
                                Cursor c = (Cursor) mListView.getAdapter().getItem(position);

                                // extract the product id of the clicked item
                                String prodID = c.getString(c.getColumnIndexOrThrow(
                                        DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_ID));

                                // get a writable db instance
                                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                                // delete item(s) in the cart that match the selected product id
                                db.delete(DatabaseHelper.CartEntry.TABLE_NAME,
                                        DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_ID + "=" +
                                                prodID, null);

                                // reload the cart listview
                                CartFragment.this.reloadCart();
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();


            }
        });

        // now that the view is set up, load the items into the ListView
        this.reloadCart();

        return mRootView;
    }
}
