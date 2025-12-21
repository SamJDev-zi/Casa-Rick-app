package com.casarick.app.util;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BarcodeGenerator {
    /**
     * Genera un código de barras simulado de 13 dígitos como String.
     * En un entorno real, se debería verificar la unicidad en la base de datos.
     * @return String de 13 dígitos.
     */
    public static String generateUniqueBarcode() {
        long timestamp = System.currentTimeMillis();
        String ts = String.valueOf(timestamp);

        if (ts.length() < 13) {
            StringBuilder sb = new StringBuilder(ts);
            while(sb.length() < 13) {
                sb.insert(0, '0');
            }
            return sb.toString();
        }
        return ts.substring(ts.length() - 13);
    }

    public static Pane createBarcode(String code, double width, double height) {
        Pane pane = new Pane();
        pane.setPrefSize(width, height);

        // Simulación visual basada en el código (HASH)
        // Esto genera un patrón único por producto, pero no es un estándar ISO.
        // Si tienes ZXing, avísame para darte el código con esa librería.

        double x = 0;
        double barWidth = width / (code.length() * 10);

        for (char c : code.toCharArray()) {
            int val = (int) c;
            String bin = Integer.toBinaryString(val);
            for (char b : bin.toCharArray()) {
                if (b == '1') {
                    Rectangle rect = new Rectangle(x, 0, barWidth, height);
                    rect.setFill(Color.BLACK);
                    pane.getChildren().add(rect);
                }
                x += barWidth + 0.5; // Espacio
            }
        }
        return pane;
    }
}
