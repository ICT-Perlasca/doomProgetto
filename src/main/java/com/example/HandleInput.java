package com.example;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.robot.Robot;
import javafx.stage.Window;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

public class HandleInput {
    
    private double rotation = 0; //per visuale (orizzontale)
    private double pitch = 0; //visuale verticale, in gradi (positivo = in su)
    private static final double MAX_PITCH = 85;
    private boolean buffer[];

    public HandleInput(@SuppressWarnings("exports") Scene scene) {
        buffer = new boolean[255];

        //input da tastiera
        scene.setOnKeyPressed(event -> {
            buffer[event.getCode().getCode()] = true;
        });

        scene.setOnKeyReleased(event -> {
            buffer[event.getCode().getCode()] = false;
        });


        //ROTAZIONE VISUALE: movimento relativo, il mouse viene ricentrato
        //ad ogni movimento cosi' non si blocca mai ai bordi dello schermo
        scene.setCursor(Cursor.NONE);
        Robot robot = new Robot();
        scene.setOnMouseMoved(event -> {
            double centerX = scene.getWidth() / 2.0;
            double centerY = scene.getHeight() / 2.0;
            double deltaX = event.getSceneX() - centerX;
            if (deltaX == 0 && event.getSceneY() == centerY) return; //evento generato dal ricentramento
            double deltaY = event.getSceneY() - centerY;
            rotation -= deltaX * 720.0 / scene.getWidth(); //stessa sensibilita' di prima
            //visuale verticale (verso verificato dall'utente: mouse in su = guardo in su), limitata per non ribaltare la camera
            pitch = Math.max(-MAX_PITCH, Math.min(MAX_PITCH, pitch + deltaY * 360.0 / scene.getHeight()));
            Window w = scene.getWindow();
            if (w != null) {
                robot.mouseMove(w.getX() + scene.getX() + centerX, w.getY() + scene.getY() + centerY);
            }
        });
        scene.setOnMouseDragged(scene.getOnMouseMoved()); //anche tenendo premuto il tasto


        //FIRE WEAPON
        scene.setOnMouseClicked(event -> {
            if(event.getButton() == MouseButton.PRIMARY) { //filtro l'input
                if(event.getClickCount() == 1) { //tolgo lo spam di click
                    buffer[event.getButton().name().length() % buffer.length] = true;
                }
            }
        }); 
    }


    public double getRotation() {return rotation;}
    public double getPitch() {return pitch;}

    //ottengo lo stato di un'evento dal buffer
    public boolean getKeyState(@SuppressWarnings("exports") KeyCode keyCode) {
        return buffer[keyCode.getCode()];
    }
    public boolean getKeyState(@SuppressWarnings("exports") MouseButton keyCode) {
        return buffer[keyCode.name().length() % buffer.length];
    }

    //una volta effettuato il click viene riportato a false l'evento
    public void setKeyState(@SuppressWarnings("exports") MouseButton keyCode, boolean bool) {
        buffer[keyCode.name().length() % buffer.length] = bool;        
    }
    public void setKeyState(@SuppressWarnings("exports") KeyCode keyCode, boolean bool) {
        buffer[keyCode.getCode()] = bool;        
    }
}
