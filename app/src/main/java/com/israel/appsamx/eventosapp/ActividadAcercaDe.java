package com.israel.appsamx.eventosapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActividadAcercaDe extends AppCompatActivity {

    @BindView(R.id.tv_email_acerca_de)
    TextView tvEmailAcercaDe;
    @BindView(R.id.tv_telefono_acerca_de)
    TextView tvTelefonoAcercaDe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_acerca_de);
        ButterKnife.bind(this);

        tvTelefonoAcercaDe.setAutoLinkMask(Linkify.PHONE_NUMBERS);
        tvTelefonoAcercaDe.setMovementMethod(LinkMovementMethod.getInstance());

        tvEmailAcercaDe.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);
        tvEmailAcercaDe.setMovementMethod(LinkMovementMethod.getInstance());

    }
}
