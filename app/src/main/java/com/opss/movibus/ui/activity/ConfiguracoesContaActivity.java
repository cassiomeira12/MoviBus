package com.opss.movibus.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.opss.movibus.R;
import com.opss.movibus.firebase.Firebase;
import com.opss.movibus.model.Usuario;
import com.opss.movibus.ui.dialog.ConfirmarDialog;
import com.opss.movibus.util.ImagemUtils;
import com.opss.movibus.util.PastasUtil;
import com.opss.movibus.util.PermissoesUtils;
import com.squareup.picasso.Picasso;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfiguracoesContaActivity extends AppCompatActivity implements ConfirmarDialog.OnConfirmListener {

    private static final int REQUEST_PERMISSIONS = 1;
    private ViewHolder vh;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes_conta);
        vh = new ViewHolder();

        //ativar setinho de voltar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mostrarDados();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)
                ImagemUtils.buscarImagem(this, "perfil.jpg");

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ImagemUtils.REQUEST_IMAGE && resultCode != 0) {

            //capturou imagem
            if (data == null || data.getData() == null) {
                processarImagem(new File(PastasUtil.getPath() + "perfil.jpg"), null);
            } else {
                //escolheu da galeria

                File file = new File(PastasUtil.getPath() + "perfil.jpg");
                processarImagem(file, data.getData());
            }

        }
    }

    //método chamado após confirmar o dialog
    @Override
    public void onConfirmListener() {

    }

    private void mostrarDados() {
        Firebase.get().getFireUsuario().getUserDocument().addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e == null) {
                    usuario = documentSnapshot.toObject(Usuario.class);

                    vh.edtNome.setText(usuario.getNome());
                    vh.edtEmail.setText(usuario.getEmail());
                    vh.edtTelefone.setText(usuario.getTelefone());

                    vh.btnSalvar.setEnabled(true);

                    Uri uri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
                    Picasso.with(getApplicationContext())
                            .load(uri)
                            .placeholder(android.R.drawable.sym_def_app_icon)
                            .error(android.R.drawable.sym_def_app_icon)
                            .into(vh.imgPerfil);
                } else { //falha
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    Toast.makeText(getApplicationContext(), "Falha de autenticacao", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void processarImagem(File file, Uri uri) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (uri != null)
                    ImagemUtils.salvarImagem(ImagemUtils.bitmapFromUri(uri, getContentResolver()), file.getAbsolutePath());

                ImagemUtils.compressImage(file.getAbsolutePath());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        vh.imgPerfil.setImageDrawable(Drawable.createFromPath(new File(PastasUtil.getPath() + "perfil.jpg").getAbsolutePath()));
                    }
                });
            }
        }).start();


    }

    public void onClick(View view) {
        String[] request = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA
        };

        switch (view.getId()) {
            case R.id.img_perfil:
                if (verificarPermissoes())
                    ImagemUtils.buscarImagem(this, "perfil.jpg");
                else
                    ActivityCompat.requestPermissions(this, request, REQUEST_PERMISSIONS);
                break;

            case R.id.btn_salvar:
                salvar();
                break;

            case R.id.btn_desativar:
                ConfirmarDialog confirmarDialog = new ConfirmarDialog();
                confirmarDialog.setOnConfirmListener(this);
                confirmarDialog.setTitleButtons("NÃO", "DESATIVAR");
                confirmarDialog.show(getSupportFragmentManager(), "Deseja realmente desativar sua conta?");
                break;

            case R.id.backgroud_image:
                if (verificarPermissoes())
                    ImagemUtils.buscarImagem(this, "perfil.jpg");
                else
                    ActivityCompat.requestPermissions(this, request, REQUEST_PERMISSIONS);
                break;

            case R.id.btn_trocar_senha:
                if (vh.linearTrocarSenha.getVisibility() == View.VISIBLE) {
                    vh.linearTrocarSenha.setVisibility(View.GONE);
                    vh.txtTrocarSenha.setText(getString(R.string.trocar_senha));
                } else {
                    vh.linearTrocarSenha.setVisibility(View.VISIBLE);
                    vh.txtTrocarSenha.setText(getString(R.string.cancelar_troca_de_senha));
                }
                break;

            case R.id.btn_cancelar:
                finish();
                break;
        }
    }

    private void salvar() {
        if (!validarCampos())
            return;

        vh.progressBar.setVisibility(View.VISIBLE);
        vh.btnSalvar.setEnabled(false);
        vh.btnCancelar.setEnabled(false);

        usuario.setNome(vh.edtNome.getText().toString());
        usuario.setTelefone(vh.edtTelefone.getText().toString());
        usuario.setEmail(vh.edtEmail.getText().toString());

        if (vh.linearTrocarSenha.getVisibility() == View.VISIBLE)
            usuario.setSenha(vh.edtNovaSenha.getText().toString());

        Firebase.get().getFireUsuario().getCollection().document(usuario.getId()).set(usuario).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), R.string.dados_atualizados, Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), R.string.ocorreu_um_erro, Toast.LENGTH_SHORT).show();
                vh.progressBar.setVisibility(View.GONE);
                vh.btnSalvar.setEnabled(true);
                vh.btnCancelar.setEnabled(true);
            }
        });
    }

    private boolean validarCampos() {
        boolean valido = true;

        if (vh.edtNome.getText().toString().trim().length() < 3) {
            valido = false;
            vh.edtNome.setError(getString(R.string.campo_invalido));
        }

        if (vh.edtTelefone.getText().toString().replace("(", "").replace(")", "")
                .replace("-", "").trim().length() < 10) {
            valido = false;
            vh.edtTelefone.setError(getString(R.string.campo_invalido));
        }

        if (vh.edtEmail.getText().toString().trim().length() < 5 || !vh.edtEmail.getText().toString().contains("@")
                || vh.edtEmail.getText().toString().contains(" ")) {
            valido = false;
            vh.edtEmail.setError(getString(R.string.campo_invalido));
        }


        if (vh.linearTrocarSenha.getVisibility() == View.VISIBLE) {
            if (!vh.edtSenha.getText().toString().equals(usuario.getSenha())) {
                vh.edtSenha.setError(getString(R.string.senha_incorreta));
                valido = false;
            }

            if (vh.edtNovaSenha.getText().toString().trim().length() < 4) {
                valido = false;
                vh.edtNovaSenha.setError(getString(R.string.senha_invalida));
            }

            if (vh.edtConfirmarSenha.getText().toString().trim().length() < 4) {
                valido = false;
                vh.edtConfirmarSenha.setError(getString(R.string.senha_nao_confere));
            }
        }

        return valido;
    }

    private boolean verificarPermissoes() {
        int write = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int camera = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if (write == PackageManager.PERMISSION_GRANTED || read == PackageManager.PERMISSION_GRANTED || camera == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;

    }

    private class ViewHolder {

        private final CircleImageView imgPerfil;
        private final EditText edtNome, edtEmail, edtSenha, edtNovaSenha, edtConfirmarSenha, edtTelefone;
        private final LinearLayout linearTrocarSenha;
        private final TextView txtTrocarSenha;
        private final ProgressBar progressBar;
        private final Button btnCancelar, btnSalvar;

        public ViewHolder() {
            btnCancelar = findViewById(R.id.btn_cancelar);
            btnSalvar = findViewById(R.id.btn_salvar);

            progressBar = findViewById(R.id.progress);

            txtTrocarSenha = findViewById(R.id.btn_trocar_senha);

            imgPerfil = findViewById(R.id.img_perfil);

            linearTrocarSenha = findViewById(R.id.linear_trocar_senha);
            edtNome = findViewById(R.id.edt_nome);
            edtEmail = findViewById(R.id.edt_email);
            edtSenha = findViewById(R.id.edt_senha);
            edtNovaSenha = findViewById(R.id.edt_nova_senha);
            edtConfirmarSenha = findViewById(R.id.edt_confirma_nova_senha);
            edtTelefone = findViewById(R.id.edt_telefone);
        }

    }
}
