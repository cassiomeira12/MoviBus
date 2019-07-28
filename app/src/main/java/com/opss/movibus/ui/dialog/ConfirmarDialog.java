package com.opss.movibus.ui.dialog;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.opss.movibus.R;

public class ConfirmarDialog extends DialogFragment implements View.OnClickListener {

    private View view;
    private ViewHolder vh;
    private OnConfirmListener onConfirmListener;

    private String titleBtnSim = "SIM";
    private String titleBtnNao = "CANCELAR";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.dialog_confirmar, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        vh = new ViewHolder(view);
        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btn_cancelar:
                dismiss();
                break;

            case R.id.btn_sim:
                try {
                    if (onConfirmListener != null)
                        onConfirmListener.onConfirmListener();
                    dismiss();
                } catch (Exception e) {
                    Toast.makeText(getContext(), R.string.ocorreu_um_erro, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void setTitleButtons(String btnNao, String btnSim) {
        this.titleBtnSim = btnSim;
        this.titleBtnNao = btnNao;
    }

    public void setOnConfirmListener(OnConfirmListener onConfirmListener) {
        this.onConfirmListener = onConfirmListener;
    }

    private class ViewHolder {
        final Button btnNao;
        final Button btnSim;
        final TextView txtInfo;

        public ViewHolder(View v) {
            btnNao = v.findViewById(R.id.btn_cancelar);
            btnSim = v.findViewById(R.id.btn_sim);
            txtInfo = v.findViewById(R.id.txt_info_imagem);

            txtInfo.setText(getTag());

            btnNao.setOnClickListener(ConfirmarDialog.this);
            btnSim.setOnClickListener(ConfirmarDialog.this);

            if (titleBtnNao == null)
                btnNao.setVisibility(View.GONE);
            else
                btnNao.setText(titleBtnNao);
            btnSim.setText(titleBtnSim);
        }
    }

    public interface OnConfirmListener {
        void onConfirmListener();
    }
}

