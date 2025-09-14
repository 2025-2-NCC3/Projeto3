 package com.example.pi.api;

// com.example.pi.BuildConfig;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://api.comedoriatia.com.br/v1/";


    private static Retrofit retrofit = null;

    /**
     * Método público e estático para obter a instância única do Retrofit.
     * A criação é "lazy", ou seja, só acontece na primeira vez que o método é chamado.
     */
public static Retrofit getRetrofitInstance() {
    if (retrofit == null) {




        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        /*if (BuildConfig.DEBUG) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }*/


        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS) // Tempo para estabelecer a conexão
                .readTimeout(30, TimeUnit.SECONDS)    // Tempo para ler os dados da resposta
                .writeTimeout(30, TimeUnit.SECONDS)   // Tempo para enviar os dados da requisição
                .build();


        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL) // Define a URL base
                .client(okHttpClient) // Define o cliente HTTP customizado
                .addConverterFactory(GsonConverterFactory.create()) // Adiciona o conversor de JSON (Gson)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create()) // Adiciona o adaptador para RxJava
                .build();
    }
    return retrofit;
}
}
