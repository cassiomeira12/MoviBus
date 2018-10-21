package com.opss.movibus.ui.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.opss.movibus.R;
import com.opss.movibus.firebase.Firebase;
import com.opss.movibus.model.Usuario;

import dmax.dialog.SpotsDialog;

public class CadastroActivity extends AppCompatActivity {

    private ViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        viewHolder = new ViewHolder();
    }

    public void singInGoogle(View view) {

    }

    public void cadastrarNovaConta(View view) {
        //Verificando se o Nome esta vazio
        if (TextUtils.isEmpty(viewHolder.edtNome.getText().toString())) {
            Snackbar.make(getCurrentFocus(), "Informe o nome", Snackbar.LENGTH_LONG).show();
            return;
        }

        //Verificando se o Telefone esta vazio
        if (TextUtils.isEmpty(viewHolder.edtTelefone.getText().toString())) {
            Snackbar.make(getCurrentFocus(), "Informe o nome", Snackbar.LENGTH_LONG).show();
            return;
        }

        //Verificando se o Email esta vazio
        if (TextUtils.isEmpty(viewHolder.edtEmail.getText().toString())) {
            Snackbar.make(getCurrentFocus(), "Informe o email", Snackbar.LENGTH_LONG).show();
            return;
        }

        //Verificando se a Senha esta vazia
        if (TextUtils.isEmpty(viewHolder.edtSenha.getText().toString())) {
            Snackbar.make(getCurrentFocus(), "Informe a senha", Snackbar.LENGTH_LONG).show();
            return;
        }

        //Verificando se o Confirmar Senha esta vazia
        if (TextUtils.isEmpty(viewHolder.edtConfirmarSenha.getText().toString())) {
            Snackbar.make(getCurrentFocus(), "Confirme a senha", Snackbar.LENGTH_LONG).show();
            return;
        }

        //Verificando se a Senha e a confirmacao da Senha sao as mesmas
        if (!TextUtils.equals(viewHolder.edtSenha.getText().toString(), viewHolder.edtConfirmarSenha.getText().toString())) {
            Snackbar.make(getCurrentFocus(), "Erro, as senhas não conferem", Snackbar.LENGTH_LONG).show();
            //viewHolder.edtSenha.requestFocus();
            return;
        }


        viewHolder.setEnable(false);

        String nome = viewHolder.edtNome.getText().toString();
        String telefone = viewHolder.edtTelefone.getText().toString();
        String email = viewHolder.edtEmail.getText().toString();
        String senha = viewHolder.edtSenha.getText().toString();

        SpotsDialog dialog = new SpotsDialog(this);
        dialog.show();

        Firebase.get().getFireUsuario().getFireAuth().createUserWithEmailAndPassword(email, senha).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                //Criando objeto do Usuario
                Usuario usuario = new Usuario();
                usuario.setId(authResult.getUser().getUid());
                usuario.setNome(nome);
                usuario.setEmail(email);
                usuario.setSenha(senha);
                usuario.setTelefone(telefone);

                cadastrarObjetoUsuario(usuario, dialog);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                viewHolder.setEnable(true);
                Snackbar.make(getCurrentFocus(), "Erro " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void cadastrarObjetoUsuario(Usuario usuario, SpotsDialog dialog) {
        Firebase.get().getFireUsuario().getCollection().document(usuario.getId()).set(usuario).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                dialog.dismiss();
                Snackbar.make(getCurrentFocus(), "Usuário cadastrado com sucesso!", Snackbar.LENGTH_LONG).show();
                startActivity(new Intent(CadastroActivity.this, MainActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                viewHolder.setEnable(true);
                Snackbar.make(getCurrentFocus(), "Erro " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private class ViewHolder {

        private final EditText edtNome, edtTelefone, edtEmail, edtSenha, edtConfirmarSenha;
        private final Button btnCadastrar;
        private final SignInButton btnGoogle;

        public ViewHolder() {
            btnCadastrar = findViewById(R.id.btn_cadastrar);
            btnGoogle = findViewById(R.id.btn_google_sign);

            edtNome = findViewById(R.id.edt_nome);
            edtTelefone = findViewById(R.id.edt_telefone);
            edtEmail = findViewById(R.id.edt_email);
            edtSenha = findViewById(R.id.edt_senha);
            edtConfirmarSenha = findViewById(R.id.edt_confirma_senha);
        }

        public void setEnable(boolean enable) {
            edtNome.setEnabled(enable);
            edtTelefone.setEnabled(enable);
            edtEmail.setEnabled(enable);
            edtSenha.setEnabled(enable);
            edtConfirmarSenha.setEnabled(enable);
            btnCadastrar.setEnabled(enable);
            btnGoogle.setEnabled(enable);
        }

    }

}
