package com.buraconet.buraconet;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

public class Ferramentas {

    public static final Integer ZOOM_CAMERA = 17;

    public static final Integer INTENSIDADE_BURACO_CORTE = 18;
    public static final Integer VELOCIDADE_CORTE = 20;

    public static final String MOSTRAR_SIM = "S";
    public static final String MOSTRAR_NAO = "N";

    // esconde o teclado
    public static void tiraTeclado(Activity activity) {
        View view = activity.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // configura a tela padrão do app
    public static void configuraTela(Activity tela) {
        Window window = tela.getWindow();
        // não faz transição na abertura das telas
        window.setWindowAnimations(0);
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        if (SDK_INT >= LOLLIPOP) {
            window.setStatusBarColor(tela.getResources().getColor(R.color.ColorPrimaryDark));
        }
    }

    public static boolean verificaInternet(Context contexto) {
        ConnectivityManager cm = (ConnectivityManager) contexto.getSystemService(CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

}