package com.opss.movibus.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.opss.movibus.firebase.Firebase;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;

import com.opss.movibus.R;
import com.opss.movibus.model.Usuario;

import javax.annotation.Nullable;

public class LoginActivity extends AppCompatActivity {

    private Intent intent;
    private LinearLayout loginLayout;


    private MaterialEditText emailText;
    private MaterialEditText senhaText;
    private TextView recuperarSenha;

    private Button btnCadastrar, btnLogar;
    private SignInButton googleButton;


    private FirebaseAuth autenticacao = FirebaseAuth.getInstance();
    //private FirebaseDatabase bancoDados;
    //private DatabaseReference bancoDadosFirebase;
    private FirebaseFirestore dataBase = FirebaseFirestore.getInstance();


    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginLayout = (LinearLayout) findViewById(R.id.login_layout);

        emailText = findViewById(R.id.email_text);
        senhaText = findViewById(R.id.senha_text);
        recuperarSenha = findViewById(R.id.recuperar_senha);

        btnCadastrar = (Button) findViewById(R.id.btn_cadastrar);
        btnLogar = (Button) findViewById(R.id.btn_logar);
        googleButton = findViewById(R.id.googleButton);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Caso o Usuario já esteja logado
        if (autenticacao.getCurrentUser() != null) {
            intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        //bancoDados = FirebaseDatabase.getInstance();
        //usuariosBD = bancoDados.getReference("Usuarios");
        //bancoDadosFirebase = FirebaseDatabase.getInstance().getReference();

        btnLogar.setOnClickListener((OnClickListener) -> logar());

        googleButton.setOnClickListener((OnClickListener) -> signInGoogle());

//        btnLogar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mostrarAlertaLogar();
//            }
//        });

        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarAlertaCadastrar();
            }
        });
    }

    public void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        // Firebase sign out
        autenticacao.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //updateUI(null);
                    }
                });
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
                // Google Sign In failed, update UI appropriately
                //Log.w(TAG, "Google sign in failed", e);
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        SpotsDialog alertaEspera = new SpotsDialog(LoginActivity.this);
        alertaEspera.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        autenticacao.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = autenticacao.getCurrentUser();

                            String nome = user.getDisplayName();
                            String email = user.getEmail();
                            String senha = "";
                            String telefone = user.getPhoneNumber();

                            //Criando objeto do Usuario
                            Usuario novoUsuario = new Usuario();
                            novoUsuario.setId(user.getUid());
                            novoUsuario.setNome(nome);
                            novoUsuario.setEmail(email);
                            novoUsuario.setSenha(senha);
                            novoUsuario.setTelefone(telefone);

                            Firebase.get().getFireUsuario().getCollection().document(novoUsuario.getId()).set(novoUsuario).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Snackbar.make(loginLayout, "Usuário cadastrado com sucesso!", Snackbar.LENGTH_LONG).show();

                                    intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();

                                    alertaEspera.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    alertaEspera.dismiss();
                                    Snackbar.make(loginLayout, "Erro " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });

                        } else {
                            alertaEspera.dismiss();
                            Snackbar.make(loginLayout, "Erro no cadastro", Snackbar.LENGTH_LONG).show();
                        }

                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        //hideProgressDialog();
        if (user != null) {
            //mStatusTextView.setText(getString(R.string.google_status_fmt, user.getEmail()));
            //mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));
            Log.i("CASSIO", user.getDisplayName());
            Log.i("CASSIO", user.getEmail());
            //Log.i("CASSIO", user.getPhoneNumber());
            Log.i("CASSIO", user.getUid());

            //findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            //findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            //mStatusTextView.setText(R.string.signed_out);
            //mDetailTextView.setText(null);
            Log.i("CASSIO", "Deu errado");
            //findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            //findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    private void logar() {
        //Verificando se o Email esta vazio
        if (TextUtils.isEmpty(emailText.getText().toString())) {
            Snackbar.make(loginLayout, "Informe o email", Snackbar.LENGTH_SHORT).show();
            return;
        }

        //Verificando se a Senha esta vazia
        if (TextUtils.isEmpty(senhaText.getText().toString())) {
            Snackbar.make(loginLayout, "Informe a senha", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String email = emailText.getText().toString();
        String senha = senhaText.getText().toString();

        SpotsDialog alertaEspera = new SpotsDialog(LoginActivity.this);
        alertaEspera.show();

        btnLogar.setEnabled(false);//Desabilitando botao de logar

        autenticacao.signInWithEmailAndPassword(email, senha).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                //dataBase.collection(Usuario.COLECAO).document().

                Firebase.get().getFireUsuario().getUserDocument().addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e == null){
                            Usuario usuario = documentSnapshot.toObject(Usuario.class);

                            intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("USUARIO_LOGADO", usuario);
                            startActivity(intent);
                            finish();
                        }else{ //falha

                        }
                    }
                });

                alertaEspera.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                alertaEspera.dismiss();
                Snackbar.make(loginLayout, "Erro " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                btnLogar.setEnabled(true);//Habilitando botao de logar
            }
        });
    }

    //Chama o Alerta para fazer Login
    private void mostrarAlertaLogar() {
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setTitle("LOGAR");
        alerta.setMessage("Use o email para Logar");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_login = inflater.inflate(R.layout.layout_logar, null);

        final MaterialEditText editEmail = layout_login.findViewById(R.id.edit_email);
        final MaterialEditText editSenha = layout_login.findViewById(R.id.edit_senha);

        alerta.setView(layout_login);

        //Botao de confirmacao
        alerta.setPositiveButton("LOGAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                btnLogar.setEnabled(false);//Desativando botao de Login

                //Verificando se o Email esta vazio
                if (TextUtils.isEmpty(editEmail.getText().toString())) {
                    Snackbar.make(loginLayout, "Informe o email", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                //Verificando se a Senha esta vazia
                if (TextUtils.isEmpty(editSenha.getText().toString())) {
                    Snackbar.make(loginLayout, "Informe a senha", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                String email = editEmail.getText().toString();
                String senha = editSenha.getText().toString();

                SpotsDialog alertaEspera = new SpotsDialog(LoginActivity.this);
                alertaEspera.show();

                autenticacao.signInWithEmailAndPassword(email, senha).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        alertaEspera.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        alertaEspera.dismiss();
                        Snackbar.make(loginLayout, "Erro " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                        btnLogar.setEnabled(true);//Habilitando botao de logar
                    }
                });

            }
        });

        //Botao de cancelamento
        alerta.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //Mostrando o alerta
        alerta.show();

    }

    private void mostrarAlertaCadastrar() {
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setTitle("CADASTAR");
        alerta.setMessage("Cadastre com o email");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_cadastrar = inflater.inflate(R.layout.layout_cadastrar, null);

        final MaterialEditText editEmail = layout_cadastrar.findViewById(R.id.edit_email);
        final MaterialEditText editSenha = layout_cadastrar.findViewById(R.id.edit_senha);
        final MaterialEditText editNome = layout_cadastrar.findViewById(R.id.edit_nome);
        final MaterialEditText editTelefone = layout_cadastrar.findViewById(R.id.edit_telefone);

        alerta.setView(layout_cadastrar);


        //Botao de confirmacao
        alerta.setPositiveButton("CADASTRAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Verificando se o Email esta vazio
                if (TextUtils.isEmpty(editEmail.getText().toString())) {
                    Snackbar.make(loginLayout, "Informe o email", Snackbar.LENGTH_LONG).show();
                    return;
                }

                //Verificando se a Senha esta vazia
                if (TextUtils.isEmpty(editSenha.getText().toString())) {
                    Snackbar.make(loginLayout, "Informe a senha", Snackbar.LENGTH_LONG).show();
                    return;
                }

                //Verificando se o Nome esta vazio
                if (TextUtils.isEmpty(editNome.getText().toString())) {
                    Snackbar.make(loginLayout, "Informe o nome", Snackbar.LENGTH_LONG).show();
                    return;
                }

                //Verificando se o Telefone esta vazio
                if (TextUtils.isEmpty(editNome.getText().toString())) {
                    Snackbar.make(loginLayout, "Informe o nome", Snackbar.LENGTH_LONG).show();
                    return;
                }

                SpotsDialog alertaEspera = new SpotsDialog(LoginActivity.this);
                alertaEspera.show();

                String nome = editNome.getText().toString();
                String email = editEmail.getText().toString();
                String senha = editSenha.getText().toString();
                String telefone = editTelefone.getText().toString();

                autenticacao.createUserWithEmailAndPassword(email, senha).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //Criando objeto do Usuario
                        Usuario novoUsuario = new Usuario();
                        novoUsuario.setId(authResult.getUser().getUid());
                        novoUsuario.setNome(nome);
                        novoUsuario.setEmail(email);
                        novoUsuario.setSenha(senha);
                        novoUsuario.setTelefone(telefone);

                        dataBase.collection("usuarios").document(novoUsuario.getId()).set(novoUsuario).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Snackbar.make(loginLayout, "Usuário cadastrado com sucesso!", Snackbar.LENGTH_LONG).show();

                                intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();

                                alertaEspera.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                alertaEspera.dismiss();
                                Snackbar.make(loginLayout, "Erro " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        alertaEspera.dismiss();
                        Snackbar.make(loginLayout, "Erro " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });

            }
        });

        //Botao de cancelamento
        alerta.setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //Mostrando o alerta
        alerta.show();

    }

}
