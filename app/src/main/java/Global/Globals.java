package Global;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.nfc.NfcAdapter;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import Global.Constants.ChangeActivity;
import brett.bearden.com.boxed.R;

public class Globals {
    private static Globals instance;

    // Camera Available
    public static boolean hasCamera = false;

    // NFC Available
    public static boolean hasNFC;

    // ID's
    public static int move_id;
    public static int box_id;
    public static String rfid;

    // Move Description differentiates each move.
    public static String move_description;

    /**
     * Move Location: Location can be a 1 or 2
     * representing the starting address and the
     * destination address respectively.
     */
    public static String location;
    public static String starting_street;
    public static String starting_city_state_zip;
    public static String destination_street;
    public static String destination_city_state_zip;

    // Box attributes
    public static int heavy;
    public static int fragile;
    public static String item;
    public static Bitmap box_image;

    // Views
    public static int list_count = 0;
    public static RelativeLayout list_layout = null;

    // Queries
    public static Cursor cursor;

    // Camera
    public static ChangeActivity previousActivity = null;

    /**
     * Set RFID Data takes an Object[] and sets each field
     * as the global values.
     * @param data
     */
    public static void setRFIDData(Object[] data){
        move_id = (int)data[0];
        box_id = (int)data[1];
        rfid = (String)data[2];
        location = (String)data[3];
        heavy = (int)data[4];
        fragile = (int)data[5];
        move_description = (String)data[13];

        // Convert the Box Image
        if(data[6] != null){
            byte[] imageArray = (byte[])data[6];
            box_image = BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
        }
        else
            box_image = null;

        // Get the address of the move
        int loc = (int)data[7];
        if(loc == 1){
            starting_street = (String)data[8];
            starting_city_state_zip = (String)data[9];
        }
        else{
            destination_street = (String)data[8];
            destination_city_state_zip = (String)data[9];
        }
        loc = (int)data[10];
        if(loc == 1){
            starting_street = (String)data[11];
            starting_city_state_zip = (String)data[12];
        }
        else{
            destination_street = (String)data[11];
            destination_city_state_zip = (String)data[12];
        }
    }

    // Get Instance
    public static synchronized Globals getInstance(){
        if(instance==null) instance=new Globals();
        return instance;
    }

    // Get Typeface
    public static Typeface getTypeface(final Context context){
        return Typeface.createFromAsset(context.getAssets(), "font/conformity.otf");
    }

    // Clear Data
    public static void clearData(){
        move_id = -1;
        box_id = -1;
        move_description = null;
        starting_street = null;
        starting_city_state_zip = null;
        destination_street = null;
        destination_city_state_zip = null;
    }

    /**
     * Hide Soft Keyboard
     * @param view
     */
    public static void hideSoftKeyboard(Context context, View view){
        InputMethodManager imm =
                (InputMethodManager)context.getSystemService(context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Build item views generates an image of each item, an edit text view,
     * and buttons to control editing and deleting the item. After each view
     * is created they are added to the list view.
     */
    public static void buildItemViews(String item, Activity activity, RelativeLayout layout, int num_items){
        // Create an image for the item
        ImageView image_item = new ImageView(activity);
        image_item.setId(num_items);
        image_item.setBackgroundResource(R.drawable.openbox);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(250, 250);

        // If there is more than one item then set extra layout parameters
        if(num_items > 1){
            // The button image will be below the previous image,
            // which is 5 id's less than the current id.
            params.addRule(RelativeLayout.BELOW, num_items-5);
            params.setMargins(0, 75, 0, 0);
        }
        image_item.setLayoutParams(params);

        // Build the TextView
        num_items++;
        EditText editText = new EditText(activity);
        editText.setId(num_items);
        editText.setText(item);
        editText.setTypeface(getTypeface(activity.getApplicationContext()));
        editText.setTextSize(30);
        editText.setTextColor(Color.BLACK);
        editText.setMaxLines(1);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(24)});
        editText.setFocusable(false);
        editText.setEnabled(false);

        params = new RelativeLayout.LayoutParams(new android.support.v7.app.ActionBar.LayoutParams(
                android.support.v7.app.ActionBar.LayoutParams.WRAP_CONTENT,
                android.support.v7.app.ActionBar.LayoutParams.WRAP_CONTENT));
        params.addRule(RelativeLayout.RIGHT_OF, image_item.getId());

        if(num_items > 2){
            params.setMargins(10, 80, 0, 0);
            params.addRule(RelativeLayout.BELOW, image_item.getId()-5);
        }
        else params.setMargins(10, 5, 0, 0);

        editText.setLayoutParams(params);

        // Create an image button to take a picture of the item
        num_items++;
        ImageButton btn_image = new ImageButton(activity);
        btn_image.setId(num_items);
        btn_image.setBackgroundResource(R.mipmap.ic_action_camera);

        params = new RelativeLayout.LayoutParams(
                new android.support.v7.app.ActionBar.LayoutParams(125, 125));
        params.addRule(RelativeLayout.RIGHT_OF, image_item.getId());
        params.addRule(RelativeLayout.BELOW, editText.getId());
        params.setMargins(10, 0, 0, 0);
        btn_image.setLayoutParams(params);

        // Create an image button to edit the item
        num_items++;
        Button btn_edit = new Button(activity);
        btn_edit.setId(num_items);
        btn_edit.setText("edit");
        btn_edit.setTypeface(getTypeface(activity.getApplicationContext()));
        btn_edit.setTextSize(12);
        btn_edit.setTextColor(Color.BLACK);
        btn_edit.setBackgroundResource(R.drawable.note);

        params = new RelativeLayout.LayoutParams(
                new android.support.v7.app.ActionBar.LayoutParams(120, 85));
        params.addRule(RelativeLayout.RIGHT_OF, btn_image.getId());
        params.addRule(RelativeLayout.BELOW, editText.getId());
        params.setMargins(40, 15, 0, 0);
        btn_edit.setLayoutParams(params);

        // Create an image button to delete the item
        num_items++;
        Button btn_delete = new Button(activity);
        btn_delete.setId(num_items);
        btn_delete.setText("delete");
        btn_delete.setTypeface(getTypeface(activity.getApplicationContext()));
        btn_delete.setTextSize(12);
        btn_delete.setTextColor(activity.getResources().getColor(R.color.colorDarkRed));
        btn_delete.setBackgroundResource(R.drawable.note);

        params = new RelativeLayout.LayoutParams(
                new android.support.v7.app.ActionBar.LayoutParams(150, 85));
        params.addRule(RelativeLayout.RIGHT_OF, btn_edit.getId());
        params.addRule(RelativeLayout.BELOW, editText.getId());
        params.setMargins(40, 15, 0, 0);
        btn_delete.setLayoutParams(params);

        // Add the views to the layout
        layout.addView(image_item);
        layout.addView(editText);
        layout.addView(btn_image);
        layout.addView(btn_edit);
        layout.addView(btn_delete);
    }
}