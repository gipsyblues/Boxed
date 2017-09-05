package brett.bearden.com.boxed;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class DisplayRFIDInfoActivity extends Activity {

    // NFC Adapter
    protected NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_rfid_info);

        // Initialize the button to perform device discovery
        Button btn_close = findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
            }
        });

        // NFC Adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // Check if NFC is supported or enabled
        if(nfcAdapter == null)
            Toast.makeText(this, "NFC NOT supported on this devices!", Toast.LENGTH_LONG).show();
        else if(!nfcAdapter.isEnabled())
            Toast.makeText(this, "NFC NOT Enabled!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}