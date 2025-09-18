package com.example.myapplication; // Verifique se este é o seu pacote

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Inicializa o Firebase aqui, uma única vez para todo o app
        FirebaseApp.initializeApp(this);
    }
}