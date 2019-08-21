package com.nue.absensi.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nue.absensi.R;
import com.nue.absensi.adapter.AdapterJadwal;
import com.nue.absensi.volley.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class GajiFragment extends Fragment {
    RequestQueue requestQueue;
    StringRequest stringRequest;
    ArrayList<HashMap<String, String>> list_data;
    Context context;
    Button btnProses;
    SharedPreferences local;
    TextView txtGajiTotal,txtJamMengajar;
    EditText txtTahun,txtBulan;
    ArrayList<String> listBulan,listTahun;
    int gajiTotal = 0;
    private Spinner spinTahun,spinBulan;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gaji, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        txtGajiTotal = view.findViewById(R.id.txtGajiTotal);
        txtJamMengajar = view.findViewById(R.id.txtJamTotal);
        txtBulan = view.findViewById(R.id.txtGajiBulan);
        txtTahun = view.findViewById(R.id.txtGajiTahun);
        btnProses = view.findViewById(R.id.btnGajiProses);

        local = this.getActivity().getSharedPreferences("local", Context.MODE_PRIVATE);

        txtTahun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtTahun.setFocusable(true);
                txtTahun.setFocusableInTouchMode(true);
            }
        });

        txtBulan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtBulan.setFocusable(true);
                txtBulan.setFocusableInTouchMode(true);

            }
        });


        final String pengajar,tanggal;

        pengajar = local.getString("NIP", "");
        Calendar cal = Calendar.getInstance();
        final int tahun = cal.get(Calendar.YEAR);
        int bulan = cal.get(Calendar.MONTH);      // 0 to 11

        tanggal = tahun+"-"+bulan+1;

        ambilGaji(pengajar,tanggal);


        btnProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String thn,bln;
                thn = txtTahun.getText().toString();
                bln = txtBulan.getText().toString();

                String tgl = thn+"-"+bln;

                ambilGaji(pengajar,tgl);
            }
        });


    }

    private void ambilGaji(final String pengajar, final String tanggal) {
        setGajiTotal(0);


        requestQueue = Volley.newRequestQueue(context);
        list_data = new ArrayList<>();
        list_data.clear();
        stringRequest = new StringRequest(Request.Method.POST, Server.ambilGaji
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response ", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("Hasil");
                    for (int a = 0; a < jsonArray.length(); a++) {
                        JSONObject json = jsonArray.getJSONObject(a);

                        int gaji = Integer.parseInt(json.getString("gaji"));
                        int ga = getGajiTotal()+gaji;
                        setGajiTotal(ga);
                        txtJamMengajar.setText("Total jam Mengajar "+gaji+" jam");

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                    String total = Integer.toString(getGajiTotal()*10000);
                    txtGajiTotal.setText("RP."+total);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("karyawan",pengajar);
                params.put("tanggal",tanggal);
                return params;
            }
        };
        requestQueue.add(stringRequest);



    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }


    public int getGajiTotal() {
        return gajiTotal;
    }

    public void setGajiTotal(int gajiTotal) {
        this.gajiTotal = gajiTotal;
    }
}
