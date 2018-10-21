package com.opss.movibus.ui.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.opss.movibus.firebase.Firebase;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;

import com.opss.movibus.R;
import com.opss.movibus.model.Usuario;

public class LoginActivity extends AppCompatActivity {

    private ViewHolder viewHolder;

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        viewHolder = new ViewHolder();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Caso o Usuario já esteja logado
        if (Firebase.get().getFireUsuario().getFireAuth().getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {

            }
        }
    }

    public void logar(View view) {
        //Verificando se o Email esta vazio
        if (TextUtils.isEmpty(viewHolder.emailText.getText().toString())) {
            Snackbar.make(getCurrentFocus(), "Informe o email", Snackbar.LENGTH_SHORT).show();
            return;
        }

        //Verificando se a Senha esta vazia
        if (TextUtils.isEmpty(viewHolder.senhaText.getText().toString())) {
            Snackbar.make(getCurrentFocus(), "Informe a senha", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String email = viewHolder.emailText.getText().toString();
        String senha = viewHolder.senhaText.getText().toString();

        SpotsDialog alertaEspera = new SpotsDialog(LoginActivity.this);
        alertaEspera.show();

        Firebase.get().getFireUsuario().getFireAuth().signInWithEmailAndPassword(email, senha).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Firebase.get().getFireUsuario().getCollection().document(authResult.getUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //if (documentSnapshot.exists()) {
                            Usuario usuario = documentSnapshot.toObject(Usuario.class);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("USUARIO_LOGADO", usuario);
                            startActivity(intent);
                            alertaEspera.dismiss();
                            finish();
//                        } else {
//                            alertaEspera.dismiss();
//                            Snackbar.make(getCurrentFocus(), "Erro, conta inexistente!", Snackbar.LENGTH_LONG).show();
//                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                alertaEspera.dismiss();
                Snackbar.make(getCurrentFocus(), "Erro ao tentar realizar login", Snackbar.LENGTH_LONG).show();
            }
        });

    }

    public void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        SpotsDialog dialog = new SpotsDialog(LoginActivity.this);
        dialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        Firebase.get().getFireUsuario().getFireAuth().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = Firebase.get().getFireUsuario().getFireAuth().getCurrentUser();

                    //Verificando se já existe uma conta da google cadastrada
                    Firebase.get().getFireUsuario().getCollection().document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                Usuario usuario = documentSnapshot.toObject(Usuario.class);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("USUARIO_LOGADO", usuario);
                                startActivity(intent);
                                dialog.dismiss();
                                finish();
                            } else {
                                //Cadastrando a nova Conta
                                String nome = user.getDisplayName();
                                String email = user.getEmail();
                                String senha = "";
                                String telefone = "";

                                //Criando objeto do Usuario
                                Usuario usuario = new Usuario();
                                usuario.setId(user.getUid());
                                usuario.setNome(nome);
                                usuario.setEmail(email);
                                usuario.setSenha(senha);
                                usuario.setTelefone(telefone);

                                cadastrarObjetoUsuario(usuario, dialog);
                            }
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Snackbar.make(getCurrentFocus(), "Erro ao efetuar o login", Snackbar.LENGTH_LONG).show();
            }
        });

    }

    private void cadastrarObjetoUsuario(Usuario usuario, SpotsDialog dialog) {
        Firebase.get().getFireUsuario().getCollection().document(usuario.getId()).set(usuario).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Snackbar.make(getCurrentFocus(), "Usuário cadastrado com sucesso!", Snackbar.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("USUARIO_LOGADO", usuario);
                startActivity(intent);
                dialog.dismiss();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(getCurrentFocus(), "Erro " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
    }

    private class ViewHolder {

        private final MaterialEditText emailText;
        private final MaterialEditText senhaText;
        private final TextView recuperarSenha;

        private final Button btnCadastrar, btnLogar;
        private final SignInButton googleButton;

        public ViewHolder() {
            emailText = findViewById(R.id.email_text);
            senhaText = findViewById(R.id.senha_text);
            recuperarSenha = findViewById(R.id.recuperar_senha);

            recuperarSenha.setVisibility(View.GONE);

            btnCadastrar = findViewById(R.id.btn_cadastrar);
            btnLogar = findViewById(R.id.btn_logar);
            googleButton = findViewById(R.id.googleButton);

            //btnLogar.setOnClickListener((OnClickListener) -> logar());
            googleButton.setOnClickListener((OnClickListener) -> signInGoogle());
            btnCadastrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(LoginActivity.this, CadastroActivity.class));
                }
            });
        }

    }

}
