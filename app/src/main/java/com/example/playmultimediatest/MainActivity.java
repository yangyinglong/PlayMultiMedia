package com.example.playmultimediatest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button picturePlay;

    private Button viedoPlay;

    private Button musicPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        picturePlay = (Button) findViewById(R.id.picture_play);
        viedoPlay = (Button) findViewById(R.id.video_play);
        musicPlay = (Button) findViewById(R.id.music_play);

        picturePlay.setOnClickListener(this);
        viedoPlay.setOnClickListener(this);
        musicPlay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.picture_play:
                Toast.makeText(MainActivity.this, "you click picture button", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, Picture_Play.class);
                startActivity(intent);
                break;
            case R.id.music_play:
                break;
            case R.id.video_play:
                break;
            default:
                break;
        }

    }
}
