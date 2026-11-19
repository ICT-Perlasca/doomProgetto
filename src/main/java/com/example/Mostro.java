package com.example;

import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.Group;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;


public class Mostro extends Group {

    private double speed;      //Velocità di movimento del mostro
    private int puntiVita;      //Punti vita del mostro
    private Point3D position;
    private Player player;
    private Group modello;
    private static final double ORIENTAMENTO = 180; //correzione dell'orientamento frontale
    private static final double DANNO = 10; //punti vita tolti per colpo

    public Mostro(Player player, double width, double height, double depth,
                  double speed, int puntiVita, int x, int y, int z) {
        super();
        position = new Point3D(x, y, z);
        setSpeed(speed);
        setPuntiVita(puntiVita);
        this.player = player;
        costruisciModello();
    }


    //MODELLO 3D: testa sferica con il teschio sul davanti, busto, braccia e gambe
    private void costruisciModello() {
        modello = new Group();

        PhongMaterial pelle = materiale(Color.rgb(123, 82, 41));
        PhongMaterial scuro = materiale(Color.rgb(60, 28, 20));

        //testa (il teschio e' disegnato solo sul lato frontale della sfera)
        Sphere testa = new Sphere(1.7);
        PhongMaterial mTesta = new PhongMaterial();
        mTesta.setDiffuseMap(texturaTesta());
        testa.setMaterial(mTesta);
        testa.setTranslateY(-1.8);

        //busto
        Cylinder busto = new Cylinder(1.1, 2.4);
        busto.setMaterial(pelle);
        busto.setTranslateY(0.5);
        busto.setScaleZ(0.7);

        //braccia
        Cylinder bracciaS = new Cylinder(0.35, 2.2);
        bracciaS.setMaterial(scuro);
        bracciaS.setTranslateX(-1.6);
        bracciaS.setTranslateY(0.6);
        Cylinder bracciaD = new Cylinder(0.35, 2.2);
        bracciaD.setMaterial(scuro);
        bracciaD.setTranslateX(1.6);
        bracciaD.setTranslateY(0.6);

        //gambe
        Cylinder gambaS = new Cylinder(0.45, 1.8);
        gambaS.setMaterial(scuro);
        gambaS.setTranslateX(-0.5);
        gambaS.setTranslateY(2.5);
        Cylinder gambaD = new Cylinder(0.45, 1.8);
        gambaD.setMaterial(scuro);
        gambaD.setTranslateX(0.5);
        gambaD.setTranslateY(2.5);

        modello.getChildren().addAll(busto, bracciaS, bracciaD, gambaS, gambaD, testa);
        getChildren().add(modello);
    }

    private static PhongMaterial materiale(Color c) {
        PhongMaterial m = new PhongMaterial(c);
        m.setSpecularColor(Color.rgb(30, 30, 30));
        return m;
    }

    //texture per la sfera: il teschio occupa solo un quarto della circonferenza (il fronte),
    //il resto e' colore pelle; lo sfondo azzurro dell'immagine originale diventa trasparente
    private Image texturaTesta() {
        Image src = new Image(getClass().getResourceAsStream("/textures/monster.png"));
        PixelReader r = src.getPixelReader();
        int k = 8; //ingrandimento a pixel netti
        int fw = (int) src.getWidth() * k, fh = (int) src.getHeight() * k;
        Color fondo = Color.rgb(123, 82, 41);
        WritableImage out = new WritableImage(fw * 4, fh);
        PixelWriter w = out.getPixelWriter();
        for (int py = 0; py < fh; py++) {
            for (int px = 0; px < fw * 4; px++) {
                Color c = fondo;
                int fx = px - fw * 3 / 2; //teschio centrato a meta' immagine
                if (fx >= 0 && fx < fw) {
                    Color t = r.getColor(fx / k, py / k);
                    if (!(t.getRed() < 0.05 && t.getGreen() > 0.95 && t.getBlue() > 0.95)) c = t;
                }
                w.setColor(px, py, c);
            }
        }
        return out;
    }

    //gira il modello verso il player
    private void guardaPlayer() {
        Point3D p = player.getPosition();
        double angolo = Math.toDegrees(Math.atan2(p.getX() - position.getX(), p.getZ() - position.getZ()));
        setRotationAxis(Rotate.Y_AXIS);
        setRotate(angolo + ORIENTAMENTO);
    }


    public void update() {
        Point3D playerPosition = player.getPosition(); //ottengo la posizione del player
        Point3D directionToPlayer = playerPosition.subtract(position).normalize().multiply(speed); // Calcola la direzione dal mostro al giocatore
        //ottengo il vettore di spostamento
        Vector3D vector3d = new Vector3D(directionToPlayer.getX(), directionToPlayer.getY(), directionToPlayer.getZ());
        
        //CONTROLLO COLLISIONI
        Game.getWorld().getChildren().forEach(wall -> {
            //filtro per muri (id == 1)
            if(wall.getId().equals("1")) {
                if(position.distance(wall.getTranslateX(), wall.getTranslateY(), wall.getTranslateZ()) < 20) {
                    //controllo la collisione con un muro nella direzione richiesta
                    boolean xCollision = collisionX((Box)wall, directionToPlayer.getX());
                    boolean zCollision = collisionZ((Box)wall, directionToPlayer.getZ());

                    //controllo che la collisione avvenga con l'oggetto per entrambe le coordinate
                    if(zCollision && xCollision) {
                        //controlla se alle sue spalle ha un ostacolo
                        xCollision = collisionX((Box)wall, -directionToPlayer.getX());
                        zCollision = collisionZ((Box)wall, -directionToPlayer.getZ());

                        //impone che tu NON abbia un'ostacolo intorno per fermarti
                        if(!xCollision || !zCollision) {
                            vector3d.setValues(0, 0, 0);
                        }
                    }

                }
            }

        });
        
        //applico il vettore alla posizione
        position = position.add(vector3d.getX(), vector3d.getY(), vector3d.getZ());
        setTranslateX(position.getX());
        setTranslateY(position.getY());
        setTranslateZ(position.getZ());
        guardaPlayer();

        //possibilità di danneggiare il player
        if(position.distance(playerPosition) < 1) {
            player.subisci(DANNO); //il player ignora i colpi durante il cooldown
        }
    }


    public boolean attesaAttiva(double disAttivavione){
        Point3D positionPlayer = player.getPosition();
        double dis = position.distance(positionPlayer);
        return disAttivavione >= dis;
    }

    public void subisci(int danno) {
        puntiVita -= danno;
        if(puntiVita <= 0) {
            //il mostro cade a terra
            modello.setRotationAxis(Rotate.Z_AXIS);
            modello.setRotate(90);
            setTranslateY(-0.7);
            Sounds.playSound(Sounds.DEATH);
        }
    }

    //Metodo setter di speed, obbliga l'inserimento di un valore compreso tra 0.1 e 10.
    public void setSpeed(double speed) {
        if (speed <= 0)
            this.speed = 0.1;
        else if (speed > 10)
            this.speed = 10;
        else
            this.speed = speed;
    }

    //Metodo setter di puntiVita, obbliga l'inserimento di un valore compreso tra 10 e 1000.
    public void setPuntiVita(int puntiVita) {
        if (puntiVita < 10)
            this.puntiVita = 10;
        else if (puntiVita > 1000)
            this.puntiVita = 1000;
        else
            this.puntiVita = puntiVita;
    }

    //COLLISIONI
//************************************************************* */
    public boolean collisionX(@SuppressWarnings("exports") Box wall, double dX) {

        double observerX = getTranslateX();
        double observerWidth = 3;

        double entityX = wall.getTranslateX();
        double entityWidth = 5;

        return (observerX + observerWidth + dX >= entityX && observerX + dX <= entityX + entityWidth);
    }

    public boolean collisionZ(@SuppressWarnings("exports") Box wall, double dY) {

        double observerZ = getTranslateZ();
        double observerHeight = 3;

        double entityZ = wall.getTranslateZ();
        double entityHeight = 5;

        return (observerZ + observerHeight + dY >= entityZ && observerZ + dY <= entityZ + entityHeight);
    }

    @SuppressWarnings("exports")
    public Point3D getPosition() {
        return position;
    }

    public void setPosition(@SuppressWarnings("exports") Point3D position) {
        this.position = position;
    }

    public int getPuntiVita() {
        return puntiVita;
    }
}
