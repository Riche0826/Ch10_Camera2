package flag.com.tw.ch10_camera2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    Uri imgUri; // 拍照存檔的uri
    ImageView imv; // imageView的物件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imv = (ImageView) findViewById(R.id.imageView);
    }

    public void onGet(View v){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != // 檢查是否取得權限
                PackageManager.PERMISSION_GRANTED){
            // 尚未取得權限
            ActivityCompat.requestPermissions(this, // 向使用者要求權限
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        }else savePhoto();
    }

    private void savePhoto() {
        imgUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,  // 透過內容資料庫新增一個圖片檔
                new ContentValues());

        Intent it = new Intent("android.media.action.IMAGE_CAPTURE");
        it.putExtra(MediaStore.EXTRA_OUTPUT, imgUri); // 將uri加到拍照intent的額外資料中

        startActivityForResult(it, 100); // 啟動intent並要求回傳資料
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults){
        if(requestCode == 200){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){ // 使用者允許權限
                savePhoto();
            }
        }else Toast.makeText(this, "請寫入權限", Toast.LENGTH_SHORT).show(); // 使用者拒絕權限
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == Activity.RESULT_OK && requestCode == 100){

            showImg();

        }else {
            Toast.makeText(this, "沒有拍到照片", Toast.LENGTH_LONG).show();
        }
    }

    void showImg() {
        int iw, ih, vw, vh;

        BitmapFactory.Options option = new BitmapFactory.Options(); // 建立選項物件

        option.inJustDecodeBounds = true;

        try{
            BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(imgUri), null, option
            );
        }catch (IOException e){
            Toast.makeText(this, "讀取照片資訊錯誤", Toast.LENGTH_LONG).show();

            return;
        }

        iw = option.outWidth;
        ih = option.outHeight;

        vw = imv.getWidth();
        vh = imv.getHeight();

        int scaleFactor = Math.min(iw / vw, ih / vh);

        option.inJustDecodeBounds = false;
        option.inSampleSize = scaleFactor;

        Bitmap bmp = null;

        try{
            bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(imgUri), null, option);
        }catch (IOException e){
            Toast.makeText(this, "無法取得照片", Toast.LENGTH_LONG).show();
        }

        imv.setImageBitmap(bmp);

        new AlertDialog.Builder(this)
                .setTitle("圖檔資訊")
                .setMessage("圖檔URI：" + imgUri.toString() +
                        "\n原始尺寸：" + iw + " * " + vw +
                        "\n載入尺寸：" + bmp.getWidth() + " * " + bmp.getHeight() +
                        "\n顯示尺寸：" + vw + " * " + vh)
                .setNeutralButton("關閉", null)
                .show();
    }
}
