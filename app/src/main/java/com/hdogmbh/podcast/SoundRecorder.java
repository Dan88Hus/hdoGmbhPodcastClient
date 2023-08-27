package com.hdogmbh.podcast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;


import java.io.File;


import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SoundRecorder extends AppCompatActivity {
    private static int MICROPHONE_PERMISSION_CODE = 200;
    private static int READ_STORAGE_PERMISSION_CODE = 200;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    // sharedPreferences
    SharedPreferences sharedPreferences;
    private Integer orderId;
    private Integer readerId;
    private Integer unitRecordNo;
    // Retrofit
    private ServerApi serverApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_recorder);

        Button soundRecord = findViewById(R.id.btnRecord);
        Button soundStop = findViewById(R.id.btnStop);
        soundStop.setEnabled(false);
        Button soundPlay = findViewById(R.id.btnPlay);
        soundPlay.setEnabled(false);
        Button soundUpload = findViewById(R.id.btnUpload);
        soundUpload.setEnabled(false);

        //permissions check
        if (isMicrophonePresent()){
            getMicrophonePermission();
        }
        getReadPermission();

        //buttons functions
        soundRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    soundRecord.setEnabled(false);
                    soundPlay.setEnabled(false);
                    soundStop.setEnabled(true);
                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mediaRecorder.setOutputFile(getRecordingFilePath()); // we need to give path to storing file
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                    Toast.makeText(SoundRecorder.this, R.string.sound_Record_Started, Toast.LENGTH_SHORT).show();
                    //add here if its recording stay android awake ********************************************************
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("Recording Error", e.getMessage());
                }

            }
        });

        soundStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPlay.setEnabled(true);
                soundRecord.setEnabled(true);
                soundUpload.setEnabled(true);
                soundStop.setEnabled(false);
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                Toast.makeText(SoundRecorder.this,R.string.sound_Record_Stopped, Toast.LENGTH_SHORT).show();


            }
        });

        soundPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    soundStop.setEnabled(false);
                    soundRecord.setEnabled(false);
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(getRecordingFilePath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    if(mediaPlayer.isPlaying()){
                        // to disable button during playing
                        soundPlay.setEnabled(false);
                    }

                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            // mediaplayer completion
                            soundPlay.setEnabled(true);
                            soundRecord.setEnabled(true);
                        }
                    });
                    Toast.makeText(SoundRecorder.this, R.string.sound_Record_Play, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("Sound Playing Error", e.getMessage());
                }

            }
        });

        soundUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retrieveOrderIdMatchUserIdPreferences();
                // System.out.println("OrderId: "+orderId);
                // System.out.println("readerId: "+readerId);
                    uploadFile();
                    soundUpload.setEnabled(false);

            }
        });

    } // onCreate bracket
    private boolean isMicrophonePresent(){
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)){
            return true;
        } else{
            return false;
        }
    }

    private void getMicrophonePermission (){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_CODE);
        }
    }

    private String getRecordingFilePath(){
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory, "podcast"+".mp3");
        // System.out.println("FILE getAbsolutePath: "+file.getPath());
        return file.getPath();
    }

    public void retrieveOrderIdMatchUserIdPreferences(){
        // from ReaderActivity
        sharedPreferences = getSharedPreferences("orderData", MODE_PRIVATE);
        orderId = sharedPreferences.getInt("orderId",0);
        readerId = sharedPreferences.getInt("readerId",0);
        unitRecordNo = sharedPreferences.getInt("unitRecordNo",0);
    }
    private void getReadPermission (){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_CODE);
        }
    }
    private void uploadFile() {

        File file = new File(getRecordingFilePath());

        String typeMedia = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(getRecordingFilePath());
        if (extension != null) {
            typeMedia = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse(typeMedia),file);
        //fileData name must be same as server acceptance field name
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("fileData",file.getName(),requestBody);

        Retrofit retrofit = RetrofitNetworkClient.getRetrofit();
        serverApi = retrofit.create(ServerApi.class);
//        Call call = serverApi.recordSound(filePart);
        Call call = serverApi.recordSound(filePart, orderId, readerId, unitRecordNo);

        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if(!response.isSuccessful()){
                    Toast.makeText(SoundRecorder.this, R.string.sound_Upload_Failed, Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(SoundRecorder.this, R.string.sound_Upload_Success, Toast.LENGTH_LONG).show();
                // back to new record
                finish();

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                System.out.println("T: "+t);

            }
        });

    }


}// belongs to SoundRecorder class