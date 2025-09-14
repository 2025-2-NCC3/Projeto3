package com.example.pi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.example.pi.R;
import com.example.pi.models.Product;

public class CardapioAlunos extends AppCompatActivity {

    Button botaoVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardapio);

        Product produto1 = new Product("ABC123", "Coxinha", "Coxinha recheada de frango.", 1.99, 10, "Salgados");

        botaoVoltar = findViewById(R.id.botaoVoltar);

        botaoVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(CardapioAlunos.this, MainActivity.class);
                startActivity(intent);
            }
        });

        int instancias = Product.getNumInstancias();

        for (int i = 1; i <= instancias; i++) {

        }
    }
}