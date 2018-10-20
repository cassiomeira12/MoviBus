package com.opss.movibus.ui.adapter;

import android.content.Context;
import java.util.List;


import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Collections;

import com.opss.movibus.R;
import com.opss.movibus.model.PontoFavorito;
import com.opss.movibus.model.PontoOnibus;
import com.opss.movibus.ui.activity.FavoritosActivity;
import com.opss.movibus.ui.helper.ItemTouchHelperAdapter;

public class AdapterPonto extends Adapter<PontoFavorito> implements ItemTouchHelperAdapter {
    private PontoFavorito ultimoRemovido = null;
    private int ultimaPosicaoRemovida;

    public AdapterPonto(List<PontoFavorito> itensList, Context context, Actions onClick, Actions onLongClick) {
        super(itensList, context, onClick, onLongClick);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorito, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.linearLinha.setVisibility(View.GONE);
        viewHolder.linearPonto.setVisibility(View.GONE);

        PontoFavorito ponto = itensList.get(position);

        viewHolder.linearPonto.setVisibility(View.VISIBLE);

        viewHolder.imageIcone.setImageResource(R.mipmap.ic_ponto);
        viewHolder.textDescricao.setText(ponto.getDescricao());
        viewHolder.textCoberto.setVisibility( ponto.isCoberto() ? View.VISIBLE : View.GONE );

        viewHolder.itemFavorito.setTag(position);
    }

    @Override
    public void update(PontoFavorito item) {
        super.update(item);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(itensList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(itensList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        //mostra opção de desfazer a exclusão
        Snackbar mySnackbar = Snackbar.make(FavoritosActivity.favoritosLayout, "id" + " removido dos favoritos", 7000);
        mySnackbar.setAction("Desfazer", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itensList.add(ultimaPosicaoRemovida, ultimoRemovido);
                notifyDataSetChanged();
            }
        });
        mySnackbar.show();

        ultimoRemovido = getItem(position);
        ultimaPosicaoRemovida = position;

        itensList.remove(position);
        notifyItemRemoved(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        protected final LinearLayout itemFavorito;

        protected final ImageView imageIcone;
        protected final TextView textDescricao;

        //Linha Favorita
        protected final LinearLayout linearLinha;
        protected final TextView textOrigem;
        protected final TextView textDestino;
        protected final TextView textQuantidadeOnibus;

        //Ponto Favorito
        protected final LinearLayout linearPonto;
        protected final TextView textCoberto;

        public ViewHolder(View itemView) {
            super(itemView);
            itemFavorito = itemView.findViewById(R.id.item_favorito);//Layout do Item

            imageIcone = itemView.findViewById(R.id.img_icone);//Imagem do Item
            textDescricao = itemView.findViewById(R.id.text_descricao);//Descricao do Item

            //Item Linha Favorita
            linearLinha = itemView.findViewById(R.id.linearLinha);
            textOrigem = itemView.findViewById(R.id.text_origem);
            textDestino = itemView.findViewById(R.id.text_destino);
            textQuantidadeOnibus = itemView.findViewById(R.id.quantidadeOnibus);

            //Item Ponto Favorito
            linearPonto = itemView.findViewById(R.id.linearPonto);
            textCoberto = itemView.findViewById(R.id.text_coberto);

            itemFavorito.setOnClickListener(AdapterPonto.this);
            itemFavorito.setOnLongClickListener(AdapterPonto.this);
        }

    }
}
