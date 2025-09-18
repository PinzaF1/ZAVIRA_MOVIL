package com.example.zavira_movil.remote;

import android.content.Context;

import com.example.zavira_movil.local.TokenManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // IMPORTANTE: barra final, porque en ApiService usas rutas con "/" inicial
    private static final String BASE_URL = "https://zavira-backend.onrender.com/";
    private static Retrofit retrofit;
   // HOLAAAAAAAAAAAAAAAAAAAAAAAAAA
    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            // Interceptor de logging (opcional)
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    // Pasa un TokenProvider (función) que lee SIEMPRE el token actual usando el Context
                    .addInterceptor(new AuthInterceptor(() -> TokenManager.getToken(context)))
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}