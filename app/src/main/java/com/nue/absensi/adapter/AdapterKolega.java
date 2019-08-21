package com.nue.absensi.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nue.absensi.KolegaActivity;
import com.nue.absensi.R;
import com.nue.absensi.volley.Server;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterKolega extends RecyclerView.Adapter<AdapterKolega.ViewHolder>{

    Context context;
    ArrayList<HashMap<String, String>> list_data;

    public AdapterKolega(Context context, ArrayList<HashMap<String, String>> list_data) {
        this.context = context;
        this.list_data = list_data;


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_kolega, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {



        holder.txtNama.setText(list_data.get(position).get("nama"));

        final String nama,email,tgl_lahir,no_hp,alamat,profil;

        nama = list_data.get(position).get("nama");
        email = list_data.get(position).get("email");
        tgl_lahir = list_data.get(position).get("tgl_lahir");
        no_hp = list_data.get(position).get("no_hp");
        alamat = list_data.get(position).get("alamat");
        profil = list_data.get(position).get("profil");

        String URL = Server.server+"profil/"+profil;
        Glide.with(context).load(URL).into(holder.gbrPriview);


        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, KolegaActivity.class);
                intent.putExtra("nama",nama);
                intent.putExtra("email",email);
                intent.putExtra("tgl_lahir",tgl_lahir);
                intent.putExtra("no_hp",no_hp);
                intent.putExtra("alamat",alamat);
                intent.putExtra("profil",profil);
                context.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return list_data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNama;
        RelativeLayout root;
        ImageView gbrPriview;


        public ViewHolder(View itemView) {
            super(itemView);

            txtNama = itemView.findViewById(R.id.txtKolegaListNama);
            gbrPriview = itemView.findViewById(R.id.gbrlistKolegaPriview);

            root = itemView.findViewById(R.id.holderKolega);

        }
    }


}
