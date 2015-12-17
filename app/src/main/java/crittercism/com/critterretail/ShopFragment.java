package crittercism.com.critterretail;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.crittercism.app.Crittercism;

import org.json.JSONObject;

public class ShopFragment extends Fragment {

    private WebView mWebView;
    private DatabaseHelper mDbHelper;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ShopFragment newInstance(int sectionNumber) {
        ShopFragment fragment = new ShopFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ShopFragment() {
    }

    /* Handle commands sent via webview requests */
    private void handleRequest(String command, JSONObject data) {

        System.out.println("Making app request: " + command + " with data: " + data.toString());

        // if the command requested adding item to the cart
        if (command.equalsIgnoreCase("addtocart")) {

            int productID = 0;

            try {

                // pull the product id from the JSON
                productID = data.getInt("productID");

                // get a database instance to write to
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                // Create a new map of values
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_ID, productID);
                values.put(DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_NAME,
                        data.getString("name"));
                values.put(DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_PRICE,
                        data.getDouble("price"));
                values.put(DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_DESCRIPTION,
                        data.getString("description"));
                values.put(DatabaseHelper.CartEntry.COLUMN_NAME_IMAGE_URL, data.getString("image"));
                values.put(DatabaseHelper.CartEntry.COLUMN_NAME_QUANTITY, 1);

                // Insert the new row, returning the primary key value of the new row
                long newRowId = db.insert(DatabaseHelper.CartEntry.TABLE_NAME, null, values);

                // leave a breadcrumb for any issues down the line
                Crittercism.leaveBreadcrumb("Product added to cart: "+Integer.toString(productID));

                // tell the user we were successful
                Toast.makeText(getActivity().getApplicationContext(), "Item Added to Cart!",
                        Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Toast.makeText(getActivity().getApplicationContext(), "Error adding item",
                        Toast.LENGTH_LONG).show();

                Crittercism.logHandledException(e);
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shop, container, false);

        Crittercism.leaveBreadcrumb("ShopViewDisplayed");

        // initialize our class variables
        mDbHelper = new DatabaseHelper(getActivity().getApplicationContext());

        /* Placeholder 1 */

        // override the 'back' button to go back in the webview when appropriate
        mWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
                    mWebView.goBack();
                    return true;
                }
                return false;
            }
        });

        // load our site on view create
        /* Placeholder 2 */

        // listen for custom URL schemes
        mWebView.setWebViewClient(new WebViewClient() {
            /* Placeholder 3 */
        });


        return rootView;
    }
}
