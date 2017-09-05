package brett.bearden.com.boxed;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import Database.DBHelper;
import Global.Constants.ChangeActivity;
import Global.Globals;

public class EditMoveActivity extends AppCompatActivity {

    // Globals
    protected Globals globals;
    protected DBHelper db;
    protected Typeface typeFace;

    // Buttons
    protected Button btn_submit;
    protected Button btn_cancel;
    protected Button btn_delete;
    protected ImageButton btn_info;
    protected ImageButton btn_back;

    // View
    protected EditText et_description;
    protected EditText et_starting_street;
    protected EditText et_starting_city_state_zip;
    protected EditText et_destination_street;
    protected EditText et_destination_city_state_zip;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_move);

        // Globals
        this.globals = Globals.getInstance();
        this.db = new DBHelper(this);
        this.typeFace = globals.getTypeface(this);

        // Buttons
        this.btn_submit = (Button) findViewById(R.id.btn_submit);
        this.btn_cancel = (Button) findViewById(R.id.btn_cancel);
        this.btn_delete = (Button) findViewById(R.id.btn_delete);
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

    /**
     * Setup the Action Bar
     */
    protected void setupActionBar(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);

        // Change the title
        Typeface face= Typeface.createFromAsset(getAssets(), "font/conformity.otf");
        TextView titleView = (TextView) findViewById(R.id.action_bar_title);
        titleView.setTypeface(face);
        titleView.setText(getResources().getString(R.string.edit_move));

        // Show the RFID Icon and Box Icon
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
        btn_submit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ validateData(); }
        });

        btn_back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ onBackPressed(); }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ onBackPressed(); }
        });

        btn_delete.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ warnDeleteMove(); }
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

        // Set fields
        et_description.setText(globals.move_description);
        et_starting_street.setText(globals.starting_street);
        et_starting_city_state_zip.setText(globals.starting_city_state_zip);
        et_destination_street.setText(globals.destination_street);
        et_destination_city_state_zip.setText(globals.destination_city_state_zip);
    }

    /**
     * Set Font: Apply a custom font to text.
     */
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
        btn_cancel.setTypeface(typeFace);
        btn_delete.setTypeface(typeFace);
    }

    @Override
    public void onStart(){ super.onStart(); }

    @Override
    public void onResume(){ super.onResume(); }

    /**
     * Validate Data to ensure fields are not empty.
     */
    protected void validateData(){
        String description = et_description.getText().toString().trim();

        // Make sure Description is not empty
        if(TextUtils.isEmpty(description)){
            et_description.setError("Description cannot be empty!");
            et_description.requestFocus();
            return;
        }

        // Check if a Move with the same Description already exist
        if(!description.equals(globals.move_description)){
            String statement = "SELECT * FROM MOVES WHERE " + db.MOVES_COLUMN_DESCRIPTION +
                    " = '" + description + "'";
            Cursor cursor = db.getQuery(statement);
            if(cursor.moveToFirst()){
                et_description.setError("A move with this description already exist!");
                et_description.requestFocus();
                return;
            }
        }

        // Update Move
        if(updateData(description)) changeActivity(ChangeActivity.MOVE);
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error!");
            builder.setMessage("There was an issue updating the move. Please try again.");
            builder.setPositiveButton("Close", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    /**
     * Insert the data into the SQLite Database
     */
    protected boolean updateData(String description){
        int move_id = globals.move_id;

        // Update the move
        if(db.updateMove(move_id, description)){
            globals.move_description = description;

            // Update the Starting Address
            String starting_street = et_starting_street.getText().toString().trim();
            String starting_city_state_zip = et_starting_city_state_zip.getText().toString().trim();

            if(db.updateAddress(move_id, 1, starting_street, starting_city_state_zip)){
                globals.starting_street = starting_street;
                globals.starting_city_state_zip = starting_city_state_zip;

                // Update the Destination Address
                String destination_street = et_destination_street.getText().toString().trim();
                String destination_city_state_zip = et_destination_city_state_zip.getText().toString().trim();

                if(db.updateAddress(move_id, 2, destination_street, destination_city_state_zip)){
                    globals.destination_street = destination_street;
                    globals.destination_city_state_zip = destination_city_state_zip;
                    return true;
                }
                else return false;  // Update Destination Address Failed
            }
            else return false; // Update Starting Address Failed
        }
        else return false; // Update Move Failed
    }

    // Confirm Delete Move.
    protected void warnDeleteMove(){
        String message = "Deleting move '" + globals.move_description + "' cannot be undone. " +
                "All data will be lost including box information. Press 'Yes' to continue with " +
                "the delete.";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning!");
        builder.setMessage(message);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                confirmDeleteMove();
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

    protected void confirmDeleteMove(){
        String message = "Are you sure you want to delete move '" + globals.move_description + "'?";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete!");
        builder.setMessage(message);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                deleteMove();
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
     * Delete Move will show an alert before
     * performing a cascade delete from all tables.
     */
    protected void deleteMove(){
        db.deleteMove(globals.move_id);
        globals.clearData();
        changeActivity(ChangeActivity.MAIN);
    }

    // Display a dialog with the Info about this Activity
    public void displayInfo(){
        Intent intent = new Intent(this, DisplayCreateMoveInfoActivity.class);
        startActivity(intent);
    }

    /**
     * Change to the next Activity
     */
    protected void changeActivity(ChangeActivity changeActivity){
        Intent intent = null;

        if(changeActivity.equals(ChangeActivity.MOVE))
             intent = new Intent(this, MoveActivity.class);
        else if(changeActivity.equals(ChangeActivity.MAIN))
            intent = new Intent(this, MainActivity.class);

        if(intent != null) startActivity(intent);
    }

    /**
     * On Back Press
     */
    @Override
    public void onBackPressed(){
        changeActivity(ChangeActivity.MOVE);
    }
}