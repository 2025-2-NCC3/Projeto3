package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class NavbarHelper {
    private static final String TAG = "NavbarHelper";

    public static void setupNavbar(Activity activity, String currentScreen) {
        View navbar = activity.findViewById(R.id.includeNavbar);
        if (navbar == null) {
            Log.e(TAG, "Navbar não encontrada!");
            return;
        }

        Button btnNavCardapio = navbar.findViewById(R.id.btnNavCardapio);
        Button btnNavHistorico = navbar.findViewById(R.id.btnNavHistorico);
        Button btnNavCarrinho = navbar.findViewById(R.id.btnNavCarrinho);
        Button btnNavPerfil = navbar.findViewById(R.id.btnNavPerfil);

        if (btnNavCardapio == null || btnNavHistorico == null ||
                btnNavCarrinho == null || btnNavPerfil == null) {
            Log.e(TAG, "Botões da navbar não encontrados!");
            return;
        }

        resetButtons(btnNavCardapio, btnNavHistorico, btnNavCarrinho, btnNavPerfil);
        highlightCurrentButton(currentScreen, btnNavCardapio, btnNavHistorico, btnNavCarrinho, btnNavPerfil);

        btnNavCardapio.setOnClickListener(v -> {
            if (!currentScreen.equals("cardapio")) {
                Intent intent = new Intent(activity, CardapioAlunosActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
            }
        });

        btnNavHistorico.setOnClickListener(v -> {
            if (!currentScreen.equals("historico")) {
                Toast.makeText(activity, "Tela de Histórico em desenvolvimento", Toast.LENGTH_SHORT).show();
            }
        });

        btnNavCarrinho.setOnClickListener(v -> {
            if (!currentScreen.equals("carrinho")) {
                Intent intent = new Intent(activity, CarrinhoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
            }
        });

        btnNavPerfil.setOnClickListener(v -> {
            if (!currentScreen.equals("perfil")) {
                Intent intent = new Intent(activity, PerfilActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
            }
        });
    }

    private static void resetButtons(Button... buttons) {
        for (Button btn : buttons) {
            if (btn != null) btn.setAlpha(0.6f);
        }
    }

    private static void highlightCurrentButton(String currentScreen, Button btnCardapio,
                                               Button btnHistorico, Button btnCarrinho, Button btnPerfil) {
        switch (currentScreen) {
            case "cardapio":
                if (btnCardapio != null) btnCardapio.setAlpha(1.0f);
                break;
            case "historico":
                if (btnHistorico != null) btnHistorico.setAlpha(1.0f);
                break;
            case "carrinho":
                if (btnCarrinho != null) btnCarrinho.setAlpha(1.0f);
                break;
            case "perfil":
                if (btnPerfil != null) btnPerfil.setAlpha(1.0f);
                break;
        }
    }
}