package com.hdogmbh.podcast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashScreen extends AppCompatActivity {

    ImageView imageViewSplash;
//    TextView textViewSplash;
    MediaPlayer mp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        imageViewSplash = findViewById(R.id.imageViewSplash);
//        textViewSplash = findViewById(R.id.textViewSplash);

        // Assigning xlm file
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.splash_anim);
//        textViewSplash.startAnimation(animation);

        /*
        @Deprecated
        // will run the entrance_midi
        mp = MediaPlayer.create(this,R.raw.neysesi);
        Thread timerThread =  new Thread(){
            public  void run(){
                try{
                    mp.start();
                    sleep(3000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        };
        timerThread.start();
         */

        // Handler will start main activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // will intent to start Main Activity
                Intent i = new Intent(SplashScreen.this,MainActivity.class);
                startActivity(i);
                finish();
            }
        }, 3000);



    }

    @Override
    protected void onPause() {
        super.onPause();

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /* @Deprecated
        mp.stop();
         */
    }
}