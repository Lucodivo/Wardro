package com.example.connor.wardro;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Context context;

    private LinearLayout imageLayout;
    private ImageView initialImageView;
    private ImageView editedImageView;
    private Button captureImageButton;
    private Button editImageButton;

    private File initialPhotoFile;
    private File editedPhotoFile;

    private String currentPhotoPath;

    private Bitmap capturedBitmap;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 2;

    static {
        OpenCVLoader.initDebug();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        imageLayout = (LinearLayout) findViewById(R.id.imageLayout);
        initialImageView = (ImageView) findViewById(R.id.initialImageView);
        editedImageView = (ImageView) findViewById(R.id.editedImageView);

        captureImageButton = (Button) findViewById(R.id.captureImageButton);
        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if(takePictureIntent.resolveActivity(getPackageManager()) != null){
                    //Create the File where the photo should go
                    initialPhotoFile = null;
                    try {
                        initialPhotoFile = setUpPhotoFile();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(initialPhotoFile));
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        ex.printStackTrace();
                        Log.d("Picture File: ", "Exception thrown during creation");
                        initialPhotoFile = null;
                        currentPhotoPath = null;
                    }
                }
            }
        });

        editImageButton = (Button) findViewById(R.id.editImageButton);
        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(initialPhotoFile != null){
                    ObjectExtraction objEx = new ObjectExtraction(capturedBitmap);
                    Bitmap bmp = objEx.removeBackground();
                    FileOutputStream out = null;
                    try {
                        editedPhotoFile = createImageFile();
                        currentPhotoPath = editedPhotoFile.getAbsolutePath();
                        out = new FileOutputStream(currentPhotoPath);
                        bmp.compress(Bitmap.CompressFormat.JPEG,100,out);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        Log.d("Picture File: ", "Exception thrown during creation");
                    } catch (Exception e){
                        e.printStackTrace();
                    } finally {
                        try{
                            if (out != null){
                                out.close();
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }

                    setImageLayoutPic(bmp, editedImageView);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            capturedBitmap = BitmapFactory.decodeFile(currentPhotoPath);
            setImageLayoutPic(capturedBitmap, initialImageView);
            galleryAddPic();
            // currentPhotoPath = null;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File albumF = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", albumF);
        return image;
    }

    private File setUpPhotoFile() throws IOException {
        File f = createImageFile();
        currentPhotoPath = f.getAbsolutePath();

        return f;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    // function specific to this the image layout
    private void setImageLayoutPic(Bitmap bmp, ImageView imgView) {
        // Get the dimensions of the View
        int targetW = imageLayout.getWidth() / 2;
        int targetH = imageLayout.getHeight();

        Bitmap bitmap = Bitmap.createScaledBitmap(bmp, targetW, targetH, true);
        imgView.setImageBitmap(bitmap);
    }
}
