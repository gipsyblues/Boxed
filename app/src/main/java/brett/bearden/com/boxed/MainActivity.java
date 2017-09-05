package brett.bearden.com.boxed;

import android.app.ActionBar;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v7.app.ActionBar.LayoutParams;
import android.widget.Toast;
import Database.DBHelper;
import Global.Constants.ChangeActivity;
import Global.Globals;

public class MainActivity extends AppCompatActivity {

    // Globals
    protected Globals globals;
    protected DBHelper db;
    protected Typeface typeFace;

    // Buttons
    protected Button btn_create_move;
    protected ImageButton btn_rfid;

    // Views
    protected LinearLayout listLayout;
    protected int list_count;
    protected TextView tv_no_moves;
    protected ProgressBar progress_circle;

    // NFC for RFID Tags
    protected NfcAdapter nfcAdapter;
    protected PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Globals
        this.globals = Globals.getInstance();
        this.db = new DBHelper(this);
        this.typeFace = globals.getTypeface(this);

        // Buttons
        this.btn_create_move = (Button) findViewById(R.id.btn_create_move);

        // Views
        this.listLayout = (LinearLayout) findViewById(R.id.layout_list);
        this.tv_no_moves = (TextView) findViewById(R.id.tv_no_moves);
        this.progress_circle = (ProgressBar) findViewById(R.id.progress_circle);

        setupActionBar();
        setupActivity();

        // Check if this device has a camera
        if(!globals.hasCamera) globals.hasCamera = checkCameraHardware();

        // NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Check if the device has NFC
        if(nfcAdapter == null) globals.hasNFC = false;
        else globals.hasNFC = true;
    }

    // Setup the Action Bar
    protected void setupActionBar(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);

        // Change the title
        TextView titleView = (TextView) findViewById(R.id.action_bar_title);
        titleView.setTypeface(typeFace);
        titleView.setText(getResources().getString(R.string.app_name));
        titleView.setPadding(0, 35, 0, 0);

        // Show the RFID Icon
        this.btn_rfid = (ImageButton) findViewById(R.id.btn_rfid);
        btn_rfid.setVisibility(View.VISIBLE);

        // Show the Box Icon
        ImageView box_icon = (ImageView) findViewById(R.id.box_icon);
        box_icon.setPadding(0, 10, 0, 0);
        box_icon.setVisibility(View.VISIBLE);

        // Set the background image
        BitmapDrawable background = new BitmapDrawable (BitmapFactory.decodeResource(getResources(), R.drawable.tape));
        getSupportActionBar().setBackgroundDrawable(background);
    }

    // Set up the UI and background operations.
    private void setupActivity(){
        // Set the font for any text views
        setFont();

        // Initialize the buttons with a listener for click events
        btn_create_move.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ changeActivity(ChangeActivity.CREATE_MOVE); }
        });

        btn_rfid.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ displayInfo(); }
        });
    }

    // Set Font: Apply a custom font to text.
    protected void setFont(){
        TextView tv = (TextView) findViewById(R.id.tv_title);

        tv.setTypeface(typeFace);
        btn_create_move.setTypeface(typeFace);
        tv_no_moves.setTypeface(typeFace);
    }

    @Override
    public void onStart(){ super.onStart(); }

    @Override
    public void onResume(){
        super.onResume();

        // Resume NFC
        if(globals.hasNFC) nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);

        // Clear the list view
        list_count = 0;
        listLayout.removeAllViews();
        tv_no_moves.setVisibility(View.INVISIBLE);

        // Query any saved moves
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                getMoves();
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();

        // Release NFC
        if(globals.hasNFC) nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onBackPressed(){ closeApplication(); }

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
                        changeActivity(ChangeActivity.BOX);
                    }
                    else Toast.makeText(getApplicationContext(), "No Box Found!", Toast.LENGTH_LONG);
                }
            }
        }
    }

    /**
     * Check if this device has a camera.
     * @return
     */
    private boolean checkCameraHardware(){
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    // Query all Moves
    protected void getMoves(){
        Cursor cursor = db.getQuery("SELECT * FROM MOVES");

        // Build the list of moves or show a message
        if(cursor.moveToFirst()){
            list_count = 1;
            buildMovesList(cursor);
        }
        else tv_no_moves.setVisibility(View.VISIBLE);
    }

    /**
     * Build Moves List adds a title to the scroll list view recursively.
     * Each title can be selected and will navigate to the Move Activity.
     */
    protected void buildMovesList(Cursor cursor){
        final int id = cursor.getInt(cursor.getColumnIndexOrThrow(db.MOVES_COLUMN_MOVE_ID));
        final String description = cursor.getString(cursor.getColumnIndexOrThrow(db.MOVES_COLUMN_DESCRIPTION));

        TextView textView = new TextView(this);
        textView.setText(description);
        textView.setTypeface(typeFace);
        textView.setTextSize(30);
        textView.setTextColor(Color.BLACK);
        textView.setMaxLines(1);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        // Set an On Click Listener
        textView.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                globals.move_id = id;
                globals.move_description = description;
                changeActivity(ChangeActivity.MOVE);
            }
        });

        // Add padding to all moves except the first move.
        if(list_count > 1)
            textView.setPadding(0, 25, 0, 0);

        // Add the new Move to the list view
        listLayout.addView(textView);

        // If there is another move then recursively add it.
        if(cursor.moveToNext()){
            list_count++;
            buildMovesList(cursor);
        }
    }

    // Display a dialog with the Info about this Activity
    public void displayInfo(){
        Intent intent = new Intent(this, DisplayRFIDInfoActivity.class);
        startActivity(intent);
    }

    // Change to the next activity
    protected void changeActivity(ChangeActivity nextActivity){
        progress_circle.setVisibility(View.VISIBLE);

        Intent intent = null;

        if(nextActivity.equals(ChangeActivity.MOVE))
            intent = new Intent(this, MoveActivity.class);
        else if(nextActivity.equals(ChangeActivity.CREATE_MOVE))
             intent = new Intent(this, CreateMoveActivity.class);
        else if(nextActivity.equals(ChangeActivity.CREATE_BOX))
            intent = new Intent(this, CreateMoveActivity.class);
        else if(nextActivity.equals(ChangeActivity.BOX))
            intent = new Intent(this, BoxActivity.class);

        if(intent != null) startActivity(intent);
    }

    // Close Application
    protected void closeApplication(){
        finish();
        moveTaskToBack(true);

        // Delay killing the thread to prevent device error
        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run(){
                android.os.Process.killProcess(android.os.Process.myPid()); }
        }, 2);
    }
}