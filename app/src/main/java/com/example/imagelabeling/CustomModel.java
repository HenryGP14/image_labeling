package com.example.imagelabeling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.annotation.SuppressLint;
import android.media.Image;
import android.os.Bundle;
import android.util.Size;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class CustomModel extends AppCompatActivity {

    InputImage inputImage;
    List<ImageLabel> etiquetasDeImagenes;
    PreviewView vistaPrevia;
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ProcessCameraProvider cameraProvider;
    CameraSelector cameraSelector;
    Preview preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_model);

        vistaPrevia = findViewById(R.id.vistaPrevia);
        AnalizadorFotogramas();
    }

    public void AnalizadorFotogramas(){

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(()->{
            try {
                cameraProvider = cameraProviderFuture.get();
                preview = new Preview.Builder().build();
                cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                preview.setSurfaceProvider(vistaPrevia.createSurfaceProvider());
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview);
            }
            catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                System.out.println("Hay este error : ---" + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));

        LocalModel localModel = new LocalModel.Builder().setAssetFilePath("mnasnet_1.3_224_1_metadata_1.tflite").build();
        CustomImageLabelerOptions etqImagenesPersonaliada = new CustomImageLabelerOptions.Builder(localModel)
                .setConfidenceThreshold(0.5f)
                .setMaxResultCount(5)
                .build();
        ImageLabeler etiquetadorImagenes = ImageLabeling.getClient(etqImagenesPersonaliada);

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @SuppressLint("UnsafeExperimentalUsageError")
            @Override
            public void analyze(@NonNull ImageProxy image) {
                inputImage = InputImage.fromMediaImage(image.getImage(), image.getImageInfo().getRotationDegrees());
                image.close();
            }
        });

        /*etiquetadorImagenes.process(inputImage).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
            @Override
            public void onSuccess(List<ImageLabel> imageLabels) {
                System.out.println("Tarea completada correctamente");
                etiquetasDeImagenes = imageLabels;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("La tarea falló con excepciones");
            }
        });*/

        //Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }
    public void PrimerMetodo(){
        //Cargue el modelo
        LocalModel localModel = new LocalModel.Builder().setAssetFilePath("mnasnet_1.3_224_1_metadata_1.tflite").build();

        //Configure el etiquetador de imágenes
        CustomImageLabelerOptions etqImagenesPersonaliada = new CustomImageLabelerOptions.Builder(localModel)
                .setConfidenceThreshold(0.5f)
                .setMaxResultCount(5)
                .build();

        ImageLabeler etiquetadorImagenes = ImageLabeling.getClient(etqImagenesPersonaliada);

        //Prepare la imagen de entrada
        /*class AnalizadorFotograma implements ImageAnalysis.Analyzer{
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = imageProxy.getImage();
                if(mediaImage != null){
                    imagen = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                }
            }
        }*/

        //Ejecute el etiquetador de imagenes
        etiquetadorImagenes.process(inputImage).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
            @Override
            public void onSuccess(List<ImageLabel> imageLabels) {
                System.out.println("Tarea completada correctamente");
                etiquetasDeImagenes = imageLabels;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("La tarea falló con excepciones");
            }
        });

        //Obtener información sobre las entidades etiquetadas
        for (ImageLabel label : etiquetasDeImagenes){
            String text = label.getText();
            float confidence = label.getConfidence();
            int index = label.getIndex();
        }
    }

    public void TercerMetodo(){
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(()->{
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            }
            catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                System.out.println("Hay este error : ---" + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        preview.setSurfaceProvider(vistaPrevia.createSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview);
    }
}