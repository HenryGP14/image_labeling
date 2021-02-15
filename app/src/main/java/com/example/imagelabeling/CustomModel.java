package com.example.imagelabeling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class CustomModel extends AppCompatActivity {

    static InputImage inputImage = null;
    PreviewView vistaPrevia;
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ProcessCameraProvider cameraProvider;
    Camera camera;
    CameraSelector cameraSelector;
    Preview preview;
    TextView etiqueta, text, confidence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_model);

        text = (TextView)findViewById(R.id.Text);
        etiqueta = (TextView)findViewById(R.id.Label);
        confidence = (TextView)findViewById(R.id.Confidence);

        vistaPrevia = findViewById(R.id.vistaPrevia);
        AnalizadorFotogramas();
    }

    public void AnalizadorFotogramas(){
        LocalModel localModel = new LocalModel.Builder().setAssetFilePath("mnasnet_1.3_224_1_metadata_1.tflite").build();
        CustomImageLabelerOptions etqImagenesPersonaliada = new CustomImageLabelerOptions.Builder(localModel)
                .setConfidenceThreshold(0.5f)
                .setMaxResultCount(5)
                .build();
        //ImageLabeler etiquetadorImagenes = ImageLabeling.getClient(etqImagenesPersonaliada);
        ImageLabeler etiquetadorImagenes = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(()->{
            try {
                cameraProvider = cameraProviderFuture.get();
                preview = new Preview.Builder().build();
                cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                preview.setSurfaceProvider(vistaPrevia.createSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        //.setTargetResolution(new Size(480, 640))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).setBackgroundExecutor(ContextCompat.getMainExecutor(this))
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
                    @SuppressLint("UnsafeExperimentalUsageError")
                    @Override
                    public void analyze(@NonNull ImageProxy image) {
                        if(image.getImage() != null){
                            inputImage = InputImage.fromMediaImage(image.getImage(), image.getImageInfo().getRotationDegrees());
                            //Ejecute el etiquetador de imagenes
                            etiquetadorImagenes.process(inputImage).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                                @Override
                                public void onSuccess(List<ImageLabel> imageLabels) {
                                    /*etiqueta.setText("Label: " + imageLabels.get(0).getIndex());
                                    confidence.setText("Confidence: " + imageLabels.get(0).getConfidence());
                                    text.setText("Text: " + imageLabels.get(0).getText());*/
                                    for (ImageLabel label : imageLabels){
                                        text.setText("Text: " + label.getText());
                                        confidence.setText("Confidence: " + label.getConfidence());
                                        etiqueta.setText("Label: " + label.getIndex());
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("La tarea falló con excepciones: " + e.getMessage() + ", Causado:" + e.getCause().toString());
                                }
                            });
                        }
                        else
                            System.out.println("############# Parece que no se está capturando la imagen");
                        //image.close();
                    }
                });
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            }
            catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                System.out.println("Hay este error : ---" + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));

        /*Nota: si utiliza un modelo TensorFlow Lite que es incompatible con ML Kit, obtendrá una MlKitException con el código de
        error MlKitException-INVALID_ARGUMENT y algunos detalles sobre por qué no es compatible. Consulte Requisitos de compatibilidad
        de modelos personalizados para obtener más información.

        Nota: ML Kit solo admite modelos de clasificación de imágenes personalizados. Aunque AutoML Vision permite el entrenamiento
        de modelos de detección de objetos, estos no se pueden utilizar con ML Kit.
        */
    }
}