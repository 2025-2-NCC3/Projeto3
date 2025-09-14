package com.example.pi.api;

import com.example.pi.models.User;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("users") // O caminho do endpoint, ex: https://sua-api.com/users
    Single<User> registerUser(@Body User user);
}