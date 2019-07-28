package com.opss.movibus.ui.adapter;

import android.content.Context;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import com.opss.movibus.R;
import com.opss.movibus.firebase.Firebase;
import com.opss.movibus.model.Favorito;
import com.opss.movibus.ui.activity.FavoritosActivity;
import com.opss.movibus.ui.fragment.MapsFragment;
import com.opss.movibus.ui.helper.ItemTouchHelperAdapter;

public class AdapterFavorito extends Adapter<Favorito> implements ItemTouchHelperAdapter {

    private Favorito ultimoRemovido = null;
    private int ultimaPosicaoRemovida;

    public AdapterFavorito(List<Favorito> itensList, Context context, Actions onClick, Actions onLongClick) {
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

        Favorito favorito = itensList.get(position);

        if (favorito.getLinha() != null) {

            viewHolder.linearLinha.setVisibility(View.VISIBLE);

            viewHolder.imageIcone.setImageResource(R.mipmap.ic_onibus);
            viewHolder.textDescricao.setText(favorito.getDescricao());
            viewHolder.textOrigem.setText(favorito.getOrigem());
            viewHolder.textDestino.setText(favorito.getDestino());
            //viewHolder.textQuantidadeOnibus.setText(String.valueOf(favorito.getOnibusOnline()));
            viewHolder.textQuantidadeOnibus.setText(String.valueOf(favorito.getLinha().linha.getOnibusMap().size()));

        } else if (favorito.getPonto() != null) {

            viewHolder.linearPonto.setVisibility(View.VISIBLE);

            viewHolder.imageIcone.setImageResource(R.mipmap.ic_ponto);
            viewHolder.textDescricao.setText(favorito.getDescricao());
            viewHolder.textCoberto.setText(favorito.getCoberto());
        }

        viewHolder.itemFavorito.setTag(position);
    }

    @Override
    public void update(Favorito item) {
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
        Snackbar mySnackbar = Snackbar.make(FavoritosActivity.favoritosLayout, getItem(position).getDescricao() + " removido dos favoritos", 7000);
        mySnackbar.setAction("Desfazer", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itensList.add(ultimaPosicaoRemovida, ultimoRemovido);
                notifyDataSetChanged();
                if (ultimoRemovido.getLinha() != null) {
                    Firebase.get().getFireUsuario().setFavoritoDocument(ultimoRemovido.getLinha());
                    MapsFragment.COLLECTIONS.setFavorito(ultimoRemovido.getLinha());
                } else if (ultimoRemovido.getPonto() != null) {
                    Firebase.get().getFireUsuario().setFavoritoDocument(ultimoRemovido.getPonto());
                    MapsFragment.COLLECTIONS.setFavorito(ultimoRemovido.getPonto());
                }
            }
        });
        mySnackbar.show();

        ultimoRemovido = getItem(position);
        ultimaPosicaoRemovida = position;

        if (ultimoRemovido.getLinha() != null) {
            Firebase.get().getFireUsuario().deletFavoritoDocument(ultimoRemovido.getLinha());
            MapsFragment.COLLECTIONS.removeFavorito(ultimoRemovido.getLinha());
        } else if (ultimoRemovido.getPonto() != null) {
            Firebase.get().getFireUsuario().deletFavoritoDocument(ultimoRemovido.getPonto());
            MapsFragment.COLLECTIONS.removeFavorito(ultimoRemovido.getPonto());
        }

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
            itemFavorito = (LinearLayout) itemView.findViewById(R.id.item_favorito);//Layout do Item

            imageIcone = (ImageView) itemView.findViewById(R.id.img_icone);//Imagem do Item
            textDescricao = (TextView) itemView.findViewById(R.id.text_descricao);//Descricao do Item

            //Item Linha Favorita
            linearLinha = (LinearLayout) itemView.findViewById(R.id.linearLinha);
            textOrigem = (TextView) itemView.findViewById(R.id.text_origem);
            textDestino = (TextView) itemView.findViewById(R.id.text_destino);
            textQuantidadeOnibus = (TextView) itemView.findViewById(R.id.quantidadeOnibus);

            //Item Ponto Favorito
            linearPonto = (LinearLayout) itemView.findViewById(R.id.linearPonto);
            textCoberto = (TextView) itemView.findViewById(R.id.text_coberto);

            itemFavorito.setOnClickListener(AdapterFavorito.this);
            itemFavorito.setOnLongClickListener(AdapterFavorito.this);
        }

    }

}
