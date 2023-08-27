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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlayerInputActivity extends AppCompatActivity {

    EditText editTextOrderNumber;
    Button toPlay;
    private ServerApi serverApi;
    // user input uniqueId or demandNumber
    String uniqueId;
    // response
    Integer orderId;
    // sharedPreferences
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_input);

        editTextOrderNumber = findViewById(R.id.editTextOrderNumber);

        //      toPlay Button and its initial value disabled
        toPlay = findViewById(R.id.toPlay);
//        toPlay.setEnabled(true); // must be false in production

        // to call Retrofit instance
        Retrofit retrofit = new Retrofit.Builder().baseUrl(DemanderActivity.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        serverApi = retrofit.create(ServerApi.class);

        toPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uniqueId = editTextOrderNumber.getText().toString();
                if(uniqueId != null){
                    sendUniqueId(uniqueId);
                }
                return;
            }
        });
    }

    public void sendUniqueId(String uniqueId){
        // we need constructor in here
        //uniqueId or demandNumber will send to DB, and orderId will back from Back-end
        ModelProduct modelProduct = new ModelProduct(uniqueId);
        Call<ModelProduct> call = serverApi.findOrder(modelProduct);

        call.enqueue(new Callback<ModelProduct>() {
            @Override
            public void onResponse(Call<ModelProduct> call, Response<ModelProduct> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(PlayerInputActivity.this, R.string.orderNumberNotFound, Toast.LENGTH_SHORT).show();
                    return;
                }
                // its orderId not demandNumber(uniqueId), therefore its not String
                orderId = response.body().getId();
                // System.out.println("Order found "+orderId);

                // to save product.id
                saveOrderIdToPlayPreferences(orderId);
                // Starting another activity
                Intent i = new Intent(PlayerInputActivity.this, ListToPlayActivity.class);
                startActivity(i);
            }

            @Override
            public void onFailure(Call<ModelProduct> call, Throwable t) {
                System.out.println("Error to find Inquiry"+t);
                Toast.makeText(PlayerInputActivity.this, R.string.orderNumberNotFound, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void saveOrderIdToPlayPreferences(int orderId){
        // orderId comes from sendUniqueId (product.id comes from server)
        sharedPreferences = getSharedPreferences("orderPlayData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("orderId",orderId);
        editor.apply();
    }
}