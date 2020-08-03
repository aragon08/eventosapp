package com.israel.appsamx.eventosapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActividadMenu extends AppCompatActivity {

    @BindView(R.id.lv_lista_eventos)
    ListView lv_lista_eventos;
    @BindView(R.id.tv_sin_registros)
    TextView tv_sin_registros;
    @BindView(R.id.fabAgregarEvento)
    FloatingActionButton fab_agregar_evento;

    BaseDatosEventos base_datos_eventos; //Objeto de la clase
    SQLiteDatabase manejador_sqlite; //Permite ejecutar comandos dentro de la base

    private List<Evento> lista_eventos = new ArrayList<Evento>();
    private AdaptadorEvento adaptador_evento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_menu);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        base_datos_eventos = new BaseDatosEventos(this, "BaseDatosEventos", null, 1);
        manejador_sqlite = base_datos_eventos.getWritableDatabase();

        listarEventos();
    }

    private void listarEventos()
    {
        Cursor cursor;
        cursor = manejador_sqlite.rawQuery("SELECT * FROM evento", null);
        if (cursor.moveToFirst())
        {
            adaptador_evento = new AdaptadorEvento(this, lista_eventos);
            lv_lista_eventos.setAdapter(adaptador_evento);
            lista_eventos.clear();

            do {

                int id_evento = cursor.getInt(0);
                String nombre = cursor.getString(1);
                String descripcion = cursor.getString(2);
                String fecha = cursor.getString(3);

                Evento evento = new Evento();
                evento.setId_evento(id_evento);
                evento.setNombre(nombre);
                evento.setDescripcion(descripcion);
                evento.setFecha(fecha);

                lista_eventos.add(evento);

            }while(cursor.moveToNext());

            cursor.close();

            adaptador_evento.notifyDataSetChanged();

            lv_lista_eventos.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int posicion, long arg3)
                {
                    view.setSelected(true);
                    editarEvento(lista_eventos.get(posicion).getId_evento(),
                                 lista_eventos.get(posicion).getNombre(),
                                 lista_eventos.get(posicion).getDescripcion(),
                                 lista_eventos.get(posicion).getFecha());
                }
            });

            lv_lista_eventos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int posicion, long id) {

                    opcionesEvento(posicion);
                    return true;
                }
            });
        }
        else
        {
            lv_lista_eventos.setVisibility(View.INVISIBLE);
            tv_sin_registros.setVisibility(View.VISIBLE);
        }
    }

    private void opcionesEvento(final int posicion)
    {
        final int id_evento = lista_eventos.get(posicion).getId_evento();

        CharSequence opciones[] = new CharSequence[]{"Editar", "Borrar", "Llamar", "Mensaje", "Email"};

        AlertDialog.Builder dialogEvidence = new AlertDialog.Builder(this);
        dialogEvidence.setTitle("Elige una opción");
        dialogEvidence.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch (which) {
                    case 0:
                        String nombre = lista_eventos.get(posicion).getNombre();
                        String descripcion = lista_eventos.get(posicion).getDescripcion();
                        String fecha = lista_eventos.get(posicion).getFecha();
                        editarEvento(id_evento, nombre, descripcion, fecha);
                        break;
                    case 1:
                        new AlertDialog.Builder(ActividadMenu.this)
                                .setTitle("Lista de eventos")
                                .setMessage("En verdad deseas borrar este evento?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        borrarEvento(id_evento);
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
                        break;
                    case 2:
                        accionContacto(id_evento, "llamar");
                        break;
                    case 3:
                        accionContacto(id_evento, "mensaje");
                        break;
                    case 4:
                        accionContacto(id_evento, "email");
                        break;
                }
            }
        });
        dialogEvidence.show();
    }

    private void editarEvento(int id_evento, String nombre, String descripcion, String fecha)
    {
        Intent actividadEvento = new Intent(this, ActividadEvento.class);
        actividadEvento.putExtra("modo_editar", true);
        actividadEvento.putExtra("id_evento", id_evento);
        actividadEvento.putExtra("nombre", nombre);
        actividadEvento.putExtra("descripcion", descripcion);
        actividadEvento.putExtra("fecha", fecha);
        startActivity(actividadEvento);
        finish();
    }

    private void borrarEvento(int id_evento)
    {
        manejador_sqlite.execSQL("PRAGMA foreign_keys = ON");
        String query = "DELETE FROM evento WHERE id_evento = " + id_evento;
        manejador_sqlite.execSQL(query);
        manejador_sqlite.execSQL("PRAGMA foreign_keys = OFF");

        listarEventos();
        Toast.makeText(this, "¡Se ha borrado el evento!", Toast.LENGTH_LONG).show();
    }

    private void accionContacto(int id_evento, String accion)
    {
        Intent accion_contacto = new Intent(this, ActividadContactos.class);
        accion_contacto.putExtra("id_evento", id_evento);
        accion_contacto.putExtra("accion", accion);
        startActivity(accion_contacto);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actividad, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_accion_acerca_de) {
            Intent acerca_de = new Intent(this, ActividadAcercaDe.class);
            startActivity(acerca_de);
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.fabAgregarEvento)
    public void onClick()
    {
        Intent actividadEvento = new Intent(this, ActividadEvento.class);
        actividadEvento.putExtra("modo_editar", false);
        startActivity(actividadEvento);
        finish();
    }
}
