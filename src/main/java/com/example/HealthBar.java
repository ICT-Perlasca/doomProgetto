package com.example;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

//barra della vita in basso a sinistra dello schermo
public class HealthBar extends StackPane {

    private static final double W = 260;
    private static final double H = 26;

    private final Rectangle fill = new Rectangle(W, H);
    private final Label text = new Label("100");
    private final double max;

    public HealthBar(double max) {
        this.max = max;
        setMouseTransparent(true);
        setPickOnBounds(false);
        setMaxSize(W + 4, H + 4);
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-border-color: white; -fx-border-width: 2; -fx-padding: 0;");

        fill.setFill(Color.LIMEGREEN);
        text.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        text.setTextFill(Color.WHITE);
        text.setMaxWidth(Double.MAX_VALUE);
        text.setAlignment(Pos.CENTER);
        getChildren().addAll(fill, text);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
    }

    //va chiamato dal thread JavaFX
    public void setHealth(double hp) {
        double ratio = Math.max(0, Math.min(1, hp / max));
        fill.setWidth(W * ratio);
        fill.setFill(ratio > 0.5 ? Color.LIMEGREEN : ratio > 0.25 ? Color.ORANGE : Color.RED);
        text.setText(String.valueOf((int) Math.ceil(Math.max(0, hp))));
    }
}
