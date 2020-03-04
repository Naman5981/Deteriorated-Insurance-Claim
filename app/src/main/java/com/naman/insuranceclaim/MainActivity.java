package com.naman.insuranceclaim;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        currentUser = mAuth.getCurrentUser();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {

                if(currentUser!=null){
                    Intent intent=new Intent(MainActivity.this,DamageCar.class);
                    MainActivity.this.startActivity(intent);
                    MainActivity.this.finish();
                }
                else{
                    Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                    MainActivity.this.startActivity(intent);
                    MainActivity.this.finish();
                }
            }
        }, 1500);
    }
}
