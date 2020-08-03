package com.israel.appsamx.eventosapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActividadContactos extends AppCompatActivity {

    @BindView(R.id.tv_titulo_actividad_contactos)
    TextView tv_titulo_actividad_contactos;
    @BindView(R.id.lv_lista_contactos_telefono)
    ListView lv_lista_contactos_telefono;

    private boolean mostrar_contactos_telefonicos, modo_editar, registro_agregado;
    private int id_evento;
    private String nombre;
    private String descripcion;
    private String fecha;

    private String nombre_contacto;
    private String telefono;
    private String email;

    private String accion;

    private List<Contacto> lista_contactos = new ArrayList<Contacto>();
    private AdaptadorContacto adaptador_contacto;

    private int PERMISSIONS_REQUEST_READ_CONTACTS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_contactos);
        ButterKnife.bind(this);

        if (getIntent().hasExtra("modo_editar") &&
                getIntent().hasExtra("id_evento") &&
                getIntent().hasExtra("nombre") &&
                getIntent().hasExtra("descripcion") &&
                getIntent().hasExtra("fecha") &&
                getIntent().hasExtra("registro_agregado")) {

            mostrar_contactos_telefonicos = true;

            modo_editar = getIntent().getBooleanExtra("modo_editar", false);
            id_evento = getIntent().getIntExtra("id_evento", 0);
            nombre = getIntent().getStringExtra("nombre");
            descripcion = getIntent().getStringExtra("descripcion");
            fecha = getIntent().getStringExtra("fecha");
            registro_agregado = getIntent().getBooleanExtra("registro_agregado", false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
                //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            } else {
                mostrarLosContactosTelefonicos();
            }

        } else if (getIntent().hasExtra("id_evento") &&
                getIntent().hasExtra("accion")) {
            mostrar_contactos_telefonicos = false;
            id_evento = getIntent().getIntExtra("id_evento", 0);
            accion = getIntent().getStringExtra("accion");

            switch (accion) {
                case "llamar":
                    tv_titulo_actividad_contactos.setText("Llamar contacto");
                    break;
                case "mensaje":
                    tv_titulo_actividad_contactos.setText("Mandar mensaje");
                    break;
                case "email":
                    tv_titulo_actividad_contactos.setText("Mandar email");
                    break;
            }

            mostrarLosContactosEvento();

        } else {
            Toast.makeText(this, "Un error inesperado ocurrio", Toast.LENGTH_LONG).show();
            Intent menu = new Intent(this, ActividadMenu.class);
            startActivity(menu);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if(mostrar_contactos_telefonicos)
        {
            regresarActividadEvento(false);
        }
        else
        {
            regresarMenu();
        }
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                mostrarLosContactosTelefonicos();
            } else {
                Toast.makeText(this, "Es necesario dar permiso para poder acceder a los contactos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void mostrarLosContactosTelefonicos() {
        adaptador_contacto = new AdaptadorContacto(this, lista_contactos);
        lv_lista_contactos_telefono.setAdapter(adaptador_contacto);

        int id_contacto = 0;

        lista_contactos.clear();

        String telefono_anterior = "##))$$!!#/%$&%$#)";

        ContentResolver cr = getContentResolver();
        final Cursor telefonos = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        // use the cursor to access the contacts
        while (telefonos.moveToNext()) {
            String id = telefonos.getString(telefonos.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            String nombre = telefonos.getString(telefonos.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String telefono = telefonos.getString(telefonos.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            if(!telefono_anterior.equals(telefono))
            {
                telefono_anterior = telefonos.getString(telefonos.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String email = "Sin email";

                Cursor emails = cr.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{id}, null);
                if (emails.moveToFirst()) {
                    email = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                }

                if (!nombre.isEmpty() && !telefono.isEmpty()) {

                    Contacto contacto = new Contacto();
                    contacto.setId_contacto(id_contacto);
                    contacto.setNombre(nombre);
                    contacto.setTelefono(telefono);
                    contacto.setEmail(email);

                    lista_contactos.add(contacto);

                    id_contacto++;
                }
            }
        }

        telefonos.close();

        adaptador_contacto.notifyDataSetChanged();

        lv_lista_contactos_telefono.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int posicion, long arg3) {
                view.setSelected(true);
                nombre_contacto = lista_contactos.get(posicion).getNombre();
                telefono = lista_contactos.get(posicion).getTelefono();
                email = lista_contactos.get(posicion).getEmail();
                regresarActividadEvento(true);
            }
        });
    }

    private void mostrarLosContactosEvento() {
        BaseDatosEventos base_datos_eventos = new BaseDatosEventos(this, "BaseDatosEventos", null, 1);
        SQLiteDatabase manejador_sqlite = base_datos_eventos.getWritableDatabase();

        manejador_sqlite.execSQL("PRAGMA foreign_keys = ON");
        Cursor cursor;
        cursor = manejador_sqlite.rawQuery("SELECT contacto.id_contacto, contacto.nombre, contacto.telefono, contacto.email " +
                "FROM contacto " +
                "JOIN evento_contactos ON contacto.id_contacto = evento_contactos.id_contacto " +
                "WHERE evento_contactos.id_evento = " + id_evento, null);
        if (cursor.moveToFirst()) {
            adaptador_contacto = new AdaptadorContacto(this, lista_contactos);
            lv_lista_contactos_telefono.setAdapter(adaptador_contacto);
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

            } while (cursor.moveToNext());

            cursor.close();

            adaptador_contacto.notifyDataSetChanged();

            manejador_sqlite.execSQL("PRAGMA foreign_keys = OFF");

            lv_lista_contactos_telefono.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int posicion, long arg3) {
                    view.setSelected(true);
                    switch (accion) {
                        case "llamar":
                            Intent intentoLlamar = new Intent(Intent.ACTION_DIAL);
                            intentoLlamar.setData(Uri.parse("tel:" + lista_contactos.get(posicion).getTelefono()));
                            startActivity(intentoLlamar);
                            break;
                        case "mensaje":
                            Intent intentoMensaje = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    "sms:" + lista_contactos.get(posicion).getTelefono()));
                            startActivity(intentoMensaje);
                            break;
                        case "email":
                            if(lista_contactos.get(posicion).getEmail().equals("Sin email"))
                            {
                                Toast.makeText(ActividadContactos.this, "Este contacto no tiene un email...", Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                Intent intentoEmail = new Intent(Intent.ACTION_SENDTO);
                                intentoEmail.setData(Uri.parse("mailto:"+lista_contactos.get(posicion).getEmail()));
                                intentoEmail.putExtra(Intent.EXTRA_SUBJECT, "Evento");
                                startActivity(intentoEmail);
                            }
                            break;
                    }
                }
            });
        }
    }

    private void regresarMenu()
    {
        Intent menu = new Intent(this, ActividadMenu.class);
        startActivity(menu);
        finish();
    }

    private void regresarActividadEvento(boolean agregar_telefono) {
        Intent actividad_evento = new Intent(ActividadContactos.this, ActividadEvento.class);
        actividad_evento.putExtra("modo_editar", modo_editar);
        actividad_evento.putExtra("id_evento", id_evento);
        actividad_evento.putExtra("nombre", nombre);
        actividad_evento.putExtra("descripcion", descripcion);
        actividad_evento.putExtra("fecha", fecha);
        actividad_evento.putExtra("registro_agregado", registro_agregado);
        actividad_evento.putExtra("agregar_telefono", agregar_telefono);
        if (agregar_telefono) {
            actividad_evento.putExtra("nombre_contacto", nombre_contacto);
            actividad_evento.putExtra("telefono", telefono);
            actividad_evento.putExtra("email", email);
        }
        startActivity(actividad_evento);
        finish();
    }
}