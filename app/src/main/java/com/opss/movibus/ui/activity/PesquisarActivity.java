package com.opss.movibus.ui.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.opss.movibus.R;
import com.opss.movibus.firebase.Firebase;
import com.opss.movibus.model.Linha;
import com.opss.movibus.model.LinhaFavorita;
import com.opss.movibus.model.Onibus;
import com.opss.movibus.model.PontoFavorito;
import com.opss.movibus.model.PontoOnibus;
import com.opss.movibus.model.Rota;
import com.opss.movibus.model.Usuario;
import com.opss.movibus.ui.adapter.Adapter;
import com.opss.movibus.ui.adapter.AdapterPesquisa;
import com.opss.movibus.util.SugestoesPesquisasProvider;

import java.util.ArrayList;
import java.util.List;


public class PesquisarActivity extends AppCompatActivity implements Adapter.Actions {

    // private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private AdapterPesquisa adapterPesquisa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pesquisar);

        //  toolbar = findViewById(R.id.toolbar);
        //  toolbar.setTitleTextColor(getResources().getColor(R.color.branco));
        // setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress);

        new BuscarDados();

        //ativar setinho de voltar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView;

        MenuItem menuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) menuItem.getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getResources().getString(R.string.pesquisar_));
        searchView.setOnQueryTextListener(new TextQueryListener());

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.radio_todos:
                adapterPesquisa.filtrar(AdapterPesquisa.Filtro.TODOS);
                break;

            case R.id.radio_linhas:
                adapterPesquisa.filtrar(AdapterPesquisa.Filtro.LINHAS);
                break;

            case R.id.radio_pontos:
                adapterPesquisa.filtrar(AdapterPesquisa.Filtro.PONTOS);
                break;

            case R.id.radio_itinerarios:
                adapterPesquisa.filtrar(AdapterPesquisa.Filtro.ITINERARIOS);
                break;

            default:
                Intent intent = new Intent();
                Object objeto = adapterPesquisa.getItem((int) view.getTag());

                if (objeto instanceof Linha) {
                    intent.putExtra("pesquisa", (Linha) objeto);
                } else if (objeto instanceof PontoOnibus) {
                    intent.putExtra("pesquisa", (PontoOnibus) objeto);
                } else if (objeto instanceof Rota) {
                    //intent.putExtra("pesquisa", (Rota) objeto);
                }
                setResult(MainActivity.REQUEST_PESQUISA_SELECIONADO, intent);
                finish();
                break;
        }
    }

    @Override
    public void onLongClick(View view) {

    }

    private void configurarRecyclerView(List<Object> objects) {
        adapterPesquisa = new AdapterPesquisa(objects, this, this, null);
        recyclerView.setAdapter(adapterPesquisa);
        RecyclerView.LayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layout);
    }

    public void pesquisa(String query) {
        SearchRecentSuggestions searchRecentSuggestions = new SearchRecentSuggestions(this,
                SugestoesPesquisasProvider.AUTHORITY, SugestoesPesquisasProvider.MODE);

        searchRecentSuggestions.saveRecentQuery(query, null);

        if (adapterPesquisa != null)
            adapterPesquisa.pesquisar(query);
    }

    private class TextQueryListener implements SearchView.OnQueryTextListener {

        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            pesquisa(newText);
            return true;
        }
    }

    private class BuscarDados {

        private List<Object> objects = new ArrayList<>();

        public BuscarDados() {
            buscarLinhas();
        }

        private void buscarLinhas() {
            Firebase.get().getFireLinha().getCollection().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isComplete()) {
                        List<Linha> linhas = task.getResult().toObjects(Linha.class);
                        if (linhas != null) {
                            objects.addAll(linhas);
                            buscarPontos();
                        }
                    }
                }
            });
        }

        private void buscarPontos() {
            Firebase.get().getFirePontoOnibus().getCollection().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isComplete()) {
                        List<PontoOnibus> pontoOnibuses = task.getResult().toObjects(PontoOnibus.class);
                        if (pontoOnibuses != null) {
                            objects.addAll(pontoOnibuses);
                            buscarItinerarios();
                        }
                    }
                }
            });
        }

        private void buscarItinerarios() {
            Firebase.get().getFireRota().getCollection().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    progressBar.setVisibility(View.GONE);
                    if (task.isComplete()) {
                        //    List<Rota> rotas = task.getResult().toObjects(Rota.class);
                        //   if (rotas != null) {
                        //     objects.addAll(rotas);
                        // }
                        configurarRecyclerView(objects);
                    }
                }
            });
        }

    }
}
