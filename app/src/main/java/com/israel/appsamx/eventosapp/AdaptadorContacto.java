package com.israel.appsamx.eventosapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Adan_ on 22/04/2017.
 */

public class AdaptadorContacto extends BaseAdapter
{
    private Activity actividad;
    private LayoutInflater layout_inflater;
    private List<Contacto> lista_contactos;

    public AdaptadorContacto(Activity actividad, List<Contacto> lista_contactos) {
        this.actividad = actividad;
        this.lista_contactos = lista_contactos;
    }

    @Override
    public int getCount() {
        return lista_contactos.size();
    }

    @Override
    public Object getItem(int posicion) {
        return lista_contactos.get(posicion);
    }

    @Override
    public long getItemId(int posicion) {
        return posicion;
    }

    @Override
    public View getView(int posicion, View vista, ViewGroup viewGroup) {
        if (layout_inflater == null)
            layout_inflater = (LayoutInflater) actividad.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (vista == null)
            vista = layout_inflater.inflate(R.layout.objeto_contacto, null);

        TextView tv_nombre_contacto = (TextView) vista.findViewById(R.id.tv_nombre_contacto);
        TextView tv_telefono_contacto = (TextView) vista.findViewById(R.id.tv_telefono_contacto);
        TextView tv_email_contacto = (TextView) vista.findViewById(R.id.tv_email_contacto);

        tv_nombre_contacto.setText("" + lista_contactos.get(posicion).getNombre());
        tv_telefono_contacto.setText("" + lista_contactos.get(posicion).getTelefono());
        tv_email_contacto.setText("" + lista_contactos.get(posicion).getEmail());

        return vista;
    }
}
