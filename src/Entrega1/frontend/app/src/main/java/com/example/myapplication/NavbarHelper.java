package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.Toast;

public class NavbarHelper {
    private static final String TAG = "NavbarHelper";

    // Cores
    private static final int COLOR_SELECTED = Color.parseColor("#557B56");
    private static final int COLOR_UNSELECTED = Color.parseColor("#F5F5DB");

    public static void setupNavbar(Activity activity, String currentScreen) {
        // Encontrar os FrameLayouts clicáveis
        FrameLayout navCardapio = activity.findViewById(R.id.navCardapio);
        FrameLayout navHistorico = activity.findViewById(R.id.navHistorico);
        FrameLayout navCarrinho = activity.findViewById(R.id.navCarrinho);
        FrameLayout navPerfil = activity.findViewById(R.id.navPerfil);

        if (navCardapio == null || navHistorico == null ||
                navCarrinho == null || navPerfil == null) {
            Log.e(TAG, "Itens da navbar não encontrados!");
            return;
        }

        // Resetar todos e destacar o atual
        resetAllNavItems(activity);
        highlightCurrentNavItem(activity, currentScreen);

        // Configurar listeners
        navCardapio.setOnClickListener(v -> {
            if (!currentScreen.equals("cardapio")) {
                Intent intent = new Intent(activity, CardapioAlunosActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
            }
        });

        navHistorico.setOnClickListener(v -> {
            if (!currentScreen.equals("historico")) {
                Toast.makeText(activity, "Tela de Histórico em desenvolvimento", Toast.LENGTH_SHORT).show();
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

    private static void resetAllNavItems(Activity activity) {
        setNavItemStyle(activity, R.id.iconCardapio, R.id.indicatorCardapio, false);
        setNavItemStyle(activity, R.id.iconHistorico, R.id.indicatorHistorico, false);
        setNavItemStyle(activity, R.id.iconCarrinho, R.id.indicatorCarrinho, false);
        setNavItemStyle(activity, R.id.iconPerfil, R.id.indicatorPerfil, false);
    }

    private static void highlightCurrentNavItem(Activity activity, String currentScreen) {
        switch (currentScreen) {
            case "cardapio":
                setNavItemStyle(activity, R.id.iconCardapio, R.id.indicatorCardapio, true);
                break;
            case "historico":
                setNavItemStyle(activity, R.id.iconHistorico, R.id.indicatorHistorico, true);
                break;
            case "carrinho":
                setNavItemStyle(activity, R.id.iconCarrinho, R.id.indicatorCarrinho, true);
                break;
            case "perfil":
                setNavItemStyle(activity, R.id.iconPerfil, R.id.indicatorPerfil, true);
                break;
        }
    }

    private static void setNavItemStyle(Activity activity, int iconId, int indicatorId, boolean isSelected) {
        ImageView icon = activity.findViewById(iconId);
        View indicator = activity.findViewById(indicatorId);

        if (icon != null && indicator != null) {
            int color = isSelected ? COLOR_SELECTED : COLOR_UNSELECTED;

            // Definir cor do ícone
            icon.setColorFilter(color);

            // Mostrar/esconder indicador
            indicator.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        }
    }
}