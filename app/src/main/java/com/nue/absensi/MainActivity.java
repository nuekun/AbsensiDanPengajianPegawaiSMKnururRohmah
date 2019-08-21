package com.nue.absensi;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.nue.absensi.adapter.AdapterJadwal;
import com.nue.absensi.adapter.AdapterKolega;
import com.nue.absensi.fragment.GajiFragment;
import com.nue.absensi.fragment.JadwalFragment;
import com.nue.absensi.fragment.KolegaFragment;
import com.nue.absensi.volley.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {
    private SharedPreferences local;
    NavigationView drawer;
    DrawerLayout drawerLayout;
    TextView txtnama,txtstatus;
    MenuItem itemCekIn;
    private Bitmap bitmap = null;
    private Uri imageUri;
    private static final int PICK_IMAGE = 1;
    private static final int PICK_Camera_IMAGE = 2;
    RequestQueue requestQueue;
    StringRequest stringRequest;
    int bobotGaji;
    CircleImageView gbrProfil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        local = getSharedPreferences("local", MODE_PRIVATE);
//        BottomNavigationView ButtonNav = findViewById(R.id.buttonNavBeranda);
//        ButtonNav.setOnNavigationItemSelectedListener(navListener);
        drawer = findViewById(R.id.mainDrawer);
        drawerLayout = findViewById(R.id.drawerLayout);
        drawer.setNavigationItemSelectedListener(drawerListener);

        if (savedInstanceState==null){
            getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout,new JadwalFragment()).commit();
            drawer.setCheckedItem(R.id.drawerBeranda);
        }


    }

    NavigationView.OnNavigationItemSelectedListener drawerListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            switch (menuItem.getItemId()){
                case R.id.drawerBeranda:
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout,new JadwalFragment()).commit();
                    break;
                case R.id.drawerKolega:
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout,new KolegaFragment()).commit();
                    break;
                    case R.id.drawerGaji:
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout,new GajiFragment()).commit();
                    break;
                case R.id.drawerCekIn:
                    scanBarcode();
                    break;

            }

            drawerLayout.closeDrawer(GravityCompat.START);

            return true;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        setBobotGaji(0);
        ambilGaji();
    }

    private void scanBarcode() {

        String fileName = "new-photo-name.jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, PICK_Camera_IMAGE);

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri selectedImageUri = null;
        String filePath = null;

                if (resultCode == RESULT_OK) {
                    selectedImageUri = imageUri;
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT).show();
                }

        if (selectedImageUri != null) {
            try {
                String filemanagerstring = selectedImageUri.getPath();
                String selectedImagePath = getPath(selectedImageUri);

                if (selectedImagePath != null) {
                    filePath = selectedImagePath;
                } else if (filemanagerstring != null) {
                    filePath = filemanagerstring;
                } else {
                    Toast.makeText(MainActivity.this, "Unknown path",
                            Toast.LENGTH_LONG).show();
                    Log.e("Bitmap", "Unknown path");
                }

                if (filePath != null) {
                    decodeFile(filePath);
                } else {
                    bitmap = null;
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Internal error",
                        Toast.LENGTH_LONG).show();
                Log.e(e.getClass().getName(), e.getMessage(), e);
            }
        }

    }


    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    public void decodeFile(String filePath) {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, o);
        final int REQUIRED_SIZE = 1024;
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        bitmap = BitmapFactory.decodeFile(filePath, o2);
//        gbrPriview.setImageBitmap(bitmap);


        deteksiQR(bitmap);

    }


    private void deteksiQR(Bitmap bitmap) {

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionBarcodeDetectorOptions options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(
                        FirebaseVisionBarcode.FORMAT_QR_CODE,
                        FirebaseVisionBarcode.FORMAT_PDF417
                )
                .build();
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);

        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                        prosesHasil(firebaseVisionBarcodes);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "barcode tidak di temukan!" , Toast.LENGTH_SHORT).show();
                    }
                });



    }

    private void prosesHasil(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {

        if (firebaseVisionBarcodes.isEmpty()){

            Toast.makeText(MainActivity.this, "barcode tidak di temukan !",Toast.LENGTH_LONG).show();

        }

        for (final FirebaseVisionBarcode item : firebaseVisionBarcodes) {



            int value_type = item.getValueType();
            switch (value_type){

                case FirebaseVisionBarcode.TYPE_TEXT:{

                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                    builder.setTitle("barcode berhasil di temukan !");
                   // builder.setMessage(item.getRawValue());
                    builder.setPositiveButton("proses", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            if (item.getRawValue().equals("a7x")){
                                cekIn();}

                            else{

                                Toast.makeText(MainActivity.this, "barcode tidak cocok !",Toast.LENGTH_LONG).show();

                            }


                        }
                    });
                    builder.setNegativeButton("batal", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                        }
                    });

                    android.support.v7.app.AlertDialog dialog = builder.create();
                    dialog.show();


                }
                break;

                default:
                    break;

            }
        }

    }



    private void cekIn() {

        final String karyawan = local.getString("NIP", "");

        final String gaji = Integer.toString(getBobotGaji());


        StringRequest stringRequest = new StringRequest(Request.Method.POST, Server.cekIn, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.contains("1")) {


                    Toast.makeText(MainActivity.this, "berhasil melakukan absensi",Toast.LENGTH_LONG).show();

                    itemCekIn = drawer.getMenu().findItem(R.id.drawerCekIn);
                    itemCekIn.setEnabled(false);



                }else{
                    Toast.makeText(MainActivity.this, "gagal memproses hasil",Toast.LENGTH_LONG).show();


                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();



            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("karyawan", karyawan);
                map.put("gaji", gaji);

                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);



    }


    private void Permissions() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA},
                99);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 99: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Permissions();
                }
                return;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        setBobotGaji(0);
    }


//
//    BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
//        @Override
//        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
//            Fragment selectedFragment = null;
//
//            switch (menuItem.getItemId()) {
//
//                case R.id.menu_absen:
//                    selectedFragment = new Absensi();
//                    break;
//                case R.id.menu_gaji:
//                    selectedFragment = new gaji();
//                    break;
//                case R.id.menu_jadwal:
//                    selectedFragment = new jadwal();
//                    break;
//            }
//
//            getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout,selectedFragment).commit();
//
//            return true;
//
//        }
//    };

    @Override
    protected void onStart() {
        super.onStart();

        Permissions();
//        txtnama=drawer.getHeaderView(0).findViewById(R.id.txtDrawerNama);
//        txtnama.setText("m haqqul yakin");
//


        String hari= ambilHari();

        String NIP = local.getString("NIP", "");
        ambilStatus(NIP);
        ambilGaji();
        updateUI(NIP);
        ambilProfil(NIP);

    }

    private void ambilProfil(final String nip) {


        requestQueue = Volley.newRequestQueue(this);

        stringRequest = new StringRequest(Request.Method.POST, Server.ambilProfil
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response ", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("Hasil");

                        JSONObject json = jsonArray.getJSONObject(0);

                        String gambar = json.getString("profil");
                        String URL = Server.server+"profil/"+gambar;


                    gbrProfil = drawer.getHeaderView(0).findViewById(R.id.gbrDrawerProfil);


                    Glide.with(MainActivity.this).load(URL).into(gbrProfil);

                    txtnama=drawer.getHeaderView(0).findViewById(R.id.txtDrawerNama);
                    txtnama.setText(json.getString("nama"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("karyawan",nip);

                return params;
            }
        };
        requestQueue.add(stringRequest);




    }

    private void ambilGaji() {
        setBobotGaji(0);
        final String NIP = local.getString("NIP", "");
        final String hari = ambilHari();

        requestQueue = Volley.newRequestQueue(this);

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


                        int gaji = Integer.parseInt(json.getString("bobot"));

                        int bg = getBobotGaji();

                        int tg = bg+gaji;

                        setBobotGaji(tg);


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }



                txtstatus=drawer.getHeaderView(0).findViewById(R.id.txtDrawerNIP);
                txtstatus.setText("total jam mengajar hari ini "+Integer.toString(getBobotGaji()));

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("pengajar",NIP);
                params.put("hari",hari);
                return params;
            }
        };
        requestQueue.add(stringRequest);



    }

    public String ambilHari(){

        String weekDay = "";

        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        if (Calendar.MONDAY == dayOfWeek) {
            weekDay = "senin";
        } else if (Calendar.TUESDAY == dayOfWeek) {
            weekDay = "selasa";
        } else if (Calendar.WEDNESDAY == dayOfWeek) {
            weekDay = "rabu";
        } else if (Calendar.THURSDAY == dayOfWeek) {
            weekDay = "kamis";
        } else if (Calendar.FRIDAY == dayOfWeek) {
            weekDay = "jumat";
        } else if (Calendar.SATURDAY == dayOfWeek) {
            weekDay = "sabtu";
        } else if (Calendar.SUNDAY == dayOfWeek) {
            weekDay = "minggu";
        }

       return weekDay;


    }

    private void ambilStatus(final String nip) {


        StringRequest stringRequest = new StringRequest(Request.Method.POST, Server.cekStatus, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.contains("1")) {



        itemCekIn = drawer.getMenu().findItem(R.id.drawerCekIn);
        itemCekIn.setEnabled(false);



                }else{
                 // Toast.makeText(MainActivity.this, "silahkan lakukan scanbarcode untuk absensi",Toast.LENGTH_LONG).show();


                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                itemCekIn = drawer.getMenu().findItem(R.id.drawerCekIn);
                itemCekIn.setEnabled(false);


            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("nip", nip);

                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);



    }


    private void updateUI(String nip) {

        if(nip.equals("")){
            Intent intent = new Intent(MainActivity.this , LoginActivity.class);
            startActivity(intent);
            finish();
        }

    }

    public int getBobotGaji() {
        return bobotGaji;
    }

    public void setBobotGaji(int bobotGaji) {
        this.bobotGaji = bobotGaji;
    }
}
