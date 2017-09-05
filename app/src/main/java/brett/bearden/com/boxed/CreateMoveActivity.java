package brett.bearden.com.boxed;

import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.TextView;
import Database.DBHelper;
import Global.Globals;

public class CreateMoveActivity extends AppCompatActivity {

    // Globals
    protected Globals globals;
    protected DBHelper db;
    protected Typeface typeFace;

    // Buttons
    protected Button btn_submit;
    protected ImageButton btn_info;
    protected ImageButton btn_back;

    // Views
    protected EditText et_description;
    protected EditText et_starting_street;
    protected EditText et_starting_city_state_zip;
    protected EditText et_destination_street;
    protected EditText et_destination_city_state_zip;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_move);

        // Globals
        this.globals = Globals.getInstance();
        this.db = new DBHelper(this);
        this.typeFace = globals.getTypeface(this);

        // Buttons
        this.btn_submit = (Button) findViewById(R.id.btn_submit);
        this.btn_info = (ImageButton) findViewById(R.id.btn_info);

        // Views
        this.et_description = (EditText) findViewById(R.id.et_description);
        this.et_starting_street = (EditText) findViewById(R.id.et_starting_street);
        this.et_starting_city_state_zip = (EditText) findViewById(R.id.et_starting_city_state_zip);
        this.et_destination_street = (EditText) findViewById(R.id.et_destination_street);
        this.et_destination_city_state_zip = (EditText) findViewById(R.id.et_destination_city_state_zip);

        setupActionBar();
        setupActivity();
    }

    // Setup the Action Bar
    protected void setupActionBar(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);

        // Change the title
        Typeface face= Typeface.createFromAsset(getAssets(), "font/conformity.otf");
        TextView titleView = (TextView) findViewById(R.id.action_bar_title);
        titleView.setTypeface(face);
        titleView.setText(getResources().getString(R.string.create_move));
        titleView.setPadding(0, 35, 0, 0);

        // Show Back Button
        this.btn_back = (ImageButton) findViewById(R.id.btn_back);
        btn_back.setPadding(0, 10, 0, 0);
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
        btn_submit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ validateData(); }
        });

        btn_back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ onBackPressed(); }
        });

        btn_info.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ displayInfo(); }
        });

        et_destination_city_state_zip.setOnKeyListener(new View.OnKeyListener(){
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if (event.getAction() == KeyEvent.ACTION_DOWN){
                    switch (keyCode){
                        case KeyEvent.KEYCODE_ENTER:
                            validateData();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        // Restrict the character length
        et_description.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25)});
        et_starting_street.setFilters(new InputFilter[]{new InputFilter.LengthFilter(38)});
        et_starting_city_state_zip.setFilters(new InputFilter[]{new InputFilter.LengthFilter(38)});
        et_destination_street.setFilters(new InputFilter[]{new InputFilter.LengthFilter(38)});
        et_destination_city_state_zip.setFilters(new InputFilter[]{new InputFilter.LengthFilter(38)});
    }

    // Set Font: Apply a custom font to text.
    protected void setFont(){
        TextView tv = (TextView) findViewById(R.id.title_starting);
        tv.setTypeface(typeFace);
        tv = (TextView) findViewById(R.id.title_destination);
        tv.setTypeface(typeFace);

        et_description.setTypeface(typeFace);
        et_starting_street.setTypeface(typeFace);
        et_starting_city_state_zip.setTypeface(typeFace);
        et_destination_street.setTypeface(typeFace);
        et_destination_city_state_zip.setTypeface(typeFace);
        btn_submit.setTypeface(typeFace);
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onResume(){ super.onResume(); }

    // Validate Data
    protected void validateData(){
        String description = et_description.getText().toString().trim();

        // Make sure Description is not empty
        if(TextUtils.isEmpty(description)){
            et_description.setError("Description cannot be empty!");
            et_description.requestFocus();
            return;
        }

        // Check if a Move with the same Description already exist
        String statement = "SELECT * FROM MOVES WHERE " + db.MOVES_COLUMN_DESCRIPTION +
                " = '" + description + "'";
        Cursor cursor = db.getQuery(statement);
        if(cursor.moveToFirst()){
            et_description.setError("A move with this description already exist!");
            et_description.requestFocus();
            return;
        }
        else{
            if(insertData(description)) changeActivity();
        }
    }

    // Insert the data into the SQLite Database
    protected boolean insertData(String description){
        // Insert the move
        if(db.insertMove(description)){
            // Query the move for the Move_ID
            String statement = "SELECT * FROM MOVES WHERE " + db.MOVES_COLUMN_DESCRIPTION +
                    " = '" + description + "'";
            Cursor cursor = db.getQuery(statement);
            int move_id;

            if(cursor.moveToFirst()){
                move_id = cursor.getInt(cursor.getColumnIndexOrThrow(db.MOVES_COLUMN_MOVE_ID));

                // Insert the Starting Address
                String starting_street = et_starting_street.getText().toString().trim();
                String starting_city_state_zip = et_starting_city_state_zip.getText().toString().trim();

                if(db.insertAddress(move_id, 1, starting_street, starting_city_state_zip)){
                    // Insert the Destination Address
                    String destination_street = et_destination_street.getText().toString().trim();
                    String destination_city_state_zip = et_destination_city_state_zip.getText().toString().trim();

                    if(db.insertAddress(move_id, 2, destination_street, destination_city_state_zip)){
                        // Set global data
                        globals.move_id = move_id;
                        globals.move_description = description;
                        globals.starting_street = starting_street;
                        globals.starting_city_state_zip = starting_city_state_zip;
                        globals.destination_street = destination_street;
                        globals.destination_city_state_zip = destination_city_state_zip;
                        return true;
                    }
                    else return false;  // Insert Destination Address Failed
                }
                else return false; // Insert Starting Address Failed
            }
            else return false; // Insert Move Failed
        }

        // Insert Move Failed
        return false;
    }

    // Display a dialog with the Info about this Activity
    public void displayInfo(){
        Intent intent = new Intent(this, DisplayCreateMoveInfoActivity.class);
        startActivity(intent);
    }

    // Change to the next Activity
    protected void changeActivity(){
        Intent intent = new Intent(this, MoveActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}