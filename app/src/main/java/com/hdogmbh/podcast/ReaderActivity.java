package com.hdogmbh.podcast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReaderActivity extends AppCompatActivity {

    EditText editTextOrderNumber;
    EditText editTextOrderUnitNumber;
    // user input uniqueId or demandNumber
    String uniqueId;
    Integer uniqueIdUnitNumber;
    private ServerApi serverApi;
    private String userEmail;
    private Integer userId;
    Button toRecord;
    // sharedPreferences
    SharedPreferences sharedPreferences;
    // response
    Integer orderId;
    //numberRead is for the demandNumber that user specifies
    Integer numberRead;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        editTextOrderNumber = findViewById(R.id.editTextOrderNumber);
        editTextOrderUnitNumber = findViewById(R.id.editTextOrderUnitNumber);

//      toProceed Button and its initial value disabled
        toRecord = findViewById(R.id.toRecord);
        toRecord.setEnabled(false); // must be false in production

        //to get values from DemanderActivity
        retrieveUserPreferences();

        // to call Retrofit instance
        Retrofit retrofit = new Retrofit.Builder().baseUrl(DemanderActivity.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        serverApi = retrofit.create(ServerApi.class);
        getReaderUsers(); // must be activated in production

        toRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uniqueId = editTextOrderNumber.getText().toString();
                try {
                    uniqueIdUnitNumber = Integer.parseInt(editTextOrderUnitNumber.getText().toString());
                }catch (Exception e){
                    Toast.makeText(ReaderActivity.this, R.string.demanNumberZero, Toast.LENGTH_SHORT).show();
                    return;
                }

                if(uniqueIdUnitNumber <= 0 ){
                    Toast.makeText(ReaderActivity.this, R.string.demanNumberZero, Toast.LENGTH_SHORT).show();
                    editTextOrderUnitNumber.setText("1");
                    return;
                }
                if(uniqueId != null){
                    sendUniqueId(uniqueId, uniqueIdUnitNumber); // uncomment on production
                }
//                Intent i = new Intent(ReaderActivity.this, SoundRecorder.class); // delete on production
//                startActivity(i); // delete on production
            }
        });

    }

    private void getReaderUsers() {
        //to check if user is READER
        Call<List<ModelUser>> call = serverApi.getReader();
        call.enqueue(new Callback<List<ModelUser>>() {
            @Override
            public void onResponse(Call<List<ModelUser>> call, Response<List<ModelUser>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(ReaderActivity.this, R.string.getReaderLisFailed, Toast.LENGTH_SHORT).show();
                    return;
                }

                List<ModelUser> modelUsers = response.body();
                HashMap<Integer, String> readerDetailsHashMap = new HashMap<>();

                for (ModelUser modelUser : modelUsers) {

                    if (modelUser.getEmail().equals(userEmail)) {
                        readerDetailsHashMap.put(modelUser.getId(), modelUser.getEmail());
                        Toast.makeText(ReaderActivity.this, R.string.readerFound, Toast.LENGTH_SHORT).show();
                        toRecord.setEnabled(true);
                        break;
                    } else{
                        toRecord.setEnabled(false);
                    }
                }

                if (!readerDetailsHashMap.isEmpty()) {
                    // reading from HashMap
                    for (HashMap.Entry<Integer, String> readerDetail : readerDetailsHashMap.entrySet()) {
//                    System.out.println(readerDetail.getKey()+" "+readerDetail.getValue());
                        // assign userId to Integer
                        userId = readerDetail.getKey();
                        // System.out.println("USERID: "+userId);
                    }
                } else {
                    Toast.makeText(ReaderActivity.this, R.string.readerNotFound, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ModelUser>> call, Throwable t) {
                Toast.makeText(ReaderActivity.this, R.string.getReaderLisFailed, Toast.LENGTH_SHORT).show();
                t.printStackTrace();

            }
        });
    }

    public void retrieveUserPreferences(){
        sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE);
        userEmail = sharedPreferences.getString("userEmail","noEmail");
    }

    public void sendUniqueId(String uniqueId, Integer uniqueIdUnitNumber){
        // we need constructor in here
        //uniqueId or demandNumber will send to DB, and orderId will back from Back-end
        ModelProduct modelProduct = new ModelProduct(uniqueId);
        Call<ModelProduct> call = serverApi.findOrder(modelProduct);

        call.enqueue(new Callback<ModelProduct>() {
            @Override
            public void onResponse(Call<ModelProduct> call, Response<ModelProduct> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(ReaderActivity.this, R.string.orderNumberNotFound, Toast.LENGTH_SHORT).show();
                    return;
                }
                // its orderId not demandNumber(uniqueId), therefore its not String
                orderId = response.body().getId();
                numberRead = response.body().getNumber_read();
                // check if reader input uniqueIdUnitNumber <= numberRead of demand
                if(uniqueIdUnitNumber <= numberRead){
                    // saveOrderIdPreferences after getting order id
                    saveOrderIdMatchUserIdPreferences(orderId, userId, uniqueIdUnitNumber); // uncomment on production
                    // Starting another activity
                    Intent i = new Intent(ReaderActivity.this, SoundRecorder.class);
                    startActivity(i);
                }else{
                    Toast.makeText(ReaderActivity.this, R.string.orderNumberNotFound, Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onFailure(Call<ModelProduct> call, Throwable t) {
                System.out.println("Error to find Inquiry"+t);
                Toast.makeText(ReaderActivity.this, R.string.orderNumberNotFound, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void saveOrderIdMatchUserIdPreferences(int orderId, int userId, int uniqueIdUnitNumber){
        // orderId comes from sendUniqueId, userId comes from getReaderUsers HashMap
        sharedPreferences = getSharedPreferences("orderData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("orderId",orderId);
        editor.putInt("readerId",userId); // must be activated in production
        editor.putInt("unitRecordNo",uniqueIdUnitNumber);
        editor.apply();
    }
}