package com.buraconet.buraconet;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GpsStatus.Listener, LocationListener, SensorEventListener {

    private DatabaseHelper _db;

    // menu da activity
    private Menu _menu;

    boolean canGetLocation = false;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // metros
    private static final long MIN_TIME_BW_UPDATES = 1000; // milisegundos
    private static final int PERMISSAO_LOCALIZACAO_REQUERIDA = 1; // requereu permissão para usar a localização

    // Declaring a Location Manager
    protected LocationManager locationManager;
    private Location location; // location
    private GoogleMap _mMap;
    private Boolean _movimentandoCamera = false;

    private TextView _txtSpeed;
    private TextView _txtBuraco;

    private Toolbar _toolbar;

    private SensorManager sensorManager;
    Integer _intensidadeBuraco;
    Double _novoBuraco;
    double ax;
    double ay;
    double az;

    private Buraco _buraco;

    private Integer _corBuraco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // instancia o DatabaseHelper
        _db = new DatabaseHelper(getApplicationContext());

        // instancia os controles da tela
        instanciaControles();

        // configura o GPS
        configurarGPS();

        // seta a App Bar
        _toolbar.setTitle(R.string.telaMainActivity_tituloBarra);
        setSupportActionBar(_toolbar);

        // prepara a tela
        Ferramentas.configuraTela(this);
        Ferramentas.tiraTeclado(this);

        // configura o sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        // inicializa o objeto buraco para melhorar a eficiência
        _buraco = new Buraco();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        _menu = menu;

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, _menu);

        // configura as opções da ActionBar
        MenuItem _item;

        _item = _menu.findItem(R.id.telaMain_actionLimpar);
        _item.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        // Opções ainda não implementadas
        if (id == R.id.telaMain_actionLimpar) {
            Buraco _buraco = new Buraco();
            _buraco.limparVisualizacao(this);
            _mMap.clear();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();  // Always call the superclass method first
        stopUsingGPS();
    }

    private void instanciaControles() {
        _toolbar = (Toolbar)findViewById(R.id.telaProvaNavegacao_toolbar);
        _txtSpeed = (TextView) findViewById(R.id.txtSpeed);
        _txtBuraco = (TextView) findViewById(R.id.txtBuraco);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.telaMainActivity_mensagemSemPermissaoGPS, Toast.LENGTH_SHORT).show();
            return;
        }
        _mMap = googleMap;
        _mMap.setMyLocationEnabled(true);
        _mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        _mMap.animateCamera(CameraUpdateFactory.zoomTo(Ferramentas.ZOOM_CAMERA));
        _mMap.getUiSettings().setZoomControlsEnabled(true);
        _mMap.getUiSettings().setCompassEnabled(true);
        _mMap.getUiSettings().setScrollGesturesEnabled(true);

        // seta o locationSource do mapa
        _mMap.setLocationSource(new MainActivity.CurrentLocationProvider(this));

        // seta o evento de movimento da camera
        _mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                _movimentandoCamera = true;
            }
        });

        // seta o botão de my location
        _mMap.setOnMyLocationButtonClickListener( new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick () {
                _movimentandoCamera = false;
                return true;
            }
        });

        carregarBuracos();

    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()== Sensor.TYPE_ACCELEROMETER){
            ax=event.values[0];
            ay=event.values[1];
            az=event.values[2];
            _novoBuraco = (Math.abs(ax)+Math.abs(ay)+Math.abs(az))-Ferramentas.INTENSIDADE_BURACO_CORTE;
            if(_intensidadeBuraco==null) {
                _intensidadeBuraco = 0;
            }
            if (_novoBuraco.intValue() > _intensidadeBuraco) {
                _intensidadeBuraco = _novoBuraco.intValue();
            }
        }
    }

    private void carregarBuracos() {

        int _icone;

        Cursor _buracos = _db.consultaBuracosVisiveis();
        while (_buracos.moveToNext()) {
            // define qual ícone será apresentado
            if (_buracos.getInt(1) < 3) {
                _icone = R.drawable.marker_amarelo;
            } else if (_buracos.getInt(1) < 5) {
                _icone = R.drawable.marker_laranja;
            } else {
                _icone = R.drawable.marker_vermelho;
            }
            // cria o marcador do buraco
            Marker _marker = _mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(_buracos.getDouble(2), _buracos.getDouble(3)))
                    .title(getResources().getString(R.string.telaMainActivity_nomeMarcador))
                    .icon(BitmapDescriptorFactory.fromResource(_icone))
                    .snippet(getResources().getString(R.string.telaMainActivity_descricaoMarcador)+" "+String.format("%1d",_buracos.getInt(1))));

        }
        _buracos.close();

    }

    public class CurrentLocationProvider implements LocationSource, LocationListener
    {
        private OnLocationChangedListener listener1;
        private LocationManager locationManager1;

        private CurrentLocationProvider(Context context) {
            locationManager1 = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        @Override
        public void activate(OnLocationChangedListener listener) {
            this.listener1 = listener;
            LocationProvider gpsProvider = locationManager1.getProvider(LocationManager.GPS_PROVIDER);
            if(gpsProvider != null) {
                int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, R.string.telaMainActivity_mensagemSemPermissaoGPS, Toast.LENGTH_SHORT).show();
                    return;
                }
                locationManager1.requestLocationUpdates(gpsProvider.getName(), 0, 1, this);
            }
        }

        @Override
        public void deactivate() {
            int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, R.string.telaMainActivity_mensagemSemPermissaoGPS, Toast.LENGTH_SHORT).show();
                return;
            }
            locationManager1.removeUpdates(this);
        }

        @Override
        public void onLocationChanged(Location location) {
            if(listener1 != null) {
                listener1.onLocationChanged(location);
            }
        }

        @Override
        public void onProviderDisabled(String provider) { }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }

    }

    @Override
    public void onLocationChanged(Location _location) {
        location = _location;
        carregaCoordenadas();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, R.string.telaMainActivity_mensagemGPSDesabilitado, Toast.LENGTH_LONG).show();
        canGetLocation = false;
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, R.string.telaMainActivity_mensagemGPSHabilitado, Toast.LENGTH_LONG).show();
        configurarGPS();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void configurarGPS() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            // getting GPS status
            Boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                this.canGetLocation = true;
                // verifica a permissão ACCESS_FINE_LOCATION
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Toast.makeText(MainActivity.this, R.string.telaMainActivity_mensagemSemPermissaoGPS, Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSAO_LOCALIZACAO_REQUERIDA);
                        return;
                    }
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, MainActivity.this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    carregaCoordenadas();
                }
            }
            locationManager.addGpsStatusListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                carregaSatelites();
                break;
        }
    }

    private void carregaSatelites() {
        Integer satFixos  = 0; // fixed satellites
        Integer satVistos = 0; // viewed satellites
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.telaMainActivity_mensagemSemPermissaoGPS, Toast.LENGTH_SHORT).show();
            return;
        }
        for (GpsSatellite sat : locationManager.getGpsStatus(null).getSatellites()) {
            if(sat.usedInFix()) {
                satFixos++;
            }
            satVistos++;
        }
        String _textoSatUsados =  satFixos.toString();
        String _textoSatVistos =  satVistos.toString();
        String _textoSatelites = _textoSatUsados + "/" + _textoSatVistos;
        //_txtSatelites.setText(_textoSatelites);
    }

    private void incluiMarcador() {
        // define qual ícone será apresentado
        int _icone;
        if (_intensidadeBuraco<3) {
            _icone = R.drawable.marker_amarelo;
        } else if (_intensidadeBuraco <5) {
            _icone = R.drawable.marker_laranja;
        } else {
            _icone = R.drawable.marker_vermelho;
        }
        // cria o marcador do buraco
        Marker _marker = _mMap.addMarker(new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .title(getResources().getString(R.string.telaMainActivity_nomeMarcador))
                .icon(BitmapDescriptorFactory.fromResource(_icone))
                .snippet(getResources().getString(R.string.telaMainActivity_descricaoMarcador)+" "+_intensidadeBuraco.toString()));
    }

    private void carregaCoordenadas() {

        // trata o buraco (se existir)
        if (_intensidadeBuraco==null) {
            _intensidadeBuraco=0;
        }
        if (_intensidadeBuraco > 0) {
            if (location!=null) {
                if (location.hasSpeed()) {
                    if (location.hasAccuracy()) {
                        if (location.hasAltitude()) {
                            Integer _direcao;
                            if (location.hasBearing()) {
                                _direcao = Math.round(location.getBearing());
                            } else {
                                _direcao = -1;
                            }
                            if (location.getSpeed()*3.6>Ferramentas.VELOCIDADE_CORTE) {
                                SimpleDateFormat _sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                                _buraco.set_forca(_intensidadeBuraco.intValue());
                                _buraco.set_latitude(location.getLatitude());
                                _buraco.set_longitude(location.getLongitude());
                                _buraco.set_altitude(location.getAltitude());
                                _buraco.set_velocidade(Math.round(location.getSpeed()));
                                _buraco.set_direcao(_direcao);
                                _buraco.set_data(_sdf.toString());
                                _buraco.set_corteUtilizado(Ferramentas.INTENSIDADE_BURACO_CORTE);
                                _buraco.set_mostrar(Ferramentas.MOSTRAR_SIM);
                                _db.incluiBuraco(_buraco);
                                _txtBuraco.setText(_intensidadeBuraco.toString());
                                _corBuraco = 0;
                                incluiMarcador();
                            }
                        }
                    }
                }
            }
        }
        _intensidadeBuraco = 0;

        // faz o efeito no texto do buraco
        if (_corBuraco==null) {
            _corBuraco = 15;
        }
        configuraCorTexto();

        double latitude; // latitude
        double longitude; // longitude
        double altitude; // altitude

        // check if GPS enabled
        if(canGetLocation){

            // dados do GPS
            double precisao; // accuracy
            if (location != null) {
                precisao = location.getAccuracy();
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                altitude = location.getAltitude();
            } else {
                precisao = 0;
                latitude = 0;
                longitude = 0;
                altitude = 0;
            }
            String _textoLat = String.format("%.8f", latitude) + "º";
            String _textoLon = String.format("%.8f",longitude) + "º";
            String _textoAlt = String.format("%.0f", altitude) + "m";
            String _textoPre = String.format("%.0f", precisao) + "m";
//            _txtPrecisao.setText(_textoPre);
//            _txtAltitude.setText(_textoAlt);
//            _txtLatitude.setText(_textoLat);
//            _txtLongitude.setText(_textoLon);

            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.telaMainActivity_mensagemSemPermissaoGPS, Toast.LENGTH_SHORT).show();
                return;
            }
            // atualiza o mapa
            if (location!=null) {
                if (_mMap!=null) {
                    if (!_movimentandoCamera) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        _mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, Ferramentas.ZOOM_CAMERA));
                        _mMap.setMyLocationEnabled(true);
                    }
                }
                _txtSpeed.setText(String.format("%.0f", location.getSpeed() * 3.6f));
            }

        } else {

            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            showSettingsAlert();

        }

    }

    /**
     * Function to show settings alert dialog
     * */
    public void showSettingsAlert(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(R.string.telaMainActivity_tituloSettingsGPS);
        alertDialog.setMessage(R.string.telaMainActivity_mensagemSettingsGPS);
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        // verifica a permissão ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(MainActivity.this, R.string.telaMainActivity_mensagemSemPermissaoGPS, Toast.LENGTH_LONG).show();
                return;
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSAO_LOCALIZACAO_REQUERIDA);
                return;
            }
        }
        if(locationManager != null){
            locationManager.removeUpdates(MainActivity.this);
            location = null;
        }
    }


    private void configuraCorTexto() {

        switch (_corBuraco) {
            case 0:
                _txtBuraco.setTextColor(getResources().getColor(R.color.ColorTexto0));
                break;
            case 1:
                _txtBuraco.setTextColor(getResources().getColor(R.color.ColorTexto1));
                break;
            case 2:
                _txtBuraco.setTextColor(getResources().getColor(R.color.ColorTexto2));
                break;
            case 3:
                _txtBuraco.setTextColor(getResources().getColor(R.color.ColorTexto3));
                break;
            case 4:
                _txtBuraco.setTextColor(getResources().getColor(R.color.ColorTexto4));
                break;
            case 5:
                _txtBuraco.setTextColor(getResources().getColor(R.color.ColorTexto5));
                break;
            case 6:
                _txtBuraco.setTextColor(getResources().getColor(R.color.ColorTexto6));
                break;
            case 7:
                _txtBuraco.setTextColor(getResources().getColor(R.color.ColorTexto7));
                break;
            case 8:
                _txtBuraco.setTextColor(getResources().getColor(R.color.ColorTexto8));
                break;
            case 9:
                _txtBuraco.setTextColor(getResources().getColor(R.color.ColorTexto9));
                break;
            case 10:
                _txtBuraco.setTextColor(getResources().getColor(R.color.ColorTextoA));
                break;
            case 11:
                _txtBuraco.setTextColor(getResources().getColor(R.color.ColorTextoB));
                break;
            case 12:
                _txtBuraco.setTextColor(getResources().getColor(R.color.ColorTextoC));
                break;
            case 13:
                _txtBuraco.setTextColor(getResources().getColor(R.color.ColorTextoD));
                break;
            case 14:
                _txtBuraco.setTextColor(getResources().getColor(R.color.ColorTextoE));
                break;
            case 15:
                _txtBuraco.setTextColor(getResources().getColor(R.color.ColorTextoF));
                break;
        }

        if (_corBuraco<15) { _corBuraco++; }

    }

}
