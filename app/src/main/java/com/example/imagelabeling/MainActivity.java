package com.example.imagelabeling;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.CustomRemoteModel;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.linkfirebase.FirebaseModelSource;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.IOException;
import java.security.cert.Extension;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button elegir;
    TextView resultConsult;
    ImageView imgContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        elegir = findViewById(R.id.btnElegir);
        resultConsult = findViewById(R.id.txtConsult);
        imgContainer = findViewById(R.id.imgContainer);

        LocalModel localModel =
                new LocalModel.Builder()
                        .setAssetFilePath("model.tflite")
                        // or .setAbsoluteFilePath(absolute file path to model file)
                        // or .setUri(URI to model file)
                        .build();

        CustomImageLabelerOptions customImageLabelerOptions =
                new CustomImageLabelerOptions.Builder(localModel)
                        .setConfidenceThreshold(0.5f)
                        .setMaxResultCount(5)
                        .build();
    }

    public void selecionar_img(View view) {
        resultConsult.setText(null);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccione una imagen"), 121);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 121) {
                imgContainer.setImageURI(data.getData());

                InputImage image;
                try {
                    image = InputImage.fromFilePath(getApplicationContext(), data.getData());

                    ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

                    labeler.process(image)
                            .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                                @Override
                                public void onSuccess(List<ImageLabel> labels) {
                                    for (ImageLabel label : labels) {
                                        String text = label.getText();
                                        float confidence = label.getConfidence();
                                        int index = label.getIndex();
                                        resultConsult.append(text + " " + confidence + "\n");
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            imgContainer.setImageURI(null);
            imgContainer.setImageResource(R.drawable.ic_launcher_background);
            Toast.makeText(this, "Lo lamento no se a cargado una imagen", Toast.LENGTH_SHORT).show();
        }
    }


}