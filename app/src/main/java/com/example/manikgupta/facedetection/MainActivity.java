package com.example.manikgupta.facedetection;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private FloatingActionButton fab2;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int SELECT_PICTURE = 2;
    private static final String TAG = MainActivity.class.getSimpleName();
    Bitmap imageBitmap;
    Button button;
    TextView textView;
    TextView textView2;

    Uri selectedImageUri;
    FirebaseVisionImage image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

button = (Button) findViewById(R.id.btn);
        imageView = (ImageView) findViewById(R.id.imageview);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        textView = (TextView) findViewById(R.id.textview);

        textView2 = (TextView) findViewById(R.id.textview2);




        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    firebaseFaceDetection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }







    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                // Get the url from data
                 selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // Get the path from the Uri
                    String path = getPathFromURI(selectedImageUri);
                    Log.i(TAG, "Image Path : " + path);
                    // Set the image in ImageView
                 //   ((ImageView) findViewById(R.id.imgView)).setImageURI(selectedImageUri);
                    imageView.setImageURI(selectedImageUri);
                }
            }
        }

    }

    /* Get the real path from the URI */
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }





    private void firebaseFaceDetection() throws IOException {

        FirebaseVisionFaceDetectorOptions options =
               new FirebaseVisionFaceDetectorOptions.Builder()
                        .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                        .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setMinFaceSize(0.15f)
                        .setTrackingEnabled(true)
                        .build();






      //  FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);

        try {
            image = FirebaseVisionImage.fromFilePath(this, selectedImageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        FirebaseVisionImage image = FirebaseVisionImage.fromFilePath(this, selectedImageUri);


        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);



        Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        // Task completed successfully
                                        // ...






                                       for (FirebaseVisionFace face : faces) {
                                            Rect bounds = face.getBoundingBox();
                                            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                                            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                            // nose available):
                                            FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
                                            if (leftEar != null) {
                                                FirebaseVisionPoint leftEarPos = leftEar.getPosition();
                                               // Toast.makeText(MainActivity.this, "left ear position is"+leftEarPos, Toast.LENGTH_SHORT).show();

                                            }

                                            // If classification was enabled:
                                            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                                float smileProb = face.getSmilingProbability();
                                                textView.setText("Smile prob. is- "+smileProb);
                                              //  Toast.makeText(MainActivity.this,"Smiliing probability is:"+smileProb,Toast.LENGTH_LONG).show();
                                            }
                                            if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                                float rightEyeOpenProb = face.getRightEyeOpenProbability();
                                                textView2.setText("Right eye open prob. is- "+rightEyeOpenProb);


                                                //  Toast.makeText(MainActivity.this,"Right Eye probability is:"+rightEyeOpenProb,Toast.LENGTH_LONG).show();

                                            }

                                            // If face tracking was enabled:
                                            if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                                                int id = face.getTrackingId();

                                             //   Toast.makeText(MainActivity.this,"Face id  is:"+id,Toast.LENGTH_LONG).show();

                                              //  if(id == 0)
                                              //  {
                                               //      textView.setText("Mohak there");
                                              //  }
                                            //    else {
                                                   // Toast.makeText(MainActivity.this,"Face id  is:"+id,Toast.LENGTH_LONG).show();
                                             //       textView.setText("Not recognized");
                                              //  }

                                            }
                                        }





                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        Toast.makeText(MainActivity.this, "Something went wrong, try again later", Toast.LENGTH_SHORT).show();
                                    }
                                });
    }

    @Override
    protected void onResume() {
        super.onResume();
       textView.setText(" ");
        textView2.setText(" ");

    }


}
