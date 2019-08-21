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
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nue.absensi.MainActivity;
import com.nue.absensi.R;
import com.nue.absensi.adapter.AdapterJadwal;
import com.nue.absensi.volley.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JadwalFragment extends Fragment {
    RequestQueue requestQueue;
    StringRequest stringRequest;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    ArrayList<HashMap<String, String>> list_data;
    Context context;
    SharedPreferences local;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_jadwal, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        recyclerView = view.findViewById(R.id.recJadwal);

        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        local = this.getActivity().getSharedPreferences("local", Context.MODE_PRIVATE);


        String pengajar,hari;

        pengajar = local.getString("NIP", "");
        //pengajar = "123";
        hari = "";

        ambilJadwal(pengajar,hari);


    }

    private void ambilJadwal(final String pengajar, final String hari) {

        recyclerView.setAdapter(null);
        requestQueue = Volley.newRequestQueue(context);
        list_data = new ArrayList<HashMap<String, String>>();
        list_data.clear();
        stringRequest = new StringRequest(Request.Method.POST, Server.ambilJadwal
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response ", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("Hasil");
                    for (int a = 0; a < jsonArray.length(); a++) {
                        JSONObject json = jsonArray.getJSONObject(a);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("mapel", json.getString("mapel"));
                        map.put("hari", json.getString("hari"));
                        map.put("mulai", json.getString("mulai"));
                        map.put("berakhir", json.getString("berakhir"));
                        map.put("keterangan", json.getString("keterangan"));
                        map.put("bobot", json.getString("bobot"));
                        list_data.add(map);
                        AdapterJadwal adapter = new AdapterJadwal(context, list_data);
                        recyclerView.setAdapter(adapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                params.put("pengajar",pengajar);
                params.put("hari",hari);
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


}
