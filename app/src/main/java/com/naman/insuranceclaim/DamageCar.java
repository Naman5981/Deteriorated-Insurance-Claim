package com.naman.insuranceclaim;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
    ImageView image;
    Uri mImageUri = null;
    Request request;
    String pathToImage;
    Bundle bundle;
    File file;
    Context context;
    ArrayList<String> labelarr, probabilityarr;
    File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int ALL_PERMISSIONS = 101;
        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);

        setContentView(R.layout.damagecar);
        image=findViewById(R.id.image);

        picker = findViewById(R.id.picker);
        picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageUri=null;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGES);
            }
        });

        ok = findViewById(R.id.button);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                                    bundle=new Bundle();
                                    bundle.putString("URI", String.valueOf(mImageUri));
                                    bundle.putStringArrayList("label",labelarr);
                                    bundle.putStringArrayList("pro",probabilityarr);
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
            }
        });

        capture = findViewById(R.id.capture);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageUri=null;
                Log.e("jbsdfgduivdfjg", "yha aa gya camera open kr rha hu");
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
            if (resultCode == RESULT_OK && data.getData() != null) {
                mImageUri = data.getData();
                image = findViewById(R.id.image);
                Glide.with(DamageCar.this).load(mImageUri).into(image);
                pathToImage = RealPathUtil.getRealPathFromURI_API19(getApplicationContext(), mImageUri);
                Log.e("psdjwg", "" + pathToImage);
            }
        }
        if (requestCode == CAMERA_REQUEST)
        {
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
