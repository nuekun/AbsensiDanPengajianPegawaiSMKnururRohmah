package com.nue.absensi.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nue.absensi.R;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterJadwal extends RecyclerView.Adapter<AdapterJadwal.ViewHolder>{

    Context context;
    ArrayList<HashMap<String, String>> list_data;

    public AdapterJadwal(Context context, ArrayList<HashMap<String, String>> list_data) {
        this.context = context;
        this.list_data = list_data;


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_jadwal, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {



        holder.txtMapel.setText(list_data.get(position).get("mapel"));
        holder.txtMapeShadow.setText(list_data.get(position).get("mapel"));
        holder.txtHari.setText(list_data.get(position).get("hari"));
        holder.txtMulai.setText(list_data.get(position).get("mulai"));
        holder.txtBerakhir.setText(list_data.get(position).get("berakhir"));
        holder.txtKeterangan.setText(list_data.get(position).get("keterangan"));




        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {




            }
        });

    }

    @Override
    public int getItemCount() {
        return list_data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtMapel,txtMapeShadow,txtHari,txtMulai,txtBerakhir,txtKeterangan;
        RelativeLayout root;


        public ViewHolder(View itemView) {
            super(itemView);

            txtMapel = itemView.findViewById(R.id.txtJadwalMapel);
            txtMapeShadow = itemView.findViewById(R.id.txtJadwalMapelShadow);
            txtHari = itemView.findViewById(R.id.txtJadwalHari);
            txtMulai = itemView.findViewById(R.id.txtJadwalMulai);
            txtBerakhir = itemView.findViewById(R.id.txtJadwalBerakhir);
            txtKeterangan = itemView.findViewById(R.id.txtJadwalKeterangan);
            root = itemView.findViewById(R.id.holderJadwal);

        }
    }


}
