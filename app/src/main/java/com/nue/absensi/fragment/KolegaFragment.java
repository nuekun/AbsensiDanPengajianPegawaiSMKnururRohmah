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
import com.nue.absensi.R;
import com.nue.absensi.adapter.AdapterJadwal;
import com.nue.absensi.adapter.AdapterKolega;
import com.nue.absensi.volley.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class KolegaFragment extends Fragment {
    RequestQueue requestQueue;
    StringRequest stringRequest;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    ArrayList<HashMap<String, String>> list_data;
    Context context;
    SharedPreferences local;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_kolega, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        recyclerView = view.findViewById(R.id.recKolega);

        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        local = this.getActivity().getSharedPreferences("local", Context.MODE_PRIVATE);


        String pengajar,hari;

        pengajar = local.getString("NIP", "");


        ambilKolega(pengajar);


    }

    private void ambilKolega(final String pengajar) {

        recyclerView.setAdapter(null);
        requestQueue = Volley.newRequestQueue(context);
        list_data = new ArrayList<HashMap<String, String>>();
        list_data.clear();
        stringRequest = new StringRequest(Request.Method.POST, Server.ambilKolega
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
                        map.put("email", json.getString("email"));
                        map.put("nama", json.getString("nama"));
                        map.put("tgl_lahir", json.getString("tgl_lahir"));
                        map.put("no_hp", json.getString("no_hp"));
                        map.put("alamat", json.getString("alamat"));
                        map.put("profil", json.getString("profil"));
                        list_data.add(map);
                        AdapterKolega adapter = new AdapterKolega(context, list_data);
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
                params.put("karyawan",pengajar);

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
