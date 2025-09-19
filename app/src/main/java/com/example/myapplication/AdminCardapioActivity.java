package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class AdminCardapioActivity extends AppCompatActivity {
private Produto produto;
    Button ButtonAddAC;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_cardapio);



        Button ButtonAddAC = findViewById(R.id.ButtonAddAC);
        EditText EditTextNomeProduto = findViewById(R.id.EditTextNomeProduto);
        EditText EditTextValorProduto = findViewById(R.id.EditTextValorProduto);
        EditText EditTextDetalhesProduto = findViewById(R.id.EditTextDetalhesProduto);

        ButtonAddAC.setOnClickListener(v -> {
            String nome = EditTextNomeProduto.getText().toString();
            String valorStr = EditTextValorProduto.getText().toString();
            String detalhes = EditTextDetalhesProduto.getText().toString();
        });

    }
}