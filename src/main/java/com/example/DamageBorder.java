package com.example;

import javafx.animation.FadeTransition;
import javafx.scene.layout.Region;
import javafx.util.Duration;

//bordo rosso che compare per un attimo quando il giocatore subisce danno
public class DamageBorder extends Region {

    private final FadeTransition fade = new FadeTransition(Duration.millis(700), this);

    public DamageBorder() {
        setMouseTransparent(true);
        setStyle("-fx-border-color: rgba(220,0,0,0.85); -fx-border-width: 40; "
               + "-fx-effect: innershadow(gaussian, rgba(255,0,0,0.9), 120, 0.3, 0, 0);");
        setOpacity(0);
        fade.setFromValue(1);
        fade.setToValue(0);
    }

    //va chiamato dal thread JavaFX
    public void flash() {
        fade.stop();
        fade.playFromStart();
    }
}
