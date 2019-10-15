package com.legacy07.x01bd_l;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Landing_Page extends AppCompatActivity {

    Button ota, download, internel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing__page);

        ota=findViewById(R.id.ota);
        internel=findViewById(R.id.internel);
        download=findViewById(R.id.download);
        download.setVisibility(View.GONE);

        internel.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MainActivity.class)));

        ota.setOnClickListener(v -> {
            Intent intent=new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("mode","ota");
            startActivity(intent);

        });


    }
}
