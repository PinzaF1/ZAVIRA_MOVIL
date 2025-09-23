package com.example.zavira_movil.remote;

import com.example.zavira_movil.model.Estudiante;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.model.LoginRequest;
import com.example.zavira_movil.model.KolbRequest;
import com.example.zavira_movil.model.KolbResponse;
import com.example.zavira_movil.model.PreguntasKolb;

// 👇 modelos para quiz y simulacro
import com.example.zavira_movil.model.ParadaRequest;
import com.example.zavira_movil.model.ParadaResponse;
import com.example.zavira_movil.model.CerrarRequest;
import com.example.zavira_movil.model.CerrarResponse;
import com.example.zavira_movil.model.SimulacroRequest;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    // -------------------------------
    // Login y perfil estudiante
    // -------------------------------
    @POST("estudiante/login")
    Call<ResponseBody> loginEstudiante(@Body LoginRequest request);

    @GET("perfilEstudiante")
    Call<Estudiante> getPerfilEstudiante();

    // -------------------------------
    // Test de Kolb
    // -------------------------------
    @GET("kolb/preguntas")
    Call<List<PreguntasKolb>> getPreguntas();

    @POST("kolb/guardarRespuestas")
    Call<KolbResponse> guardarRespuestas(@Body KolbRequest request);

    @GET("kolb/resultado")
    Call<KolbResultado> obtenerResultado();

    @POST("sesion/parada")   Call<ParadaResponse> crearParada(@Body ParadaRequest req);
    @POST("sesion/cerrar")   Call<CerrarResponse> cerrarSesion(@Body CerrarRequest req);
    @POST("movil/simulacro") Call<ParadaResponse> crearSimulacro(@Body SimulacroRequest req);

}
