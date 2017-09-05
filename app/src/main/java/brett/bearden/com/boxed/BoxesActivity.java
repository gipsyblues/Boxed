package brett.bearden.com.boxed;

import android.app.ActionBar;
import android.app.PendingIntent;
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
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import Database.DBHelper;
import Global.Constants;
import Global.Globals;
import android.support.v7.app.ActionBar.LayoutParams;
import android.widget.Toast;

public class BoxesActivity extends AppCompatActivity {

    // Globals
    protected Globals globals;
    protected DBHelper db;
    protected Typeface typeFace;

    // Buttons
    protected Button btn_create_box;
    protected ImageButton btn_rfid;
    protected ImageButton btn_back;
    protected ImageButton btn_search;
    protected Button btn_show_all_boxes;

    // Views
    protected TextView tv_no_boxes;
    protected TextView tv_no_items;
    protected EditText et_search;
    protected ScrollView list_scrollview;
    protected RelativeLayout list_layout;
    protected ProgressBar progress_circle;
    protected int list_count;

    // NFC for RFID Tags
    protected NfcAdapter nfcAdapter;
    protected PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boxes);

        // Globals
        this.globals = Globals.getInstance();
        this.db = new DBHelper(this);
        this.typeFace = globals.getTypeface(this);

        // Buttons
        this.btn_create_box = (Button) findViewById(R.id.btn_create_box);
        this.btn_show_all_boxes = (Button) findViewById(R.id.btn_show_all_boxes);

        // Views
        this.tv_no_boxes = (TextView) findViewById(R.id.tv_no_boxes);
        this.tv_no_items = (TextView) findViewById(R.id.tv_no_items);
        this.list_scrollview = (ScrollView) findViewById(R.id.list_scrollview);
        this.list_layout = (RelativeLayout) findViewById(R.id.list_layout);
        this.progress_circle = (ProgressBar) findViewById(R.id.progress_circle);

        // NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        setupActionBar();
        setupActivity();
    }

    /**
     * Setup the Action Bar
     */
    protected void setupActionBar(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);

        // Hide the title
        TextView titleView = (TextView) findViewById(R.id.action_bar_title);
        titleView.setVisibility(View.INVISIBLE);

        // Show the RFID Icon
        this.btn_rfid = (ImageButton) findViewById(R.id.btn_rfid);
        btn_rfid.setVisibility(View.VISIBLE);

        // Show the Back Button
        this.btn_back = (ImageButton) findViewById(R.id.btn_back);
        btn_back.setVisibility(View.VISIBLE);

        // Show the Search Button
        this.btn_search = (ImageButton) findViewById(R.id.btn_search);
        btn_search.setVisibility(View.VISIBLE);

        // Show the Search EditText
        this.et_search = (EditText) findViewById(R.id.et_search);
        et_search.setVisibility(View.VISIBLE);

        // Restrict the new item character length
        et_search.setFilters(new InputFilter[]{new InputFilter.LengthFilter(22)});

        // Set the background image
        BitmapDrawable background = new BitmapDrawable (BitmapFactory.decodeResource(getResources(), R.drawable.tape));
        getSupportActionBar().setBackgroundDrawable(background);
    }

    // Set up the UI and background operations.
    private void setupActivity(){
        // Set the font for any text views
        setFont();

        // Initialize the buttons with a listener for click events
        btn_show_all_boxes.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ showAllBoxes(); }
        });

        btn_back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ onBackPressed(); }
        });

        btn_rfid.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ displayInfo(); }
        });

        btn_create_box.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                changeActivity(Constants.ChangeActivity.CREATE_BOX); }
        });

        btn_search.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ searchBoxes(); }
        });

        et_search.setOnKeyListener(new View.OnKeyListener(){
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    switch (keyCode){
                        case KeyEvent.KEYCODE_ENTER:
                            searchBoxes();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Set Font: Apply a custom font to text.
     */
    protected void setFont(){
        btn_create_box.setTypeface(typeFace);
        btn_show_all_boxes.setTypeface(typeFace);
        tv_no_boxes.setTypeface(typeFace);
        tv_no_items.setTypeface(typeFace);
    }

    @Override
    public void onStart(){
        super.onStart();

        // Query any saved boxes
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                getBoxes();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        // Resume NFC
        if(globals.hasNFC) nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
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
                        changeActivity(Constants.ChangeActivity.BOX);
                    }
                    else Toast.makeText(getApplicationContext(), "No Box Found!", Toast.LENGTH_LONG);
                }
            }
        }
    }

    /**
     * Query all Boxes
     */
    protected void getBoxes(){
        Cursor cursor = db.getQuery("SELECT * FROM " + db.BOXES_TABLE_NAME + " WHERE " +
                db.BOXES_COLUMN_MOVE_ID + " = " + globals.move_id + " ORDER BY "
                + db.BOXES_COLUMN_BOX_ID);

        if(cursor.moveToFirst()){
            this.list_count = 1;
            setListParameters();
            buildBoxesList(cursor);
        }
        else tv_no_boxes.setVisibility(View.VISIBLE);
    }

    // Show All Boxes
    protected void showAllBoxes(){
        tv_no_items.setVisibility(View.INVISIBLE);
        btn_show_all_boxes.setVisibility(View.INVISIBLE);

        // Reset List Scroll View
        list_count = 1;
        list_layout.removeAllViews();
        setListParameters();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                getBoxes();
            }
        });
    }

    /**
     *  Build Box List
     * @param cursor
     */
    protected void buildBoxesList(Cursor cursor){
        final int id = cursor.getInt(cursor.getColumnIndexOrThrow(db.BOXES_COLUMN_BOX_ID));
        final String rfid = cursor.getString(cursor.getColumnIndexOrThrow(db.BOXES_COLUMN_RFID));
        final String location = cursor.getString(cursor.getColumnIndexOrThrow(db.BOXES_COLUMN_LOCATION));
        final int heavy = cursor.getInt(cursor.getColumnIndexOrThrow(db.BOXES_COLUMN_HEAVY));
        final int fragile = cursor.getInt(cursor.getColumnIndexOrThrow(db.BOXES_COLUMN_FRAGILE));
        final byte[] imageArray = cursor.getBlob(cursor.getColumnIndexOrThrow(db.BOXES_COLUMN_IMAGE));

        // Build the box image
        ImageButton btn_image = new ImageButton(this);
        btn_image.setId(list_count);

        if(imageArray!= null){
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
            btn_image.setImageBitmap(bitmap);
        }
        else
            btn_image.setBackgroundResource(R.drawable.box);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(200, 200);

        if(list_count > 1){
            params.addRule(RelativeLayout.BELOW, list_count-2);
            btn_image.setPadding(0, 20, 0, 0);
        }
        btn_image.setLayoutParams(params);

        // Build the TextView
        TextView textView = new TextView(this);
        textView.setText(id + ": " + location);
        textView.setTypeface(typeFace);
        textView.setTextSize(30);
        textView.setTextColor(Color.BLACK);
        textView.setMaxLines(1);

        params = new RelativeLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        params.addRule(RelativeLayout.RIGHT_OF, btn_image.getId());
        textView.setPadding(20, 45, 0, 0);

        if(list_count > 1)
            params.addRule(RelativeLayout.BELOW, list_count-2);

        textView.setLayoutParams(params);

        // Set an On Click Listener
        textView.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                globals.box_id = id;
                globals.rfid = rfid;
                globals.location = location;
                globals.heavy = heavy;
                globals.fragile = fragile;
                setGlobalImage(imageArray);

                changeActivity(Constants.ChangeActivity.BOX);
            }
        });

        // Add the new Move to the list view
        list_layout.addView(btn_image);
        list_layout.addView(textView);

        // If there is another move then recursively add it.
        if(cursor.moveToNext()){
            list_count += 2;
            buildBoxesList(cursor);
        }
    }

    // Set Global Box Image
    protected void setGlobalImage(byte[] imageArray){
        if(imageArray != null){
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
            globals.box_image = bitmap;
        }
        else globals.box_image = null;
    }

    // Search Boxes
    protected void searchBoxes(){
        // Hide soft keyboard
        globals.hideSoftKeyboard(getApplicationContext(), et_search);

        String item = et_search.getText().toString().trim();

        // If the search field is empty or there are no boxes to search then do nothing
        if(TextUtils.isEmpty(item) || tv_no_boxes.getVisibility() == View.VISIBLE){
            et_search.setText("");
            et_search.clearFocus();
            return;
        }

        Cursor cursor = db.getQuery("SELECT A.* FROM " + db.BOXES_TABLE_NAME + " A " +
                        " WHERE A." + db.BOXES_COLUMN_BOX_ID + " IN (SELECT B." +
                        db.BOX_CONTENT_COLUMN_BOX_ID + " FROM " + db.BOX_CONTENT_TABLE_NAME + " B " +
                " WHERE B." + db.BOX_CONTENT_COLUMN_ITEM + " LIKE '%" + item + "%') " +
                " ORDER BY " + db.BOXES_COLUMN_BOX_ID);

        // Reset List Scroll View
        list_count = 1;
        list_layout.removeAllViews();

        // Either build the list or show a no item found message
        if(cursor.moveToFirst()){
            tv_no_items.setVisibility(View.INVISIBLE);
            btn_show_all_boxes.setVisibility(View.VISIBLE);

            // Set Scrollview Parameters
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            params.addRule(RelativeLayout.BELOW, btn_show_all_boxes.getId());
            params.setMargins(30, 0, 0, 170);
            list_scrollview.setLayoutParams(params);

            // Build list
            buildBoxesList(cursor);
        }
        else{
            tv_no_items.setText("No boxes found with\nitem: " + item);
            tv_no_items.setVisibility(View.VISIBLE);
            btn_show_all_boxes.setVisibility(View.VISIBLE);
        }

        // Reset search field
        et_search.setText("");
        et_search.clearFocus();
    }

    // Set Scroll View List Parameters
    protected void setListParameters(){
        // Set Scrollview Parameters
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        params.addRule(RelativeLayout.BELOW, findViewById(R.id.image_line).getId());
        params.setMargins(30, 0, 0, 170);
        list_scrollview.setLayoutParams(params);
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

        if(nextActivity.equals(Constants.ChangeActivity.MOVE))
            intent = new Intent(this, MoveActivity.class);
        else if(nextActivity.equals(Constants.ChangeActivity.CREATE_BOX))
            intent = new Intent(this, CreateBoxActivity.class);
        else if(nextActivity.equals(Constants.ChangeActivity.BOX))
            intent = new Intent(this, BoxActivity.class);

        if(intent != null) startActivity(intent);
    }

    @Override
    public void onBackPressed(){ changeActivity(Constants.ChangeActivity.MOVE); }
}