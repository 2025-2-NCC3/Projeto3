package com.example.pi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

<<<<<<< HEAD:app/src/main/java/com/example/pi/MainActivity.java
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
=======
import com.example.pi.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
>>>>>>> 87378b548a49dbe2ecd64ab8f727207bf5997fc7:app/src/main/java/com/example/pi/activities/LoginActivity.java
        });
    }
}