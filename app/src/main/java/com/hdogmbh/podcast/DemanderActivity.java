package com.hdogmbh.podcast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DemanderActivity extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;
    private TextView unit_price; // To show unit price
    public static final String BASE_URL = "https://hdogmbh.de/api/"; // need to be changed IP or URL of server when deployed
    private ServerApi serverApi;
    // send button
    private Button demandSendButton, readerButton, demandPlayButton;
    private TextView to_whome;
    private TextView purpose;
    private TextView description;
    private EditText number_read;
    private String to_whomeValue;
    private String purposeValue;
    private int number_readValue;
    private String descriptionValue;
    private float unitPriceValue;
    // sharedPreferences
    SharedPreferences sharedPreferences;
    String userEmail;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demander);

        //DemandButton to play
        demandPlayButton = findViewById(R.id.demandPlayButton);

        readerButton = findViewById(R.id.readerButton);
        unit_price = findViewById(R.id.unit_price);
        // demandSend button
        demandSendButton = findViewById(R.id.demandSend);
        to_whome = findViewById(R.id.demandname);
        purpose = findViewById(R.id.demandpurpose);
        number_read = findViewById(R.id.demandnumber);
        description = findViewById(R.id.demandexplanation);


        // it retrieve userEmail
        retrieveUserPreferences();

        // demandSendButton onclick
        demandSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                to_whomeValue = to_whome.getText().toString();
                purposeValue = purpose.getText().toString();
                //try-catch to convert string to Integer
                try {
                    number_readValue =  Integer.parseInt(number_read.getText().toString());
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(DemanderActivity.this, R.string.demanNumberZero, Toast.LENGTH_SHORT).show();
                    return; // will be un-commented on production
                }

                descriptionValue = description.getText().toString();
                //try-catch for converting text to double
                try {
                    unitPriceValue = Float.parseFloat(unit_price.getText().toString());
                }catch (Exception e){
                    e.printStackTrace();
                    unitPriceValue = 1.00F;
                }

                // form validation
                if(number_readValue <= 0){
                    Toast.makeText(DemanderActivity.this, R.string.demanNumberZero, Toast.LENGTH_SHORT).show();
                    return;
                }

//                if(unitPriceValue <= 1){
//                    // if any error for fetching unit_price, 1 is default value for Integer unitPriceValue
//                    Toast.makeText(DemanderActivity.this, R.string.unitPriceError, Toast.LENGTH_SHORT).show();
//                    return;
//                }

                if(to_whome.length() != 0 && purpose.length() != 0 ){
                    // to send some data to CreditCardActivity
                    Intent i = new Intent(DemanderActivity.this,CreditCardFormActivity.class);
                    saveDemandDataPreferences(to_whomeValue, purposeValue, number_readValue, descriptionValue, unitPriceValue);
                    startActivity(i);
                }else{
                    Toast.makeText(DemanderActivity.this, R.string.demanFormFill, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });


        // to call Retrofit instance
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        serverApi = retrofit.create(ServerApi.class);
        getUnit_price();

        readerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // we will check if user is reader or not on readerActivity
                Intent i = new Intent(DemanderActivity.this, ReaderActivity.class);
                startActivity(i);
            }
        });

        demandPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(DemanderActivity.this, PlayerInputActivity.class);
                startActivity(i);
            }
        });
    }

    private void getUnit_price(){
        // we dont need constructor in here that we don't send any data
        Call<ModelProduct> call = serverApi.getUnit_price();
        call.enqueue(new Callback<ModelProduct>() {
            @Override
            public void onResponse(Call<ModelProduct> call, Response<ModelProduct> response) {
                if(!response.isSuccessful()){
                    Toast.makeText(DemanderActivity.this, R.string.gPriceFailed, Toast.LENGTH_SHORT).show();
                    return;
                };
                float getUnit_price = response.body().getUnit_price();
                unit_price.setText(""+getUnit_price);

            }

            @Override
            public void onFailure(Call<ModelProduct> call, Throwable t) {
                Toast.makeText(DemanderActivity.this, R.string.gPriceFailed, Toast.LENGTH_SHORT).show();
                Log.d("errorCall",""+t.getMessage().toString());

            }
        });
    }

    public void saveDemandDataPreferences(String to_whomeValue, String purposeValue, int number_readValue, String descriptionValue, float unitPriceValue){
        sharedPreferences = getSharedPreferences("demandData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("to_whomeValue",to_whomeValue);
        editor.putString("purposeValue",purposeValue);
        editor.putInt("number_readValue",number_readValue);
        editor.putString("descriptionValue",descriptionValue);
        editor.putFloat("unitPriceValue", unitPriceValue);
        editor.apply();
    }

    public void retrieveUserPreferences(){
        sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE);
        userEmail = sharedPreferences.getString("userEmail","noEmail");

    }

}