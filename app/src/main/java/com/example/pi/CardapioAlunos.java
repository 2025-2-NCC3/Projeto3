package com.example.pi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class CardapioAlunos extends AppCompatActivity {

    Button botaoVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cardapio_alunos);

        botaoVoltar = findViewById(R.id.botaoVoltar);

        botaoVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(CardapioAlunos.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}