package crittercism.com.critterretail;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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

        String headerText = cursor.getString(cursor.getColumnIndexOrThrow(
                DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_NAME));
        rowHeader.setText(headerText);

        String descText = cursor.getString(cursor.getColumnIndexOrThrow(
                DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_DESCRIPTION));
        rowDesc.setText(descText);

        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(
                DatabaseHelper.CartEntry.COLUMN_NAME_PRODUCT_PRICE));
        String priceString = String.format("$%.2f", price);
        rowPrice.setText(priceString);

        String prodImageURL = cursor.getString(cursor.getColumnIndexOrThrow(
                DatabaseHelper.CartEntry.COLUMN_NAME_IMAGE_URL));

        new DownloadImageTask(prodImage).execute(prodImageURL);
    }

}
