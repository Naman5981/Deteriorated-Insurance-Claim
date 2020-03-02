package com.naman.insuranceclaim;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DamageCar extends AppCompatActivity {

    private int PICK_IMAGES,CAMERA_REQUEST=1888;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123,MY_CAMERA_REQUEST_CODE = 100;
    Button picker, ok,capture;
    OkHttpClient client;
    LinearLayout layout;
    Uri mImageUri = null;
    ArrayList<Uri> mArrayUri;
    Request request;
    String pathToImage,car_type_str,regist_no_str,car_name_str,price_str,man_year_str;
    String[] car_type_list = {"Hatchback" , "SUV" , "Sedan"};
    Bundle bundle;
    Spinner car_type_spin;
    ArrayList<String> labelarr, probabilityarr;
    File photoFile;
    EditText man_year,car_name,regist_no,car_price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int ALL_PERMISSIONS = 101;
        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);

        setContentView(R.layout.carcheck);
        layout =findViewById(R.id.imageView);
        picker = findViewById(R.id.picker);
        man_year = findViewById(R.id.man_year);
        car_name = findViewById(R.id.car_name);
        regist_no = findViewById(R.id.regist_no);
        car_price = findViewById(R.id.car_price);


        //Spinner
        car_type_spin = findViewById(R.id.car_type_spin);
        ArrayAdapter array = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, car_type_list);
        array.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        car_type_spin.setAdapter(array);
        car_type_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                car_type_str = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageUri=null;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select atleast 3 Pictures"), PICK_IMAGES);
            }
        });

        ok = findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( (man_year.getText().toString().trim().equals("") && car_name.getText().toString().trim().equals("")
                    && regist_no.getText().toString().trim().equals("")) && car_price.getText().toString().trim().equals(""))
                {
                    man_year.setError("This field is required");
                    car_name.setError("This field is required");
                    car_price.setError("This field is required");
                    regist_no.setError("This field is required");
                }

                else if(man_year.getText().toString().length() >0 && car_name.getText().toString().length() >0
                        && regist_no.getText().toString().length() >0 && car_price.getText().toString().length() >0)
                {
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        if (mImageUri != null) {
                            Log.e("position", "InIF");
                            client = new OkHttpClient();
                            final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg");

                            RequestBody requestBody = new MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("modelId", "2e13199d-4442-48d4-9e1e-b88090f3c666")
                                    .addFormDataPart("files", pathToImage,
                                            RequestBody.create(new File(pathToImage), MEDIA_TYPE_JPG))
                                    .build();
//                                    RequestBody formBody = new FormBody.Builder()
//                                            .add("modelId", "2e13199d-4442-48d4-9e1e-b88090f3c666")
//                                            .add("urls", "https://goo.gl/ICoiHc").build();

                            Log.e("position", "InREQUESTBODY");

                            request = new Request.Builder()
                                    .url("https://app.nanonets.com/api/v2/MultiLabelClassification/Model/2e13199d-4442-48d4-9e1e-b88090f3c666/LabelFiles/")
                                    .post(requestBody)
                                    .addHeader("Authorization", Credentials.basic("HEWJBag9ie5gQjVl4oBNoClyH6R4dIiz", ""))
                                    .build();
//                                    request = new Request.Builder()
//                                            .url(imageurl)
//                                            .post(formBody)
//                                            .addHeader("Authorization", Credentials.basic("HEWJBag9ie5gQjVl4oBNoClyH6R4dIiz",""))
//                                            .build();

                            Log.e("position", "InREQUEST" + request);
                            try {
                                Response response = client.newCall(request).execute();
                                JSONObject jsonObject = new JSONObject(response.body().string());
                                Log.e("response", "" + response + jsonObject);

                                if (jsonObject.getString("message").equals("Success")) {
                                    labelarr = new ArrayList<>();
                                    probabilityarr = new ArrayList<>();
                                    JSONArray data = jsonObject.getJSONArray("result");
                                    String dataa = data.toString();
                                    dataa = dataa.substring(1, dataa.length() - 1);
                                    JSONObject jsonObject1 = new JSONObject(dataa);
                                    JSONArray jsonArray = jsonObject1.getJSONArray("prediction");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        // select the particular JSON data
                                        JSONObject object = jsonArray.getJSONObject(i);
                                        String label = object.getString("label");
                                        String probability = object.getString("probability");

                                        // add to the lists in the specified format
                                        labelarr.add(label);
                                        probabilityarr.add(probability);
                                    }
                                    regist_no_str = regist_no.getText().toString();
                                    car_name_str = car_name.getText().toString();
                                    price_str = car_price.getText().toString();
                                    man_year_str = man_year.getText().toString();

                                    bundle=new Bundle();
//                                    bundle.putParcelableArrayList("URI",mArrayUri);
                                    bundle.putString("URI", String.valueOf(mImageUri));
                                    bundle.putStringArrayList("label",labelarr);
                                    bundle.putStringArrayList("pro",probabilityarr);
                                    bundle.putString("man_year",man_year_str);
                                    bundle.putString("regist_no",regist_no_str);
//                                    bundle.putString("car_name",car_name_str);
                                    bundle.putString("car_price",price_str);
                                    bundle.putString("car_type",car_type_str);
                                    bundle.putInt("n",jsonArray.length());
                                    Intent intent =new Intent(DamageCar.this,Result.class);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                }
                            } catch (IOException e) {
                                Log.e("erroeeee", "" + e);
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                thread.start();
            }}
        });

        capture = findViewById(R.id.capture);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageUri=null;
//                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                String pathFolder = "/Deteriorated Vehicle";
                File f = new File(new File(Environment.getExternalStorageDirectory() + pathFolder), "filename.jpg");

                File direct = new File(Environment.getExternalStorageDirectory() + pathFolder);

                if (!direct.exists()) {
                    File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + pathFolder);
                    wallpaperDirectory.mkdirs();
                }
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    photoFile = Environment.getExternalStoragePublicDirectory(pathFolder+
                            "/"+TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())+".jpg");

                    mImageUri= FileProvider.getUriForFile(DamageCar.this, BuildConfig.APPLICATION_ID + ".provider",photoFile);
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                mImageUri);
                        startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                        Log.e("jbsdfgduivdfjg", "yha aa gya");
                    }
                }
            }
            });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGES) {
            if (resultCode == RESULT_OK && data.getClipData() != null ) {
                mArrayUri=new ArrayList<>();
                ClipData mClipData = data.getClipData();
                mImageUri = mClipData.getItemAt(0).getUri();
                for (int i = 0; i < 3; i++) {
                    ClipData.Item item = mClipData.getItemAt(i);
                    Uri uri = item.getUri();
                    mArrayUri.add(uri);
                }
                pathToImage = RealPathUtil.getRealPathFromURI_API19(getApplicationContext(), mImageUri);
                for (int i = 0; i < mClipData.getItemCount(); i++) {
                    ImageView image = new ImageView(this);
                    image.setLayoutParams(new android.view.ViewGroup.LayoutParams(400,400));

                    // Adds the view to the layout
                    layout.addView(image);
                    Glide.with(DamageCar.this).load(mArrayUri.get(i)).into(image);
                    Log.e("psdjwg", "" + pathToImage);
                }
            }
        }
        if (requestCode == CAMERA_REQUEST)
        {
            ImageView image = new ImageView(this);
            image.setLayoutParams(new android.view.ViewGroup.LayoutParams(200,200));

            // Adds the view to the layout
            layout.addView(image);
            mArrayUri=new ArrayList<>();
            Glide.with(DamageCar.this).load(mImageUri).dontAnimate().into(image);
            if (data != null && data.getData() != null) {

                Log.e("pathtoimagssdf",""+mImageUri);
                pathToImage = RealPathUtil.getRealPathFromURI_API19(this, data.getData());
                Toast.makeText(this, "Image path " + pathToImage, Toast.LENGTH_LONG).show();
            } else {
                if (photoFile != null) {
                    pathToImage = photoFile.getAbsolutePath();
                    Toast.makeText(this, "Image path " + pathToImage, Toast.LENGTH_LONG).show();
                }
            }

            }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
