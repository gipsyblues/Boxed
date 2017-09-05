package brett.bearden.com.boxed;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.util.List;
import Database.DBHelper;
import Global.Constants;
import Global.Constants.ChangeActivity;
import Global.Globals;

public class CameraActivity extends AppCompatActivity {

    // Globals
    protected Globals globals;
    protected DBHelper db;
    protected Typeface typeFace;

    // Buttons
    protected ImageButton btn_back;
    protected ImageButton btn_flash;

    // Camera
    private Camera mCamera;
    protected CameraPreview mPreview;
    protected boolean flash_on;
    protected Camera.Parameters params;
    protected List<String> flashModes;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Optional: Hide the status bar at the top of the window
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);

        // Globals Instance
        this.globals = Globals.getInstance();
        this.db = new DBHelper(this);
        this.typeFace = globals.getTypeface(this);

        this.flash_on = false;

        setActionBar();
        setActivity();
    }

    // Set the Action Bar
    protected void setActionBar(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);

        // Change the title
        TextView titleView = (TextView) findViewById(R.id.action_bar_title);
        titleView.setTypeface(typeFace);
        titleView.setText("Camera Preview");

        // Show the Back Button
        this.btn_back = (ImageButton) findViewById(R.id.btn_back);
        btn_back.setVisibility(View.VISIBLE);

        // Show the flash Button
        this.btn_flash = (ImageButton) findViewById(R.id.btn_flash);
        btn_flash.setVisibility(View.VISIBLE);

        // Set the background image
        BitmapDrawable background = new BitmapDrawable (BitmapFactory.decodeResource(getResources(), R.drawable.tape));
        getSupportActionBar().setBackgroundDrawable(background);
    }

    // Handle Picture Callback
    protected Camera.PictureCallback mPicture = new Camera.PictureCallback(){
        @Override
        public void onPictureTaken(byte[] data, Camera camera){
            releaseCamera();

            // Prepare the image
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            // Rotate the image to portrait view
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap = resizeAndCropImage(bitmap, 200, 200);

            // Determine which activity the picture is for and update the database
            if(globals.previousActivity.equals(ChangeActivity.BOX)){
                String item = globals.item.replaceAll("'", "''");
                db.updateBoxItem(globals.move_id, globals.box_id, item, bitmapToByte(bitmap));
                globals.item = null;
            }
            else if(globals.previousActivity.equals(ChangeActivity.CREATE_BOX))
                globals.box_image = bitmap;
            else if(globals.previousActivity.equals(ChangeActivity.EDIT_BOX))
                globals.box_image = bitmap;

            changeActivity(globals.previousActivity);
        }
    };

    // Set Activity
    protected void setActivity(){
        // Add a listener to the Capture button
        btn_back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ onBackPressed(); }
        });

        btn_flash.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ setFlash(); }
        });

        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        // Auto focus then capture image
                        mCamera.autoFocus(new Camera.AutoFocusCallback(){
                            public void onAutoFocus(boolean success, Camera camera){
                                mCamera.takePicture(null, null, mPicture);
                            }
                        });
                    }
                }
        );
    }

    // On Start
    @Override
    public void onStart(){
        super.onStart();
    }

    // On Pause
    @Override
    protected void onResume(){
        super.onResume();

        new Handler().post(new Runnable(){
            @Override
            public void run() {
                // Create an instance of Camera
                mCamera = getCameraInstance();

                // Create our Preview view and set it as the content of our activity.
                if(mCamera != null){
                    params = mCamera.getParameters();
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    params.setPictureFormat(ImageFormat.JPEG);
                    params.setJpegQuality(100);
                    flashModes = params.getSupportedFlashModes();
                    mCamera.setParameters(params);
                    mPreview = new CameraPreview(getApplicationContext(), mCamera);
                }

                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                mCamera.setDisplayOrientation(90);
                preview.addView(mPreview);
            }
        });
    }

    // On Pause
    @Override
    protected void onPause() {
        super.onPause();

        // release the camera immediately on pause event
        releaseCamera();
    }

    // Set the Camera Flash
    protected void setFlash(){
        if(flash_on){
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)){
                params = mCamera.getParameters();
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                params.setPictureFormat(ImageFormat.JPEG);
                params.setJpegQuality(100);
                flashModes = params.getSupportedFlashModes();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(params);
                btn_flash.setBackgroundResource(R.drawable.flash_off);
                flash_on = false;
            }
        }
        else{
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                params.setPictureFormat(ImageFormat.JPEG);
                params.setJpegQuality(100);
                flashModes = params.getSupportedFlashModes();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                mCamera.setParameters(params);
                btn_flash.setBackgroundResource(R.drawable.flash_on);
                flash_on = true;
            }
        }
    }

    /**
     * Resize and Crop an Bitmap then return new Bitmap
     * @param original_image
     * @param width
     * @param height
     * @return
     */
    protected Bitmap resizeAndCropImage(Bitmap original_image, int width, int height){
        Bitmap new_image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        float originalWidth = original_image.getWidth();
        float originalHeight = original_image.getHeight();

        Canvas canvas = new Canvas(new_image);

        float scale = width / originalWidth;

        float xTranslation = 0.0f;
        float yTranslation = (height - originalHeight * scale) / 2.0f;

        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);

        Paint paint = new Paint();
        paint.setFilterBitmap(true);

        canvas.drawBitmap(original_image, transformation, paint);

        return new_image;
    }

    /**
     * Convert Bitmap to Byte[]
     * @param bitmap
     * @return
     */
    protected byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    // Release Camera for other applications
    private void releaseCamera(){
        if(mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }

    // Get Camera Instance
    public Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch(Exception e){
            Toast.makeText(getApplicationContext(), "Camera not available!", Toast.LENGTH_SHORT);
        }
        return c; // returns null if camera is unavailable
    }

    // Change to the next activity
    protected void changeActivity(ChangeActivity nextActivity){
        Intent intent = null;

        if(nextActivity.equals(ChangeActivity.BOX))
            intent = new Intent(this, BoxActivity.class);
        else if(nextActivity.equals(ChangeActivity.BOXES))
            intent = new Intent(this, BoxesActivity.class);
        else if(nextActivity.equals(ChangeActivity.CREATE_BOX))
            intent = new Intent(this, CreateBoxActivity.class);
        else if(nextActivity.equals(Constants.ChangeActivity.EDIT_BOX))
            intent = new Intent(this, EditBoxActivity.class);

        if(intent != null){
            globals.previousActivity = null;
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed(){
        releaseCamera();
        changeActivity(ChangeActivity.BOXES);
    }
}