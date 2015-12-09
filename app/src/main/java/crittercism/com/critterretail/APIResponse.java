package crittercism.com.critterretail;

/**
 * Created by chrisbeauchamp on 12/8/15.
 */
public interface APIResponse {
    public void success();
    public void failure(String error, int statusCode);
}