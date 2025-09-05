package com.example.pi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private Button buttonTeste;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Layout da primeira tela
        buttonTeste = findViewById(R.id.buttonTeste);

        buttonTeste.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                Intent irParaSegundaTela = new Intent(MainActivity.this, AddProductActivity.class);
                startActivity(irParaSegundaTela);
            }
        });
    }
}