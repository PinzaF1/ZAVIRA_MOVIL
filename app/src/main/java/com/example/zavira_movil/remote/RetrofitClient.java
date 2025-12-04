package com.example.zavira_movil.remote;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.example.zavira_movil.local.TokenManager;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    private static final String TAG = "RetrofitClient";
    private static final String BASE_URL = "https://eduexce-backend.ddns.net/";

    // EMULADOR: Descomentar si usas emulador
    // private static final String BASE_URL = "http://10.0.2.2:3333/";

    // DISPOSITIVO FÍSICO: Descomenta y reemplaza con IP de tu PC
    // private static final String BASE_URL = "http://192.168.X.X:3333/";

    private static Retrofit retrofit;
    private static Context appContext;
    private static String baseUrl = BASE_URL;

    private RetrofitClient() {}

    /** Llama esto una vez (por ejemplo en Application o en tu primera Activity) */
    public static void init(Context context) {
        if (context != null) {
            appContext = context.getApplicationContext();
            Log.d(TAG, "init: context inicializado");
        } else {
            Log.w(TAG, "init: se recibió context nulo");
        }
    }

    /** Usar cuando ya llamaste init(Context) antes */
    public static synchronized Retrofit getInstance() {
        if (retrofit == null) {
            buildRetrofit();
        }
        return retrofit;
    }

    /** Construye el cliente Retrofit con la URL actual */
    private static synchronized void buildRetrofit() {
        Log.d(TAG, "buildRetrofit: construyendo retrofit (baseUrl=" + baseUrl + ")");

        // Interceptor de logs (útil en desarrollo)
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Interceptor para Authorization
        Interceptor authInterceptor = chain -> {
            Request original = chain.request();
            Request.Builder builder = original.newBuilder();

            if (appContext != null) {
                try {
                    String token = TokenManager.getToken(appContext);
                    if (token != null && !token.trim().isEmpty()) {
                        builder.header("Authorization", "Bearer " + token);
                        Log.d(TAG, "authInterceptor: token presente (length=" + token.length() + ")");
                    } else {
                        Log.d(TAG, "authInterceptor: token ausente o vacío");
                    }
                } catch (Exception e) {
                    Log.w(TAG, "authInterceptor: error al obtener token: " + e.getMessage());
                }
            } else {
                Log.w(TAG, "authInterceptor: appContext es nulo, no se agrega Authorization");
            }

            // Headers por defecto
            builder.header("Accept", "application/json");
            builder.header("Content-Type", "application/json");

            Request req = builder.build();
            Log.d(TAG, "authInterceptor: request -> " + req.method() + " " + req.url());
            return chain.proceed(req);
        };

        // Interceptor para capturar X-Request-Id de las respuestas
        Interceptor responseIdInterceptor = chain -> {
            Request request = chain.request();
            okhttp3.Response response = chain.proceed(request);

            try {
                String requestId = response.header("X-Request-Id");
                if (requestId != null && appContext != null) {
                    Log.d(TAG, "responseIdInterceptor: X-Request-Id=" + requestId + " for " + request.url());
                    // Guardar en SharedPreferences si es necesario
                    appContext.getSharedPreferences("api_logs", Context.MODE_PRIVATE)
                            .edit()
                            .putString("last_request_id", requestId)
                            .apply();
                } else if (requestId == null) {
                    Log.d(TAG, "responseIdInterceptor: no se encontró X-Request-Id en respuesta para " + request.url());
                }
            } catch (Exception e) {
                Log.w(TAG, "responseIdInterceptor: excepción: " + e.getMessage());
            }

            return response;
        };

        // Interceptor para manejar sesiones expiradas (401)
        Interceptor sessionInterceptor = chain -> {
            Request request = chain.request();
            okhttp3.Response response = chain.proceed(request);

            int code = response.code();
            Log.d(TAG, "sessionInterceptor: response code=" + code + " for " + request.url());

            if (code == 401 && appContext != null) {
                Log.i(TAG, "sessionInterceptor: 401 detectado, limpiando token y enviando broadcast");
                try {
                    TokenManager.clearAll(appContext);

                    // Enviar broadcast explícito para que Activities/Fragments puedan reaccionar
                    Intent intent = new Intent("com.example.zavira_movil.ACTION_SESSION_EXPIRED");
                    intent.setPackage(appContext.getPackageName()); // Hacer el intent explícito
                    appContext.sendBroadcast(intent);
                    Log.d(TAG, "sessionInterceptor: broadcast enviado");
                } catch (Exception e) {
                    Log.w(TAG, "sessionInterceptor: error al limpiar token o enviar broadcast: " + e.getMessage());
                }
            }

            return response;
        };

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(log)
                .addInterceptor(authInterceptor)
                .addNetworkInterceptor(responseIdInterceptor)
                .addInterceptor(sessionInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Log.d(TAG, "buildRetrofit: retrofit construido exitosamente");
    }

    public static synchronized void setBaseUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            Log.w(TAG, "setBaseUrl: URL nula o vacía, ignorando");
            return;
        }

        String normalized = url.trim();
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }

        baseUrl = normalized;
        // Invalidar instancia para forzar rebuild con nueva URL
        retrofit = null;
        Log.d(TAG, "setBaseUrl: nueva URL configurada: " + baseUrl);
    }

    public static String getBaseUrl() {
        return baseUrl;
    }
}

