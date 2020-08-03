package com.israel.appsamx.eventosapp;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActividadEvento extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    @BindView(R.id.tv_titulo_actividad_evento)
    TextView tv_titulo_actividad_evento;
    @BindView(R.id.et_nombre_evento)
    EditText et_nombre_evento;
    @BindView(R.id.et_descripcion_evento)
    EditText et_descripcion_evento;
    @BindView(R.id.et_fecha_evento)
    EditText et_fecha_evento;
    @BindView(R.id.lv_lista_contactos)
    ListView lv_lista_contactos;
    @BindView(R.id.btn_evento)
    Button btn_evento;

    BaseDatosEventos base_datos_eventos; //Objeto de la clase
    SQLiteDatabase manejador_sqlite; //Permite ejecutar comandos dentro de la base

    private boolean modo_editar, agregar_telefono, registro_agregado = false;
    private int id_evento = 0;
    private String nombre;
    private String descripcion;
    private String fecha;

    private List<Contacto> lista_contactos = new ArrayList<Contacto>();
    private AdaptadorContacto adaptador_contacto;

    DatePickerDialog selector_fecha;

    @Override
    protected void onCreate(Bundle datos) {
        super.onCreate(datos);
        setContentView(R.layout.actividad_evento);
        ButterKnife.bind(this);

        base_datos_eventos = new BaseDatosEventos(this, "BaseDatosEventos", null, 1);
        manejador_sqlite = base_datos_eventos.getWritableDatabase();

        selector_fecha = new DatePickerDialog(this,this,2017,01,01);

        if (getIntent().hasExtra("modo_editar")) {

            modo_editar = getIntent().getBooleanExtra("modo_editar", false);

            if (modo_editar)
            {
                btn_evento.setText("Guardar cambios");
                obtenerExtras();
                listarContactosEvento();
                registro_agregado = true;
            }
            else
            {
                tv_titulo_actividad_evento.setText("Nuevo evento");
                btn_evento.setText("Agregar evento");
            }
        }

        if (getIntent().hasExtra("agregar_telefono"))
        {
            obtenerExtras();
            registro_agregado = getIntent().getBooleanExtra("registro_agregado", false);
            agregar_telefono = getIntent().getBooleanExtra("agregar_telefono", false);
            if (agregar_telefono && getIntent().hasExtra("nombre_contacto") && getIntent().hasExtra("telefono") &&
                getIntent().hasExtra("email"))
            {
                if(!modo_editar && !registro_agregado)
                {
                    agregarEvento();
                }

                agregarContacto(getIntent().getStringExtra("nombre_contacto"),
                                getIntent().getStringExtra("telefono"),
                                getIntent().getStringExtra("email"));
            }
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day)
    {
        String m = "" + (month + 1), d = "" + day;

        if (m.length() == 1)
            m = "0" + m;

        if (d.length() == 1)
            d = "0" + d;

        et_fecha_evento.setText(year + "-" + m + "-" + d);
    }

    private void obtenerExtras()
    {
        id_evento =  getIntent().getIntExtra("id_evento", 0);
        nombre =  getIntent().getStringExtra("nombre");
        descripcion =  getIntent().getStringExtra("descripcion");
        fecha =  getIntent().getStringExtra("fecha");

        tv_titulo_actividad_evento.setText("Editar evento");
        et_nombre_evento.setText(nombre);
        et_descripcion_evento.setText(descripcion);
        et_fecha_evento.setText(fecha);
    }

    @Override
    public void onBackPressed()
    {
        if(!modo_editar)
        {
            new AlertDialog.Builder(ActividadEvento.this)
                    .setTitle("Nuevo evento")
                    .setMessage("En verdad deseas regresar al menú? Todos los cambios no guardados se perderan...")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (registro_agregado && !modo_editar) {
                                borrarEvento();
                            }
                            regresarMenu();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else
        {
            regresarMenu();
        }
    }

    private void regresarMenu()
    {
        Intent menu = new Intent(ActividadEvento.this, ActividadMenu.class);
        startActivity(menu);
        finish();
    }

    private void listarContactosEvento()
    {
        manejador_sqlite.execSQL("PRAGMA foreign_keys = ON");
        Cursor cursor;
        cursor = manejador_sqlite.rawQuery("SELECT contacto.id_contacto, contacto.nombre, contacto.telefono, contacto.email " +
                                           "FROM contacto " +
                                           "JOIN evento_contactos ON contacto.id_contacto = evento_contactos.id_contacto " +
                                           "WHERE evento_contactos.id_evento = " + id_evento, null);
        if (cursor.moveToFirst())
        {
            adaptador_contacto = new AdaptadorContacto(this, lista_contactos);
            lv_lista_contactos.setAdapter(adaptador_contacto);

            lista_contactos.clear();

            do {

                int id_contacto = cursor.getInt(0);
                String nombre = cursor.getString(1);
                String telefono = cursor.getString(2);
                String email = cursor.getString(3);

                Contacto contacto = new Contacto();
                contacto.setId_contacto(id_contacto);
                contacto.setNombre(nombre);
                contacto.setTelefono(telefono);
                contacto.setEmail(email);

                lista_contactos.add(contacto);

            }while(cursor.moveToNext());

            cursor.close();

            adaptador_contacto.notifyDataSetChanged();

            manejador_sqlite.execSQL("PRAGMA foreign_keys = OFF");

            lv_lista_contactos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int posicion, long arg3) {

                    new AlertDialog.Builder(ActividadEvento.this)
                            .setTitle("Lista de contactos")
                            .setMessage("Desea borrar este contacto del evento?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    borrarContacto(lista_contactos.get(posicion).getId_contacto());
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    dialog.dismiss();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });

            lv_lista_contactos.setVisibility(View.VISIBLE);
        }
        else
        {
            lv_lista_contactos.setVisibility(View.INVISIBLE);
        }
    }

    @OnClick({R.id.et_fecha_evento, R.id.btn_agregar_contacto, R.id.btn_evento})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.et_fecha_evento:
                selector_fecha.show();
                break;
            case R.id.btn_agregar_contacto:
                Intent agregar_contactos = new Intent(this, ActividadContactos.class);
                agregar_contactos.putExtra("modo_editar", modo_editar);
                agregar_contactos.putExtra("id_evento", id_evento);
                agregar_contactos.putExtra("nombre", et_nombre_evento.getText().toString());
                agregar_contactos.putExtra("descripcion", et_descripcion_evento.getText().toString());
                agregar_contactos.putExtra("fecha", et_fecha_evento.getText().toString());
                agregar_contactos.putExtra("registro_agregado", registro_agregado);
                startActivity(agregar_contactos);
                finish();

                break;
            case R.id.btn_evento:
                if(verificarCampos())
                {
                    actualizarEvento();
                }
                break;
        }
    }

    private boolean verificarCampos()
    {
        if(et_nombre_evento.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Agregue un nombre al evento", Toast.LENGTH_LONG).show();
            return false;
        }

        if(et_descripcion_evento.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Agregue una descripcion al evento", Toast.LENGTH_LONG).show();
            return false;
        }

        if(et_fecha_evento.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Agregue una fecha al evento", Toast.LENGTH_LONG).show();
            return false;
        }

        if(!registro_agregado)
        {
            Toast.makeText(this, "!Es necesario agregar al menos un contacto al evento!", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void agregarEvento()
    {
        ContentValues nuevoEvento = new ContentValues();
        nuevoEvento.put("nombre", et_nombre_evento.getText().toString());
        nuevoEvento.put("descripcion", et_descripcion_evento.getText().toString());
        nuevoEvento.put("fecha", et_fecha_evento.getText().toString());

        //Insertamos el registro en la base de datos
        id_evento = (int) manejador_sqlite.insert("evento", null, nuevoEvento);

        //Toast.makeText(this, "EVENTO: "+id_evento, Toast.LENGTH_LONG).show();

        registro_agregado = true;

    }

    private void actualizarEvento()
    {
        String query = "UPDATE evento " +
                       "SET nombre = '" + et_nombre_evento.getText() + "', " +
                           "descripcion = '" + et_descripcion_evento.getText() + "', " +
                           "fecha = '" + et_fecha_evento.getText() + "' " +
                           "WHERE id_evento = " + id_evento;
        manejador_sqlite.execSQL(query);
        if(modo_editar)
        {
            Toast.makeText(this, "Evento actualizado", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "Evento agregado", Toast.LENGTH_LONG).show();
            regresarMenu();
        }
    }

    private void borrarEvento()
    {
        manejador_sqlite.execSQL("PRAGMA foreign_keys = ON");
        String query = "DELETE FROM evento WHERE id_evento = " + id_evento;
        manejador_sqlite.execSQL(query);
        manejador_sqlite.execSQL("PRAGMA foreign_keys = OFF");
        manejador_sqlite.execSQL(query);
    }

    public void agregarContacto(String nombre, String telefono, String email)
    {
        int id_contacto;

        Cursor cursor;
        String query =
           "SELECT id_contacto FROM contacto " +
           "WHERE nombre = '" + nombre + "' AND telefono = '" + telefono + "' AND email = '" + email + "'";
        cursor = manejador_sqlite.rawQuery(query, null);
        if (cursor.moveToFirst())
        {
            id_contacto = cursor.getInt(0);
        }
        else
        {
            //Creamos el registro a insertar como objeto ContentValues
            ContentValues nuevoContacto = new ContentValues();
            nuevoContacto.put("nombre", nombre);
            nuevoContacto.put("telefono", telefono);
            nuevoContacto.put("email", email);

            //Insertamos el registro en la base de datos
            id_contacto = (int) manejador_sqlite.insert("contacto", null, nuevoContacto);
        }

        //Toast.makeText(this, "id_contacto: " + id_contacto + " id_evento: " + id_evento, Toast.LENGTH_LONG).show();

        query = "SELECT * FROM evento_contactos WHERE id_evento = " + id_evento + " AND id_contacto = " + id_contacto;
        cursor = manejador_sqlite.rawQuery(query, null);
        if (cursor.moveToFirst())
        {
            Toast.makeText(this, "El contacto ya se encuentra en la lista...", Toast.LENGTH_LONG).show();
        }
        else
        {
            //Insertamos el registro en la base de datos
            query = "INSERT INTO evento_contactos(id_evento, id_contacto) " +
                    "VALUES(" + id_evento + "," + id_contacto + ")";
            manejador_sqlite.execSQL(query);

            listarContactosEvento();
        }
    }

    private void borrarContacto(int id_contacto)
    {
        String query1 = "DELETE FROM evento_contactos WHERE id_contacto = " + id_contacto;
        manejador_sqlite.execSQL(query1);
        String query2 = "DELETE FROM contacto WHERE id_contacto = " + id_contacto;
        manejador_sqlite.execSQL(query2);
        listarContactosEvento();
        Toast.makeText(this, "¡Se ha borrado el contacto del evento!", Toast.LENGTH_LONG).show();
    }
}