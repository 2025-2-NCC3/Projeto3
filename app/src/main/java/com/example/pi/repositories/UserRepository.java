package com.example.pi.repositories;

import com.example.pi.models.User;
import com.example.pi.api.ApiClient; // Supondo que você criou o cliente Retrofit
import com.example.pi.api.ApiService;
import io.reactivex.rxjava3.core.Single;

public class UserRepository {

    private ApiService apiService;

    public UserRepository() {
        // Obtenha a instância do serviço Retrofit
        this.apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
    }

    public Single<User> registerUser(String nome, String email, String senha) {
        if (nome == null || email == null || senha == null) {
            // Retorna um erro se os dados forem nulos, evitando NullPointerException
            return Single.error(new IllegalArgumentException("Nome, email e senha não podem ser nulos"));
        }
        User newUser = new User(nome, email, senha);
        return apiService.registerUser(newUser);
    }
}