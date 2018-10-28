package com.opss.movibus.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.opss.movibus.R;
import com.opss.movibus.firebase.Firebase;
import com.opss.movibus.model.Favorito;
import com.opss.movibus.model.Linha;
import com.opss.movibus.model.LinhaFavorita;
import com.opss.movibus.model.PontoFavorito;
import com.opss.movibus.model.PontoOnibus;
import com.opss.movibus.model.Usuario;
import com.opss.movibus.ui.fragment.MapsFragment;
import com.opss.movibus.util.ImagemUtils;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Intent intent;

    public static BottomDrawer appDrawer;//Menu inferior

    private FragmentManager fragmentManager;//Gerenciador de Fragmentos
    private MapsFragment mapsFragment;//Fragment do GoogleMaps

    public static final int REQUEST_FOVORITO_SELECIONADO = 1;
    public static final int REQUEST_PESQUISA_SELECIONADO = 2;
    public static final int REQUEST_LINHA_FAVORITA_SELECIONADO = 3;
    public static final int REQUEST_PONTO_FAVORITO_SELECIONADO = 4;

    private static List<Favorito> FAVORITOS;

    public static Map<String, Favorito> FAVORITOS_MAP;

    public static Map<String, LinhaFavorita> LINHAS_FAVORITAS;
    public static Map<String, PontoFavorito> PONTOS_FAVORITOS;

    public static Usuario USUARIO_LOGADO;
    public static ViewHolder vh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vh = new ViewHolder();

        //Inicia o Menu Inferior
        appDrawer = new BottomDrawer(MainActivity.this);

        //Instanciando o Gerenciado de Fragmentos
        fragmentManager = getSupportFragmentManager();
        mapsFragment = new MapsFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.frame_google_maps, mapsFragment);
        transaction.commitAllowingStateLoss();

        FAVORITOS = new ArrayList<>();
        FAVORITOS_MAP = new HashMap<>();
        LINHAS_FAVORITAS = new HashMap<>();
        PONTOS_FAVORITOS = new HashMap<>();

        mostrarDados();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (vh.toolbarPrincipal.getVisibility() == View.VISIBLE) {
            getMenuInflater().inflate(R.menu.main, menu);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        String id;
        Linha linha = null;
        PontoOnibus ponto = null;
        LinhaFavorita linhaFavorita = null;
        PontoFavorito pontoFavorito = null;

        switch (resultCode) {
            case REQUEST_FOVORITO_SELECIONADO:
                id = data.getStringExtra("favorito");

                linhaFavorita= MapsFragment.COLLECTIONS.getLinhaFavorita(id);
                if (linhaFavorita != null) {
                    mostrarToolbarFiltro();
                    vh.txtFiltro.setText("Linha " + linhaFavorita.getNome());
                    mapsFragment.activityResultLinha(id);
                }

                pontoFavorito = MapsFragment.COLLECTIONS.getPontoFavorito(id);
                if (pontoFavorito != null) {
                    mapsFragment.activityResultPontoOnibus(id);
                }
                break;

            case REQUEST_PESQUISA_SELECIONADO:
                id = data.getStringExtra("pesquisa");

                linha = MapsFragment.COLLECTIONS.getLinha(id);
                if (linha != null) {
                    mostrarToolbarFiltro();
                    vh.txtFiltro.setText("Linha " + linha.getNome());
                    mapsFragment.activityResultLinha(id);
                }

                ponto = MapsFragment.COLLECTIONS.getPontoOnibus(id);
                if (ponto != null) {
                    mapsFragment.activityResultPontoOnibus(id);
                }
                break;

            case REQUEST_LINHA_FAVORITA_SELECIONADO:
                id = data.getStringExtra("linha_favorita");
                linhaFavorita= MapsFragment.COLLECTIONS.getLinhaFavorita(id);
                if (linhaFavorita != null) {
                    mostrarToolbarFiltro();
                    vh.txtFiltro.setText("Linha " + linhaFavorita.getNome());
                    mapsFragment.activityResultLinha(id);
                }
                break;

            case REQUEST_PONTO_FAVORITO_SELECIONADO:
                id = data.getStringExtra("ponto_favorito");
                pontoFavorito = MapsFragment.COLLECTIONS.getPontoFavorito(id);
                if (pontoFavorito != null) {
                    mapsFragment.activityResultPontoOnibus(id);
                }
                break;
        }

    }

    @Override
    public boolean onSupportNavigateUp() {

        if (vh.toolbarPesquisa.getVisibility() == View.GONE) {
            vh.drawerLayout.openDrawer(Gravity.START);
            return false;
        }

        vh.toolbarPrincipal.setVisibility(View.VISIBLE);
        vh.toolbarPesquisa.setVisibility(View.GONE);
        setSupportActionBar(vh.toolbarPrincipal);

        mapsFragment.mostrarTodosOsOnibus();

        //ativar setinho de voltar
        // getSupportActionBar().setDisplayShowHomeEnabled(false);
        //getSupportActionBar().setHomeButtonEnabled(false);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        return true;
    }

    public void abrirTelaPesquisar(View view) {
        appDrawer.close();
        this.intent = new Intent(this, PesquisarActivity.class);
        startActivityForResult(intent, REQUEST_PESQUISA_SELECIONADO);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        appDrawer.close();

        int id = item.getItemId();

        if (id == R.id.action_favoritos) {
            this.intent = new Intent(this, FavoritosActivity.class);
            startActivityForResult(intent, REQUEST_FOVORITO_SELECIONADO);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mapsFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_linhas:
                this.intent = new Intent(this, LinhasFavoritasActivity.class);
                startActivityForResult(intent, REQUEST_LINHA_FAVORITA_SELECIONADO);
                break;

            case R.id.nav_pontos:
                this.intent = new Intent(this, PontosFavoritosActivity.class);
                startActivityForResult(intent, REQUEST_PONTO_FAVORITO_SELECIONADO);
                break;

            case R.id.nav_configuracoes:
                this.intent = new Intent(this, ConfiguracoesActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_sair:
                this.mapsFragment.pararLocalizacaoGPS();
                Firebase.get().getFireUsuario().getFireAuth().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
        }

        appDrawer.close();
        vh.drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed() {

        if (appDrawer.isVisible()) {
            appDrawer.closeAnimate();
            return;
        }

        if (vh.toolbarPesquisa.getVisibility() == View.VISIBLE) {
            onSupportNavigateUp();
            return;
        }

        if (vh.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            vh.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void mostrarToolbarFiltro() {
        vh.toolbarPrincipal.setVisibility(View.GONE);
        vh.toolbarPesquisa.setVisibility(View.VISIBLE);
        setSupportActionBar(vh.toolbarPesquisa);
        invalidateOptionsMenu();

        //ativar setinho de voltar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void firebaseConections() {
        Firebase.get().getFireUsuario().getUserDocument().collection(LinhaFavorita.COLECAO).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        LinhaFavorita linhaFavorita = doc.toObject(LinhaFavorita.class);
                        LINHAS_FAVORITAS.put(linhaFavorita.getIdLinha(), linhaFavorita);
                    }
                }
            }
        });
    }

    private void mostrarDados() {

        USUARIO_LOGADO = (Usuario) getIntent().getSerializableExtra("USUARIO_LOGADO");

        if (USUARIO_LOGADO == null) {

            Firebase.get().getFireUsuario().getUserDocument().addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (e == null) {
                        Usuario usuario = documentSnapshot.toObject(Usuario.class);

                        USUARIO_LOGADO = usuario;
                        vh.textNome.setText(USUARIO_LOGADO.getNome());
                        vh.textTelefone.setText(USUARIO_LOGADO.getEmail());

                        ImagemUtils.picassoUserImage(MainActivity.this, vh.imgPerfil);
//                        Uri uri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
//                        Picasso.with(MainActivity.this)
//                                .load(uri)
//                                .placeholder(R.drawable.baseline_account_circle_white_48dp)
//                                .error(R.drawable.baseline_account_circle_white_48dp)
//                                .into(vh.imgPerfil);

                    } else { //falha
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        Toast.makeText(getApplicationContext(), "Falha de autenticacao", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });

        } else {
            vh.textNome.setText(USUARIO_LOGADO.getNome());
            vh.textTelefone.setText(USUARIO_LOGADO.getEmail());

            ImagemUtils.picassoUserImage(MainActivity.this, vh.imgPerfil);
//            Uri uri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
//            Picasso.with(MainActivity.this)
//                    .load(uri)
//                    .placeholder(R.drawable.baseline_account_circle_white_48dp)
//                    .error(R.drawable.baseline_account_circle_white_48dp)
//                    .into(vh.imgPerfil);
        }

    }

    public MapsFragment getMapsFragment() {
        return mapsFragment;
    }

    public void abrirTelaConfiguracoesConta(View view) {
        startActivity(new Intent(MainActivity.this, ConfiguracoesContaActivity.class));
    }

    public class ViewHolder {
        public CircleImageView imgPerfil;
        public TextView textNome;
        public TextView textTelefone;

        public Toolbar toolbarPrincipal, toolbarPesquisa;
        public TextView txtFiltro;

        public DrawerLayout drawerLayout;//Drawer MenuLateral

        public NavigationView navigationView;

        public ViewHolder() {
            navigationView = findViewById(R.id.menu_lateral);
            navigationView.setNavigationItemSelectedListener(MainActivity.this);

            imgPerfil = navigationView.getHeaderView(0).findViewById(R.id.img_perfil);
            textNome = navigationView.getHeaderView(0).findViewById(R.id.txt_nome_usuario);
            textTelefone = navigationView.getHeaderView(0).findViewById(R.id.txt_usuario_email);

            //Toolbar estar no xml content_main
            toolbarPrincipal = findViewById(R.id.toolbar);//Barra de pesquisa superior
            toolbarPesquisa = findViewById(R.id.toolbar_pesquisa);//Barra de pesquisa superior
            setSupportActionBar(toolbarPrincipal);

            txtFiltro = toolbarPesquisa.findViewById(R.id.text_filtro);

            drawerLayout = findViewById(R.id.drawer_layout);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbarPrincipal, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            drawerLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    appDrawer.close();
                }
            });
        }
    }
}
