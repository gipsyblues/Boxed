package brett.bearden.com.boxed;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import Global.Constants.ChangeActivity;
import Database.DBHelper;
import Global.Globals;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.io.IOException;
import java.util.List;

public class MoveActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Globals
    protected Globals globals;
    protected DBHelper db;
    protected Typeface typeFace;

    // Buttons
    protected Button btn_edit_move;
    protected Button btn_show_boxes;
    protected ImageButton btn_back;
    protected ImageButton btn_rfid;

    // Views
    protected TextView tv_description;
    protected TextView tv_starting_street;
    protected TextView tv_starting_city_state_zip;
    protected TextView tv_destination_street;
    protected TextView tv_destination_city_state_zip;
    protected ProgressBar progress_circle;

    // Google Maps
    protected GoogleMap mMap;
    protected SupportMapFragment mapFragment;

    // NFC for RFID Tags
    protected NfcAdapter nfcAdapter;
    protected PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move);

        // Globals
        this.globals = Globals.getInstance();
        this.db = new DBHelper(this);
        this.typeFace = globals.getTypeface(this);

        // Buttons
        this.btn_edit_move = (Button) findViewById(R.id.btn_edit_move);
        this.btn_show_boxes = (Button) findViewById(R.id.btn_show_boxes);

        // Views
        this.tv_description = (TextView) findViewById(R.id.et_description);
        this.tv_starting_street = (TextView) findViewById(R.id.et_starting_street);
        this.tv_starting_city_state_zip = (TextView) findViewById(R.id.et_starting_city_state_zip);
        this.tv_destination_street = (TextView) findViewById(R.id.et_destination_street);
        this.tv_destination_city_state_zip = (TextView) findViewById(R.id.et_destination_city_state_zip);
        this.progress_circle = (ProgressBar) findViewById(R.id.progress_circle);

        // Google maps
        this.mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        setupActionBar();
        setupActivity();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMap = googleMap;
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

                LatLng loc_starting = null;
                LatLng loc_destination = null;

                // Add Starting Address Marker
                String address = globals.starting_street + " " + globals.starting_city_state_zip;
                address = address.trim();
                if(!TextUtils.isEmpty(address)){
                    loc_starting = getLocationFromAddress(getApplicationContext(), address);
                    mMap.addMarker(new MarkerOptions().position(loc_starting).title("Starting Address"));
                }

                // Add Destination Address Marker
                address = globals.destination_street + " " + globals.destination_city_state_zip;
                address = address.trim();
                if(!TextUtils.isEmpty(address)){
                    loc_destination = getLocationFromAddress(getApplicationContext(), address);
                    mMap.addMarker(new MarkerOptions().position(loc_destination).title("Starting Address"));
                }

                // Draw line between markers
                if(loc_starting != null && loc_destination != null){
                    PolylineOptions line= new PolylineOptions().add(
                            new LatLng(loc_starting.latitude, loc_starting.longitude),
                            new LatLng(loc_destination.latitude, loc_destination.longitude))
                            .width(5).color(Color.RED);
                    mMap.addPolyline(line);

                    // Center the map between the two markers
                    final LatLng final_starting = loc_starting;
                    final LatLng final_destination = loc_destination;
                    final Handler handler_coords = new Handler();
                    handler_coords.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            centerMap(final_starting, final_destination);
                        }
                    }, 300);
                }
                else{
                    if(loc_starting != null){
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc_starting));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
                    }
                    else if(loc_destination != null){
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc_destination));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
                    }
                }


            }
        }, 100);
    }

    // Get Location From Address
    protected LatLng getLocationFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try{
            address = coder.getFromLocationName(strAddress, 5);
            if(address == null) return null;

            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );
        }catch(IOException ex){ ex.printStackTrace(); }
        return p1;
    }

    // Center the map between markers
    protected void centerMap(LatLng loc_starting, LatLng loc_destination){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(loc_starting);
        builder.include(loc_destination);
        LatLngBounds bounds = builder.build();

        int padding = 75; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }

    // Setup the Action Bar
    protected void setupActionBar(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);

        // Change the title
        TextView titleView = (TextView) findViewById(R.id.action_bar_title);
        titleView.setTypeface(typeFace);
        titleView.setText(globals.move_description);

        if(globals.move_description.toString().length() > 15)
            titleView.setTextSize(30);

        // Show the RFID Icon and Box Icon
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
    protected void setupActivity(){
        // Set the font for any text views
        setFont();

        // Initialize the buttons with a listener for click events
        btn_edit_move.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){changeActivity(ChangeActivity.EDIT_MOVE);
            }
        });

        btn_show_boxes.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ changeActivity(ChangeActivity.BOXES); }
        });

        btn_rfid.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ displayInfo(); }
        });

        btn_back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ onBackPressed(); }
        });

        // Set fields
        tv_description.setText(globals.move_description);
    }

    // Set Font: Apply a custom font to text.
    protected void setFont(){
        TextView tv = (TextView) findViewById(R.id.title_starting);
        tv.setTypeface(typeFace);
        tv = (TextView) findViewById(R.id.title_destination);
        tv.setTypeface(typeFace);

        tv_description.setTypeface(typeFace);
        tv_starting_street.setTypeface(typeFace);
        tv_starting_city_state_zip.setTypeface(typeFace);
        tv_destination_street.setTypeface(typeFace);
        tv_destination_city_state_zip.setTypeface(typeFace);
        btn_edit_move.setTypeface(typeFace);
        btn_show_boxes.setTypeface(typeFace);
    }

    @Override
    public void onStart(){ super.onStart(); }

    @Override
    public void onResume(){
        super.onResume();

        // Resume NFC
        if(globals.hasNFC) nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);

        // Set the locations
        new Handler().post(new Runnable() {
            @Override
            public void run() {queryLocation(1);
                queryLocation(2);
                setLocations();
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
                        changeActivity(ChangeActivity.BOX);
                    }
                    else Toast.makeText(getApplicationContext(), "No Box Found!", Toast.LENGTH_LONG);
                }
            }
        }
    }

    /**
     * Query a location address.
     * Location = 1 or 2.
     * 1) Starting Address
     * 2) Destination Address
     */
    protected void queryLocation(int location){
        Cursor cursor = db.getQuery("SELECT * FROM " + db.ADDRESS_TABLE_NAME + " WHERE " +
        db.ADDRESS_COLUMN_MOVE_ID + " = " + globals.move_id + " AND " + db.ADDRESS_COLUMN_LOCATION
        + " = " + location);

        String street;
        String city_street_zip;
        if(cursor.moveToFirst()){
            street = cursor.getString(cursor.getColumnIndex(db.ADDRESS_COLUMN_STREET));
            city_street_zip = cursor.getString(cursor.getColumnIndex(db.ADDRESS_COLUMN_CITY_STATE_ZIP));

            if(street != null){
                if(location == 1)
                    globals.starting_street = street;
                else
                    globals.destination_street = street;
            }

            if(city_street_zip != null){
                if(location == 1)
                    globals.starting_city_state_zip = city_street_zip;
                else
                    globals.destination_city_state_zip = city_street_zip;
            }
        }
    }

    // Set Locations
    protected void setLocations(){
        tv_starting_street.setText(globals.starting_street);
        tv_starting_city_state_zip.setText(globals.starting_city_state_zip);
        tv_destination_street.setText(globals.destination_street);
        tv_destination_city_state_zip.setText(globals.destination_city_state_zip);
    }

    // Display a dialog with the Info about RFID
    protected void displayInfo(){
        Intent intent = new Intent(this, DisplayRFIDInfoActivity.class);
        startActivity(intent);
    }

    // Change to the next activity
    protected void changeActivity(ChangeActivity nextActivity){
        progress_circle.setVisibility(View.VISIBLE);

        Intent intent = null;

        if(nextActivity.equals(ChangeActivity.MAIN))
            intent = new Intent(this, MainActivity.class);
        if(nextActivity.equals(ChangeActivity.EDIT_MOVE))
            intent = new Intent(this, EditMoveActivity.class);
        else if(nextActivity.equals(ChangeActivity.BOXES))
            intent = new Intent(this, BoxesActivity.class);
        else if(nextActivity.equals(ChangeActivity.BOX))
            intent = new Intent(this, BoxActivity.class);

        if(intent != null) startActivity(intent);
    }

    @Override
    public void onBackPressed(){
        globals.clearData();
        changeActivity(ChangeActivity.MAIN);
    }
}