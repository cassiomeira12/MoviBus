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

import com.opss.movibus.R;
import com.opss.movibus.model.PontoFavorito;
import com.opss.movibus.ui.adapter.Adapter;
import com.opss.movibus.ui.adapter.AdapterPonto;
import com.opss.movibus.ui.fragment.MapsFragment;
import com.opss.movibus.ui.helper.SimpleItemTouchHelperCallback;

import java.util.ArrayList;
import java.util.List;

public class PontosFavoritosActivity extends AppCompatActivity implements Adapter.Actions {

    private RecyclerView recyclerView;
    private AdapterPonto adapter;
    public static View favoritosLayout;

    private List<PontoFavorito> favoritoList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pontos_favoritos);

        this.favoritosLayout = findViewById(R.id.linhas_favoritas);
        this.favoritoList = new ArrayList<>();
        this.recyclerView = findViewById(R.id.recycler_view_favoritos);
        this.adapter = new AdapterPonto(favoritoList, this, this, this);
        this.recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
        RecyclerView.LayoutManager layout = new LinearLayoutManager(this, LinearLayout.VERTICAL, false);
        recyclerView.setLayoutManager(layout);

        //ativar setinho de voltar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                for (PontoFavorito ponto : MapsFragment.COLLECTIONS.pontosFavoritos.values()) {
                    favoritoList.add(ponto);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        PontoFavorito ponto = adapter.getItem((int) view.getTag());
        intent.putExtra("ponto_favorito", ponto.getIdPonto());
        setResult(MainActivity.REQUEST_PONTO_FAVORITO_SELECIONADO, intent);
        finish();
    }

    @Override
    public void onLongClick(View view) {

    }
}
