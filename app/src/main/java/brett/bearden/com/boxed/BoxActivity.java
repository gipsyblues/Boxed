package brett.bearden.com.boxed;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import Database.DBHelper;
import Global.Constants;
import Global.Globals;

public class BoxActivity extends AppCompatActivity {

    // Globals
    protected Globals globals;
    protected DBHelper db;
    protected Typeface typeFace;

    // Buttons
    protected ImageButton btn_edit_box;
    protected ImageButton btn_rfid;
    protected ImageButton btn_back;
    protected ImageButton btn_add_item;

    // Views
    protected TextView tv_no_items;
    protected ImageView img_heavy;
    protected ImageView img_fragile;
    protected ImageView img_box;
    protected EditText et_item;
    protected ProgressBar progress_circle;
    protected TextView titleView;

    // List
    protected RelativeLayout list_layout;
    protected int list_count;
    protected HashMap item_map;

    // NFC for RFID Tags
    protected NfcAdapter nfcAdapter;
    protected PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box);

        // Globals
        this.globals = Globals.getInstance();
        this.db = new DBHelper(this);
        this.typeFace = globals.getTypeface(this);

        // Buttons
        this.btn_edit_box = (ImageButton) findViewById(R.id.btn_edit_box);
        this.btn_add_item = (ImageButton) findViewById(R.id.btn_add_item);

        // Views
        this.tv_no_items = (TextView) findViewById(R.id.tv_no_items);
        this.img_heavy = (ImageView) findViewById(R.id.img_heavy);
        this.img_fragile = (ImageView) findViewById(R.id.img_fragile);
        this.img_box = (ImageView) findViewById(R.id.img_box);
        this.et_item = (EditText) findViewById(R.id.et_item);
        this.list_layout = (RelativeLayout) findViewById(R.id.list_layout);
        this.progress_circle = (ProgressBar) findViewById(R.id.progress_circle);

        // Item Map stores items and their edit text view id for editing.
        this.item_map = new HashMap();

        // NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        setActionBar();
        setActivity();
    }

    // Set the Action Bar
    protected void setActionBar(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);

        // Change the title
        titleView = (TextView) findViewById(R.id.action_bar_title);
        titleView.setTypeface(typeFace);
        titleView.setText("Box # " + globals.box_id);

        // Show the RFID Icon
        this.btn_rfid = (ImageButton) findViewById(R.id.btn_rfid);
        btn_rfid.setVisibility(View.VISIBLE);

        // Show the Back Button
        this.btn_back = (ImageButton) findViewById(R.id.btn_back);
        btn_back.setVisibility(View.VISIBLE);

        // Set the background image
        BitmapDrawable background = new BitmapDrawable (BitmapFactory.decodeResource(getResources(), R.drawable.tape));
        getSupportActionBar().setBackgroundDrawable(background);
    }

    // Set the UI and background operations.
    private void setActivity(){
        // Set the font for any text views
        setFont();

        // Add button listeners
        btn_edit_box.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ changeActivity(Constants.ChangeActivity.EDIT_BOX); }
        });
        btn_back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ onBackPressed(); }
        });
        btn_rfid.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ displayInfo(); }
        });
        btn_add_item.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ addItem(); }
        });

        // Restrict the new item character length
        et_item.setFilters(new InputFilter[]{new InputFilter.LengthFilter(22)});
    }

    // Set Font: Apply a custom font to text.
    protected void setFont(){
        TextView tv = (TextView) findViewById(R.id.tv_add_item);
        tv.setTypeface(typeFace);
        tv = (TextView) findViewById(R.id.tv_heavy);
        tv.setTypeface(typeFace);
        tv = (TextView) findViewById(R.id.tv_fragile);
        tv.setTypeface(typeFace);

        tv_no_items.setTypeface(typeFace);
        et_item.setTypeface(typeFace);
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onResume(){
        super.onResume();

        // Resume NFC
        if(globals.hasNFC) nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                setBoxContent();
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();

        // Release NFC
        if(globals.hasNFC) nfcAdapter.disableForegroundDispatch(this);
    }

    // On New Intent Get RFID Tag ID
    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);

        String action = intent.getAction();
        if(globals.hasNFC){
            if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)){
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                if(tag == null) Toast.makeText(this, "No Tag ID Found", Toast.LENGTH_SHORT);
                else{
                    // Convert the Tag ID to a String
                    String rfid = "";
                    byte[] tagId = tag.getId();
                    for(int i=0; i<tagId.length; i++)
                        rfid += "" + Integer.toHexString(tagId[i] & 0xFF);

                    // Query the RFID and retrieve the box and move information
                    Object[] data = db.queryRFID(rfid);

                    // If the query returned results then set the data and change activity
                    if(data != null){
                        globals.setRFIDData(data);
                        list_layout.removeAllViews();
                        titleView.setText("Box # " + globals.box_id);

                        new Handler().post(new Runnable() {
                            @Override
                            public void run() { setBoxContent();
                            }
                        });
                    }
                    else Toast.makeText(getApplicationContext(), "No Box Found!", Toast.LENGTH_LONG);
                }
            }
        }
    }

    // Set Box Content
    protected void setBoxContent(){
        // Check if the box is heavy or fragile
        if(globals.heavy == 1)
            img_heavy.setBackgroundResource(R.drawable.checked);
        if(globals.fragile == 1)
            img_fragile.setBackgroundResource(R.drawable.checked);

        // Set the box image
        if(globals.box_image != null)
            img_box.setImageBitmap(globals.box_image);

        // Query any items saved in the box
        getBoxContent();
    }

    /**
     * Build the box content by either a query result
     * or grab the list globally. The global list is
     * shared between the Box List Activity and the
     * Add Item Activity. This prevents having to
     * query the box content switching between
     * activities.
     */
    protected void getBoxContent(){
        Cursor cursor = db.getQuery("SELECT * FROM " + db.BOX_CONTENT_TABLE_NAME + " WHERE " +
                db.BOX_CONTENT_COLUMN_MOVE_ID + " = " + globals.move_id + " AND " +
                db.BOX_CONTENT_COLUMN_BOX_ID + " = " + globals.box_id);

        /**
         * If the query returned results, then increase the list count.
         * Make sure the no items message is visible and build the list views.
         */
        if(cursor.moveToFirst()){
            tv_no_items.setVisibility(View.INVISIBLE);
            list_count = 1;
            buildBoxContentList(cursor);
            globals.cursor = cursor;
        }
        else list_count = 0;

        // If there are no items in the list, then display a message
        if(list_count < 1)
            tv_no_items.setVisibility(View.VISIBLE);
    }

    /**
     *  Build Box Content List
     * @param cursor
     */
    protected void buildBoxContentList(Cursor cursor){
        final String item = cursor.getString(cursor.getColumnIndexOrThrow(db.BOX_CONTENT_COLUMN_ITEM));
        final byte[] image = cursor.getBlob(cursor.getColumnIndexOrThrow(db.BOX_CONTENT_COLUMN_IMAGE));

        // Convert the image byte[] to a bitmap
        Bitmap bitmap = null;
        if(image != null) bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

        // Build the item views
        if(bitmap != null) buildItemViews(item, bitmap);
        else buildItemViews(item, null);

        // If there is another move then recursively add it.
        if(cursor.moveToNext()){
            list_count ++;
            buildBoxContentList(cursor);
        }
    }

    /**
     * Check that the add item field is not empty
     * then add the item to the list.
     */
    protected void addItem(){
        String item = et_item.getText().toString().trim();

        if(TextUtils.isEmpty(item)){
            et_item.setError("Enter an item to add.");
        }
        else{
            tv_no_items.setVisibility(View.INVISIBLE);
            list_count++;
            buildItemViews(item, null);
            db.insertBoxItem(globals.move_id, globals.box_id, item);
            et_item.setText("");
        }
    }

    /**
     * Build item views generates an image of each item, an edit text view,
     * and buttons to control editing and deleting the item. After each view
     * is created they are added to the list view.
     */
    protected void buildItemViews(String item, Bitmap image){
        // Create an image for the item
        final ImageView image_item = new ImageView(this);
        image_item.setId(list_count);

        // Apply saved image or use default image
        if(image != null) image_item.setImageBitmap(image);
        else image_item.setBackgroundResource(R.drawable.openbox);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(250, 250);

        // If there is more than one item then set extra layout parameters
        if(list_count > 1){
            // The button image will be below the previous image,
            // which is 5 id's less than the current id.
            params.addRule(RelativeLayout.BELOW, list_count-5);
            params.setMargins(0, 75, 0, 0);
        }
        image_item.setLayoutParams(params);

        // Build the TextView
        list_count++;
        final EditText editText = new EditText(this);
        editText.setId(list_count);
        editText.setText(item);
        editText.setTypeface(globals.getTypeface(this));
        editText.setTextSize(30);
        editText.setTextColor(Color.BLACK);
        editText.setMaxLines(1);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(22)});
        editText.setEnabled(false);

        params = new RelativeLayout.LayoutParams(new android.support.v7.app.ActionBar.LayoutParams(
                android.support.v7.app.ActionBar.LayoutParams.WRAP_CONTENT,
                android.support.v7.app.ActionBar.LayoutParams.WRAP_CONTENT));
        params.addRule(RelativeLayout.RIGHT_OF, image_item.getId());

        if(list_count > 2){
            params.setMargins(10, 80, 0, 0);
            params.addRule(RelativeLayout.BELOW, image_item.getId()-5);
        }
        else params.setMargins(10, 5, 0, 0);

        editText.setLayoutParams(params);

        // Create an image button to take a picture of the item
        list_count++;
        final ImageButton btn_image = new ImageButton(this);
        btn_image.setId(list_count);
        btn_image.setBackgroundResource(R.mipmap.ic_action_camera);

        params = new RelativeLayout.LayoutParams(
                new android.support.v7.app.ActionBar.LayoutParams(100, 100));
        params.addRule(RelativeLayout.RIGHT_OF, image_item.getId());
        params.addRule(RelativeLayout.BELOW, editText.getId());
        params.setMargins(10, 0, 0, 0);
        btn_image.setLayoutParams(params);

        // Camera Button Listener
        btn_image.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                globals.item = editText.getText().toString().trim();
                globals.previousActivity = Constants.ChangeActivity.BOX;
                changeActivity(Constants.ChangeActivity.CAMERA);
            }
        });

        // Create an image button to edit the item
        list_count++;
        final ImageButton btn_edit = new ImageButton(this);
        btn_edit.setId(list_count);
        btn_edit.setBackgroundResource(R.mipmap.ic_action_edit);

        params = new RelativeLayout.LayoutParams(
                new android.support.v7.app.ActionBar.LayoutParams(75, 75));
        params.addRule(RelativeLayout.RIGHT_OF, btn_image.getId());
        params.addRule(RelativeLayout.BELOW, editText.getId());
        params.setMargins(40, 15, 0, 0);
        btn_edit.setLayoutParams(params);

        // Edit Button Listener
        btn_edit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ enableSaveItem(editText.getId(), editText, btn_edit); }
        });

        // Create an image button to delete the item
        list_count++;
        final ImageButton btn_delete = new ImageButton(this);
        btn_delete.setId(list_count);
        btn_delete.setBackgroundResource(R.mipmap.ic_action_remove);

        params = new RelativeLayout.LayoutParams(
                new android.support.v7.app.ActionBar.LayoutParams(75, 75));
        params.addRule(RelativeLayout.RIGHT_OF, btn_edit.getId());
        params.addRule(RelativeLayout.BELOW, editText.getId());
        params.setMargins(40, 15, 0, 0);
        btn_delete.setLayoutParams(params);

        // Delete Button Listener
        btn_delete.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                warnDeleteItem(editText.getId(), editText);
            }
        });

        // Add the views to the layout
        list_layout.addView(image_item);
        list_layout.addView(editText);
        list_layout.addView(btn_image);
        list_layout.addView(btn_edit);
        list_layout.addView(btn_delete);
    }

    /**
     * Change the button image icon between edit/save.
     * @param id: Layout id to save old item description in hash map.
     *          The old description is used to update the database.
     * @param editText: Item description
     * @param btn_edit: Toggle for editing and saving an item.
     */
    protected void enableSaveItem(int id, EditText editText, ImageButton btn_edit){
        String item = editText.getText().toString().trim();

        // If saving then validate the content to make sure it is not empty
        if(editText.isEnabled()){

            if(TextUtils.isEmpty(item))
                editText.setError("Description cannot be empty.");
            else{
                editText.setEnabled(false);
                editText.clearFocus();
                btn_edit.setBackgroundResource(R.mipmap.ic_action_edit);

                // Check if the user made a change to the item description
                String old_description = (String) item_map.get(id);
                if(!TextUtils.equals(old_description, item)){
                    old_description = old_description.replaceAll("'", "''");
                    db.updateBoxItem(globals.move_id, globals.box_id, old_description, item);
                }

                // Remove the item from the map
                item_map.remove(id);
            }
        }
        else{
            item_map.put(id, item);
            editText.setEnabled(true);
            btn_edit.setBackgroundResource(R.mipmap.ic_action_save);
        }
    }

    // Confirm Delete Item.
    protected void warnDeleteItem(final int layout_id, final EditText editText){
        String message = "Are you sure you want to delete\nitem: " +
                editText.getText().toString().trim() + "?";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete:");
        builder.setMessage(message);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                deleteItem(layout_id, editText);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){ dialog.dismiss(); }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Confirm Continue Without Saving.
    protected void warnUnsavedItems(){
        String message = "There are unsaved items.\n " +
                "Continue without saving?";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning!");
        builder.setMessage(message);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                changeActivity(Constants.ChangeActivity.BOXES);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){ dialog.dismiss(); }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Delete Item
     * @param layout_id
     * @param editText
     */
    protected void deleteItem(int layout_id, EditText editText){
        // If the edit text view is enabled, then delete it from the hash map.
        if(editText.isEnabled()) item_map.remove(layout_id);

        // Delete from the database
        String item = editText.getText().toString().trim();
        item = item.replace("'", "''");
        db.deleteBoxItem(globals.move_id, globals.box_id, item);

        // Delete from the layout
        list_layout.removeAllViews();

        // Rebuild the item list
        getBoxContent();
    }

    /**
     * Display a dialog with the Info about this Activity
     */
    public void displayInfo(){
        Intent intent = new Intent(this, DisplayRFIDInfoActivity.class);
        startActivity(intent);
    }

    /**
     * Change to the next activity
     */
    protected void changeActivity(Constants.ChangeActivity nextActivity){
        progress_circle.setVisibility(View.VISIBLE);

        Intent intent = null;

        if(nextActivity.equals(Constants.ChangeActivity.EDIT_BOX))
            intent = new Intent(this, EditBoxActivity.class);
        else if(nextActivity.equals(Constants.ChangeActivity.BOXES))
            intent = new Intent(this, BoxesActivity.class);
        else if(nextActivity.equals(Constants.ChangeActivity.MAIN))
            intent = new Intent(this, MainActivity.class);
        else if(nextActivity.equals(Constants.ChangeActivity.CAMERA))
            intent = new Intent(this, CameraActivity.class);

        if(intent != null) startActivity(intent);
    }

    /**
     * On Back Press
     */
    @Override
    public void onBackPressed(){
        globals.box_id = -1;
        globals.box_image = null;
        globals.heavy = -1;
        globals.fragile = -1;
        globals.location = null;

        // If there is any unsaved items then show a warning.
        if(!item_map.isEmpty()) warnUnsavedItems();
        else changeActivity(Constants.ChangeActivity.BOXES); }
}