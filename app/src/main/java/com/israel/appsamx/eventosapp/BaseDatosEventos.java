package com.israel.appsamx.eventosapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Adan_ on 22/04/2017.
 */

public class BaseDatosEventos extends SQLiteOpenHelper
{
    String tabla_contacto = "CREATE TABLE contacto(" +
                                "id_contacto INTEGER PRIMARY KEY NOT NULL, " +
                                "nombre TEXT NOT NULL, " +
                                "telefono TEXT NOT NULL, " +
                                "email TEXT NOT NULL)";

    String tabla_evento = "CREATE TABLE evento(" +
                                "id_evento INTEGER PRIMARY KEY NOT NULL," +
                                "nombre TEXT NOT NULL," +
                                "descripcion TEXT NOT NULL," +
                                "fecha DATETIME NOT NULL)";

    String tabla_evento_contactos = "CREATE TABLE evento_contactos(" +
                                        "id INTEGER PRIMARY KEY NOT NULL," +
                                        "id_evento INTEGER NOT NULL, " +
                                        "id_contacto INTEGER NOT NULL, " +
                                        "FOREIGN KEY(id_evento) REFERENCES evento(id_evento) ON DELETE CASCADE, " +
                                        "FOREIGN KEY(id_contacto) REFERENCES contacto(id_contacto) ON DELETE CASCADE)";


    public BaseDatosEventos(Context contexto, String nombre, SQLiteDatabase.CursorFactory cursor, int version)
    {
        super(contexto, nombre, cursor, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        sqLiteDatabase.execSQL(tabla_contacto);
        sqLiteDatabase.execSQL(tabla_evento);
        sqLiteDatabase.execSQL(tabla_evento_contactos);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {

    }
}
