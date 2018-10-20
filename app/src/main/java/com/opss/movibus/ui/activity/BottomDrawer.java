package com.opss.movibus.ui.activity;

import android.animation.Animator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import com.opss.movibus.R;
import com.opss.movibus.firebase.Firebase;
import com.opss.movibus.location.ReverseGeo;
import com.opss.movibus.model.LinhaFavorita;
import com.opss.movibus.model.Onibus;
import com.opss.movibus.model.PontoFavorito;

import com.opss.movibus.model.PontoOnibus;
import com.opss.movibus.model.Usuario;

public class BottomDrawer implements  View.OnClickListener, View.OnTouchListener, ReverseGeo.OnTaskComplete {
    private MainActivity activity;
    private View bottomDrawer;

    //Firebase
    //private DatabaseReference databaseReference;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private DocumentReference usuarioDocumento;

    //Layout da View
    //Barra superior
    private DrawerLayout mainLayout;
    private TextView identificadorText;
    private ConstraintLayout openClose;
    private ImageView openCloseImage;
    private ImageView favoritoImage;

    //Layout Linha
    private LinearLayout linearLinha;
    private TextView textOrigem;
    private TextView textDestino;
    private TextView textVia;
    private ImageView imgElevador;
    private Button acompanharButton;
    private Button itinerarioButton;

    //Layout Ponto
    private LinearLayout linearPonto;
    private TextView textEndereco;
    private TextView textCoberto;
    private RecyclerView recyclerView;

    List<Observer> observerOnibus = new ArrayList<>();

    //-----------------------------

    private boolean totalmenteAberto;



    private Marker marker;
    private Onibus onibus;
    private PontoOnibus ponto;

    private LinhaFavorita linhaFavorita;
    private PontoFavorito pontoFavorito;

    private float dx;
    private float dy;
    private int lastAction;

    //private int waitMS = 2000;  // The time after which the drawer is closing automatically
    private Handler handlCountDown;
    private static final int DRAWER_UP = 1;
    private static final int DRAWER_DOWN = 0;
    private static int direct;
    private int color;
    private int mainlayoutHeight;
    private int currentDrawer = -1;
    private boolean isSwitched = false;

    private float heightStatusMenu = 200;

    private Handler handClose = new Handler();

    private enum S {OPEN_NOW, OPEN, CLOSE_NOW, CLOSE, CANCELED_NOW, CANCEL, TIME_OFF}     // States of animation
    private S animState = S.CLOSE;

    public BottomDrawer(Activity activity) {
        this.activity = (MainActivity) activity;
        initialize();
        getLayoutHeight();
    }

    private void initialize() {
        //Barra Superior
        bottomDrawer =  activity.findViewById(R.id.bottom_drawer);
        identificadorText = bottomDrawer.findViewById(R.id.identificador);
        openClose = bottomDrawer.findViewById(R.id.barra_open_close);
        openCloseImage = bottomDrawer.findViewById(R.id.img_open_close);
        favoritoImage = bottomDrawer.findViewById(R.id.img_favorito);
        favoritoImage.setImageResource(R.drawable.ic_star_border_black_24dp);

        //Layout Linha
        linearLinha = bottomDrawer.findViewById(R.id.linearLinha);
        textOrigem = bottomDrawer.findViewById(R.id.text_origem);
        textDestino = bottomDrawer.findViewById(R.id.text_destino);
        textVia = bottomDrawer.findViewById(R.id.text_via);
        imgElevador = bottomDrawer.findViewById(R.id.img_elevador);
        acompanharButton = bottomDrawer.findViewById(R.id.acompanharButton);
        itinerarioButton = bottomDrawer.findViewById(R.id.itinerarioButton);
        itinerarioButton.setVisibility(View.GONE);

        //Layout Ponto
        linearPonto = bottomDrawer.findViewById(R.id.linearPonto);
        textEndereco = bottomDrawer.findViewById(R.id.text_endereco);
        textCoberto = bottomDrawer.findViewById(R.id.text_coberto);
        //recyclerView = bottomDrawer.findViewById(R.id.recycler_onibus);

        //Ocultando Layouts
        linearLinha.setVisibility(View.GONE);
        linearPonto.setVisibility(View.GONE);

        handlCountDown = new Handler();

        this.totalmenteAberto = false;

        //Firebase
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //this.databaseReference = FirebaseDatabase.getInstance().getReference();
        //this.databaseReference = databaseReference.child("usuarios").child(userID).child("favoritos");

        this.usuarioDocumento = database.collection(Usuario.COLECAO).document(userID);

        bottomDrawer.setOnClickListener(this);
        openClose.setOnTouchListener(this);

        openCloseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (totalmenteAberto) {
                    if (onibus != null && onibus.getAcompanhando()) {
                        marker.showInfoWindow();
                    }
                    closeAnimate();
                } else {
                    abrirTotalmente();
                }
            }
        });

        this.favoritoImage.setOnClickListener((OnClickListener) -> favoritoAction() );
        this.acompanharButton.setOnClickListener((OnClickListener) -> acompanharOnibus() );
        this.itinerarioButton.setOnClickListener((OnClickListener) -> verItinerario() );

        this.onibus = null;
        this.ponto = null;
    }

    private void favoritoAction() {

        //Favoritando
        if (linhaFavorita == null && pontoFavorito == null) {
            if (onibus != null) {
                //favoritarLinha(onibus);
                favoritar(onibus);
                return;
            }

            if (ponto != null) {
                //favoritarPonto(ponto);
                favoritar(ponto);
                return;
            }
        }

        //Desfavoritar Linha
        if (linhaFavorita != null) {
            desfavoritar(linhaFavorita);
            return;
        }

        //Desfavoritar Ponto
        if (pontoFavorito != null) {
            desfavoritar(pontoFavorito);
            return;
        }

    }

    //FAVORITAR LINHA
    private void favoritar(Onibus onibus) {
        LinhaFavorita linhaFavorita = new LinhaFavorita(onibus.getLinha());

        String id = usuarioDocumento.collection(LinhaFavorita.COLECAO).document().getId();
        linhaFavorita.setId(id);

        usuarioDocumento.collection(LinhaFavorita.COLECAO).document(id).set(linhaFavorita);

        MainActivity.LINHAS_FAVORITAS.put(linhaFavorita.getIdLinha(), linhaFavorita);
        favoritoImage.setImageResource(R.drawable.ic_star_black_24dp);
        this.linhaFavorita = linhaFavorita;
        Toast.makeText(activity, "Linha Favoritada", Toast.LENGTH_LONG).show();
    }
    //FAVORITAR PONTO DE ONIBUS
    private void favoritar(PontoOnibus pontoOnibus) {
        PontoFavorito pontoFavorito = new PontoFavorito(pontoOnibus);

        String id = usuarioDocumento.collection(PontoFavorito.COLECAO).document().getId();
        pontoFavorito.setId(id);

        usuarioDocumento.collection(PontoFavorito.COLECAO).document(id).set(pontoFavorito);

        MainActivity.PONTOS_FAVORITOS.put(pontoFavorito.getIdPonto(), pontoFavorito);
        favoritoImage.setImageResource(R.drawable.ic_star_black_24dp);
        this.pontoFavorito = pontoFavorito;
        Toast.makeText(activity, "Ponto Favoritado", Toast.LENGTH_LONG).show();
    }
    //DESFAVORITAR LINHA
    private void desfavoritar(LinhaFavorita linha) {

        usuarioDocumento.collection(LinhaFavorita.COLECAO).document(linha.getId()).delete();

        Toast.makeText(activity, "Linha Desfavoritada", Toast.LENGTH_LONG).show();
        this.favoritoImage.setImageResource(R.drawable.ic_star_border_black_24dp);
        MainActivity.LINHAS_FAVORITAS.remove(linha.getIdLinha());
        this.linhaFavorita = null;
    }
    //DESFAVORITAR PONTO
    private void desfavoritar(PontoFavorito ponto) {

        usuarioDocumento.collection(PontoFavorito.COLECAO).document(ponto.getId()).delete();

        Toast.makeText(activity, "Linha Desfavoritada", Toast.LENGTH_LONG).show();
        this.favoritoImage.setImageResource(R.drawable.ic_star_border_black_24dp);
        MainActivity.PONTOS_FAVORITOS.remove(ponto.getIdPonto());
        this.pontoFavorito = null;
    }

    public void acompanharOnibus() {

        if (onibus.getAcompanhando()) {
            onibus.setAcompanhando(false);
            marker.hideInfoWindow();
        } else {
            onibus.setAcompanhando(true);
        }

        if (onibus.getAcompanhando()) {
            teste("PARAR ACOMPANHAMENTO", "#e74c3c", acompanharButton);
//            acompanharButton.setText("PARAR ACOMPANHAMENTO");
//            int color = Color.parseColor("#e74c3c");
//            acompanharButton.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC));
        } else {
            teste("INICIAR ACOMPANHAMENTO", "#44bd32", acompanharButton);
//            acompanharButton.setText("INICIAR ACOMPANHAMENTO");
//            int color = Color.parseColor("#44bd32");
//            acompanharButton.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC));
        }

        this.activity.getMapsFragment().acompanharOnibus(onibus);
    }

    private void teste(String texto, String cor, Button button) {
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
                button.setText(texto);
                int color = Color.parseColor(cor);
                button.getBackground().mutate().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC));
//            }
//        });
    }

    public void verItinerario() {
        this.activity.getMapsFragment().verItinerario(onibus);
        this.closeAnimate();
    }

    @Override
    public void onTaskComplete(String result) {
        textEndereco.setText(result);
        ponto.setDescricao(result);
        Firebase.get().getFirePontoOnibus().updateDocument(ponto);
    }

    private void getLayoutHeight() {
        mainLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);

        ViewTreeObserver vto = mainLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mainlayoutHeight = mainLayout.getMeasuredHeight();
                bottomDrawer.setY(mainlayoutHeight);
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//                    mainLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                } else {
//                    mainLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                }
            }
        });
    }

    @Override
    public void onClick(View v) {


    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dx = bottomDrawer.getX() - event.getRawX();
                dy = bottomDrawer.getY() - event.getRawY();
                lastAction = MotionEvent.ACTION_DOWN;
                break;

            case MotionEvent.ACTION_MOVE:
                Log.d("CASSIO", String.valueOf(event.getRawY()+dy));

                if ((event.getRawY()+dy) > mainlayoutHeight/3) {
                    bottomDrawer.setY(event.getRawY() + dy);
                    lastAction = MotionEvent.ACTION_MOVE;
                    totalmenteAberto = true;
                    openCloseImage.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                }

                if ((event.getRawY()+dy) > mainlayoutHeight - (mainlayoutHeight/3)) {
                    totalmenteAberto = false;
                    openCloseImage.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                }

                //if ((event.getRawY()+dy) > mainlayoutHeight - (mainlayoutHeight/10)) {
                if ((event.getRawY()+dy) > mainlayoutHeight - 100) {
                    animState = S.CLOSE;
                    this.isSwitched = false;
                    direct = DRAWER_UP;
                    bottomDrawer.setY(mainlayoutHeight);
                    this.marker.hideInfoWindow();
                }

            case MotionEvent.ACTION_UP:
                if (lastAction == MotionEvent.ACTION_DOWN) {
                    Log.d("CASSIO", "onClick2");
                }
                break;

            default:
                return false;
        }
        return true;
    }



    private void drawerMovement(int movement){
        switch (movement) {
            case DRAWER_UP: // --------------------------------------------------------------------- Drawer UP
                //bottomDrawer.animate().translationY(mainlayoutHeight - heightStatusMenu).setListener(new AnimationListener());
                bottomDrawer.animate().translationY(mainlayoutHeight - (mainlayoutHeight/3)).setListener(new AnimationListener());
                direct = DRAWER_UP;
                break;

            case DRAWER_DOWN: // ------------------------------------------------------------------- Drawer DOWN
                bottomDrawer.animate().translationY(mainlayoutHeight).setListener(new AnimationListener());
                direct = DRAWER_DOWN;
                break;
        }
    }

    private void switchToNewDrawer(int currentDrawer){
        if (isSwitched){
            //refreshData(currentDrawer);
            switchDrawer(currentDrawer);
            isSwitched = false;
        }
    }

    private void abrirTotalmente() {
        totalmenteAberto = true;
        openCloseImage.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
        bottomDrawer.animate().translationY(mainlayoutHeight/3).setListener(new AnimationListener());
        direct = DRAWER_UP;
    }

    public void abrir(Onibus onibus, Marker marker) {
        this.linearPonto.setVisibility(View.GONE);
        this.linearLinha.setVisibility(View.VISIBLE);//Mostrando Layout da Linha
        this.identificadorText.setText(onibus.getLinha().getNome());

        this.totalmenteAberto = false;
        this.onibus = onibus;
        this.marker = marker;

        this.openCloseImage.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
        this.favoritoImage.setImageResource(R.drawable.ic_star_border_black_24dp);

        if (onibus.getAcompanhando()) {
            teste("PARAR ACOMPANHAMENTO", "#e74c3c", acompanharButton);
            //acompanharButton.setText("PARAR ACOMPANHAMENTO");
        } else {
            teste("INICIAR ACOMPANHAMENTO", "#44bd32", acompanharButton);
            //acompanharButton.setText("INICIAR ACOMPANHAMENTO");
        }

        if (onibus.isElevador()) {
            this.imgElevador.setVisibility(View.VISIBLE);
        } else {
            this.imgElevador.setVisibility(View.GONE);
        }

        this.textOrigem.setText(onibus.getLinha().getOrigem());
        this.textDestino.setText(onibus.getLinha().getDestino());

        if (onibus.getLinha().getVia().isEmpty()) {
            this.textVia.setVisibility(View.GONE);
        } else {
            this.textVia.setVisibility(View.VISIBLE);
            this.textVia.setText(onibus.getLinha().getVia());
        }

        switchDrawer(0);
    }

    public void abrir(PontoOnibus ponto, Marker marker) {
        this.linearLinha.setVisibility(View.GONE);
        this.linearPonto.setVisibility(View.VISIBLE);//Mostrando Layout Ponto de Onibus
        this.identificadorText.setText("Ponto");

        this.totalmenteAberto = false;
        this.ponto = ponto;
        this.marker = marker;

        this.openCloseImage.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
        this.favoritoImage.setImageResource(R.drawable.ic_star_border_black_24dp);

        this.textEndereco.setText(ponto.getDescricao());

        if (ponto.isCoberto()) {
            this.textCoberto.setVisibility(View.VISIBLE);
        } else {
            this.textCoberto.setVisibility(View.INVISIBLE);
        }

        LatLng latLng = new LatLng(ponto.getLatitude(), ponto.getLongitude());

        new ReverseGeo(activity, BottomDrawer.this).execute(latLng);

        switchDrawer(0);
    }

    public void setFavorito(LinhaFavorita linhaFavorita) {
        this.linhaFavorita = linhaFavorita;
        favoritoImage.setImageResource(R.drawable.ic_star_black_24dp);
    }

    public void setFavorito(PontoFavorito pontoFavorito) {
        this.pontoFavorito = pontoFavorito;
        favoritoImage.setImageResource(R.drawable.ic_star_black_24dp);
    }

    public void closeAnimate() {
        this.isSwitched = false;
        this.totalmenteAberto = false;
        this.animState = S.CLOSE;
        this.drawerMovement(DRAWER_DOWN);
        limpar();
    }

    public void close() {
        this.isSwitched = false;
        this.totalmenteAberto = false;
        this.animState = S.CLOSE;
        this.bottomDrawer.setY(mainlayoutHeight);
        if (onibus != null && onibus.getAcompanhando()) {
            marker.showInfoWindow();
        } else if (marker != null) {
            marker.hideInfoWindow();
        }
        limpar();
    }

    private Runnable ss = new Runnable() {
        @Override
        public void run() {
            openCloseImage.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
            favoritoImage.setImageResource(R.drawable.ic_star_border_black_24dp);
            linearLinha.setVisibility(View.GONE);
            linearPonto.setVisibility(View.GONE);
        }
    };

    private void closeDrawer2(){
        animState = S.TIME_OFF;
        this.isSwitched = false;
        drawerMovement(DRAWER_DOWN);
        limpar();
        // Turning on the automatic timer closing drawer
        //handlCountDown.postDelayed(closeDrawerTimer, waitMS);
    }

    private void fechar2() {

//        animState = S.CLOSE;
//        totalmenteAberto = false;
//        openCloseImage.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
//        favoritoImage.setImageResource(R.drawable.ic_star_border_black_24dp);
//        marker.hideInfoWindow();
//        limpar();

        animState = S.TIME_OFF;
        this.isSwitched = false;
        drawerMovement(DRAWER_DOWN);
    }

    private void fecharTotalmente2() {
        this.totalmenteAberto = false;
        this.openCloseImage.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
        //this.bottomDrawer.animate().translationY(mainlayoutHeight).setListener(new AnimationListener());
        direct = DRAWER_DOWN;
        //if (marker != null) {
            this.marker.hideInfoWindow();
        //}
        fechar();
        limpar();
    }

    private void fechar() {
        animState = S.CLOSE;
        this.isSwitched = false;
        direct = DRAWER_UP;
        favoritoImage.setImageResource(R.drawable.ic_star_border_black_24dp);
        bottomDrawer.setY(mainlayoutHeight);
    }

    private void limpar() {
        this.marker = null;
        this.onibus = null;
        this.ponto = null;
        this.linhaFavorita = null;
        this.pontoFavorito = null;
    }

    public void switchDrawer(int selectedDrawer){
        switch (animState) {
            case CLOSE: // ------------------------------------------------------------------------- DRAWER UP
                //refreshData(selectedDrawer);
                drawerMovement(DRAWER_UP);
                break;

            case OPEN: // -------------------------------------------------------------------------- DRAWER DOWN
                if (selectedDrawer != currentDrawer){
                    drawerMovement(DRAWER_DOWN);
                    this.isSwitched = true;
                }
                break;

            case OPEN_NOW: // ---------------------------------------------------------------------- DRAWER is OPENING NOW
                if (selectedDrawer != currentDrawer){
                    drawerMovement(DRAWER_DOWN);
                }
                break;

            case CLOSE_NOW: // --------------------------------------------------------------------- DRAWER is CLOSING NOW
                if (selectedDrawer != currentDrawer){
                    drawerMovement(DRAWER_UP);
                }
                break;

            case TIME_OFF: // ---------------------------------------------------------------------- Closing the drawer because time is over
                drawerMovement(DRAWER_DOWN);
                break;
        }
        currentDrawer = selectedDrawer;
    }

    // --------------------------------------------------------------------------------------------- Changing the information on the drawer
    private void refreshDataS(int currentDrawer){
        switch (currentDrawer) {
            case 1: // Drawer 1
                //color = R.color.color_1;
                break;
            case 2: // Drawer 2
                //color = R.color.color_2;
                break;
            case 3: // Drawer 3
                //color = R.color.color_3;
                break;
        }
        bottomDrawer.setBackgroundColor(ContextCompat.getColor(activity, color));
        //String drawerDescr = "Drawer " + Integer.toString(currentDrawer);
        //drawerTxt.setText(drawerDescr);
    }

    //----------------------------------------------------------------

    private Runnable closeDrawerTimer = new Runnable() {
        @Override
        public void run() {
            closeAnimate();
        }
    };

    private class AnimationListener implements  Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {
            if ((direct == DRAWER_UP) && (animState != S.CANCELED_NOW) && (animState != S.CANCEL)  && (animState != S.TIME_OFF)) animState = S.OPEN_NOW;
            if ((direct == DRAWER_DOWN) && (animState != S.CANCELED_NOW) && (animState != S.CANCEL)  && (animState != S.TIME_OFF)) animState = S.CLOSE_NOW;

            // Turning off the automatic timer closing drawer
            handlCountDown.removeCallbacks(closeDrawerTimer);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if ((direct == DRAWER_UP) && (animState != S.CANCELED_NOW) && (animState != S.CANCEL)){
                animState = S.OPEN;

                // Turning on the automatic timer closing drawer
                //handlCountDown.postDelayed(closeDrawerTimer, waitMS);
            }
            if ((direct == DRAWER_DOWN) && (animState != S.CANCELED_NOW) && (animState != S.CANCEL)) animState = S.CLOSE;
            Log.d("Test", "End Animation: " + animState);

            // Animation Cancel
            if (animState == S.CANCELED_NOW){
                if (direct == DRAWER_UP){
                    Log.d("Test", "Animation Cancel - DIRECT UP: " + animState);
                    drawerMovement(DRAWER_DOWN);
                    animState = S.CANCEL;
                }else { // DIRECT DOWN
                    Log.d("Test", "Animation Cancel - DIRECT DOWN: " + animState);
                    animState = S.CANCEL;
                }
            }

            if ((animState != S.CANCELED_NOW) && (animState != S.CANCEL) && (animState != S.TIME_OFF))
                switchToNewDrawer(currentDrawer);

            // Close Drawer after animation cancel
            if (animState == S.CANCEL){
                //if (animState == S.CLOSE)
                    //refreshData(currentDrawer);
                animState = S.OPEN_NOW;
                drawerMovement(DRAWER_UP);
                Log.d("Test", "Animation Cancel");
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            animState = S.CANCELED_NOW;
            isSwitched = true;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {}
    }
}
