package com.opss.movibus.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opss.movibus.firebase.Firebase;
import com.opss.movibus.location.camera.Acompanhar;
import com.opss.movibus.location.camera.NaoAcompanhar;
import com.opss.movibus.model.Linha;
import com.opss.movibus.model.LinhaFavorita;
import com.opss.movibus.model.Onibus;
import com.opss.movibus.model.PontoFavorito;
import com.opss.movibus.model.PontoOnibus;
import com.opss.movibus.ui.activity.BottomDrawer;
import com.opss.movibus.ui.activity.MainActivity;
import com.opss.movibus.ui.fragment.marker.MarkerObjetct;
import com.opss.movibus.ui.fragment.marker.OnibusMarker;
import com.opss.movibus.ui.fragment.marker.PontoMarker;
import com.opss.movibus.util.PermissoesUtils;
import com.opss.movibus.util.SharedPrefManager;

import static android.content.Context.LOCATION_SERVICE;


public class MapsFragment extends SupportMapFragment implements OnMapReadyCallback, LocationListener,
        GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    private MainActivity activity;
    private GoogleMap mMap;

    private LocationManager locationManager;
    private Location mLastLocation;
    private static final long MIN_DISTANCE_FOR_UPDATE = 10;
    private static final long MIN_TIME_FOR_UPDATE = 1000 * 60 * 2;

    private BottomDrawer appDrawer;

    private static int ZOOM_MAX = 20;
    private static int ZOOM_MIN = 12;
    private static int ZOOM_SELECT = 18;

    private LatLng centroConquista = new LatLng(-14.86, -40.839);

    private Map<String, Object> onibusMap;
    private Map<String, Object> pontoOnibusMap;
    private Map<String, Map<String, Object>> markerMap;

    private Map<String, Map<String, Object>> lista;

    //Firebase
    private FirebaseFirestore dataBase = FirebaseFirestore.getInstance();
    private CollectionReference onibusCollections;
    private GeoFire geoFire;

    private OnibusMarker onibusAcompanhando = null;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getMapAsync(this);

        this.activity = (MainActivity) getActivity();
        //this.appDrawer = activity.vh.appDrawer;
        this.appDrawer = MainActivity.appDrawer;

        onibusMap = new HashMap<>();
        pontoOnibusMap = new HashMap<>();
        markerMap = new HashMap<>();
        lista = new HashMap<>();

        markerMap.put("onibus", onibusMap);
        markerMap.put("ponto", pontoOnibusMap);

        locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
        if (new SharedPrefManager(getContext()).getLocationGPS() && PermissoesUtils.resquestLocationPermission(getContext())) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
            locationManager2();
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

        if (new SharedPrefManager(getContext()).getLocationGPS()) {
            if (PermissoesUtils.resquestLocationPermission(getContext())) {//Verificando as permissoes de acesso ao GPS
                mMap.setMyLocationEnabled(true);
                locationManager2();
            } else {
                PermissoesUtils.verificarPermissaoLocation(activity, getContext());//Solicitando acesso ao GPS
            }
        }

        LatLngBounds conquista = new LatLngBounds(new LatLng(-14.90, -40.91), new LatLng(-14.81, -40.78)); // Setando limites do mapa com coordenadas sudoestes e nordestes
        mMap.setLatLngBoundsForCameraTarget(conquista);//Limitando a visao do Google Maps

        mMap.setMinZoomPreference(ZOOM_MIN);//Setendo ZOM minimo
        mMap.setMaxZoomPreference(ZOOM_MAX);//Setando ZOM maximo
        mMap.moveCamera(CameraUpdateFactory.newLatLng(centroConquista));//Movendo a camera para o centro de Conquista

        mMap.setOnMyLocationButtonClickListener(this);//Action do MyLocationButton
        mMap.setOnMapClickListener(this);//Action ao clicar sobre o Mapa
        mMap.setOnMarkerClickListener(this);//Action ao clicar sobre o Marker

        mMap.setTrafficEnabled(false);//Desabilitando o trafico
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);//Habilitando controle de Zoom

        firebaseConectionsLinha();
        //firebaseConectionsOnibus();
        firebaseConectionsPontoOnibus();
    }

    private void firebaseConectionsLinha() {
        Firebase.get().getFireLinha().getCollection().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Linha linha = doc.toObject(Linha.class);
                        if (linha != null) {
                            Map<String, Object> onibusMap = new HashMap<>();
                            lista.put(linha.getId(), onibusMap);
                        }
                    }
                    firebaseConectionsOnibus();
                }
            }
        });
    }

    private void firebaseConectionsOnibus() {

        Firebase.get().getFireOnibus().getCollection().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Onibus onibus = doc.toObject(Onibus.class);

                        OnibusMarker onibusMarker = new OnibusMarker(onibus);//Criando Marker do Onibus
                        onibusMarker.setDocumentReference(doc.getReference());//Adicionando Referencia do Documento
                        onibusMarker.getMarker(mMap);//Adicionando Marker no Google Maps
                        onibusMarker.getMarker().setTag(onibus);//Adicionando o Onibus como Tag no Marker

                        onibus.setMarker(onibusMarker);//Adicionando o marker ao Objeto Onibus
                        onibus.atualizarLinha();//Recebendo o Objeto Linha no Onibus

                        LinhaFavorita linhaFavoritada = MainActivity.LINHAS_FAVORITAS.get(onibus.getIdLinha());
                        if (linhaFavoritada != null) {
                            linhaFavoritada.addOnibusOnline();
                        }

                        lista.get(onibus.getIdLinha()).put(onibus.getId(), onibusMarker);
                        onibusMap.put(doc.getId(), onibusMarker);
                    }
                }
            }
        });

//        dataBase.collection(Onibus.COLECAO).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isComplete()) {
//                    for (QueryDocumentSnapshot doc : task.getResult()) {
//                        Onibus onibus = doc.toObject(Onibus.class);
//
//                        OnibusMarker onibusMarker = new OnibusMarker(onibus);
//                        onibusMarker.setDocumentReference(doc.getReference());
//                        onibusMarker.getMarker(mMap);
//                        //onibusMarker.marker.setTag(onibus);
//                        onibusMarker.getMarker().setTag(onibus);
//
//                        onibus.setMarker(onibusMarker);
//
//                        onibus.getIdLinha();
//
//                        LinhaFavorita l = MainActivity.LINHAS_FAVORITAS.get(onibus.getIdLinha());
//                        if (l != null) {
//                            l.addOnibusOnline();
//                        }
//
//                        onibus.atualizarLinha();
//
//                        onibusMap.put(doc.getId(), onibusMarker);
//                    }
//                }
//            }
//        });

    }

    private void firebaseConectionsPontoOnibus() {

//        LatLng l1 = new LatLng(-14.887643, -40.802569);
//        LatLng l2 = new LatLng(-14.888117, -40.802987);
//        LatLng l3 = new LatLng(-14.888425, -40.803313);
//        LatLng l4 = new LatLng(-14.889932, -40.804553);
//        LatLng l5 = new LatLng(-14.891238, -40.805761);
//
//        List<LatLng> l = new ArrayList<>();
//        l.add(l1);
//        l.add(l2);
//        l.add(l3);
//        l.add(l4);
//        l.add(l5);
//
//        Rota rota = new Rota();
//        rota.setId(Firebase.get().getFireRota().generateKey());
//        rota.setCoordenadas(l);

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

                        pontoOnibusMap.put(ponto.getId(), pontoMarker);
                    }
                }
            }
        });

//        dataBase.collection(PontoOnibus.COLECAO).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isComplete()) {
//                    for (QueryDocumentSnapshot doc : task.getResult()) {
//                        PontoOnibus ponto = doc.toObject(PontoOnibus.class);
//                        PontoMarker pontoMarker = new PontoMarker(ponto);
//                        pontoMarker.getMarker(mMap);
//                        pontoMarker.getMarker().setTag(ponto);
//
//                        ponto.setMarker(pontoMarker);
//
//                        //rota.addPonto(ponto);
//                        pontoOnibusMap.put(ponto.getId(), pontoMarker);
//                    }
//
//                    //Firebase.get().getFireRota().setDocument(rota);
//                }
//            }
//        });

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PermissoesUtils.REQUEST_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {//GPS Location Request
            if (permissions.length > 0 && permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) && permissions[1].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Snackbar.make(getView(), "GPS Conectado", Snackbar.LENGTH_LONG).show();
                locationManager2();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            Snackbar.make(getView(), "GPS Desconectado", Snackbar.LENGTH_LONG).show();
            new SharedPrefManager(getContext()).setLocationGPS(getContext(), false);
        }

    }

    private void teste() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(activity, "Sim", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(activity, "Nao", Toast.LENGTH_SHORT).show();
        }
    }

    private void buildAlertMessageNoGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("O GPS está desativado, deseja ativá-lo?")
                .setCancelable(true)
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean resquestLocationPermission() {
        boolean fineLocation = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseLocation = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        return  fineLocation && coarseLocation;
        //return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void locationManager2() {
        //Localizacao via GPS
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_FOR_UPDATE, MIN_DISTANCE_FOR_UPDATE, this);
        //Localizacao via INTERNET
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_FOR_UPDATE, MIN_DISTANCE_FOR_UPDATE, this);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Toast.makeText(activity, "GPS Provider", Toast.LENGTH_SHORT).show();
            return;
        }

//        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//            mLastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            Toast.makeText(activity, "Network Provider", Toast.LENGTH_SHORT).show();
//            return;
//        }
    }

    @SuppressLint("MissingPermission")
    private void locationManager() {
        //locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);

        //Localizacao via GPS
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        //Localizacao via INTERNET
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            Location lat = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lat != null) {
                mLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                moveCamera(lat, ZOOM_MAX);
            }

        } else {

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Location lat = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (lat != null) {
                    mLastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    moveCamera(lat, ZOOM_MAX);
                }
            }

        }
    }

    public void iniciarLocalizacao2() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //String locationProviders = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ALLOWED_GEOLOCATION_ORIGINS);

            LocationManager service = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
            boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // Check if enabled and if not send user to the GPS settings
            if (!enabled) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }



            if (locationManager == null) {
                locationManager2();
            }

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (mLastLocation != null) {
                    moveCamera(mLastLocation, ZOOM_MAX);
                }
                return;
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                mLastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (mLastLocation != null) {
                    moveCamera(mLastLocation, ZOOM_MAX);
                }
                return;
            }

        } else {

        }
    }

    public void iniciarLocalizacao() {
//        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
//            ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
//            //boolean s = ((LocationManager) activity.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
//            boolean s = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//
//            Log.i("CASSIO", String.valueOf(s));
//
//            //Localizacao via GPS
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
//            //Localizacao via INTERNET
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
//
//            mMap.setMyLocationEnabled(true);
//        }
    }

    public void pararLocalizacaoGPS() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (locationManager != null) {
                locationManager.removeUpdates(this);//Removendo a localizacao do GPS
            }

            if (mMap != null) {
                mMap.setMyLocationEnabled(false);
            }

            mLastLocation = null;
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        //locationManager();
        iniciarLocalizacao2();
        return true;
    }

    //Muda a visao da Camera para uma coordenada
    public void moveCamera(LatLng latLng, float zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public void moveCamera(Location location, float zoom) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
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
        appDrawer.close();

        //Market do ONIBUS
        if (marker.getTag() instanceof Onibus) {
            Onibus onibusSelecionado = (Onibus) marker.getTag();
            marker.showInfoWindow();
            moveCamera(marker.getPosition(), ZOOM_SELECT);

            if (onibusAcompanhando != null) {
                if (onibusAcompanhando.getOnibus().getId().equals(onibusSelecionado.getId())) {
                    onibusSelecionado = onibusAcompanhando.getOnibus();
                }
            }

            appDrawer.abrir(onibusSelecionado, marker);

            LinhaFavorita linhaFavorita = MainActivity.LINHAS_FAVORITAS.get(onibusSelecionado.getIdLinha());

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

            PontoFavorito pontoFavorito = MainActivity.PONTOS_FAVORITOS.get(pontoSelecionado.getId());

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

    }

    @Override
    public void onProviderDisabled(String s) {

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
        }

    }

    public void verItinerario(Onibus onibus) {
        List<LatLng> decodedPath = null;

//        Firebase.get().getFireRota().getCollection().document(onibus.getLinha().getIdRotaIda()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                Rota rota = documentSnapshot.toObject(Rota.class);
//                for (LatLng latLng : rota.getCoordenadas()) {
//                    decodedPath.add(latLng);
//                }
//                mMap.addPolyline(new PolylineOptions().addAll(decodedPath).color(Color.GRAY));
//            }
//        });


        //        Firebase.get().getFireRota().getCollection().document(onibus.getLinha().getIdRotaIda()).get("coordenadas").addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isComplete()) {
//                    for (QueryDocumentSnapshot doc : task.getResult()) {
//                        LatLng l1 = doc.toObject(LatLng.class);
//                        decodedPath.add(l1);
//                        Log.i("CASSIO", "aqui");
//                    }
//                    //mMap.addPolyline(new PolylineOptions().addAll(decodedPath).color(Color.GRAY));
//                }
//            }
//        });

//        Firebase.get().getFireRota().getCollection().document(onibus.getLinha().getIdRotaIda()).collection("coordenadas").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//
//            }
//        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//
//            }
//        });


//        decodedPath.add(new LatLng(0,0)); // latitude e longitude
//        decodedPath.add(new LatLng(1,1)); // latitude e longitude
//        decodedPath.add(new LatLng(2,2)); // latitude e longitude
//        mMap.addPolyline(new PolylineOptions().addAll(decodedPath).color(Color.GRAY));
    }

    public void activityResultLinha(String idLinha) {

        for (Object marker : onibusMap.values()) {
            ((OnibusMarker) marker).getMarker().setVisible(false);
        }

        Map<String, Object> map = lista.get(idLinha);

        if (map == null) {
            Snackbar.make(getView(), "Essa linha não possui Ônibus Online", Snackbar.LENGTH_LONG).show();
            return;
        }

        if (map.isEmpty()) {
            Snackbar.make(getView(), "Essa linha não possui Ônibus Online", Snackbar.LENGTH_LONG).show();
            return;
        }

        for (Object marker : map.values()) {
            ((OnibusMarker) marker).getMarker().setVisible(true);
        }

        Snackbar.make(getView(), "Linha filtrada", Snackbar.LENGTH_LONG).show();

    }

    public void mostrarTodosOsOnibus() {
        for (Object marker : onibusMap.values()) {
            ((OnibusMarker) marker).getMarker().setVisible(true);
        }
    }

    public void activityResultPontoOnibus(String idPonto) {
        PontoMarker marker = (PontoMarker) pontoOnibusMap.get(idPonto);

        if (marker == null) {
            return;
        }

        marker.getMarker().showInfoWindow();
        moveCamera(marker.getMarker().getPosition(), ZOOM_SELECT);
    }

    private void pontosDeOnibus() {

        LatLng l1 = new LatLng(-14.887643, -40.802569);
        LatLng l2 = new LatLng(-14.888117, -40.802987);
        LatLng l3 = new LatLng(-14.888425, -40.803313);
        LatLng l4 = new LatLng(-14.889932, -40.804553);
        LatLng l5 = new LatLng(-14.891238, -40.805761);

//        adicionarPontoMapa(l1);
//        adicionarPontoMapa(l2);
//        adicionarPontoMapa(l3);
//        adicionarPontoMapa(l4);
//        adicionarPontoMapa(l5);


    }

    private void adicionarOnibusMapa(LatLng latLng) {
        String id = Firebase.get().getFireOnibus().generateKey();

        Onibus onibus = new Onibus();
        onibus.setId(id);
        onibus.setIdLinha("i57qiNbMpjR6FH7WEakG");
        onibus.setPosicao(latLng.latitude, latLng.longitude);

        OnibusMarker onibusMarker = new OnibusMarker(onibus);
        Marker marker = onibusMarker.getMarker(mMap);

        Firebase.get().getFireOnibus().setDocument(onibus);
    }

    private void adicionarPontoMapa(LatLng latLng) {
//        Ponto ponto = new Ponto();
//        ponto.setIdentificador(latLng.toString());
//        ponto.setLatitude(latLng.latitude);
//        ponto.setLongitude(latLng.longitude);

        String id = dataBase.collection(PontoOnibus.COLECAO).document().getId();

        PontoOnibus ponto = new PontoOnibus();
        ponto.setId(id);
        ponto.setPosicao(latLng.latitude, latLng.longitude);

        PontoMarker pontoMarker = new PontoMarker(ponto);
        Marker marker = pontoMarker.getMarker(mMap);
        marker.setTag(ponto);

        dataBase.collection(PontoOnibus.COLECAO).document(id).set(ponto);
    }

    private void adicionarLinha() {
        String id = Firebase.get().getFireLinha().generateKey();

        Linha linha = new Linha();
        linha.setId(id);
        linha.setNome("D36");
        linha.setVia("Fainor");
        linha.setOrigem("Conquista VI");
        linha.setDestino("UESB");
        linha.setIdRotaIda("GsvFy5FgzDjI6kRkzG6p");
        linha.setIdRotaVolta("haeZjzAyDUzsdt08Wex0");

        Firebase.get().getFireLinha().setDocument(linha);
    }

}
