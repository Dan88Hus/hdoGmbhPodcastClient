package com.hdogmbh.podcast;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;

public interface ServerApi {

    //<ResponseBody> can be type if we dont know model fields
    @POST("getDefaultPrice")
//    Call<ModelProduct> getUnit_price(@Body ModelProduct modelProduct); we dont send any data to body so commented
    Call<ModelProduct> getUnit_price();

    @POST("sendDemandData")
    // we will use constructor, Body modifier will encapsulate details
    // res. is String therefore Call<String>
    Call<String> updateDetails(@Body ModelProduct modelProduct);


    @POST("getReaderUser")
//    Call<ModelUser> getReader(@Body ModelUser modelUser); we dont send any data to body so commented
    Call<List<ModelUser>> getReader();

    @POST("addDemanderUser")
        // we will use constructor, Body modifier will encapsulate details
        // res. is Integer therefore Call<String>
    Call<Integer> addDemander(@Body ModelUser modelUser);

    @POST("findOrder")
        // we will use constructor, Body modifier will encapsulate details
    Call<ModelProduct> findOrder(@Body ModelProduct modelProduct);

    @Multipart
    @POST("uploadRecord")
//    Call<RequestBody> recordSound(@Part MultipartBody.Part fileData);
    Call<Void> recordSound(@Part MultipartBody.Part fileData, @Part("orderId") Integer orderId, @Part("readerId") Integer readerId, @Part("unitRecordNo") Integer unitRecordNo);


    @POST("listOrderRecords")
        // we will use constructor to send orderId, Body modifier will encapsulate details
    Call<List<ModelVoiceRecord>> listOrderRecords(@Body ModelVoiceRecord ProductId);

    @Streaming
    @POST("sendVoiceRecord")
        // we will use constructor to send orderId, Body modifier will encapsulate details
    Call<ResponseBody> sendVoiceRecord(@Body ModelVoiceRecord id);

    @POST("sendVoiceRecordRating")
        // we will use constructor to send orderId, Body modifier will encapsulate details
    Call<ModelVoiceRecord> sendVoiceRecordRating(@Body ModelVoiceRecord id);


}
