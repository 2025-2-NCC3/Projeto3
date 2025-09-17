package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class CardapioAlunosActivity extends AppCompatActivity {

    Button botaoVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardapio);

        Produto produto1 = new Produto("ABC123", "Coxinha", "Coxinha recheada de frango.", 1.99, 10, 1);

        botaoVoltar = findViewById(R.id.botaoVoltar);

        botaoVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(CardapioAlunosActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        int instancias = Produto.getNumeroInstancias();

        for (int i = 1; i <= instancias; i++) {

        }
    }
}