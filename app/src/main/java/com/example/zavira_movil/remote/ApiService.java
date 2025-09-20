package com.example.zavira_movil.remote;

import com.example.zavira_movil.model.Estudiante;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.model.LoginRequest;

import com.example.zavira_movil.model.KolbRequest;
import com.example.zavira_movil.model.KolbResponse;
import com.example.zavira_movil.model.PreguntasKolb;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @POST("estudiante/login")
    Call<ResponseBody> loginEstudiante(@Body LoginRequest request);

    @GET("perfilEstudiante")
    Call<Estudiante> getPerfilEstudiante();

    @GET("kolb/preguntas")
    Call<List<PreguntasKolb>> getPreguntas();

    @POST("kolb/guardarRespuestas")
    Call<KolbResponse> guardarRespuestas(@Body KolbRequest request);

    @GET("/kolb/resultado")
    Call<KolbResultado> obtenerResultado();


    //  MEtodos para la foto de perfil
    @Multipart
    @POST("users/me/photo")
    Call<ResponseBody> subirFoto(@Part MultipartBody.Part foto);

    @DELETE("users/me/photo")
    Call<ResponseBody> eliminarFoto();
}