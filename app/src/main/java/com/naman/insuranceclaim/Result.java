package com.naman.insuranceclaim;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class Result extends AppCompatActivity {

    LinearLayout linearLayout;
    ImageView imageView;
    Button send_to_mail;
    String pro1 = null, pro2 = null, pro3= null, pro4= null, pro5= null;
    Float temp;
    String car_type;
    Double est_price=0.0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        Bundle bundle=getIntent().getExtras();
        String uri = bundle.getString("URI");
        final String regist_no_str2,car_name_str2,price_str2,man_year_str2,car_type_str;
        regist_no_str2 = bundle.getString("regist_no");
        man_year_str2 = bundle.getString("man_year");
        price_str2 = bundle.getString("car_price");
        car_name_str2 = bundle.getString("car_name");
        car_type= bundle.getString("car_type");
        ArrayList<String> pro=bundle.getStringArrayList("pro");
        ArrayList<String> label=bundle.getStringArrayList("label");
        int n=bundle.getInt("n");
        linearLayout =  (LinearLayout) findViewById(R.id.linearlayout);
        imageView=findViewById(R.id.capturedimage);
        Glide.with(this).load(uri).into(imageView);
        send_to_mail = findViewById(R.id.btn_send_mail);
        send_to_mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","", null));
                String subject = "Deteriorated Vehicle Report";
                String message = regist_no_str2 + " " + man_year_str2 + " " + price_str2+"\n ESTIMATED PRICE "+ " "+est_price;
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(intent, "Choose an Email client :"));
            }
        });

        for(int i=0;i<n;i++) {
            temp=Float.valueOf(pro.get(i));
            Log.e("dhfefr",""+temp);
            String data=label.get(i) + "," + "" + pro.get(i);

            if(car_type.equals("Hatchback"))
            {
                if (label.get(i).equals("broken_lights") && temp >= 0.2)
                {
                    est_price=est_price+2000;
                }
                if (label.get(i).equals("broken_bumper") && temp >= 0.2)
                {
                    est_price=est_price+10000;
                }
                if (label.get(i).equals("broken_glass") && temp >= 0.2)
                {
                    est_price=est_price+5000;
                }
                if (label.get(i).equals("scratch") && temp >= 0.2)
                {
                    est_price=est_price+600;
                }
                if (label.get(i).equals("dent") && temp >= 0.2)
                {
                    est_price=est_price+600;
                }
            }
//            Toast.makeText(this, ""+est_price, Toast.LENGTH_SHORT).show();
//        }
    }
        TextView tv = new TextView(this);
        tv.setText("Estimated Price = "+est_price);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setTextSize(20);
        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(tv);
//        for(int i = 0 ; i < n ; i++)
//        {
//            if(i==1)
//                pro1 = pro.get(1);
//            if(i==2)
//                pro2 = pro.get(2);
//            if(i==3)
//                pro3 = pro.get(3);
//            if(i==4)
//                pro4 = pro.get(4);
//            if(i==5)
//                pro5 = pro.get(5);
//        }
//        Toast.makeText(this, pro5, Toast.LENGTH_SHORT).show();
}
}
