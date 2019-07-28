package com.opss.movibus.ui.activity;

import android.content.Intent;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;
import android.widget.LinearLayout;

import com.opss.movibus.R;
import com.opss.movibus.model.LinhaFavorita;
import com.opss.movibus.ui.adapter.Adapter;
import com.opss.movibus.ui.adapter.AdapterLinha;
import com.opss.movibus.ui.fragment.MapsFragment;
import com.opss.movibus.ui.helper.SimpleItemTouchHelperCallback;

import java.util.ArrayList;
import java.util.List;

public class LinhasFavoritasActivity extends AppCompatActivity implements Adapter.Actions {

    private RecyclerView recyclerView;
    private AdapterLinha adapter;
    public static View favoritosLayout;

    private List<LinhaFavorita> favoritoList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritos);

        this.favoritosLayout = findViewById(R.id.meus_favoritos);
        this.favoritoList = new ArrayList<>();
        this.recyclerView = findViewById(R.id.recycler_view_favoritos);
        this.adapter = new AdapterLinha(favoritoList, this, this, this);
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
                for (LinhaFavorita linha : MapsFragment.COLLECTIONS.linhasFavoritas.values()) {
                    favoritoList.add(linha);
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
        LinhaFavorita linha = adapter.getItem((int) view.getTag());
        intent.putExtra("linha_favorita", linha.getIdLinha());
        setResult(MainActivity.REQUEST_LINHA_FAVORITA_SELECIONADO, intent);
        finish();
    }

    @Override
    public void onLongClick(View view) {

    }
}
