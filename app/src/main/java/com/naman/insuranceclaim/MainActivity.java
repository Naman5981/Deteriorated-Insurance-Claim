package com.naman.insuranceclaim;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button save,damage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        save=findViewById(R.id.save);
        damage=findViewById(R.id.damage);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SaveCar.class);
                startActivity(intent);
            }
        });
        damage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(MainActivity.this,DamageCar.class);
                startActivity(intent);
            }
        });
    }
}
