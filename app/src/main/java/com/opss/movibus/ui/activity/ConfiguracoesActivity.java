package com.opss.movibus.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.opss.movibus.R;
import com.opss.movibus.firebase.Firebase;
import com.opss.movibus.model.Usuario;
import com.opss.movibus.util.ImagemUtils;
import com.opss.movibus.util.PermissoesUtils;
import com.opss.movibus.util.SharedPrefManager;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfiguracoesActivity extends AppCompatActivity {

    private CircleImageView imgPerfil;
    private TextView textNome;
    private TextView textEmail;

    private PreferencesFragment fragment;
    private Usuario usuario;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracao);

        this.imgPerfil = findViewById(R.id.img_perfil);
        this.textNome = findViewById(R.id.txt_nome);
        this.textEmail = findViewById(R.id.txt_email);

        this.fragment = new PreferencesFragment();

        getFragmentManager().beginTransaction().replace(R.id.frame_preferences, fragment).commit();

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
        fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.linear_conta) {
            startActivity(new Intent(this, ConfiguracoesContaActivity.class));
        }
    }

    private void mostrarDados() {
        Firebase.get().getFireUsuario().getUserDocument().addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e == null) {
                    usuario = documentSnapshot.toObject(Usuario.class);

                    textNome.setText(usuario.getNome());
                    textEmail.setText(usuario.getEmail());

                    ImagemUtils.picassoUserImage(getApplicationContext(), imgPerfil);
//                    Uri uri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
//                    Picasso.with(getApplicationContext())
//                            .load(uri)
//                            .placeholder(R.drawable.baseline_account_circle_white_48dp)
//                            .error(R.drawable.baseline_account_circle_white_48dp)
//                            .into(imgPerfil);

                } else { //falha
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    Toast.makeText(getApplicationContext(), "Falha de autenticacao", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    public static class PreferencesFragment extends PreferenceFragment {

        private Context context;
        private SharedPrefManager prefManager;

        private SwitchPreference notificacoesSwitch;
        private SwitchPreference localizacaoGPSSwitch;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            this.context = getActivity().getApplicationContext();

            this.prefManager = new SharedPrefManager(context);

            this.notificacoesSwitch = (SwitchPreference) findPreference("key_notificacoes");
            this.notificacoesSwitch.setChecked(prefManager.getNotification());

            this.localizacaoGPSSwitch = (SwitchPreference) findPreference("key_localizacao_gps");
            this.localizacaoGPSSwitch.setChecked(prefManager.getLocationGPS());

            notificacoesSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    prefManager.setNotification(context, (boolean) newValue);
                    return true;
                }
            });

            localizacaoGPSSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    prefManager.setLocationGPS(context, (boolean) newValue);
                    if ((boolean) newValue) {
                        PermissoesUtils.verificarPermissaoLocation(getActivity(), context);
                    }
                    return true;
                }
            });
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == PermissoesUtils.REQUEST_LOCATION && grantResults[0] == PackageManager.PERMISSION_DENIED) {//GPS Location Request
                localizacaoGPSSwitch.setChecked(false);
                prefManager.setLocationGPS(context, false);
            }
        }
    }

}
