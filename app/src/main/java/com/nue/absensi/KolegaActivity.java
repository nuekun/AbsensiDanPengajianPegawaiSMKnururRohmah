package com.nue.absensi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nue.absensi.volley.Server;

import de.hdodenhof.circleimageview.CircleImageView;

public class KolegaActivity extends AppCompatActivity {

    TextView txtNama,txtEmail,txtTglLahir,txtNohp,txtAlamat;
    CircleImageView gbrPriview ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kolega);

        txtNama = findViewById(R.id.txtKolegaNama);
        txtEmail = findViewById(R.id.txtKolegaEmail);
        txtTglLahir = findViewById(R.id.txtKolegaTglLahir);
        txtNohp = findViewById(R.id.txtKolegaHp);
        txtAlamat = findViewById(R.id.txtKolegaAlamat);

        gbrPriview = findViewById(R.id.gbrKolegaPriview);

        Intent intent = getIntent();
        String nama = intent.getStringExtra("nama");
        String email = intent.getStringExtra("email");
        String tgl_lahir = intent.getStringExtra("tgl_lahir");
        String no_hp = intent.getStringExtra("no_hp");
        String alamat = intent.getStringExtra("alamat");
        String profil = intent.getStringExtra("profil");
        String URL = Server.server+"profil/"+profil;

        Glide.with(this).load(URL).into(gbrPriview);
        txtNama.setText(nama);
        txtEmail.setText(email);
        txtTglLahir.setText(tgl_lahir);
        txtNohp.setText(no_hp);
        txtAlamat.setText(alamat);


    }
}
