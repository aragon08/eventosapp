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

public class AdaptadorEvento extends BaseAdapter
{
    private Activity actividad;
    private LayoutInflater layout_inflater;
    private List<Evento> lista_eventos;

    public AdaptadorEvento(Activity actividad, List<Evento> lista_eventos) {
        this.actividad = actividad;
        this.lista_eventos = lista_eventos;
    }

    @Override
    public int getCount() {
        return lista_eventos.size();
    }

    @Override
    public Object getItem(int posicion) {
        return lista_eventos.get(posicion);
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
            vista = layout_inflater.inflate(R.layout.objeto_evento, null);

        TextView tv_nombre_evento = (TextView) vista.findViewById(R.id.tv_nombre_evento);
        TextView tv_descripcion_evento = (TextView) vista.findViewById(R.id.tv_descripcion_evento);
        TextView tv_fecha_evento = (TextView) vista.findViewById(R.id.tv_fecha_evento);

        tv_nombre_evento.setText(lista_eventos.get(posicion).getNombre());
        tv_descripcion_evento.setText(lista_eventos.get(posicion).getDescripcion());
        tv_fecha_evento.setText(lista_eventos.get(posicion).getFecha());

        return vista;
    }
}
