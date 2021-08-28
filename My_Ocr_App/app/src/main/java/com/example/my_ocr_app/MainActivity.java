package com.example.my_ocr_app;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.Frame;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    Button button_capture, button_copy;
    TextView textView_data;
    private static final int REQUEST_CAMERA_CODE = 100;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_capture = findViewById(R.id.button_capture);
        button_copy = findViewById(R.id.button_copy);
        textView_data = findViewById(R.id.text_data);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, REQUEST_CAMERA_CODE);
        }

        button_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
            }
        });

        button_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String scanned_text =textView_data.getText().toString();
                copyToClipBoard(scanned_text);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of CropImageActivity
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    getTextFromImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            }
        }



    private void getTextFromImage(Bitmap bitmap){
        TextRecognizer recognizer = new TextRecognizer.Builder(this).build();
        if (!recognizer.isOperational()){
            Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
        }
        else{
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<Text.TextBlock>textBlockSparseArray = recognizer.detect(frame);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i=0; i<textBlockSparseArray.size();i++){
                Text.TextBlock textBlock = textBlockSparseArray.valueAt(i);
                stringBuilder.append(textBlock.getText());
                stringBuilder.append("\n");
            }

            textView_data.setText(stringBuilder.toString());
            button_capture.setText("Retake");
            button_copy.setVisibility(View.VISIBLE);
        }

    }


    private void copyToClipBoard(String text){

        ClipboardManager clipboard= (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied data",text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(MainActivity.this,"Copied to clipboard",Toast.LENGTH_SHORT).show();
    }
}
