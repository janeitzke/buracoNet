package com.buraconet.buraconet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	// #####   BANCO DE DADOS   #####

	// Versao do banco de dados
	private static final int DATABASE_VERSION = 5;

	// Nome do banco de dados
	private static final String DATABASE_NAME = "buraconet.db";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// nomes das tabelas
	private static final String TABLE_BURACO = "buraco";

	// colunas da tabela BURACO
	private static final String COLUMN_BURACO_FORCA = "forca";
	private static final String COLUMN_BURACO_LATITUDE = "latitude";
	private static final String COLUMN_BURACO_LONGITUDE = "longitude";
	private static final String COLUMN_BURACO_ALTITUDE = "altitude";
	private static final String COLUMN_BURACO_VELOCIDADE = "velocidade";
	private static final String COLUMN_BURACO_DIRECAO = "direcao";
	private static final String COLUMN_BURACO_DATA = "data";
	private static final String COLUMN_BURACO_CORTE_UTILIZADO = "corte";
	private static final String COLUMN_BURACO_MOSTRAR = "indicadorMostrar";

	// constantes das tabelas
	private static final String TEXT_TYPE = " TEXT";
	private static final String NUMBER_TYPE = " NUMBER";
	private static final String COMMA_SEP = ", ";

	// Criando as tabelas
	@Override
	public void onCreate(SQLiteDatabase db) {

		final String SQL_CREATE_BURACO = "CREATE TABLE " + TABLE_BURACO + " (_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_BURACO_FORCA + NUMBER_TYPE + COMMA_SEP +
				COLUMN_BURACO_LATITUDE + NUMBER_TYPE + COMMA_SEP +
				COLUMN_BURACO_LONGITUDE + NUMBER_TYPE + COMMA_SEP +
				COLUMN_BURACO_ALTITUDE + NUMBER_TYPE + COMMA_SEP +
				COLUMN_BURACO_VELOCIDADE + NUMBER_TYPE + COMMA_SEP +
				COLUMN_BURACO_DIRECAO + NUMBER_TYPE + COMMA_SEP +
				COLUMN_BURACO_DATA + TEXT_TYPE + COMMA_SEP +
				COLUMN_BURACO_CORTE_UTILIZADO + TEXT_TYPE + COMMA_SEP +
				COLUMN_BURACO_MOSTRAR + TEXT_TYPE + " )";
		db.execSQL(SQL_CREATE_BURACO);

	}

	// Atualizando o banco de dados
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		// Dropa as tabeles antigas, se existirem
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_BURACO);

		// Cria o banco de dados novamente
		onCreate(db);

	}

	final SQLiteDatabase db = this.getWritableDatabase();

	public void close(){
		db.close();
	}




	// ##### BURACAO #####

	// Inclui Buraco
	public void incluiBuraco(Buraco buraco) {
		final SQLiteDatabase db = this.getWritableDatabase();
		final ContentValues values = new ContentValues();
		values.put(COLUMN_BURACO_FORCA, buraco.get_forca());
		values.put(COLUMN_BURACO_LATITUDE, buraco.get_latitude());
		values.put(COLUMN_BURACO_LONGITUDE, buraco.get_longitude());
		values.put(COLUMN_BURACO_ALTITUDE, buraco.get_altitude());
		values.put(COLUMN_BURACO_VELOCIDADE, buraco.get_velocidade());
		values.put(COLUMN_BURACO_DIRECAO, buraco.get_direcao());
		values.put(COLUMN_BURACO_DATA, buraco.get_data());
		values.put(COLUMN_BURACO_CORTE_UTILIZADO, buraco.get_corteUtilizado());
		values.put(COLUMN_BURACO_MOSTRAR, buraco.get_mostrar());
		// Inserting Row
		db.insert(TABLE_BURACO, null, values);
	}

	// Altera o indicador de visualização
	public void limparVisualizacao() {
		final SQLiteDatabase db = this.getWritableDatabase();
		final ContentValues values = new ContentValues();
		values.put(COLUMN_BURACO_MOSTRAR, Ferramentas.MOSTRAR_NAO);
		// Updating Row
		db.update(TABLE_BURACO,values,null,null);
	}

	// Consulta todos os Buracos
	public Cursor consultaTodosBuracos() {
		final SQLiteDatabase db = this.getReadableDatabase();
		return db.rawQuery(
				"SELECT _ID, " +
						COLUMN_BURACO_FORCA + ", " +
						COLUMN_BURACO_LATITUDE + ", " +
						COLUMN_BURACO_LONGITUDE + ", " +
						COLUMN_BURACO_ALTITUDE + ", " +
						COLUMN_BURACO_VELOCIDADE + ", " +
						COLUMN_BURACO_DIRECAO + ", " +
						COLUMN_BURACO_DATA + ", " +
						COLUMN_BURACO_CORTE_UTILIZADO + ", " +
						COLUMN_BURACO_MOSTRAR +
						" FROM " + TABLE_BURACO, null);
	}

	// Consulta todos os Buracos
	public Cursor consultaBuracosVisiveis() {
		final SQLiteDatabase db = this.getReadableDatabase();
		return db.rawQuery(
				"SELECT _ID, " +
						COLUMN_BURACO_FORCA + ", " +
						COLUMN_BURACO_LATITUDE + ", " +
						COLUMN_BURACO_LONGITUDE + ", " +
						COLUMN_BURACO_ALTITUDE + ", " +
						COLUMN_BURACO_VELOCIDADE + ", " +
						COLUMN_BURACO_DIRECAO + ", " +
						COLUMN_BURACO_DATA + ", " +
						COLUMN_BURACO_CORTE_UTILIZADO + ", " +
						COLUMN_BURACO_MOSTRAR +
						" FROM " + TABLE_BURACO +
						" WHERE " + COLUMN_BURACO_MOSTRAR + " = '" + Ferramentas.MOSTRAR_SIM + "'", null);
	}

}
