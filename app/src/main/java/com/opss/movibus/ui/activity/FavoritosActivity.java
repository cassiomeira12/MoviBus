package com.opss.movibus.ui.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import com.opss.movibus.R;
import com.opss.movibus.model.Favorito;
import com.opss.movibus.model.LinhaFavorita;
import com.opss.movibus.model.PontoFavorito;
import com.opss.movibus.ui.adapter.Adapter;
import com.opss.movibus.ui.adapter.AdapterFavorito;
import com.opss.movibus.ui.fragment.MapsFragment;
import com.opss.movibus.ui.helper.SimpleItemTouchHelperCallback;


public class FavoritosActivity extends AppCompatActivity implements Adapter.Actions {

    private RecyclerView recyclerView;
    private AdapterFavorito adapter;
    public static View favoritosLayout;

    private List<Favorito> favoritoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritos);

        this.favoritosLayout = findViewById(R.id.meus_favoritos);

        this.favoritoList = new ArrayList<>();
        this.recyclerView = findViewById(R.id.recycler_view_favoritos);
        this.adapter = new AdapterFavorito(favoritoList, this, this, this);
        this.recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
        RecyclerView.LayoutManager layout = new LinearLayoutManager(this, LinearLayout.VERTICAL, false);
        recyclerView.setLayoutManager(layout);

        getFavoritosCollections();

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
    public void onClick(View view) {
        Intent intent = new Intent();
        Favorito favorito = adapter.getItem((int) view.getTag());
        if (favorito.getLinha() != null) {
            intent.putExtra("favorito", favorito.getLinha().getIdLinha());
        } else if (favorito.getPonto() != null) {
            intent.putExtra("favorito", favorito.getPonto().getIdPonto());
        }
        setResult(MainActivity.REQUEST_FOVORITO_SELECIONADO, intent);
        finish();
    }

    @Override
    public void onLongClick(View view) {

    }

    private void getFavoritosCollections() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                for (LinhaFavorita linha : MapsFragment.COLLECTIONS.linhasFavoritas.values()) {
                    Favorito favorito = new Favorito(linha);
                    favoritoList.add(favorito);
                    adapter.notifyDataSetChanged();
                }

            }
        });

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                for (PontoFavorito ponto : MapsFragment.COLLECTIONS.pontosFavoritos.values()) {
                    Favorito favorito = new Favorito(ponto);
                    favoritoList.add(favorito);
                    adapter.notifyDataSetChanged();
                }

            }
        });

    }

}
