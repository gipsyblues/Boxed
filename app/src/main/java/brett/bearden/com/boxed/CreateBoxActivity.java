package brett.bearden.com.boxed;

import android.app.ActionBar;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import Database.DBHelper;
import Global.Constants;
import Global.Globals;

public class CreateBoxActivity extends AppCompatActivity {

    // Globals
    protected Globals globals;
    protected DBHelper db;
    protected Typeface typeFace;

    // Buttons
    protected Button btn_submit;
    protected ImageButton btn_rfid;
    protected ImageButton btn_info;
    protected ImageButton btn_back;
    protected ImageButton btn_camera;
    protected Button btn_default_img;
    protected Button btn_cancel;

    // Views
    protected CheckBox chk_heavy;
    protected CheckBox chk_fragile;
    protected EditText et_id;
    protected EditText et_rfid;
    protected Spinner sp_location;
    protected ImageView image_error;
    protected ImageView img_box;

    // NFC for RFID Tags
    protected NfcAdapter nfcAdapter;
    protected PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_box);

        // Globals
        this.globals = Globals.getInstance();
        this.db = new DBHelper(this);
        this.typeFace = globals.getTypeface(this);

        // Buttons
        this.btn_submit = (Button) findViewById(R.id.btn_submit);
        this.btn_cancel = (Button) findViewById(R.id.btn_cancel);
        this.btn_info = (ImageButton) findViewById(R.id.btn_info);
        this.btn_camera = (ImageButton) findViewById(R.id.btn_camera);
        this.btn_default_img = (Button) findViewById(R.id.btn_default_img);

        // Views
        this.chk_heavy = (CheckBox) findViewById(R.id.chkBox_heavy);
        this.chk_fragile = (CheckBox) findViewById(R.id.chkBox_fragile);
        this.et_id = (EditText) findViewById(R.id.et_id);
        this.et_rfid = (EditText) findViewById(R.id.et_rfid);
        this.sp_location = (Spinner) findViewById(R.id.sp_location);
        this.image_error = (ImageView) findViewById(R.id.image_error);
        this.img_box = (ImageView) findViewById(R.id.image_box);

        // NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        setupActionBar();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                setupActivity();
            }
        });
    }

    /**
     * Setup the Action Bar
     */
    protected void setupActionBar(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);

        // Change the title
        TextView titleView = (TextView) findViewById(R.id.action_bar_title);
        titleView.setTypeface(typeFace);
        titleView.setText(getResources().getString(R.string.create_box));
        titleView.setPadding(0, 35, 0, 0);

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

    // Set up the UI and background operations.
    private void setupActivity(){
        // Set the font for any text views
        setFont();

        // Initialize the buttons with a listener for click events
        btn_submit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ validateBox(); }
        });

        btn_rfid.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ displayInfo(); }
        });

        btn_info.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ displayInfo(); }
        });

        btn_back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ onBackPressed(); }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ onBackPressed(); }
        });

        btn_camera.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                int id = -1;
                String str_id = et_id.getText().toString().trim();
                if(!TextUtils.isEmpty(str_id)) id = Integer.parseInt(str_id);
                globals.box_id = id;
                globals.heavy = checkBoxSelected(chk_heavy);
                globals.fragile = checkBoxSelected(chk_fragile);
                globals.rfid = et_rfid.getText().toString().trim();
                globals.location = sp_location.getSelectedItem().toString();
                globals.previousActivity = Constants.ChangeActivity.CREATE_BOX;
                changeActivity(Constants.ChangeActivity.CAMERA);
            }
        });

        btn_default_img.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                img_box.setImageResource(R.drawable.box);
            }
        });

        // Restrict character length
        et_id.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        et_rfid.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
    }

    /**
     * Set Font: Apply a custom font to text.
     */
    protected void setFont(){
        TextView tv = (TextView) findViewById(R.id.tv_id);
        tv.setTypeface(typeFace);
        tv = (TextView) findViewById(R.id.tv_rfid);
        tv.setTypeface(typeFace);
        tv = (TextView) findViewById(R.id.tv_locations);
        tv.setTypeface(typeFace);

        btn_submit.setTypeface(typeFace);
        btn_default_img.setTypeface(typeFace);
        btn_cancel.setTypeface(typeFace);
        chk_heavy.setTypeface(typeFace);
        chk_fragile.setTypeface(typeFace);
        et_id.setTypeface(typeFace);
        et_rfid.setTypeface(typeFace);
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

        // Set the box image
        if(globals.box_image != null) img_box.setImageBitmap(globals.box_image);
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
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)){
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if(tag == null) Toast.makeText(this, "No Tag ID Found", Toast.LENGTH_SHORT);
            else{
                // Convert the Tag ID to a String
                String tagInfo = "";
                byte[] tagId = tag.getId();
                for(int i=0; i<tagId.length; i++)
                    tagInfo += "" + Integer.toHexString(tagId[i] & 0xFF);

                // Set the edit text view with the tag id
                et_rfid.setText(tagInfo);
            }
        }
    }

    /**
     * Validate Box
     */
    protected void validateBox(){
        int id = -1;
        String str_id = et_id.getText().toString().trim();

        // Make sure Box Id is not empty
        if(TextUtils.isEmpty(str_id)){
            et_id.setError("Id cannot be empty!");
            et_id.requestFocus();
            return;
        }
        else id = Integer.parseInt(str_id);

        // Check if the Id already exist
        String statement = "SELECT * FROM " + db.BOXES_TABLE_NAME + " WHERE " +
                db.BOXES_COLUMN_MOVE_ID + " = " + globals.move_id + " AND "
                + db.BOXES_COLUMN_BOX_ID + " = " + id;
        Cursor cursor = db.getQuery(statement);
        if(cursor.moveToFirst()){
            et_id.setError("Box ID already exist!");
            et_id.requestFocus();
            return;
        }

        // Make sure a room location is selected
        String location = sp_location.getSelectedItem().toString();
        if(location.equals(Constants.select_location)){
            image_error.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Please select a location.", Toast.LENGTH_SHORT).show();
            sp_location.requestFocus();
            return;
        }

        // Check if RFID already exist
        String rfid = et_rfid.getText().toString().trim();
        if(!TextUtils.isEmpty(rfid)){
            if(rfid != globals.rfid){
                statement = "SELECT * FROM " + db.BOXES_TABLE_NAME + " WHERE " +
                        db.BOXES_COLUMN_MOVE_ID + " = " + globals.move_id + " AND "
                        + db.BOXES_COLUMN_RFID + " = '" + rfid + "'";
                cursor = db.getQuery(statement);
                if(cursor.moveToFirst()){
                    et_rfid.setError("RFID already exist!");
                    et_rfid.requestFocus();
                    return;
                }
            }
        }
        else rfid = null;
        saveBox(id, rfid, location);
    }

    /**
     * Save Box
     * @param box_id
     * @param location
     */
    protected void saveBox(int box_id, String rfid, String location){
        // Check boxes
        int heavy = checkBoxSelected(chk_heavy);
        int fragile = checkBoxSelected(chk_fragile);

        // Image
        byte[] image = null;

        if(globals.box_image != null) image = bitmapToByte(globals.box_image);

        if(db.insertBox(globals.move_id, box_id, rfid, location, heavy, fragile, image)){
            globals.box_id = box_id;
            globals.rfid = rfid;
            globals.location = location;
            globals.heavy = heavy;
            globals.fragile = fragile;

            changeActivity(Constants.ChangeActivity.BOX);
        }
    }

    /**
     * Convert Bitmap to Byte[]
     * @param bitmap
     * @return
     */
    protected byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    /**
     * Check if a Check Box is Selected
     * @param checkBox
     * @return: 0-unchecked, 1-checked
     */
    protected int checkBoxSelected(CheckBox checkBox){
        if(checkBox.isChecked()) return 1;
        else return 0;
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
        Intent intent = null;

        if(nextActivity.equals(Constants.ChangeActivity.MOVE))
            intent = new Intent(this, MoveActivity.class);
        else if(nextActivity.equals(Constants.ChangeActivity.BOXES))
            intent = new Intent(this, BoxesActivity.class);
        else if(nextActivity.equals(Constants.ChangeActivity.BOX))
            intent = new Intent(this, BoxActivity.class);
        else if(nextActivity.equals(Constants.ChangeActivity.CAMERA))
            intent = new Intent(this, CameraActivity.class);
        else if(nextActivity.equals(Constants.ChangeActivity.MAIN))
            intent = new Intent(this, MainActivity.class);

        if(intent != null) startActivity(intent);
    }

    /**
     * On Back Press
     */
    @Override
    public void onBackPressed(){
        globals.box_id = -1;
        globals.heavy = -1;
        globals.fragile = -1;
        globals.rfid = null;
        globals.location = null;
        changeActivity(Constants.ChangeActivity.BOXES);
    }
}