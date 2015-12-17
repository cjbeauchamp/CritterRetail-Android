package crittercism.com.critterretail;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.crittercism.app.Crittercism;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chrisbeauchamp on 11/28/15.
 */
public class ShopFragment extends Fragment {

    private WebView mWebView;
    DatabaseHelper mDbHelper;

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


    private void handleRequest(String command, JSONObject data) {

        System.out.println("Making app request: " + command + " with data: " + data.toString());

        if (command.equalsIgnoreCase("addtocart")) {

            int productID = 0;

            try {
                productID = data.getInt("productID");


                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                // Create a new map of values, where column names are the keys
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_ID, productID);
                values.put(DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_NAME, data.getString("name"));
                values.put(DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_PRICE, data.getDouble("price"));
                values.put(DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_DESCRIPTION, data.getString("description"));
                values.put(DatabaseHelper.CartEntry.COLUMN_NAME_IMAGE_URL, data.getString("image"));
                values.put(DatabaseHelper.CartEntry.COLUMN_NAME_QUANTITY, 1);

                // Insert the new row, returning the primary key value of the new row
                long newRowId = db.insert(DatabaseHelper.CartEntry.TABLE_NAME, null, values);
                System.out.println("Inserted row id: " + Long.toString(newRowId));

                System.out.println("Product added to cart: " + Integer.toString(productID));
                Crittercism.leaveBreadcrumb("Product added to cart: " + Integer.toString(productID));
                Toast.makeText(getActivity().getApplicationContext(), "Item Added to Cart!", Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Toast.makeText(getActivity().getApplicationContext(), "Error adding item", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shop, container, false);

        Crittercism.leaveBreadcrumb("ShopViewDisplayed");

        mDbHelper = new DatabaseHelper(getActivity().getApplicationContext());

        mWebView = (WebView) rootView.findViewById(R.id.shop_webview);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

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

        mWebView.loadUrl("http://10.0.3.2:8000/");



        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("apprequest://")) {
                    Uri uri = Uri.parse(url);
                    String command = uri.getHost();
                    String jsonString = uri.getFragment();

                    try {
                        JSONObject json = new JSONObject(jsonString);
                        handleRequest(command, json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return true;
                }
                return false;
            }
        });


        return rootView;
    }
}
