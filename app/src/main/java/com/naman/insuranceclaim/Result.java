package com.naman.insuranceclaim;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class Result extends AppCompatActivity {

    LinearLayout linearLayout;
    ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        Bundle bundle=getIntent().getExtras();
        String uri = bundle.getString("URI");
        ArrayList<String> pro=bundle.getStringArrayList("pro");
        ArrayList<String> label=bundle.getStringArrayList("label");
        int n=bundle.getInt("n");
        linearLayout =  (LinearLayout) findViewById(R.id.linearlayout);
        imageView=findViewById(R.id.capturedimage);
        Glide.with(this).load(uri).into(imageView);

        for(int i=0;i<n;i++) {
            String data=label.get(i) + "," + "" + pro.get(i);
            TextView tv = new TextView(this);
            tv.setText(data);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            Log.e("jlryutiyoupi[opiuyt",""+label.get(i) + "," + "" + pro.get(i));
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            linearLayout.addView(tv);
        }
    }
}
