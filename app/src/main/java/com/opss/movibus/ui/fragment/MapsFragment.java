package com.opss.movibus.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opss.movibus.R;
import com.opss.movibus.firebase.Firebase;
import com.opss.movibus.location.camera.Acompanhar;
import com.opss.movibus.location.camera.NaoAcompanhar;
import com.opss.movibus.model.Coordenada;
import com.opss.movibus.model.Linha;
import com.opss.movibus.model.LinhaFavorita;
import com.opss.movibus.model.Onibus;
import com.opss.movibus.model.PontoFavorito;
import com.opss.movibus.model.PontoOnibus;
import com.opss.movibus.model.Rota;
import com.opss.movibus.ui.activity.BottomDrawer;
import com.opss.movibus.ui.activity.MainActivity;
import com.opss.movibus.ui.fragment.marker.OnibusMarker;
import com.opss.movibus.ui.fragment.marker.PontoMarker;
import com.opss.movibus.util.PermissoesUtils;
import com.opss.movibus.util.SharedPrefManager;

import static android.content.Context.LOCATION_SERVICE;


public class MapsFragment extends SupportMapFragment implements OnMapReadyCallback, LocationListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MapsFragment";

    private MainActivity activity;
    private GoogleMap mMap;

    private LocationManager locationManager;
    private static final long MIN_DISTANCE_FOR_UPDATE = 10;
    private static final long MIN_TIME_FOR_UPDATE = 1000 * 60 * 2;

    private BottomDrawer appDrawer;

    private static int ZOOM_MAX = 20;
    private static int ZOOM_MIN = 12;
    private static int ZOOM_SELECT = 18;

    private LatLng centroConquista = new LatLng(-14.86, -40.839);

    public static CollectionFirebase COLLECTIONS;
    private OnibusMarker onibusAcompanhando = null;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getMapAsync(this);

        this.activity = (MainActivity) getActivity();
        this.appDrawer = MainActivity.appDrawer;

        locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (new SharedPrefManager(getContext()).getLocationGPS() && PermissoesUtils.resquestLocationPermission(getContext())) {
            if (PermissoesUtils.resquestLocationPermission(getContext())) {
                if (verificarGPSLigado()) {
                    if (mMap != null) {
                        mMap.setMyLocationEnabled(true);
                        locationManager2();
                    }
                }
            }
        } else {
            pararLocalizacaoGPS();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pararLocalizacaoGPS();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        Log.d(TAG, "onMapReady");

        if (new SharedPrefManager(getContext()).getLocationGPS()) {
            if (PermissoesUtils.resquestLocationPermission(getContext())) {//Verificando as permissoes de acesso ao GPS
                activity.vh.locationButton.setImageResource(R.drawable.baseline_gps_fixed_24);
                if (verificarGPSLigado()) {
                    mMap.setMyLocationEnabled(true);
                    locationManager2();
                }
            } else {
                PermissoesUtils.verificarPermissaoLocation(activity, getContext());//Solicitando acesso ao GPS
            }
        }

        LatLngBounds conquista = new LatLngBounds(new LatLng(-14.90, -40.91), new LatLng(-14.81, -40.78)); // Setando limites do mapa com coordenadas sudoestes e nordestes
        mMap.setLatLngBoundsForCameraTarget(conquista);//Limitando a visao do Google Maps

        mMap.setMinZoomPreference(ZOOM_MIN);//Setendo ZOM minimo
        mMap.setMaxZoomPreference(ZOOM_MAX);//Setando ZOM maximo
        mMap.moveCamera(CameraUpdateFactory.newLatLng(centroConquista));//Movendo a camera para o centro de Conquista

        //mMap.setOnMyLocationButtonClickListener(this);//Action do MyLocationButton
        mMap.setOnMapClickListener(this);//Action ao clicar sobre o Mapa
        mMap.setOnMarkerClickListener(this);//Action ao clicar sobre o Marker

        mMap.setTrafficEnabled(false);//Desabilitando o trafico
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        //mMap.getUiSettings().setZoomControlsEnabled(true);//Habilitando controle de Zoom

        COLLECTIONS = new CollectionFirebase();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissoesUtils.REQUEST_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {//GPS Location Request
            if (permissions.length > 0 && permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) && permissions[1].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                //activity.vh.locationButton.setImageResource(R.drawable.baseline_gps_fixed_24);
                locationManager2();
            }
        } else {
            new SharedPrefManager(getContext()).setLocationGPS(getContext(), false);
            activity.vh.locationButton.setImageResource(R.drawable.baseline_gps_not_fixed_24);
            //Snackbar.make(getView(), "GPS Desativado", Snackbar.LENGTH_LONG).show();
        }
    }

    private boolean verificarGPSLigado() {
        //Verificar se o GPS do dispositivo esta ligado
        LocationManager service = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        return service.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @SuppressLint("MissingPermission")
    private void locationManager2() {
        if (!verificarGPSLigado()) {//Verifica se o GPS esta ligado
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);//Inicia a tela de configuracao do GPS
        } else {//GPS Ligado
            activity.vh.locationButton.setImageResource(R.drawable.baseline_gps_fixed_24);
            //Snackbar.make(getView(), "GPS Ativado", Snackbar.LENGTH_LONG).show();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_FOR_UPDATE, MIN_DISTANCE_FOR_UPDATE, this);

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    moveCamera(lastLocation, ZOOM_MAX);
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void pararLocalizacaoGPS() {
        if (PermissoesUtils.resquestLocationPermission(getContext())) {
            if (locationManager != null) {
                locationManager.removeUpdates(this);
            }
            if (mMap != null) {
                mMap.setMyLocationEnabled(false);
            }
        }
    }

    public void onMyLocation() {
        if (new SharedPrefManager(getContext()).getLocationGPS()) {
            if (PermissoesUtils.resquestLocationPermission(getContext())) {//Verificando as permissoes de acesso ao GPS
                locationManager2();
            } else {
                PermissoesUtils.verificarPermissaoLocation(activity, getContext());//Solicitando acesso ao GPS
            }
        } else {
            new SharedPrefManager(getContext()).setLocationGPS(getContext(), true);
            if (PermissoesUtils.resquestLocationPermission(getContext())) {
                //activity.vh.locationButton.setImageResource(R.drawable.baseline_gps_fixed_24);
                //Snackbar.make(getView(), "GPS Conectado", Snackbar.LENGTH_LONG).show();
                //locationManager2();
                //iniciarLocalizacao2();
            } else {
                PermissoesUtils.verificarPermissaoLocation(activity, getContext());//Solicitando acesso ao GPS
            }
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (onibusAcompanhando != null) {
                    Rota rota = onibusAcompanhando.getOnibus().getLinha().getRotaIDA();
                    Marker marker = onibusAcompanhando.getMarker();
                    //for (Coordenada co : rota.getCoordenadas()) {
                        //Log.d(TAG, "run: " + co.toString());
                        marker.setPosition(new LatLng(-14.86410466, -40.81881584));
                        //onibusAcompanhando.getMarker().setPosition(new LatLng(co.latitude, co.longitude));
                        //SystemClock.sleep(100);
                    //}
                }
            }
        });
    }

    @Override
    public void onMapClick(LatLng latLng) {

        if (onibusAcompanhando != null && onibusAcompanhando.getOnibus().getAcompanhando()) {
            onibusAcompanhando.getMarker().showInfoWindow();
        }

        appDrawer.closeAnimate();

        //adicionarOnibusMapa(latLng);
        //adicionarPontoMapa("Ponto", latLng);
        //adicionarOnibusMapa("R09",latLng);
        //adicionarLinha();

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (appDrawer.isVisible()) {
            appDrawer.close();
        }

        //Market do ONIBUS
        if (marker.getTag() instanceof Onibus) {
            Onibus onibusSelecionado = (Onibus) marker.getTag();
            marker.showInfoWindow();
            //moveCamera(marker.getPosition(), ZOOM_SELECT);

            if (onibusAcompanhando != null) {
                if (onibusAcompanhando.getOnibus().getId().equals(onibusSelecionado.getId())) {
                    onibusSelecionado = onibusAcompanhando.getOnibus();
                }
            }

            onibusAcompanhando = onibusSelecionado.getMarker();

            appDrawer.abrir(onibusSelecionado, marker);
            //verItinerario(onibusSelecionado);

            LinhaFavorita linhaFavorita = COLLECTIONS.linhasFavoritas.get(onibusSelecionado.getIdLinha());
            if (linhaFavorita != null) {
                appDrawer.setFavorito(linhaFavorita);
            }

            return true;
        }

        //Market do PONTO
        if (marker.getTag() instanceof PontoOnibus) {
            PontoOnibus pontoSelecionado = (PontoOnibus) marker.getTag();
            marker.showInfoWindow();
            moveCamera(marker.getPosition(), ZOOM_SELECT);

            appDrawer.abrir(pontoSelecionado, marker);

            PontoFavorito pontoFavorito = COLLECTIONS.pontosFavoritos.get(pontoSelecionado.getId());
            if (pontoFavorito != null) {
                appDrawer.setFavorito(pontoFavorito);
            }

            return true;
        }

        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
//        if (mLastLocation != null) {
//            this.mLastLocation = location;
//
//            //tracker.tracker(location.getLatitude(), location.getLongitude());
//
//            if (Utils.isNetworkAvailable(getContext())) {
//                Toast.makeText(activity, "Online", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(activity, "Offline", Toast.LENGTH_SHORT).show();
//            }
//
//        } else {
//            mLastLocation = location;
//        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        //Snackbar.make(getView(), "GPS Conectado", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String s) {

    }

    //Muda a visao da Camera para uma coordenada
    public void moveCamera(LatLng latLng, float zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public void moveCamera(Location location, float zoom) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public void acompanharOnibus(Onibus onibus) {

        if (onibus.getAcompanhando()) {

            if (onibusAcompanhando != null && !onibusAcompanhando.getOnibus().getId().equals(onibus.getId())) {
                onibusAcompanhando.getOnibus().setAcompanhando(false);
                onibusAcompanhando.setCamera(new NaoAcompanhar());
            }

            onibusAcompanhando = onibus.getMarker();

            onibus.getMarker().setCamera(new Acompanhar(mMap));
            onibus.getMarker().getMarker().showInfoWindow();
            moveCamera(new LatLng(onibus.getLatitude(), onibus.getLongitude()), ZOOM_SELECT);

        } else {
            onibusAcompanhando.getOnibus().setAcompanhando(false);
            onibusAcompanhando.setCamera(new NaoAcompanhar());
            appDrawer.closeAnimate();
        }

    }

    public void verItinerario(Onibus onibus) {
        if (onibus.getLinha() == null) {
            return;
        }

        Firebase.get().getFireRota().getCollectionCoordenadas(onibus.getLinha().getIdRotaIda()).orderBy("numero", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                List<Coordenada> teste = queryDocumentSnapshots.toObjects(Coordenada.class);

                List<LatLng> decodedPath = new ArrayList<>();

                Rota rota = new Rota();
                rota.setCoordenadas(teste);
                onibus.getLinha().setRotaIDA(rota);

                for (Coordenada co : teste) {
                    decodedPath.add(new LatLng(co.latitude, co.longitude));
                }

                Polyline poly = mMap.addPolyline(new PolylineOptions().addAll(decodedPath).color(Color.GREEN));
                //poly.remove();
            }
        });

        Firebase.get().getFireRota().getCollectionCoordenadas(onibus.getLinha().getIdRotaVolta()).orderBy("numero", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                List<Coordenada> teste = queryDocumentSnapshots.toObjects(Coordenada.class);

                List<LatLng> decodedPath = new ArrayList<>();

                Rota rota = new Rota();
                rota.setCoordenadas(teste);
                onibus.getLinha().setRotaVolta(rota);

                for (Coordenada co : teste) {
                    decodedPath.add(new LatLng(co.latitude, co.longitude));
                }

                PolylineOptions polyOptions = new PolylineOptions();
                polyOptions.color(Color.BLUE);
                polyOptions.width(5);
                polyOptions.startCap(new SquareCap());
                polyOptions.endCap(new SquareCap());
                polyOptions.jointType(JointType.ROUND);
                polyOptions.addAll(decodedPath);

                Polyline poly = mMap.addPolyline(polyOptions);
                //poly.remove();
            }
        });

    }

    public void mostrarTodosOsOnibus() {
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
                for (Linha linha : COLLECTIONS.linhasOnibus.values()) {
                    linha.setOnibusVisible(true);
                }
//            }
//        });
    }

    public void activityResultLinha(String idLinha) {
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
                for (Linha linha : COLLECTIONS.linhasOnibus.values()) {
                    linha.setOnibusVisible(false);
                }

                Linha linha = COLLECTIONS.linhasOnibus.get(idLinha);
                linha.setOnibusVisible(true);
                Snackbar.make(getView(), "Linha filtrada", Snackbar.LENGTH_LONG).show();
//            }
//        });
    }

    public void activityResultPontoOnibus(String idPonto) {
        PontoOnibus ponto = COLLECTIONS.pontoOnibus.get(idPonto);
        ponto.getMarker().getMarker().showInfoWindow();
        moveCamera(ponto.getMarker().getMarker().getPosition(), ZOOM_SELECT);
    }

    public class CollectionFirebase implements Serializable {

        public Map<String, LinhaFavorita> linhasFavoritas;
        public Map<String, PontoFavorito> pontosFavoritos;

        public Map<String, Linha> linhasOnibus;
        public Map<String, PontoOnibus> pontoOnibus;

        private ProgressBar progressBar;

        public CollectionFirebase() {
            linhasFavoritas = new HashMap<>();
            pontosFavoritos = new HashMap<>();

            linhasOnibus = new HashMap<>();
            pontoOnibus = new HashMap<>();

            progressBar = activity.findViewById(R.id.progress);

            getLinhaFavoritaCollections();
            getPontoFavoritoCollections();
            getLinhaOnibusCollections();
            getPontoOnibusCollections();
        }

        private void getLinhaFavoritaCollections() {
            Firebase.get().getFireUsuario().getUserDocument().collection(LinhaFavorita.COLECAO).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isComplete()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            LinhaFavorita linha = doc.toObject(LinhaFavorita.class);
                            linha.linha = linhasOnibus.get(linha.getIdLinha());
                            linhasFavoritas.put(linha.getIdLinha(), linha);
                        }
                        getOnibusCollections();
                    }
                }
            });
        }

        private void getPontoFavoritoCollections() {
            Firebase.get().getFireUsuario().getUserDocument().collection(PontoFavorito.COLECAO).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isComplete()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            PontoFavorito ponto = doc.toObject(PontoFavorito.class);
                            pontosFavoritos.put(ponto.getIdPonto(), ponto);
                        }
                    }
                }
            });
        }

        private void getLinhaOnibusCollections() {
            Firebase.get().getFireLinha().getCollection().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isComplete()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Linha linha = doc.toObject(Linha.class);
                            linhasOnibus.put(linha.getId(), linha);
                        }
                        getLinhaFavoritaCollections();
                        //getOnibusCollections();
                    }
                }
            });
        }

        private void getOnibusCollections() {
            Firebase.get().getFireOnibus().getCollection().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isComplete()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Onibus onibus = doc.toObject(Onibus.class);

                            Linha linha = linhasOnibus.get(onibus.getIdLinha());
                            if (linha != null) {
                                linha.setOnibus(onibus);
                                onibus.setLinha(linha);
                            }

                            LinhaFavorita linhaFavorita  = linhasFavoritas.get(onibus.getIdLinha());
                            if (linhaFavorita != null) {
                                linhaFavorita.addOnibusOnline();
                            }

                            OnibusMarker marker = new OnibusMarker(onibus);//Criando Marker do Onibus
                            //marker.setDocumentReference(doc.getReference());//Adicionando Referencia do Documento
                            marker.getMarker(mMap);//Adicionando Marker no Google Maps
                            marker.getMarker().setTag(onibus);//Adicionando o Onibus como Tag no Marker

                            onibus.setMarker(marker);//Adicionando o marker ao Objeto Onibus
                        }

                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }

        private void getPontoOnibusCollections() {
            Firebase.get().getFirePontoOnibus().getCollection().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isComplete()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            PontoOnibus ponto = doc.toObject(PontoOnibus.class);

                            PontoMarker pontoMarker = new PontoMarker(ponto);
                            pontoMarker.getMarker(mMap);
                            pontoMarker.getMarker().setTag(ponto);

                            ponto.setMarker(pontoMarker);

                            pontoOnibus.put(ponto.getId(), ponto);
                        }
                    }
                }
            });
        }

        public void setFavorito(LinhaFavorita linha) {
            linhasFavoritas.put(linha.getIdLinha(), linha);
        }

        public void setFavorito(PontoFavorito ponto) {
            pontosFavoritos.put(ponto.getIdPonto(), ponto);
        }

        public LinhaFavorita getLinhaFavorita(String id) {
            return linhasFavoritas.get(id);
        }

        public PontoFavorito getPontoFavorito(String id) {
            return pontosFavoritos.get(id);
        }

        public Linha getLinha(String id) {
            return linhasOnibus.get(id);
        }

        public PontoOnibus getPontoOnibus(String id) {
            return pontoOnibus.get(id);
        }

        public void removeFavorito(LinhaFavorita linha) {
            linhasFavoritas.remove(linha.getIdLinha());
        }

        public void removeFavorito(PontoFavorito ponto) {
            pontosFavoritos.remove(ponto.getIdPonto());
        }

    }

}
