package com.example.playmultimediatest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MusicPlayActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int CHOOSE_MUSIC = 1;

    private String pathFather;

    private MediaPlayer mediaPlayer = new MediaPlayer();

    private Button play, pause, stop, choice;

    private static ArrayList<String> audioPaths = new ArrayList<>();

    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);
        play = (Button) findViewById(R.id.music_start);
        pause = (Button) findViewById(R.id.music_pause);
        stop = (Button) findViewById(R.id.music_stop);
        choice = (Button) findViewById(R.id.music_path_choice);
        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);
        choice.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.music_start:
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
                break;
            case R.id.music_pause:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                break;
            case R.id.music_stop:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                break;
            case R.id.music_path_choice:
                if (ContextCompat.checkSelfPermission(MusicPlayActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MusicPlayActivity.this, new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 1);
                } else {
                    initMusicPlayer();
                }
                break;
            default:
                break;
        }

    }

    private void initMusicPlayer() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("audio/*");
        startActivityForResult(intent, CHOOSE_MUSIC);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initMusicPlayer();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_MUSIC:
                if (resultCode == RESULT_OK) {
                    // 判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理
                        handleAudioOnKitKat(data);
                    } else {
                        // 4.4以下系统使用这个方法处理
                        handleAudioBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void handleAudioBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String audioPath = getAudioPath(uri, null);
        pathFather = audioPath.substring(0, audioPath.lastIndexOf('/'));
        Log.d("Picture_Play", pathFather);
        getAudioPathFromSD();
        displayAudio(audioPath);

    }

    @TargetApi(19)
    private void handleAudioOnKitKat(Intent data) {
        String audioPath = null;
        Uri uri = data.getData();
        Log.d("TAG", "handleAudioOnKitKat: uri is " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Audio.Media._ID + "=" + id;
                audioPath = getAudioPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                audioPath = getAudioPath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            audioPath = getAudioPath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            audioPath = uri.getPath();
        }
        pathFather = audioPath.substring(0, audioPath.lastIndexOf('/'));
        // picturePath.setText(pathFather);
        Log.d("MusicPlayActivity", pathFather);
        getAudioPathFromSD();
        displayAudio(audioPath); // 根据图片路径显示图片
    }

    private String getAudioPath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayAudio(String audioPath) {
        //getImagePathFromSD();

        if (audioPath != null) {
            Log.d("MusicPlayActivity", audioPath);
            String name = audioPath.substring(audioPath.lastIndexOf("/")+1);
            Log.d("MusicPlayActivity", name);
            //字符串.substring(字符串.lastIndexOf("\\")+1)
            //pictureName.setText(name);
            index = audioPaths.indexOf(audioPath);
            for (String path : audioPaths)
                    Log.d("MusicPlayActivity", path);
            //pages.setText(String.valueOf(index+1)+"/"+String.valueOf(audioPaths.size()));
            //Bitmap bitmap = BitmapFactory.decodeFile(audioPath);
            try {
                mediaPlayer.setDataSource(audioPath);
                mediaPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    private void getAudioPathFromSD() {
        // 得到sd卡内image文件夹的路径   File.separator(/)
        String filePath = pathFather + '/';
        // 得到该路径文件夹下所有的文件

        //String filePath = Environment.getExternalStorageDirectory().toString() + File.separator
        //+ "image";
        File fileAll = new File(filePath);
        File[] files = fileAll.listFiles();
        // 将所有的文件存入ArrayList中,并过滤所有图片格式的文件
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (checkIsAudioFile(file.getPath())) {
                audioPaths.add(file.getPath());
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private boolean checkIsAudioFile(String fName) {
        boolean isAudioFile = false;
        // 获取扩展名
        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
                fName.length()).toLowerCase();
        if (FileEnd.equals("mp3")) {
            isAudioFile = true;
        } else {
            isAudioFile = false;
        }
        return isAudioFile;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
