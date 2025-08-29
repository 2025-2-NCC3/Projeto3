package com.example.pi.api;

import com.example.pi.models.Order;
import com.example.pi.api.OrderRequest;
import com.example.pi.api.OrderResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/orders")
    Call<OrderResponse> createOrder(@Body OrderRequest orderRequest);
}