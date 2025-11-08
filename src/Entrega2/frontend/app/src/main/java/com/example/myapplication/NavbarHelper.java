package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class NavbarHelper {
    private static final String TAG = "NavbarHelper";

    public static void setupNavbar(Activity activity, String currentScreen) {
        View navbar = activity.findViewById(R.id.includeNavbar);
        if (navbar == null) {
            Log.e(TAG, "Navbar não encontrada!");
            return;
        }

        // Buscar os FrameLayouts
        FrameLayout navCardapio = navbar.findViewById(R.id.navCardapio);
        FrameLayout navHistorico = navbar.findViewById(R.id.navHistorico);
        FrameLayout navCarrinho = navbar.findViewById(R.id.navCarrinho);
        FrameLayout navPerfil = navbar.findViewById(R.id.navPerfil);

        // Buscar os indicadores
        View indicatorCardapio = navbar.findViewById(R.id.indicatorCardapio);
        View indicatorHistorico = navbar.findViewById(R.id.indicatorHistorico);
        View indicatorCarrinho = navbar.findViewById(R.id.indicatorCarrinho);
        View indicatorPerfil = navbar.findViewById(R.id.indicatorPerfil);

        // Buscar os ícones
        ImageView iconCardapio = navbar.findViewById(R.id.iconCardapio);
        ImageView iconHistorico = navbar.findViewById(R.id.iconHistorico);
        ImageView iconCarrinho = navbar.findViewById(R.id.iconCarrinho);
        ImageView iconPerfil = navbar.findViewById(R.id.iconPerfil);

        if (navCardapio == null || navHistorico == null ||
                navCarrinho == null || navPerfil == null) {
            Log.e(TAG, "Elementos da navbar não encontrados!");
            return;
        }

        // Resetar todos os indicadores e ícones
        resetNavbar(indicatorCardapio, indicatorHistorico, indicatorCarrinho, indicatorPerfil,
                iconCardapio, iconHistorico, iconCarrinho, iconPerfil);

        // Destacar o item atual
        highlightCurrentScreen(currentScreen,
                indicatorCardapio, indicatorHistorico, indicatorCarrinho, indicatorPerfil,
                iconCardapio, iconHistorico, iconCarrinho, iconPerfil);

        // Configurar cliques
        navCardapio.setOnClickListener(v -> {
            if (!currentScreen.equals("cardapio")) {
                Intent intent = new Intent(activity, CardapioAlunosActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
            }
        });

        navHistorico.setOnClickListener(v -> {
            if (!currentScreen.equals("historico")) {
                Intent intent = new Intent(activity, MeusPedidosActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
            }
        });

        navCarrinho.setOnClickListener(v -> {
            if (!currentScreen.equals("carrinho")) {
                Intent intent = new Intent(activity, CarrinhoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
            }
        });

        navPerfil.setOnClickListener(v -> {
            if (!currentScreen.equals("perfil")) {
                Intent intent = new Intent(activity, PerfilActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
            }
        });
    }

    private static void resetNavbar(View... views) {
        for (View view : views) {
            if (view != null) {
                if (view.getId() == R.id.indicatorCardapio ||
                        view.getId() == R.id.indicatorHistorico ||
                        view.getId() == R.id.indicatorCarrinho ||
                        view.getId() == R.id.indicatorPerfil) {
                    // Esconder indicadores
                    view.setVisibility(View.GONE);
                } else {
                    // Diminuir opacidade dos ícones
                    view.setAlpha(0.5f);
                }
            }
        }
    }

    private static void highlightCurrentScreen(String currentScreen,
                                               View indicatorCardapio, View indicatorHistorico,
                                               View indicatorCarrinho, View indicatorPerfil,
                                               ImageView iconCardapio, ImageView iconHistorico,
                                               ImageView iconCarrinho, ImageView iconPerfil) {
        switch (currentScreen) {
            case "cardapio":
                if (indicatorCardapio != null) indicatorCardapio.setVisibility(View.VISIBLE);
                if (iconCardapio != null) iconCardapio.setAlpha(1.0f);
                break;
            case "historico":
                if (indicatorHistorico != null) indicatorHistorico.setVisibility(View.VISIBLE);
                if (iconHistorico != null) iconHistorico.setAlpha(1.0f);
                break;
            case "carrinho":
                if (indicatorCarrinho != null) indicatorCarrinho.setVisibility(View.VISIBLE);
                if (iconCarrinho != null) iconCarrinho.setAlpha(1.0f);
                break;
            case "perfil":
                if (indicatorPerfil != null) indicatorPerfil.setVisibility(View.VISIBLE);
                if (iconPerfil != null) iconPerfil.setAlpha(1.0f);
                break;
        }
    }
}