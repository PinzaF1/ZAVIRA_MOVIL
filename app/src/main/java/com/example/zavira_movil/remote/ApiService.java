package com.example.zavira_movil.remote;

import com.example.zavira_movil.model.Estudiante;
import com.example.zavira_movil.model.LoginRequest;
import com.example.zavira_movil.model.LoginResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @POST("loginEstudiante")
    Call<ResponseBody> loginEstudiante(@Body LoginRequest request);


    @GET("perfilEstudiante")
    Call<Estudiante> getPerfilEstudiante();
}

