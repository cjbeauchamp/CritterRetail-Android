package crittercism.com.critterretail;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.crittercism.app.Crittercism;

public class ShippingActivity extends AppCompatActivity {

    private TextView mZipcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping);

        Crittercism.leaveBreadcrumb("ShippingViewDisplayed");

        mZipcode = (TextView) this.findViewById(R.id.shipping_zip);

        Button continueButton = (Button) this.findViewById(R.id.shipping_continue);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog mDialog = new ProgressDialog(ShippingActivity.this);
                mDialog.setMessage("Submitting Form...");
                mDialog.setCancelable(false);
                mDialog.setIndeterminate(true);
                mDialog.show();

                new APIRequest(new APIResponse() {
                    @Override
                    public void success() {
                        mDialog.hide();

                        Intent intent = new Intent(ShippingActivity.this, BillingActivity.class);
                        startActivity(intent);
                    }
                    @Override
                    public void failure(String error, int statusCode) {
                        mDialog.hide();

                        String message = "Uh oh! Something bad happened!";

                        // invalid payment info
                        if(statusCode == 300) {
                            message = "Invalid payment info!";
                        }

                        // failed txn on server
                        else if(statusCode == 500) {
                            message = "Transaction failed due to server error!";
                            throw new RuntimeException("Uncaught exception - unable to parse JSON.");
                        }

                        // invalid server response
                        else if(statusCode == 600) {
                            Crittercism.failTransaction("checkout");
                            message = "Server error, unable to parse response";
                        } else {
                            Crittercism.failTransaction("checkout");
                            message = "Server error, unable to parse response";
                        }

                        // show to user
                        new android.app.AlertDialog.Builder(ShippingActivity.this)
                                .setTitle("Error!")
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();

                        System.out.println("Request finished with error: " + error + " and code: " + statusCode);
                    }
                }).execute("confirmPayment/"+mZipcode.getText());

            }
        });

    }
}
