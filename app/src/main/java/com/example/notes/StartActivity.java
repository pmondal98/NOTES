package com.example.notes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.notes.user_sign.LoginActivity;
import com.example.notes.user_sign.RegisterActivity;
import com.google.firebase.auth.FirebaseAuth;

public class StartActivity extends AppCompatActivity {

    private Button btnlogin,btnregistration;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (Build.VERSION.SDK_INT>=21)
        {
            Window window=this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.mycolor));
        }

        btnlogin=findViewById(R.id.btnlogin);
        btnregistration=findViewById(R.id.btnregistration);

        firebaseAuth=FirebaseAuth.getInstance();

        UpdateUI();

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });

        btnregistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Register();
            }
        });
    }

    private void Login()
    {
        startActivity(new Intent(StartActivity.this, LoginActivity.class));
    }

    private void Register()
    {
        startActivity(new Intent(StartActivity.this, RegisterActivity.class));
    }

    private void UpdateUI()
    {
        if (firebaseAuth.getCurrentUser() != null)
        {
            Log.i("StartActivity :","firebaseAuth != null");
            startActivity(new Intent(StartActivity.this,MainActivity.class));
            finish();
        }
        else
        {
            Log.i("StartActivity :","firebaseAuth == null");
        }
    }
}
