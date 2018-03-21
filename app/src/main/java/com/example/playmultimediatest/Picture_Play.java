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
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.example.playmultimediatest.R.id.picture;

public class Picture_Play extends AppCompatActivity implements View.OnClickListener{

    public static final int CHOOSE_PHOTO = 1;

    private Button previous, last, choice;

    private TextView pages, pictureName, picturePath;

    private static ArrayList<String> paths = new ArrayList<>();

    private int index;

    private String pathFather;

    private ImageView picturePlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture__play);
        picturePlay = (ImageView) findViewById(picture);
        previous = (Button) findViewById(R.id.previous_picture);
        last = (Button) findViewById(R.id.last_picture);
        choice = (Button) findViewById(R.id.path_choice);
        pages = (TextView) findViewById(R.id.pages);
        pictureName = (TextView) findViewById(R.id.picture_name);
        picturePath = (TextView) findViewById(R.id.picture_path);

        previous.setOnClickListener(this);
        last.setOnClickListener(this);
        choice.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previous_picture:
                if (paths == null || paths.size() == 0) {
                    Toast.makeText(this, "no picture", Toast.LENGTH_SHORT).show();
                } else {
                    index = index - 1;
                    if (index < 0) {
                        index += 1;
                        Toast.makeText(this, "it is the first picture", Toast.LENGTH_SHORT).show();
                    } else {
                        displayImage(paths.get(index));
                    }
                }

                break;
            case R.id.last_picture:
                if (paths == null || paths.size() == 0) {
                    Toast.makeText(this, "no picture", Toast.LENGTH_SHORT).show();
                } else {
                    index = index + 1;
                    if (index > paths.size() - 1) {
                        index -= 1;
                        Toast.makeText(this, "it is the last picture", Toast.LENGTH_SHORT).show();
                    } else {
                        displayImage(paths.get(index));
                    }
                }

                break;
            case R.id.path_choice:
                paths.clear();
                if (ContextCompat.checkSelfPermission(Picture_Play.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Picture_Play.this, new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 1);
                } else {
                    openAlbum();
                }
                break;
            default:
                break;
        }
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
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
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    // 判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        handleImageOnKitKat(data);
                    } else {
                        // 4.4以下系统使用这个方法处理图片
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        Log.d("TAG", "handleImageOnKitKat: uri is " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        pathFather = imagePath.substring(0, imagePath.lastIndexOf('/'));
        picturePath.setText(pathFather);
        Log.d("Picture_Play", pathFather);
        getImagePathFromSD();
        displayImage(imagePath); // 根据图片路径显示图片
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        pathFather = imagePath.substring(0, imagePath.lastIndexOf('/'));
        Log.d("Picture_Play", pathFather);
        getImagePathFromSD();
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
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

    private void displayImage(String imagePath) {
        //getImagePathFromSD();

        if (imagePath != null) {
            Log.d("Picture_Play", imagePath);
            String name = imagePath.substring(imagePath.lastIndexOf("/")+1);
            //字符串.substring(字符串.lastIndexOf("\\")+1)
            pictureName.setText(name);
            index = paths.indexOf(imagePath);
            pages.setText(String.valueOf(index+1)+"/"+String.valueOf(paths.size()));
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            picturePlay.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }


    private void getImagePathFromSD() {
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
            if (checkIsImageFile(file.getPath())) {
                paths.add(file.getPath());
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private boolean checkIsImageFile(String fName) {
        boolean isImageFile = false;
        // 获取扩展名
        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
                fName.length()).toLowerCase();
        if (FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif")
                || FileEnd.equals("jpeg")|| FileEnd.equals("bmp") ) {
            isImageFile = true;
        } else {
            isImageFile = false;
        }
        return isImageFile;
    }


}
