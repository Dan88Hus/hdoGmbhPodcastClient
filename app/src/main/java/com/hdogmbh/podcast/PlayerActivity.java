package com.hdogmbh.podcast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class PlayerActivity extends AppCompatActivity {

    private Button buttonPlayPause;
    private TextView textViewFileName;
    // sharedPreferences
    SharedPreferences sharedPreferences;
    // recordedVoiceAbsPath as String
    String recordedVoiceAbsPath;
    String recordName;
    Integer voiceRecordId;
    //float rating number
    Float rateFloat;
    // MediaPlayer
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        buttonPlayPause = findViewById(R.id.buttonPlayPause);
        textViewFileName = findViewById(R.id.textViewFileName);
        // retrieve playerData Preferences from saved PlayerAdapter asyncTask
        retrievePlayerDataPreferences();
        // System.out.println("recordedVoiceAbsPath PlayerActivity: "+recordedVoiceAbsPath);


        textViewFileName.setText(recordName);
        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(recordedVoiceAbsPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (Exception e){
            e.printStackTrace();
        }

        // mediaPlayer completion Listener
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                System.out.println("I am completed ");
                // we used fragment to rate
                FragmentManager fragmentManager = getSupportFragmentManager();
                RatingFragment ratingFragment = new RatingFragment();

                // to send data from activity to fragment we used bundle method
                Bundle bundle = new Bundle();
                bundle.putInt("voiceRecordId",voiceRecordId);
                ratingFragment.setArguments(bundle);

                ratingFragment.show(fragmentManager,"RatingFragment");

            }
        });

        buttonPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    buttonPlayPause.setBackgroundResource(R.drawable.player_start);
                }else{
                    mediaPlayer.start();
                    buttonPlayPause.setBackgroundResource(R.drawable.player_pause);
                }
            }
        });


    }

    public void retrievePlayerDataPreferences(){
        // from ReaderActivity
        sharedPreferences = getSharedPreferences("playerData", MODE_PRIVATE);
        recordedVoiceAbsPath = sharedPreferences.getString("recordedVoiceAbsPath","no_file_path");
        recordName = sharedPreferences.getString("recordName","no_file_name");
        voiceRecordId = sharedPreferences.getInt("voiceRecordId",0);
    }

    @Override
    protected void onDestroy() {

        mediaPlayer.release();
        File fileDelete = new File(recordedVoiceAbsPath);
        if (fileDelete.exists()) {
            try {
                fileDelete.delete();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}