package com.nue.absensi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.nue.absensi.volley.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {
    EditText txtNip;
    Button btnMasuk;
    RequestQueue requestQueue;
    StringRequest stringRequest;
    private SharedPreferences local;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtNip=findViewById(R.id.txtLoginNip);
        btnMasuk=findViewById(R.id.btnLoginMasuk);

        final String token = FirebaseInstanceId.getInstance().getToken();


        btnMasuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nip = txtNip.getText().toString();

                cekToken(nip,token);
            }
        });


    }

    private void cekToken(final String nip, final String token) {

        requestQueue = Volley.newRequestQueue(this);
        stringRequest = new StringRequest(Request.Method.POST, Server.token
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response ", response);
                if(response.contains("1")) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("Hasil");
                        JSONObject json = jsonArray.getJSONObject(0);
                        String hasil = json.getString("token");
                        Toast.makeText(LoginActivity.this, hasil, Toast.LENGTH_SHORT).show();


                        if (hasil.equals(token)){
                            loginToken(nip,token);
                        }else if (hasil.equals("")){
                            loginAwal(nip,token);
                        }else {
                            Toast.makeText(LoginActivity.this, "token tidak cocok", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(LoginActivity.this, "NIP salah !",Toast.LENGTH_LONG).show();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("nip",nip);
                return params;
            }
        };
        requestQueue.add(stringRequest);


    }

    private void loginToken(final String nip, final String token) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Server.loginToken, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.contains("1")) {
                    Toast.makeText(LoginActivity.this, "berhasil login",Toast.LENGTH_LONG).show();
                    local = getSharedPreferences("local", MODE_PRIVATE);
                    local.edit().putString("NIP", nip).commit();
                    Intent intent = new Intent(LoginActivity.this , MainActivity.class);
                    startActivity(intent);
                    finish();


                }else{
                    Toast.makeText(LoginActivity.this, "gagal login",Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("nip", nip);
                map.put("token", token);

                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void loginAwal(final String nip, final String token) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Server.loginAwal, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.contains("1")) {
                    Toast.makeText(LoginActivity.this, "Berhasil register perangkat",Toast.LENGTH_LONG).show();
                    loginToken(nip,token);
                }else{
                    Toast.makeText(LoginActivity.this, "Gagal",Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("nip", nip);
                map.put("token", token);

                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

}
