package crittercism.com.critterretail;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

class APIRequest extends AsyncTask<String, String, String> {

    private String mError = null;
    private APIResponse mAPIResponse;
    private int mStatusCode = 0;

    public APIRequest(APIResponse apiResponse) {
        mAPIResponse = apiResponse;
    }

    @Override
    protected String doInBackground(String... path) {
        String responseString = null;
        try {
            URL url = new URL("http://10.0.3.2:8000/api/" + path[0]);
            System.out.println("Requesting URL: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            mStatusCode = conn.getResponseCode();

            if(mStatusCode < 200 || mStatusCode >= 300) {
                throw new Exception("Invalid HTTP status code returned");
            }

        } catch (IOException e) {
            mError = e.getLocalizedMessage();
        } catch (Exception e) {
            mError = e.getLocalizedMessage();
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(mError==null) {
            mAPIResponse.success();
        } else {
            mAPIResponse.failure(mError, mStatusCode);
        }
    }
}
