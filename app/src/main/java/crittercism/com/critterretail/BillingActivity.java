package crittercism.com.critterretail;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crittercism.app.Crittercism;

public class BillingActivity extends AppCompatActivity {

    private DatabaseHelper mDbHelper;

    // UI Elements
    private TextView mCartText;
    private TextView mShippingText;
    private TextView mTaxText;
    private TextView mTotalText;

    /* Reloads the UI with the proper figures */
    private void reloadPrices() {

        // get a database instance to call
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

        // query for the sum of the item prices in the cart
        Cursor cursor = db.rawQuery("SELECT sum(" +
                DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_PRICE + ") FROM " +
                DatabaseHelper.CartEntry.TABLE_NAME, null);

        // select the first (only) returned row
        cursor.moveToFirst();

        // extract the sum from the result
        double cartTotal =  cursor.getDouble(0);

        // close the cursor
        cursor.close();

        // set the new price values in the UI
        mCartText.setText("$" + String.valueOf(cartTotal));
        mShippingText.setText("$7.99");
        mTaxText.setText("$1.42");
        mTotalText.setText(String.format("$%.2f", (cartTotal+7.99+1.42)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up our UI
        setContentView(R.layout.activity_billing);
        Button billingContinue = (Button) this.findViewById(R.id.billing_continue);

        // bind the UI items
        mCartText = (TextView) this.findViewById(R.id.billing_cart_total);
        mShippingText = (TextView) this.findViewById(R.id.billing_shipping_total);
        mTaxText = (TextView) this.findViewById(R.id.billing_tax_total);
        mTotalText = (TextView) this.findViewById(R.id.billing_total);

        // track where we are in the flow
        Crittercism.leaveBreadcrumb("CheckoutViewDisplayed");

        // create a database helper instance
        mDbHelper = new DatabaseHelper(this.getApplicationContext());

        // set up a click listener with the continue button
        billingContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // we're about to make an API call so show a UI loader
                final ProgressDialog mDialog = new ProgressDialog(BillingActivity.this);
                mDialog.setMessage("Completing purchase...");
                mDialog.setCancelable(false);
                mDialog.show();

                // make an API request to the 'completePurchase' endpoint
                new APIRequest(new APIResponse() {
                    @Override
                    public void success() {

                        // clear our cart
                        SQLiteDatabase db = mDbHelper.getWritableDatabase();
                        db.delete(DatabaseHelper.CartEntry.TABLE_NAME, null, null);

                        // hide the loading dialog
                        mDialog.hide();

                        // tell the user we succeeded
                        Toast.makeText(BillingActivity.this, "Purchase Complete!",
                                Toast.LENGTH_LONG).show();

                        // successfully end the transaction
                        Crittercism.endTransaction("checkout");

                        // remove all but the root activity from the stack
                        Intent intent = new Intent(BillingActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    @Override
                    public void failure(String error, int statusCode) {

                        // hide the loading dialog
                        mDialog.hide();

                        // for demo's sake, when we get a 300 response, we trigger a crash
                        // to see
                        if(statusCode == 300) {
                            throw new RuntimeException("Uncaught exception: unable to parse JSON.");
                        }

                        // general invalid server response
                        else {

                            // mark as a failed transaction since the payment didn't process
                            Crittercism.failTransaction("checkout");

                            // tell the user something bad happened
                            new android.app.AlertDialog.Builder(BillingActivity.this)
                                    .setTitle("Error!")
                                    .setMessage("Unable to parse server response")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }

                        System.out.println("Request finished with error: " + error +
                                " and code: " + statusCode);
                    }
                }).execute("completePurchase");

            }
        });

        // the view is loaded, load in our pricing table
        this.reloadPrices();
    }
}
