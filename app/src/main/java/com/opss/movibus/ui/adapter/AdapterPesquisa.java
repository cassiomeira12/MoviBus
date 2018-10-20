package com.opss.movibus.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.opss.movibus.R;
import com.opss.movibus.model.Linha;
import com.opss.movibus.model.PontoOnibus;
import com.opss.movibus.model.Rota;

import java.util.ArrayList;
import java.util.List;

public class AdapterPesquisa extends Adapter<Object> {

    private List<Object> naoExibidos = new ArrayList<>();

    public AdapterPesquisa(List<Object> itensList, Context context, Actions onClick, Actions onLongClick) {
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

        Object object = itensList.get(position);

        if (object instanceof PontoOnibus) {
            PontoOnibus pontoOnibus = (PontoOnibus) object;

            viewHolder.linearPonto.setVisibility(View.VISIBLE);

            viewHolder.imageIcone.setImageResource(R.mipmap.ic_ponto);
            viewHolder.textDescricao.setText(pontoOnibus.getDescricao());
            viewHolder.textCoberto.setVisibility( pontoOnibus.isCoberto() ? View.VISIBLE : View.GONE );

        } else if (object instanceof Linha) {
            Linha linha = (Linha) object;

            viewHolder.linearLinha.setVisibility(View.VISIBLE);

            viewHolder.imageIcone.setImageResource(R.mipmap.ic_onibus);
            viewHolder.textDescricao.setText(linha.getNome() + " VIA " + linha.getVia());
            viewHolder.textOrigem.setText(linha.getOrigem());
            viewHolder.textDestino.setText(linha.getDestino());

        } else {//itinerário

        }

        viewHolder.itemFavorito.setTag(position);
    }

    @Override
    public void update(Object item) {
        super.update(item);
    }

    public void pesquisar(String query) {
        itensList.addAll(naoExibidos);
        naoExibidos.clear();

        query = query.toUpperCase();
        for (Object o : itensList)
            if (!o.toString().toUpperCase().contains(query.toUpperCase()))
                naoExibidos.add(o);

        for (Object o : naoExibidos)
            itensList.remove(o);

        notifyDataSetChanged();
    }

    public void filtrar(Filtro filtro) {
        itensList.addAll(naoExibidos);
        naoExibidos.clear();

        switch (filtro) {
            case TODOS:
                itensList.addAll(naoExibidos);
                break;

            case LINHAS:
                for (Object o : itensList)
                    if (!(o instanceof Linha))
                        naoExibidos.add(o);
                break;

            case PONTOS:
                for (Object o : itensList)
                    if (!(o instanceof PontoOnibus))
                        naoExibidos.add(o);
                break;

            case ITINERARIOS:
                for (Object o : itensList)
                    if (!(o instanceof Rota))
                        naoExibidos.add(o);
                break;
        }


        for (Object o : naoExibidos)
            itensList.remove(o);

        notifyDataSetChanged();
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

            itemFavorito.setOnClickListener(AdapterPesquisa.this);
            itemFavorito.setOnLongClickListener(AdapterPesquisa.this);
        }

    }

    public enum Filtro {
        TODOS, LINHAS, PONTOS, ITINERARIOS
    }
}
