package com.example;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;

//mirino al centro dello schermo con animazione di sparo
public class Crosshair extends Pane {

    private static final double GAP = 6;      //distanza a riposo dal centro
    private static final double LEN = 10;     //lunghezza dei trattini
    private static final double RECOIL = 10;  //quanto si allargano sparando

    private final Group group = new Group();
    private final Line[] lines = new Line[4]; //su, giu, sinistra, destra
    private final Circle dot = new Circle(1.5, Color.WHITE);
    private final Circle flash = new Circle(0, Color.TRANSPARENT);
    private double gap = GAP;
    private Timeline anim;

    public Crosshair() {
        setMouseTransparent(true);
        setPickOnBounds(false);
        for (int i = 0; i < 4; i++) {
            lines[i] = new Line();
            lines[i].setStrokeWidth(2);
            lines[i].setStroke(Color.WHITE);
        }
        flash.setStroke(Color.TRANSPARENT);
        group.getChildren().addAll(lines);
        group.getChildren().addAll(dot, flash);
        getChildren().add(group);
        layoutLines();

        //il mirino resta sempre al centro, anche se cambia la dimensione
        widthProperty().addListener((o, a, b) -> center());
        heightProperty().addListener((o, a, b) -> center());
    }

    private void center() {
        group.setTranslateX(getWidth() / 2);
        group.setTranslateY(getHeight() / 2);
    }

    private void layoutLines() {
        lines[0].setStartY(-gap);        lines[0].setEndY(-gap - LEN);
        lines[1].setStartY(gap);         lines[1].setEndY(gap + LEN);
        lines[2].setStartX(-gap);        lines[2].setEndX(-gap - LEN);
        lines[3].setStartX(gap);         lines[3].setEndX(gap + LEN);
    }

    //animazione di sparo: trattini si allargano e diventano rossi, poi tornano a riposo
    //(va chiamato dal thread JavaFX)
    public void shoot() {
        if (anim != null) anim.stop();
        javafx.beans.property.DoubleProperty g = new javafx.beans.property.SimpleDoubleProperty(GAP + RECOIL);
        g.addListener((o, a, b) -> { gap = b.doubleValue(); layoutLines(); });
        for (Line l : lines) l.setStroke(Color.RED);
        dot.setFill(Color.RED);
        flash.setStroke(Color.ORANGE);
        gap = GAP + RECOIL;
        layoutLines();
        g.set(GAP + RECOIL);

        anim = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(g, GAP + RECOIL),
                new KeyValue(flash.radiusProperty(), 4),
                new KeyValue(flash.opacityProperty(), 1)),
            new KeyFrame(Duration.millis(220),
                new KeyValue(g, GAP),
                new KeyValue(flash.radiusProperty(), 26),
                new KeyValue(flash.opacityProperty(), 0)));
        anim.setOnFinished(e -> {
            for (Line l : lines) l.setStroke(Color.WHITE);
            dot.setFill(Color.WHITE);
            flash.setStroke(Color.TRANSPARENT);
        });
        anim.play();
    }
}
