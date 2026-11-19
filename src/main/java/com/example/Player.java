package com.example;

import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.geometry.Point3D;

public class Player extends Box {

    //forma
    private static final double HEIGHT = 5;
    private static final double WIDTH = 2;
    private static final double DEPTH = 2;

    //attributi
    private HandleInput input;
    private PerspectiveCamera camera;
    private Weapon weapon;
    private Point3D position;
    private Vector3D vector3d;
    private double rotation;
    private final Rotate pitchRotate = new Rotate(0, Rotate.X_AXIS);
    private final Rotate weaponPitchRotate = new Rotate(0, Rotate.X_AXIS);
    private Runnable onShoot; //chiamato ad ogni sparo (per le animazioni)

    public void setOnShoot(Runnable onShoot) {this.onShoot = onShoot;}
    private double puntiVita = 100;
    private java.util.function.DoubleConsumer onHealthChange;
    private Runnable onDeath;
    private Runnable onHit; //chiamato quando il danno viene effettivamente subito
    private static final long DANNO_COOLDOWN_MS = 2000; //tempo minimo tra due colpi subiti
    private long ultimoDanno = -DANNO_COOLDOWN_MS;

    public void setOnHit(Runnable r) {this.onHit = r;}

    
    // Costruttore 
    @SuppressWarnings("exports")
    public Player(PerspectiveCamera camera, HandleInput input, Weapon weapon, int x, int y, int z) {
        super(WIDTH, HEIGHT, DEPTH); //entita'
    
        this.input = input;
        this.weapon = weapon;
        this.camera = camera;

        //INIT
        position = new Point3D(x, y, z);
        vector3d = new Vector3D();
        
        //impostazione dimensioni hitbox
        setTranslateY(position.getY());
        setRotationAxis(position);

        //settaggio camera
        this.camera.setTranslateY(position.getY());
        this.camera.setFieldOfView(70);
        this.camera.setNearClip(0.1);
        this.camera.setFarClip(1000.0);
        //rotazione orizzontale (yaw): asse Y verso l'alto (y negativo in JavaFX), cosi' la
        //direzione di movimento calcolata con -sin/cos dell'angolo coincide con lo sguardo
        this.camera.setRotationAxis(new Point3D(0, -1, 0));
        //inclinazione verticale (pitch), applicata dopo lo yaw: asse X locale
        this.camera.getTransforms().add(pitchRotate);

        //settaggio arma
        this.weapon.setTranslateY(-4.65);
        this.weapon.setRotationAxis(position);
        this.weapon.setPosition(position);
        //l'arma segue lo sguardo: ruota attorno alla posizione della camera (non attorno al
        //proprio centro), cosi' resta ferma nello stesso punto dello schermo
        weaponPitchRotate.setPivotY(position.getY() - this.weapon.getTranslateY());
        this.weapon.getTransforms().add(weaponPitchRotate);
    }

    
    public void update() {
        handleInput();          //leggo l'input dell'utente e modifico la posizione
        updateMovement();       //aggiornamento player
        updateCamera();         //aggiornamento camera
        updateWeapon();         //aggiornamento arma
    }

    private void updateMovement() {
        //sposto la hitbox
        setTranslateX(position.getX());
        setTranslateY(position.getY());
        setTranslateZ(position.getZ());
        //imposto la rotazione
        this.setRotate(rotation);
    }

    private void updateWeapon() {
        //sposto la hitbox
        weapon.setTranslateX(position.getX());
        camera.setTranslateY(position.getY());
        weapon.setTranslateZ(position.getZ());
        //imposto la rotazione
        weapon.setRotate(rotation);
    }

    private void updateCamera() {
        //sposto la camera
        this.camera.setTranslateX(position.getX());
        this.camera.setTranslateY(position.getY());
        this.camera.setTranslateZ(position.getZ());
        //imposto la rotazione
        this.camera.setRotate(rotation);
        weaponPitchRotate.setAngle(-input.getPitch());
        pitchRotate.setAngle(-input.getPitch()); //in JavaFX l angolo positivo sull asse X guarda in basso
    }
    

    //leggo le richieste dell'utente dal buffer
    private void handleInput() {

        //prendo la rotazione richiesta
        rotation = input.getRotation();

        //prendo l'angolo di rotazione della camera e lo trasformo in movimento
        double angleRad = Math.toRadians(camera.getRotate());

        // Fermo
        vector3d = new Vector3D();

        // Movimento
        if(input.getKeyState(KeyCode.W)) { //movimento in Z+
            vector3d = vector3d.add(-Math.sin(angleRad), 0, Math.cos(angleRad));
        }
        if(input.getKeyState(KeyCode.A)) { //movimento in X-
            vector3d = vector3d.add(-Math.sin(angleRad +90), 0, -Math.sin(angleRad));
        }
        if(input.getKeyState(KeyCode.S)) {  //movimento in Z-
            vector3d = vector3d.add(Math.sin(angleRad), 0, -Math.cos(angleRad));
        }
        if(input.getKeyState(KeyCode.D)) { //movimento in X+
            vector3d = vector3d.add(-Math.sin(angleRad -90), 0, Math.sin(angleRad));
        }

        //FIRE WEAPON
        if(input.getKeyState(MouseButton.PRIMARY)) {
            Sounds.playSound(Sounds.SHOOT); //riproduco il suono dello sparo
            fireWeapon(); //controllo per colpire il nemico
            if (onShoot != null) javafx.application.Platform.runLater(onShoot); //animazione mirino
            input.setKeyState(MouseButton.PRIMARY, false); //ripristino l'evento
        }

        //controllo che nella direzione richiesta non ci siano muri, solo alla richiesta di input
        if(input.getKeyState(KeyCode.W) 
        || input.getKeyState(KeyCode.A) 
        || input.getKeyState(KeyCode.S) 
        || input.getKeyState(KeyCode.D)) {
            checkCollision();
        }
        
        //aggiorno la posizione
        position = position.add(vector3d.getX(), vector3d.getY(), vector3d.getZ()); 
    }


    private void fireWeapon() {
        //scorro tutti i nemici
        Game.getWorld().getChildren().forEach(entity -> {
            //filtro per mostri
            if(entity.getId().equals("3")) {
                Mostro mostro = (Mostro)entity;
                if(getPosition().distance(mostro.getPosition()) < 50) {
                    //ottengo la direzione nella quale il player è girato ed ottengo il vettore di spostamento
                    double angle = Math.toRadians(camera.getRotate());
                    Vector3D vector = new Vector3D(-Math.sin(angle), 0, Math.cos(angle));
                    //creo quello che sarebbe il proiettile che viaggia nella direzione vettoriale di dove guardi
                    Point3D proiettile = new Point3D(position.getX(), position.getY(), position.getZ());

                    boolean collision = false;
                    //itero per quanto range ha lo sparo
                    for(int i = 0; i < 50 && !collision; i++) {
                        //applico lo spostamento sul proiettile
                        proiettile = proiettile.add(vector.x, vector.y, vector.z);
                        //controllo che il proiettile raggiunga e superi il mostro, margine di errore: 3
                        if(mostro.getPosition().distance(proiettile) < 3) {
                            mostro.subisci(getWeapon().getDamage());
                            collision = true;
                        }
                    }
                }
            }
        });
    }


    private void checkCollision() {
        //iterazione per tutti gli elementi, muri e pavimenti
        Game.getWorld().getChildren().forEach(wall -> {
            //filtro per muri (id == 1)
            if(wall.getId().equals("1")) {
                if(this.position.distance(wall.getTranslateX(), wall.getTranslateY(), wall.getTranslateZ()) < 20) {
                    //controllo la collisione con un muro nella direzione richiesta
                    boolean xCollision = collisionX((Box)wall, vector3d.getX());
                    boolean zCollision = collisionZ((Box)wall, vector3d.getZ());

                    //controllo che la collisione avvenga con l'oggetto per entrambe le coordinate
                    if(zCollision && xCollision) {      
                        //controlla se alle sue spalle ha un ostacolo
                        xCollision = collisionX((Box)wall, -vector3d.getX());
                        zCollision = collisionZ((Box)wall, -vector3d.getZ());
                        
                        //impone che tu NON abbia un'ostacolo a destra/sinistra per fermarti in X
                        if(!xCollision) vector3d.x = 0;
                        
                        //impone che tu NON abbia un'ostacolo avanti/dietro per fermarti in Z
                        if(!zCollision) vector3d.z = 0;
                    }
                }
            } 
        });
    }


        //COLLISIONI
//************************************************************* */
    public boolean collisionX(@SuppressWarnings("exports") Box wall, double dX) {

        double observerX = getTranslateX();
        double observerWidth = WIDTH+1;

        double entityX = wall.getTranslateX();
        double entityWidth = wall.getWidth();

        return (observerX+observerWidth+dX >= entityX && observerX+dX <= entityX+entityWidth); 
    }

    public boolean collisionZ(@SuppressWarnings("exports") Box wall, double dY) {

        double observerZ = getTranslateZ();
        double observerHeight = DEPTH+1;

        double entityZ = wall.getTranslateZ();
        double entityHeight = wall.getDepth();

        return (observerZ+observerHeight+dY >= entityZ && observerZ+dY <= entityZ+entityHeight); 
    }

    //Metodo getter della posizione
    @SuppressWarnings("exports")
    public Point3D getPosition() {
        return position;
    }

    public void setPosition(@SuppressWarnings("exports") Point3D position) {
        this.position = position;
    }

    //Metodo getter dei punti vita
    public double getPuntiVita() {
        return puntiVita;
    }

    //Metodo setter dei puntiVita
    public void setPuntiVita (int vitaRimasta){
        this.puntiVita = vitaRimasta;
    }

//********************************************************* */


    public Weapon getWeapon() {
        return weapon;
    }
    
    public void setOnHealthChange(java.util.function.DoubleConsumer c) {this.onHealthChange = c;}
    public void setOnDeath(Runnable r) {this.onDeath = r;}
    public boolean isMorto() {return puntiVita <= 0;}

    //chiamato dal thread del gameloop
    public synchronized void subisci(double danno) {
        if(isMorto()) return; //gia' morto: l'evento parte una volta sola
        long ora = System.currentTimeMillis();
        if(ora - ultimoDanno < DANNO_COOLDOWN_MS) return; //invulnerabile dopo un colpo
        ultimoDanno = ora;
        if(onHit != null) javafx.application.Platform.runLater(onHit);
        this.puntiVita = Math.max(0, puntiVita - danno);
        double vita = puntiVita;
        if(onHealthChange != null) javafx.application.Platform.runLater(() -> onHealthChange.accept(vita));
        if(puntiVita <= 0 && onDeath != null) {
            javafx.application.Platform.runLater(onDeath);
        }
    }

}
