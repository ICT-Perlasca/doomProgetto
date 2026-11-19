package com.example;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

public class Sounds {
    
    //effetti
    public static final String SHOOT = "src/main/resources/sounds/shoot.wav";
    public static final String WALK = "src/main/resources/sounds/walk.wav";
    public static final String DEATH = "src/main/resources/sounds/death.wav";
    
    //soundtrack
    public static final String MENU = "src/main/resources/sounds/menu.wav";
    public static final String GAME = "src/main/resources/sounds/game.wav";


    private static final java.util.Set<MediaPlayer> attivi = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static boolean audioNonDisponibile = false;

    //riproduce il file audio (WAV: gli mp3 non sono decodificabili da JavaFX con gli ffmpeg recenti), possibilmente uno delle costanti
    public static void playSound(String ASOUND) {
        if (audioNonDisponibile) return;
        try {
            java.net.URL url = Sounds.class.getResource("/sounds/" + new File(ASOUND).getName());
            if (url == null) return; //file mancante (es. menu.mp3)
            MediaPlayer mp = new MediaPlayer(new Media(url.toExternalForm()));
            attivi.add(mp); //evita che il garbage collector fermi il suono
            Runnable fine = () -> { attivi.remove(mp); mp.dispose(); };
            if (ASOUND.equals(GAME) || ASOUND.equals(MENU)) {
                mp.setCycleCount(MediaPlayer.INDEFINITE); //la musica di sottofondo si ripete
            } else {
                mp.setOnEndOfMedia(fine);
            }
            mp.setOnError(fine);
            mp.play();
        } catch (Exception e) {
            audioNonDisponibile = true; //niente backend audio: disattivo senza riempire i log
            System.err.println("Audio disattivato: " + e.getMessage());
        }
    }
}
