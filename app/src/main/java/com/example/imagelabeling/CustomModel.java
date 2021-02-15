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

    InputImage imagen;
    List<ImageLabel> etiquetasDeImagenes;
    PreviewView vistaPrevia;
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_model);

        vistaPrevia = findViewById(R.id.vistaPrevia);
        TercerMetodo();
        //PrimerMetodo();

        /*ImageAnalysis imageAnalysis =
            new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                int rotationDegrees = image.getImageInfo().getRotationDegrees();
                    // insert your code here.
                }
            });

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);*/
        //SegundoMetodo();
    }

    private ImageCapture imageCapture;
    public  void PrimerMetodo(){
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
        etiquetadorImagenes.process(imagen).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
            @Override
            public void onSuccess(List<ImageLabel> imageLabels) {
                //Tarea completada correctamente
                System.out.println("Método OnSuccess");
                etiquetasDeImagenes = imageLabels;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //La tarea falló con excepciones
                System.out.println("Método onFailure: La tarea falló con excepciones");
            }
        });

        //Obtener información sobre las entidades etiquetadas
        for (ImageLabel label : etiquetasDeImagenes){
            String text = label.getText();
            float confidence = label.getConfidence();
            int index = label.getIndex();
        }
    }

    public void SegundoMetodo(){
        PreviewView previewView = findViewById(R.id.vistaPrevia);

        ListenableFuture cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // Ahora se garantiza que el proveedor de la cámara estará disponible
                ProcessCameraProvider cameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();

                // Configure el caso de uso del buscador de vista para mostrar la vista previa de la cámara
                Preview preview = new Preview.Builder().build();

                // Configure el caso de uso de captura para permitir que los usuarios tomen fotos
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // Elija la cámara requiriendo una lente orientada
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // Adjunte casos de uso a la cámara con el mismo propietario del ciclo de vida
                Camera camera = cameraProvider.bindToLifecycle(
                        ((LifecycleOwner) this),
                        cameraSelector,
                        preview,
                        imageCapture);

                // Conecte el caso de uso de la vista previa a la vista previa
                preview.setSurfaceProvider(
                        previewView.createSurfaceProvider());
            } catch (InterruptedException | ExecutionException e) {
                // Currently no exceptions thrown. cameraProviderFuture.get()
                // shouldn't block since the listener is being called, so no need to
                // handle InterruptedException.
                System.out.println("---Error:--- " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
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
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);
    }
}