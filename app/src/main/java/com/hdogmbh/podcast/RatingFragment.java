package com.hdogmbh.podcast;

import static com.hdogmbh.podcast.DemanderActivity.BASE_URL;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RatingFragment extends DialogFragment {
    // extends Fragment to DialogFragment as it will be dialog

    Button btnOk, btnCancel;
    RatingBar ratingBar;
    Integer voiceRecordId;
    ServerApi serverApi;


    public RatingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_rating, container, false);

        ratingBar = view.findViewById(R.id.ratingBar);
        btnOk = view.findViewById(R.id.buttonOk);
        btnCancel = view.findViewById(R.id.buttonCancel);
        Bundle bundle = getArguments();
        voiceRecordId = bundle.getInt("voiceRecordId");

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendRecordId(voiceRecordId,ratingBar.getRating());
                getDialog().dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();

            }
        });

        return view;
    }
    public void sendRecordId(Integer voiceRecordId, Float rating){
        // to call Retrofit instance
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        serverApi = retrofit.create(ServerApi.class);

        // we need constructor in here
        ModelVoiceRecord modelVoiceRecord = new ModelVoiceRecord(voiceRecordId,rating);
        Call<ModelVoiceRecord> call = serverApi.sendVoiceRecordRating(modelVoiceRecord);

        call.enqueue(new Callback<ModelVoiceRecord>() {
            @Override
            public void onResponse(Call<ModelVoiceRecord> call, Response<ModelVoiceRecord> response) {
                if(!response.isSuccessful()){
                    System.out.println("it is successful");
                }
            }

            @Override
            public void onFailure(Call<ModelVoiceRecord> call, Throwable t) {
                System.out.println("it is not successful");
            }
        });
    }
}