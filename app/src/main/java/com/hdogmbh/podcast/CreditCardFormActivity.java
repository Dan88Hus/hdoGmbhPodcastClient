package com.hdogmbh.podcast;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.cardform.view.CardForm;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CreditCardFormActivity extends AppCompatActivity {

    private String demandingName;
    private String demandingPurpose;
    private int demandingNumber;
    private String demandingDescription;
    private float demandingUnitPrice;
    private float totalPrice;
    // define ServerApi
    private ServerApi serverApi;
    // for response json we define List
    String responseOrderId = null;
    // sharedPreferences
    SharedPreferences sharedPreferences;
    //checkBox
    CheckBox checkBoxSalesContract;
    TextView textViewSalesContract;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_card_form);
        Button buy = findViewById(R.id.btnBuy);
        CardForm cardForm = findViewById(R.id.card_form);
        checkBoxSalesContract = (CheckBox) findViewById(R.id.checkBoxSalesContract);
        textViewSalesContract = (TextView) findViewById(R.id.textViewSalesContract);

        textViewSalesContract.setMovementMethod(LinkMovementMethod.getInstance());

        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .cardholderName(CardForm.FIELD_REQUIRED)
                .actionLabel(""+R.string.label_purchase_success) // Button Label
                .setup(CreditCardFormActivity.this);

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkBoxSalesContract.isChecked()){
                    Toast.makeText(CreditCardFormActivity.this, R.string.sales_contract_error, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (cardForm.isValid()){
                    // code development here
                    //to get values from DemanderActivity
                    retrieveDemandDataPreferences();

                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(CreditCardFormActivity.this);
                    alertBuilder.setTitle(R.string.purchaseConfirmForm);

                    // Format 2 decimal number
                    DecimalFormat dfGerman = new DecimalFormat("#,###.##",
                            new DecimalFormatSymbols(Locale.GERMAN));

                    totalPrice = demandingUnitPrice*demandingNumber;
                    alertBuilder.setMessage(getString(R.string.purTotal)+": "+dfGerman.format(totalPrice));

                /*
                Card number: " + cardForm.getCardNumber() + "\n" +
                "Card expiry date: " + cardForm.getExpirationDateEditText().getText().toString() + "\n" +
                "Card CVV: " + cardForm.getCvv() + "\n" +
                "Postal code: " + cardForm.getPostalCode() + "\n" +
                "Phone number: " + cardForm.getMobileNumber()
                */

                    alertBuilder.setPositiveButton(R.string.purchasePositiveAlert, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            Toast.makeText(CreditCardFormActivity.this, R.string.label_purchase_thanks,Toast.LENGTH_SHORT).show();
                            // ********************************send data to IYZICO********************************************
                            // to call Retrofit instance
                            Retrofit retrofit = new Retrofit.Builder().baseUrl(DemanderActivity.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
                            serverApi = retrofit.create(ServerApi.class);

                            sendDemandData(); // this will be called after iyizco response on production
//                            sendDemandData will intent orderId to SuccessActivity
                        }
                    });

                    alertBuilder.setNegativeButton(R.string.purchaseNegativeAlert, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertBuilder.create();
                    alertDialog.show();
                } else {
                    Toast.makeText(CreditCardFormActivity.this, R.string.purchase_complete_form, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendDemandData(){
        // we need constructor in here
        //unitPrice will get from DB, and calculation will be on Back-end
        ModelProduct modelProduct = new ModelProduct(this.demandingName,this.demandingPurpose,this.demandingDescription,this.demandingNumber);
        Call<String> call = serverApi.updateDetails(modelProduct);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(!response.isSuccessful()){
                    Toast.makeText(CreditCardFormActivity.this, R.string.sendDemandDataFailed, Toast.LENGTH_SHORT).show();
                    return;
                };

                Toast.makeText(CreditCardFormActivity.this,  R.string.demanFormSuccess, Toast.LENGTH_SHORT).show();
                responseOrderId = response.body();
                Intent intent = new Intent(CreditCardFormActivity.this,CreditCardSuccessActivity.class);
                intent.putExtra("responseOrderId",responseOrderId);
                startActivity(intent);

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(CreditCardFormActivity.this, R.string.sendDemandDataFailed, Toast.LENGTH_SHORT).show();
                System.out.println("Failure Message sendDemandData: "+t);

            }
        });

    }

    public void retrieveDemandDataPreferences(){
        sharedPreferences = getSharedPreferences("demandData", MODE_PRIVATE);

        demandingName = sharedPreferences.getString("to_whomeValue","noToWhome");
        demandingPurpose = sharedPreferences.getString("purposeValue","noPurpose");
        demandingNumber = sharedPreferences.getInt("number_readValue",1);
        demandingDescription = sharedPreferences.getString("descriptionValue","noDescription");
        demandingUnitPrice = sharedPreferences.getFloat("unitPriceValue",1.00F);
    }

}