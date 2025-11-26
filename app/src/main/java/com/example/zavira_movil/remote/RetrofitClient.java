package com.example.zavira_movil.remote;

import android.content.Context;

import com.example.zavira_movil.local.TokenManager;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    //  URL NGROK ACTUALIZADA (con soporte OpenAI/IA)
    private static final String BASE_URL = "https://gillian-semiluminous-blubberingly.ngrok-free.dev/";

    // DEBUG LOCAL (comentado hasta resolver firewall):
    // private static final String BASE_URL = "http://192.168.1.5:3333/";
    
    // EMULADOR: Descomentar si usas emulador
    // private static final String BASE_URL = "http://10.0.2.2:3333/";
    
    // DISPOSITIVO FÍSICO: Descomenta y reemplaza con IP de tu PC
    // private static final String BASE_URL = "http://192.168.X.X:3333/";
    

    private static Retrofit retrofit;
    private static Context appContext; // para leer el token

    private RetrofitClient() {}

    /** Llama esto una vez (por ejemplo en Application o en tu primera Activity) */
    public static void init(Context context) {
        if (context != null) appContext = context.getApplicationContext();
    }

    /** Compatibilidad: permite usar getInstance(this) como tú lo estabas haciendo */
    public static Retrofit getInstance(Context context) {
        init(context);
        return getInstance();
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
        // Interceptor de logs (útil en desarrollo)
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Interceptor para Authorization
        Interceptor authInterceptor = chain -> {
            Request original = chain.request();
            Request.Builder builder = original.newBuilder();

            if (appContext != null) {
                String token = TokenManager.getToken(appContext);
                if (token != null && !token.trim().isEmpty()) {
                    builder.header("Authorization", "Bearer " + token);
                }
            }
            // JSON por defecto
            builder.header("Accept", "application/json");
            builder.header("Content-Type", "application/json");

            return chain.proceed(builder.build());
        };

        // Interceptor que detecta 401 y realiza logout centralizado (best-effort)
        Interceptor sessionInterceptor = chain -> {
            okhttp3.Response response = chain.proceed(chain.request());
            try {
                if (response.code() == 401 && appContext != null) {
                    // Limpieza local del token
                    com.example.zavira_movil.local.TokenManager.clearAll(appContext);
                    // Enviar broadcast para que Activities/Fragments puedan reaccionar
                    // Intent explícito al BroadcastReceiver local (evita problema con receiver no exportado)
                    android.content.Intent intent = new android.content.Intent(appContext, com.example.zavira_movil.SessionExpiredReceiver.class);
                    intent.setAction("com.example.zavira_movil.ACTION_SESSION_EXPIRED");
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                    appContext.sendBroadcast(intent);
                }
            } catch (Exception ignored) {
            }
            return response;
        };

        // Network interceptor para capturar X-Request-Id de las respuestas y guardarlo en SharedPreferences
        Interceptor responseIdInterceptor = chain -> {
            okhttp3.Response response = chain.proceed(chain.request());
            try {
                if (appContext != null) {
                    String requestId = response.header("X-Request-Id");
                    if (requestId != null && !requestId.trim().isEmpty()) {
                        android.util.Log.d("RetrofitClient", "X-Request-Id: " + requestId);
                        android.content.SharedPreferences prefs = appContext.getSharedPreferences("api_prefs", Context.MODE_PRIVATE);
                        prefs.edit().putString("last_request_id", requestId).apply();
                    }
                }
            } catch (Exception ignored) {
            }
            return response;
        };

        OkHttpClient ok = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .addInterceptor(sessionInterceptor)
                .addNetworkInterceptor(responseIdInterceptor)
                .addInterceptor(log)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(ok)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /** Permite cambiar la URL base en tiempo de ejecución; reconstrye Retrofit si cambia */
    public static synchronized void setBaseUrl(String url) {
        if (url == null || url.trim().isEmpty()) return;
        String normalized = url.trim();
        if (!normalized.endsWith("/")) normalized = normalized + "/";
        if (!normalized.equals(baseUrl)) {
            baseUrl = normalized;
            // invalidar instancia para forzar rebuild con nueva URL
            retrofit = null;
        }
    }

    public static String getBaseUrl() {
        return baseUrl;
    }
}