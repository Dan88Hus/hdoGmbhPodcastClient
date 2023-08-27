package com.hdogmbh.podcast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CreditCardSuccessActivity extends AppCompatActivity {

    String userName;
    String userSurname;
    String userEmail;
    String responseOrderId;
    TextView textViewOrderNumber;
    // sharedPreferences
    SharedPreferences sharedPreferences;
    // define ServerApi
    private ServerApi serverApi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_card_success);

        textViewOrderNumber = findViewById(R.id.textViewOrderNumber);

        //to get values from CreditCardFormActivity sendDemand method
        Intent i = getIntent();
        responseOrderId = i.getStringExtra("responseOrderId");

        // get User values to record from MainActivity, from useData preferences
        retrieveUserPreferences();

        textViewOrderNumber.setText(responseOrderId);

        // to call Retrofit instance
        Retrofit retrofit = new Retrofit.Builder().baseUrl(DemanderActivity.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        serverApi = retrofit.create(ServerApi.class);

        sendUserDataToCreate(); // user create
    }

    public void retrieveUserPreferences(){
        sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE);
        userName = sharedPreferences.getString("userName","noName");
        userSurname = sharedPreferences.getString("userSurname","noSurname");
        userEmail = sharedPreferences.getString("userEmail","noEmail");
    }

    // sharedPreferences must be deleted in this activity

    private void sendUserDataToCreate(){

        ModelUser modelUser = new ModelUser(this.userEmail, this.userName, this.userSurname, this.responseOrderId);
        Call<Integer> call = serverApi.addDemander(modelUser);

        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if(!response.isSuccessful()){
                    Toast.makeText(CreditCardSuccessActivity.this, R.string.demandCreateUser, Toast.LENGTH_SHORT).show();
                    return;
                };
                //System.out.println("RESPONSE "+ response.body());
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                System.out.println("ERROR"+t);

            }
        });
    }
}