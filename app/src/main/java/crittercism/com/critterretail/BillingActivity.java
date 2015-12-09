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

public class BillingActivity extends AppCompatActivity {

    private DatabaseHelper mDbHelper;

    private void reloadPrices() {
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


        Cursor cursor = db.rawQuery("SELECT sum(" + DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_PRICE + ") FROM " + DatabaseHelper.CartEntry.TABLE_NAME, null);
        cursor.moveToFirst();
        double cartTotal =  cursor.getDouble(0);
        cursor.close();

        final TextView cartText = (TextView) this.findViewById(R.id.billing_cart_total);
        final TextView shippingText = (TextView) this.findViewById(R.id.billing_shipping_total);
        final TextView taxText = (TextView) this.findViewById(R.id.billing_tax_total);
        final TextView totalText = (TextView) this.findViewById(R.id.billing_total);

        cartText.setText("$" + String.valueOf(cartTotal));
        shippingText.setText("$7.99");
        taxText.setText("$1.42");
        totalText.setText(String.format("$%.2f", (cartTotal+7.99+1.42)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);

        mDbHelper = new DatabaseHelper(this.getApplicationContext());

        this.reloadPrices();

        Button billingContinue = (Button) this.findViewById(R.id.billing_continue);
        billingContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog mDialog = new ProgressDialog(BillingActivity.this);
                mDialog.setMessage("Submitting Form...");
                mDialog.setCancelable(false);
                mDialog.show();

                new APIRequest(new APIResponse() {
                    @Override
                    public void success() {
                        mDialog.hide();

                        // clear our cart
                        SQLiteDatabase db = mDbHelper.getWritableDatabase();
                        db.delete(DatabaseHelper.CartEntry.TABLE_NAME, null, null);

                        // show a toast
                        Toast.makeText(BillingActivity.this, "Purchase Complete!", Toast.LENGTH_LONG).show();


                        // pop the activities
                        Intent intent = new Intent(BillingActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Removes other Activities from stack
                        startActivity(intent);
                    }
                    @Override
                    public void failure(String error, int statusCode) {
                        mDialog.hide();

                        // trigger a crash
                        if(statusCode == 300) {
                            throw new RuntimeException("Uncaught exception - unable to parse JSON.");
                        }

                        // invalid server response
                        else {
                            new android.app.AlertDialog.Builder(BillingActivity.this)
                                    .setTitle("Error!")
                                    .setMessage("Unable to parse server response")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }

                        System.out.println("Request finished with error: " + error + " and code: " + statusCode);
                    }
                }).execute("completePurchase");

            }
        });
    }
}
