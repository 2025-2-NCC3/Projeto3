package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

public class NavbarHelper {

    public static void setupNavbar(Activity activity, String currentScreen) {
        View navbar = activity.findViewById(R.id.includeNavbar);
        if (navbar == null) return;

        Button btnNavCardapio = navbar.findViewById(R.id.btnNavCardapio);
        Button btnNavHistorico = navbar.findViewById(R.id.btnNavHistorico);
        Button btnNavCarrinho = navbar.findViewById(R.id.btnNavCarrinho);
        Button btnNavPerfil = navbar.findViewById(R.id.btnNavPerfil);

        // Destacar botão da tela atual
        resetButtons(btnNavCardapio, btnNavHistorico, btnNavCarrinho, btnNavPerfil);
        highlightCurrentButton(currentScreen, btnNavCardapio, btnNavHistorico, btnNavCarrinho, btnNavPerfil);

        // Navegação - Cardápio
        btnNavCardapio.setOnClickListener(v -> {
            if (!currentScreen.equals("cardapio")) {
                Intent intent = new Intent(activity, CardapioAlunosActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
            }
        });

        // Navegação - Histórico
        btnNavHistorico.setOnClickListener(v -> {
            if (!currentScreen.equals("historico")) {
                // IMPORTANTE: Crie essa Activity depois
                // Intent intent = new Intent(activity, HistoricoActivity.class);
                // intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                // activity.startActivity(intent);

                // Por enquanto, mostre uma mensagem
                android.widget.Toast.makeText(activity, "Tela de Histórico em desenvolvimento", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // Navegação - Carrinho (já existe)
        btnNavCarrinho.setOnClickListener(v -> {
            if (!currentScreen.equals("carrinho")) {
                Intent intent = new Intent(activity, CarrinhoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
            }
        });

        // Navegação - Perfil (já existe)
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
            btn.setAlpha(0.6f);
        }
    }

    private static void highlightCurrentButton(String currentScreen, Button btnCardapio,
                                               Button btnHistorico, Button btnCarrinho, Button btnPerfil) {
        switch (currentScreen) {
            case "cardapio":
                btnCardapio.setAlpha(1.0f);
                break;
            case "historico":
                btnHistorico.setAlpha(1.0f);
                break;
            case "carrinho":
                btnCarrinho.setAlpha(1.0f);
                break;
            case "perfil":
                btnPerfil.setAlpha(1.0f);
                break;
        }
    }
}