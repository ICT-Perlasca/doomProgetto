package com.example;

import java.util.ArrayList;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.util.Duration;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Game extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private Timeline timer; //gameloop sul thread JavaFX
    private static final int FPS = 30; // Frame per secondo
    private static final long FRAME_TIME = 1000 / FPS; // Tempo in millisecondi per frame

    private static final double WIDTH = javafx.stage.Screen.getPrimary().getBounds().getWidth();
    private static final double HEIGHT = javafx.stage.Screen.getPrimary().getBounds().getHeight();
    
    private static Mappa world;
    private Scene scene;
    private SubScene subScene;
    private Crosshair crosshair;
    private HealthBar healthBar;
    private DamageBorder damageBorder;

    private Weapon weapon;
    private PerspectiveCamera camera;
    private Player player;
    private HandleInput input;

    private ArrayList<Mostro> mostri;


    @Override
    public void start(@SuppressWarnings("exports") Stage primaryStage) {
        world = new Mappa("/mappa.txt");
        //la scena 3D sta in una SubScene, sopra ci metto il mirino 2D
        subScene = new SubScene(world, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        crosshair = new Crosshair();
        healthBar = new HealthBar(100);
        StackPane.setAlignment(healthBar, Pos.BOTTOM_LEFT);
        StackPane.setMargin(healthBar, new Insets(0, 0, 30, 30));
        damageBorder = new DamageBorder();
        StackPane root = new StackPane(subScene, crosshair, damageBorder, healthBar);
        scene = new Scene(root, WIDTH, HEIGHT);
        subScene.widthProperty().bind(scene.widthProperty());
        subScene.heightProperty().bind(scene.heightProperty());

        makePlayer(); //INIT player
        makeMonsters(); //INIT monsters

        //aggiungo gli elementi dinamici al mondo
        world.getChildren().addAll(mostri); //aggiungo i mostri
        mostri.forEach(mostro -> {mostro.update();});
        world.getChildren().addAll(player, weapon); //aggiungo il player

        primaryStage.setTitle("MYDOOM");//zola consiglia di cambiare nome
        primaryStage.setScene(scene);
        primaryStage.setFullScreenExitHint(""); //niente messaggio "premi ESC"
        //ESC non deve uscire dal fullscreen: chiude il gioco
        primaryStage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) System.exit(0);
        });
        primaryStage.setFullScreen(true);
        primaryStage.show();

        //gameloop
        start();

        //chiusura programma
        primaryStage.
        setOnCloseRequest(event -> {
            System.exit(0);
        });
    }


    //fine del gioco: ferma il loop e mostra la schermata finale
    private void gameOver() {
        timer.stop();
        Label titolo = new Label("GAME OVER");
        titolo.setFont(Font.font("Monospaced", FontWeight.BOLD, 96));
        titolo.setTextFill(Color.WHITE);
        Label sotto = new Label("premi INVIO o ESC per uscire");
        sotto.setFont(Font.font("Monospaced", 24));
        sotto.setTextFill(Color.LIGHTGRAY);
        VBox box = new VBox(20, titolo, sotto);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: rgba(120,0,0,0.75);");
        ((StackPane) scene.getRoot()).getChildren().add(box);
        scene.setCursor(Cursor.DEFAULT);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.ESCAPE) System.exit(0);
        });
    }


    //gameloop
    public void start() {
        Sounds.playSound(Sounds.GAME);
        //il loop gira sul thread JavaFX: modificare camera e nodi da un altro thread
        //puo' far perdere gli aggiornamenti (camera ferma mentre il giocatore si muove)
        timer = new Timeline(new KeyFrame(Duration.millis(FRAME_TIME), e -> {
            //aggiornamento mostri
            mostri.forEach(mostro -> {
                if(mostro.getPuntiVita() > 0 && mostro.attesaAttiva(50)) {
                    mostro.update();
                }
            });
            // aggiornamento player
            player.update();
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }


    //inizializzazione mostri
    private void makeMonsters() {
        //inizializzo l'array
        mostri = new ArrayList<>();
        //creo i mostri e li aggiungo all'array
        world.getMonsters_spawnPoints().forEach(monster_spawnPoint -> {
            int x = (int)monster_spawnPoint.getX();
            int y = (int)monster_spawnPoint.getY();
            int z = (int)monster_spawnPoint.getZ();
            Mostro mostro = new Mostro( //creo mostro nella posizione di spawn
                player, 5,5,5, 0.5, 60, x, y, z);
            mostro.setId("3"); //identificativo per mostro
            //aggiungo il mostro alla lista
            mostri.add(mostro);
        });
    }


    //inizializzazione player
    private void makePlayer() {
        //creazione player e aggiunta camera alla scena
        camera = new PerspectiveCamera(true);
        input = new HandleInput(scene);
        weapon = new Weapon(15, 20);
        player = new Player(camera, input, weapon, 0, -5, 0);
        subScene.setCamera(camera); //aggiungo la camera alla scena
        player.setOnShoot(crosshair::shoot);
        player.setOnHealthChange(healthBar::setHealth);
        player.setOnHit(damageBorder::flash);
        player.setOnDeath(this::gameOver);
        
        player.setId("2"); //imposto un id per distinzione
        weapon.setId("2"); //imposto un id per distinzione

        //imposto la posizione del player al suo spawnPoint
        player.setPosition(world.getPlayer_spawnPoint());
    }


    public static Mappa getWorld() {return world;}

    public Player getPlayer() {return player;}
}
