package brett.bearden.com.boxed;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import Global.Globals;

public class SplashActivity extends AppCompatActivity {

    // Globals
    protected Globals globals;
    protected Typeface typeFace;

    // Views
    protected TextView tv_title;
    protected ImageView icon;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Globals
        this.globals = Globals.getInstance();
        this.typeFace = globals.getTypeface(this);

        // Views
        this.tv_title = (TextView) findViewById(R.id.tv_title);
        this.icon = (ImageView) findViewById(R.id.icon);

        tv_title.setTypeface(typeFace);
        setupActionBar();
    }

    // Setup the Action Bar
    protected void setupActionBar(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);

        // Set the background image
        BitmapDrawable background = new BitmapDrawable (BitmapFactory.decodeResource(getResources(), R.drawable.tape));
        getSupportActionBar().setBackgroundDrawable(background);
    }

    @Override
    public void onStart(){
        super.onStart();

        final Animation fadeInTitle = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_animation);
        final Animation fadeInIcon = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_animation);

        fadeInIcon.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation){}
            @Override
            public void onAnimationRepeat(Animation animation){}
            @Override
            public void onAnimationEnd(Animation animation){ fadeOut(); }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tv_title.startAnimation(fadeInTitle);
                tv_title.setVisibility(View.VISIBLE);
                icon.startAnimation(fadeInIcon);
                icon.setVisibility(View.VISIBLE);
            }
        }, 1500);
    }

    // Fade Out
    protected void fadeOut(){
        final Animation fadeOutTitle = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out_animation);
        final Animation fadeOutIcon = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out_animation);

        fadeOutIcon.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation){}
            @Override
            public void onAnimationRepeat(Animation animation){}
            @Override
            public void onAnimationEnd(Animation animation){
                tv_title.setVisibility(View.INVISIBLE);
                icon.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run(){
                tv_title.startAnimation(fadeOutTitle);
                icon.startAnimation(fadeOutIcon);
            }
        }, 3000);
    }

    @Override
    public void onResume(){
        super.onResume();

    }

    @Override
    public void onPause(){ super.onPause(); }

    @Override
    public void onBackPressed(){ }
}