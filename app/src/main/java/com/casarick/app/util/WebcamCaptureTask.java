package com.casarick.app.util;

import com.github.sarxos.webcam.Webcam;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.embed.swing.SwingFXUtils;

import java.awt.image.BufferedImage;

/**
 * Tarea para manejar el flujo de video de la webcam en un hilo separado.
 */
public class WebcamCaptureTask extends Task<Void> {

    private final ImageView liveView;
    private final Webcam webcam;
    private volatile boolean running = true;

    public WebcamCaptureTask(ImageView liveView, Webcam webcam) {
        this.liveView = liveView;
        this.webcam = webcam;
    }

    @Override
    protected Void call() throws Exception {
        if (webcam == null) {
            System.err.println("Webcam no inicializada.");
            return null;
        }

        while (running && !isCancelled()) {
            BufferedImage image = webcam.getImage();
            if (image != null) {
                Image fxImage = SwingFXUtils.toFXImage(image, null);

                javafx.application.Platform.runLater(() -> liveView.setImage(fxImage));
            }
            Thread.sleep(50);
        }
        return null;
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        stopCamera();
    }

    public void stopCamera() {
        this.running = false;
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }
}