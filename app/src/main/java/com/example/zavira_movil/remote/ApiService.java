package com.example.zavira_movil.remote;

import com.example.zavira_movil.QuizCerrarRequest;
import com.example.zavira_movil.QuizInicialResponse;
import com.example.zavira_movil.model.Estudiante;
import com.example.zavira_movil.model.HistorialItem;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.model.LoginRequest;

import com.example.zavira_movil.model.KolbRequest;
import com.example.zavira_movil.model.KolbResponse;
import com.example.zavira_movil.model.MateriaDetalle;
import com.example.zavira_movil.model.PreguntasKolb;
import com.example.zavira_movil.model.MateriaProgreso;
import com.example.zavira_movil.model.ProgresoMateria;
import com.example.zavira_movil.model.ResumenGeneral;
import com.example.zavira_movil.model.Reto;
import com.example.zavira_movil.model.RetoEstado;
import com.example.zavira_movil.model.RetoRespuesta;
import com.example.zavira_movil.model.RetoRonda;


import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    //Logue Estudiante
    @POST("estudiante/login")
    Call<ResponseBody> loginEstudiante(@Body LoginRequest request);

    @GET("perfilEstudiante")
    Call<Estudiante> getPerfilEstudiante();

    //Estilo de Kold
    @GET("kolb/preguntas")
    Call<List<PreguntasKolb>> getPreguntas();

    @POST("kolb/enviar")
    Call<KolbResponse> guardarRespuestas(@Body KolbRequest request);

    @GET("kolb/resultado")
    Call<KolbResultado> obtenerResultado();

    //Examen Inicial
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

    //MEtodos para la foto de perfil
    @Multipart
    @POST("users/me/photo")
    Call<ResponseBody> subirFoto(@Part MultipartBody.Part foto);

    @DELETE("users/me/photo")
    Call<ResponseBody> eliminarFoto();




        // ---------- Progreso Móvil ----------
        @GET("movil/progreso/resumen")
        Call<ResumenGeneral> getResumen();

    @GET("movil/progreso/materias")
    Call<List<MateriaDetalle>> getMaterias();

    @GET("movil/progreso/historial")
    Call<List<HistorialItem>> getHistorial();


        // retos 1 vs 1

        @POST("movil/retos")
        Call<Reto> crearReto(@Body Reto reto);

    // 2. Aceptar un reto
    @POST("movil/retos/{id_reto}/aceptar")
    Call<RetoEstado> aceptarReto(@Path("id_reto") int idReto);

    // 3. Responder una ronda
    @POST("movil/retos/ronda")
    Call<RetoRespuesta> responderRonda(@Body RetoRonda ronda);

    // 4. Ver estado de un reto
    @GET("movil/retos/{id_reto}")
    Call<RetoEstado> estadoReto(@Path("id_reto") int idReto);



}