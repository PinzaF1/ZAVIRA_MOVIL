package com.example.zavira_movil.remote;

import com.example.zavira_movil.BasicResponse;
import com.example.zavira_movil.PreguntaAcademica;
import com.example.zavira_movil.QuizCerrarRequest;
import com.example.zavira_movil.QuizInicialResponse;
import com.example.zavira_movil.QuizResponse;
import com.example.zavira_movil.model.Estudiante;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.model.LoginRequest;

import com.example.zavira_movil.model.KolbRequest;
import com.example.zavira_movil.model.KolbResponse;
import com.example.zavira_movil.model.PreguntasKolb;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    @POST("estudiante/login")
    Call<ResponseBody> loginEstudiante(@Body LoginRequest request);


    @GET("perfilEstudiante")

    Call<Estudiante> getPerfilEstudiante();

    @GET("kolb/preguntas")
    Call<List<PreguntasKolb>> getPreguntas();

    @POST("kolb/enviar")
    Call<KolbResponse> guardarRespuestas(@Body KolbRequest request);

    @GET("kolb/resultado")
    Call<KolbResultado> obtenerResultado();



    @POST("quizz/iniciar")
    Call<QuizInicialResponse> iniciar(
            @Header("Authorization") String bearerToken,
            @Body Map<String, Object> body
    );


    @POST("quiz-inicial/cerrar")
    Call<ResponseBody> cerrar(
            @Header("Authorization") String bearerToken,
            @Body QuizCerrarRequest request
    );


}