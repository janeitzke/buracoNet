package com.buraconet.buraconet;

import android.content.Context;

public class Buraco {

    // variáveis privadas
    private Integer _id;
    private Integer _forca;
    private Double _latitude;
    private Double _longitude;
    private Double _altitude;
    private Integer _velocidade;
    private Integer _direcao;
    private String _data;
    private Integer _corteUtilizado;
    private String _mostrar;

    // Construtor vazio
    public Buraco(){
    }

    // _id
    public Integer get_id(){ return this._id; }
    public void set_id(Integer _id){ this._id = _id; }

    // _forca
    public Integer get_forca(){ return this._forca; }
    public void set_forca(Integer _forca){ this._forca = _forca; }

    // _latitude
    public Double get_latitude(){ return this._latitude; }
    public void set_latitude(Double _latitude){ this._latitude = _latitude; }

    // _longitude
    public Double get_longitude(){ return this._longitude; }
    public void set_longitude(Double _longitude){ this._longitude = _longitude; }

    // _altitude
    public Double get_altitude(){ return this._altitude; }
    public void set_altitude(Double _altitude){ this._altitude = _altitude; }

    // _velocidade
    public Integer get_velocidade(){ return this._velocidade; }
    public void set_velocidade(Integer _velocidade){ this._velocidade = _velocidade; }

    // _direcao
    public Integer get_direcao(){ return this._direcao; }
    public void set_direcao(Integer _direcao){ this._direcao = _direcao; }

    // _data
    public String get_data(){ return this._data; }
    public void set_data(String _data){ this._data = _data; }

    // _corteUtilizado
    public Integer get_corteUtilizado(){ return this._corteUtilizado; }
    public void set_corteUtilizado(Integer _corteUtilizado){ this._corteUtilizado = _corteUtilizado; }

    // _mostrar
    public String get_mostrar(){ return this._mostrar; }
    public void set_mostrar(String _mostrar){ this._mostrar = _mostrar; }

    // Limpar a visualização dos buracos
    public void limparVisualizacao(Context _contexto) {
        DatabaseHelper _db;
        _db = new DatabaseHelper(_contexto);
        _db.limparVisualizacao();
    }
}
