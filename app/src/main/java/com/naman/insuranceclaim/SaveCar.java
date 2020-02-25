package com.naman.insuranceclaim;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.grpc.Context;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SaveCar extends AppCompatActivity {

    public FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final String CAR_KEY = "car_name";
    private static final String MANUFACTURE_KEY = "manufacture_year";
    private int PICK_IMAGES,CAMERA_REQUEST=1888;
    Button picker, ok,capture;
    OkHttpClient client;
    ImageView image;
    EditText man_year,car_name;
    Uri mImageUri = null;
    Request request;
    String pathToImage;
    Bundle bundle;
    ArrayList<String> labelarr, probabilityarr;
    File photoFile;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        int ALL_PERMISSIONS = 101;
        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);

        setContentView(R.layout.savecar);
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

        ok = findViewById(R.id.ok);
        man_year=findViewById(R.id.man_year);
        car_name=findViewById(R.id.car_name);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebase_add();
                thread_car();
                uploadImage();

            }

        });

        capture = findViewById(R.id.capture);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageUri=null;
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

                    mImageUri= FileProvider.getUriForFile(SaveCar.this, BuildConfig.APPLICATION_ID + ".provider",photoFile);
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                mImageUri);
                        startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                    }
                }
            }
            });
    }

    private void uploadImage()
    {
        if(mImageUri!=null)
        {
            StorageReference reference = storageReference.child("Images/"+ UUID.randomUUID().toString());
            reference.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(SaveCar.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled()
                {
                    Toast.makeText(SaveCar.this, "Operation cancelled", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                }
            });
        }
    }

    private void firebase_add()
    {
        String man_year_str = man_year.getText().toString();
        String car_name_str = car_name.getText().toString();
        Map<String, Object> userCarData = new HashMap<>();
        userCarData.put(CAR_KEY, car_name_str);
        userCarData.put(MANUFACTURE_KEY, man_year_str);

        db.collection("users").add(userCarData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(SaveCar.this, "Car Info Saved", Toast.LENGTH_SHORT).show();
                Log.d(CAR_KEY, "DocumentSnapshot added with ID: " + documentReference.getId());
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SaveCar.this, "Some Error Occured", Toast.LENGTH_SHORT).show();
                    }
        });
    }

    private void thread_car()
    {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                if (mImageUri != null) {
                    client = new OkHttpClient();
                    final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg");

                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("modelId", "e45a1aac-7659-4e32-9013-fd6cad21a2e7")
                            .addFormDataPart("files", pathToImage,
                                    RequestBody.create(new File(pathToImage), MEDIA_TYPE_JPG))
                            .build();
//                                    RequestBody formBody = new FormBody.Builder()
//                                            .add("modelId", "2e13199d-4442-48d4-9e1e-b88090f3c666")
//                                            .add("urls", "https://goo.gl/ICoiHc").build();


                    request = new Request.Builder()
                            .url("https://app.nanonets.com/api/v2/MultiLabelClassification/Model/e45a1aac-7659-4e32-9013-fd6cad21a2e7/LabelFiles/")
                            .post(requestBody)
                            .addHeader("Authorization", Credentials.basic("UVemafYg57euzBRNfo_PzNlhu7o7LioR", ""))
                            .build();
//                                    request = new Request.Builder()
//                                            .url(imageurl)
//                                            .post(formBody)
//                                            .addHeader("Authorization", Credentials.basic("HEWJBag9ie5gQjVl4oBNoClyH6R4dIiz",""))
//                                            .build();

                    try {
                        Response response = client.newCall(request).execute();
                        JSONObject jsonObject = new JSONObject(response.body().string());

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
                            Intent intent =new Intent(SaveCar.this,Result.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGES) {
            if (resultCode == RESULT_OK && data.getData() != null) {
                mImageUri = data.getData();
                image = findViewById(R.id.image);
                Glide.with(SaveCar.this).load(mImageUri).into(image);
                pathToImage = RealPathUtil.getRealPathFromURI_API19(getApplicationContext(), mImageUri);
            }
        }
        if (requestCode == CAMERA_REQUEST)
        {
            Glide.with(SaveCar.this).load(mImageUri).dontAnimate().into(image);
            if (data != null && data.getData() != null) {
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
