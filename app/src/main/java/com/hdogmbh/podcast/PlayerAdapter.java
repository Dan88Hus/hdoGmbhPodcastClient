package com.hdogmbh.podcast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.MusicViewHolder> {
    // first variable type is array , 2nd one type is context, this class will be responsible to defining card_music we designed

    ArrayList<String> list;
    Context mContext;
    // ServerApi
    private ServerApi serverApi;
    // voice Record Id
    private Integer voiceRecordId;
    // voice Record Name
    private String recordName;
    // recordedVoiceAbsPath
    private String recordedVoiceAbsPath;
    // sharedPreferences
    SharedPreferences sharedPreferences;

    public PlayerAdapter(ArrayList<String> list, Context mContext) {
        this.list = list;
        this.mContext = mContext;

    }


    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // add music list to cardView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_music, parent, false);
        // pass this layout as parameter to MusicViewHolder
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // this method is do operations
        //""+modelVoice.getId()+"_"+modelVoice.getFilePath().substring(13) => 3_2022-03-09-16-18-15-podcast.mp3
        recordName = list.get(position);

        holder.textViewFileName.setText(recordName);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //this finds the first occurrence of "_" as index
                int charSelected = list.get(position).indexOf("_");

                // Now charPosition can be -1, if lets say the string had no "_" at all in it i.e. no "_" is found.
                //So check and account for it.
                if (charSelected != -1)
                {
                    //this will give first char till end of first '_'
//                    voiceRecordId = Integer.parseInt(recordName.substring(0 , charPosition));
                    try {
                        voiceRecordId = Integer.parseInt(list.get(position).substring(0 , charSelected));
                        System.out.println("voiceRecordId: "+voiceRecordId);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Error to convert ID");
                    }
                }

//                Toast.makeText(holder.itemView.getContext(), ""+voiceRecordId, Toast.LENGTH_SHORT).show();

                // to call Retrofit instance
                Retrofit retrofit = new Retrofit.Builder().baseUrl(DemanderActivity.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
                serverApi = retrofit.create(ServerApi.class);
                // send voice record id to server
                sendRecordId(voiceRecordId);
            }
        });

    }

    @Override
    public int getItemCount() {
        if(list.size()==0){
            System.out.println("size is zero");
            // starting DeletedRecordsActivity
            Intent intent = new Intent(mContext,DeletedRecordsActivity.class);
            mContext.startActivity(intent);
        }
        // length of the array
        return list.size();
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder {
        // to define components of card_music design
        private TextView textViewFileName;
        private CardView cardView;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            // here itemView will represent cardView design
            textViewFileName = itemView.findViewById(R.id.textViewFileName);
            cardView = itemView.findViewById(R.id.cardView);

        }
    }

    // this methods also write recordedVoice to disk which comes as response
    public void sendRecordId(int voiceRecordId) {
        // we need constructor in here
        ModelVoiceRecord modelVoiceRecord = new ModelVoiceRecord(voiceRecordId);

        Call<ResponseBody> call = serverApi.sendVoiceRecord(modelVoiceRecord);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.isSuccessful()) {

                    // AsyncTask is Deprecated on Android 11
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            //Background jobs
                            boolean writtenToDisk = writeResponseFile(response.body());
                            //System.out.println("SAVED FIlE: " + writtenToDisk);
                            // System.out.println("String recordedVoiceAbsPath: "+recordedVoiceAbsPath);
                            savePlayerDataPreferences(recordedVoiceAbsPath);
                            // starting PlayerActivity
                            Intent intent = new Intent(mContext,PlayerActivity.class);
                            mContext.startActivity(intent);
                        }
                    });

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println("failed");

            }
        });



    }

    private boolean writeResponseFile(ResponseBody body) {


        try {
            ContextWrapper contextWrapper = new ContextWrapper(mContext.getApplicationContext());
            File downloadDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File recordedVoicePath = new File(downloadDirectory.getAbsolutePath() + File.separator + recordName);
            recordedVoiceAbsPath = recordedVoicePath.getAbsolutePath();



            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                try {
                    outputStream = new FileOutputStream(recordedVoicePath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                while (true) {
                    int read = 0;

                    try {
                        read = inputStream.read(fileReader);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                }
                // its buffered , flush makes buffered to persistent
                outputStream.flush();


                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }

    }

    public void savePlayerDataPreferences(String recordedVoiceAbsPath){
        sharedPreferences = mContext.getSharedPreferences("playerData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("recordedVoiceAbsPath",recordedVoiceAbsPath);
        editor.putString("recordName",recordName);
        editor.putInt("voiceRecordId",voiceRecordId);
        editor.apply();
    }
}
