package com.hdogmbh.podcast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ListToPlayActivity extends AppCompatActivity {

    // PlayerAdapter and ArrayList
    private PlayerAdapter playerAdapter;

    private ArrayList<String> playerArrayList = new ArrayList<>();


    //RecyclerView
    private RecyclerView recyclerView;
    private final int REQUEST_CODE = 1;

    // sharedPreferences
    SharedPreferences sharedPreferences;
    //orderId
    private Integer orderId;
    // ServerApi
    private ServerApi serverApi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_to_play);

        //recyclerView
        recyclerView = findViewById(R.id.recyclerView);
        //to sort audio 1 by 1
        recyclerView.setLayoutManager(new LinearLayoutManager(ListToPlayActivity.this));
        //to design as grid structure but we dont use grid layout
        // recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));

        // check read_external_storage permission
        if(ContextCompat.checkSelfPermission(ListToPlayActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ListToPlayActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
        // else we read internal storage to list files getAllAudioFiles()

        // from PlayerInputActivity
        retrieveOrderIdToPlayPreferences();
        // System.out.println("orderId: "+orderId);

        // to call Retrofit instance
        Retrofit retrofit = new Retrofit.Builder().baseUrl(DemanderActivity.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        serverApi = retrofit.create(ServerApi.class);


        // retrieve record list from server
        retrieveListOrderRecords(orderId);




    }

    public void retrieveOrderIdToPlayPreferences(){
        // from ReaderActivity
        sharedPreferences = getSharedPreferences("orderPlayData", MODE_PRIVATE);
        orderId = sharedPreferences.getInt("orderId",0);
    }

    public void retrieveListOrderRecords(Integer orderId){
        // we need constructor in here
        //orderId (product.id) will be send to server and will return list of voice records
        ModelVoiceRecord modelVoiceRecord = new ModelVoiceRecord(orderId);
        Call<List<ModelVoiceRecord>> call = serverApi.listOrderRecords(modelVoiceRecord);
        //System.out.println("ORDERID: "+orderId );
        call.enqueue(new Callback<List<ModelVoiceRecord>>() {
            @Override
            public void onResponse(Call<List<ModelVoiceRecord>> call, Response<List<ModelVoiceRecord>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(ListToPlayActivity.this, R.string.getRecordListFailed, Toast.LENGTH_SHORT).show();
                    return;
                }
                //System.out.println("Response "+response.body());
                List<ModelVoiceRecord> modelVoiceRecordList = response.body();

                for (ModelVoiceRecord modelVoice : modelVoiceRecordList) {
                    //System.out.println("ID: " + modelVoice.getId());
                    //System.out.println("getFilePath: " + modelVoice.getFilePath());
                    // saving ID and filePath to hashMap
                    // voiceRecords/2022-03-09-16-18-15-podcast.mp3
                    playerArrayList.add(""+modelVoice.getId()+"_"+modelVoice.getFilePath().substring(13));
//                    playerAdapter.notifyDataSetChanged();
                }
                // for(String list: playerArrayList){
                    //System.out.println(list.toString());
                //}

                playerAdapter = new PlayerAdapter(playerArrayList,ListToPlayActivity.this);
                recyclerView.setAdapter(playerAdapter);

//                //need to fix this method
//                try {
//                    getAllAudioFiles();
//                } catch (PackageManager.NameNotFoundException e) {
//                    e.printStackTrace();
//                }

            }

            @Override
            public void onFailure(Call<List<ModelVoiceRecord>> call, Throwable t) {
                Toast.makeText(ListToPlayActivity.this, R.string.getRecordListFailed, Toast.LENGTH_SHORT).show();
            }
        });

    }

    // after read_external_storage permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if( requestCode == REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            // we will show the audio files
            try {
                getAllAudioFiles();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    public void getAllAudioFiles() throws PackageManager.NameNotFoundException {
        // if first run and allow it works , second times not work, fix it
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File downloadDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        File mainFile = new File(String.valueOf(downloadDirectory));
        // System.out.println("absolutePath: "+mainFile.getAbsolutePath());
    }
}