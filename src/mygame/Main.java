package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import java.util.ArrayList;
import java.util.Random;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication implements AnimEventListener {

    //Material
    Material stone_mat;
    Material gun_mat;
    Material enemy_mat;

    //Player
    private CharacterControl player;
    private Spatial gun;
    private Vector3f camDir = new Vector3f();
    private Vector3f camLeft = new Vector3f();
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;
    private BitmapText hud;
    private long score = 0;
    private Vector3f vectorDifference;

    //Inimigo
    private Spatial enemy;
    private AnimControl control1;
    private AnimChannel channel1;
    private ArrayList<Inimigo> enemies = new ArrayList<Inimigo>();

    //Fisica da aplicaçao
    private BulletAppState bulletAppState;
    private CollisionResults results = new CollisionResults();

    //Fisica da bala
    private RigidBodyControl ball_phy;
    private static final Sphere sphere;
    private ArrayList<Geometry> balas = new ArrayList<Geometry>();

    //Efeitos sonoros 
    private AudioNode audio_gun;
    private AudioNode audio_background;

    static {
        /**
         * Initialize the cannon ball geometry
         */
        sphere = new Sphere(16, 16, 0.1f, true, false);
        sphere.setTextureMode(TextureMode.Projected);
    }

    public static void main(String[] args) {
        AppSettings defs = new AppSettings(true);
        defs.setFullscreen(true);
        defs.setWidth(1366);
        defs.setHeight(768);
        defs.setFrequency(60);

        Main app = new Main();
        app.setShowSettings(false);
        app.setSettings(defs);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        initKeys();
        initMaterials();
        initPhysics();
        initPlayer();
        initLight();
        initMap();
        initCrosshairs();
        hud = new BitmapText(guiFont, false);
        initHUD();
        initAudio();
        initScene();
        initEnemy(4);
    }

    @Override
    public void simpleUpdate(float tpf) {
        movePlayer();
        colisaoBalaInimigo();
        inimigoSeguePlayer(tpf*8);
        
        Random r = new Random();
        int n = r.nextInt(10000) + 1;
        
        if (n < 12) initEnemy(4);
        else if (n < 24) initEnemy(5);
        else if (n < 36) initEnemy(6);
        else if (n < 48) initEnemy(7);
        else if (n < 60) initEnemy(8);
        else if (n < 72) initEnemy(9);
        else if (n < 84) initEnemy(10);
        else if (n < 96) initEnemy(11);
    }

    private void initPlayer() {
        //Arma
        gun = assetManager.loadModel("Models/Gun/AKcomTextura.obj");
        gun.setMaterial(gun_mat);
        gun.scale(0.1f);
        rootNode.attachChild(gun);

        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(2f, 2f, 1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setViewDirection(cam.getDirection());
        player.setJumpSpeed(10);
        player.setFallSpeed(30);
        player.setGravity(new Vector3f(0, -30f, 0));
        player.setPhysicsLocation(new Vector3f(30f, 10f, 0f));
        bulletAppState.getPhysicsSpace().add(player);
    }

    private void initKeys() {
        inputManager.addMapping("SliceVertical", new KeyTrigger(KeyInput.KEY_V));
        inputManager.addMapping("SliceHorizontal", new KeyTrigger(keyInput.KEY_X));
        inputManager.addMapping("RunBase", new KeyTrigger(keyInput.KEY_C));
        inputManager.addMapping("shoot", new MouseButtonTrigger(mouseInput.BUTTON_LEFT));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(actionListener, "SliceVertical");
        inputManager.addListener(actionListener, "SliceHorizontal");
        inputManager.addListener(actionListener, "RunBase");
        inputManager.addListener(actionListener, "shoot");
        inputManager.addListener(actionListener, "Left");
        inputManager.addListener(actionListener, "Right");
        inputManager.addListener(actionListener, "Up");
        inputManager.addListener(actionListener, "Down");
        inputManager.addListener(actionListener, "Jump");
    }

    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("SliceVertical") && !keyPressed) {
                if (!channel1.getAnimationName().equals("SliceVertical")) {
                    channel1.setAnim("SliceVertical", 0.50f);
                    channel1.setLoopMode(LoopMode.Loop);
                }
            }
            if (name.equals("SliceHorizontal") && !keyPressed) {
                if (!channel1.getAnimationName().equals("SliceHorizontal")) {
                    channel1.setAnim("SliceHorizontal", 0.50f);
                    channel1.setLoopMode(LoopMode.Loop);
                }
            }
            if (name.equals("RunBase") && !keyPressed) {
                if (!channel1.getAnimationName().equals("RunBase")) {
                    channel1.setAnim("RunBase", 0.50f);
                    channel1.setLoopMode(LoopMode.Loop);
                }
            }
            if (name.equals("shoot") && !keyPressed) {
                makeCannonBall();
                audio_gun.playInstance();
            }
            if (name.equals("Left")) {
                if (keyPressed) {
                    left = true;
                } else {
                    left = false;
                }
            } else if (name.equals("Right")) {
                if (keyPressed) {
                    right = true;
                } else {
                    right = false;
                }
            } else if (name.equals("Up")) {
                if (keyPressed) {
                    up = true;
                } else {
                    up = false;
                }
            } else if (name.equals("Down")) {
                if (keyPressed) {
                    down = true;
                } else {
                    down = false;
                }
            }
            if (name.equals("Jump") && !keyPressed) {
                player.jump(new Vector3f(0, 10f, 0));
            }
        }
    };

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void makeCannonBall() {
        /**
         * Create a cannon ball geometry and attach to scene graph.
         */
        Geometry ball_geo = new Geometry("cannon ball", sphere);
        ball_geo.setMaterial(stone_mat);
        rootNode.attachChild(ball_geo);
        /**
         * Position the cannon ball
         */
        Vector3f camLocation = getCamera().getLocation();
        ball_geo.setLocalTranslation(getCamera().getDirection().scaleAdd(3, camLocation));
        //ball_geo.setLocalTranslation(cam.getLocation());
        /**
         * Make the ball physical with a mass > 0.0f
         */
        ball_phy = new RigidBodyControl(1f);
        /**
         * Add physical ball to physics space.
         */
        ball_geo.addControl(ball_phy);
        bulletAppState.getPhysicsSpace().add(ball_phy);
        ball_phy.setCcdSweptSphereRadius(.1f);
        ball_phy.setCcdMotionThreshold(0.001f);
        /**
         * Accelerate the physical ball to shoot it.
         */
        ball_phy.setLinearVelocity(cam.getDirection().mult(100));

        balas.add(ball_geo);
    }

    private void initEnemy(int num) { //num = lugar onde o inimigo irá nascer(4, 5 ou 6 na matriz)
        int[][] map = getMap();
        int x = 0, z = 0;
        
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map.length; j++) {
                if (map[i][j] == num) {
                    x = i;
                    z = j;
                }
            }
        }
        enemy = assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
                
        enemies.add(new Inimigo(enemy, x, 4, z));
        System.out.println(enemies.size());
        rootNode.attachChild(enemy);
        bulletAppState.getPhysicsSpace().add(enemies.get(enemies.size() - 1).getRigidBodyControl());
        
        //System.out.println(control1.getAnimationNames());
    }

    private void initLight() {
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(cam.getDirection().setY(10f));
        rootNode.addLight(sun);

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);
    }

    private void initCrosshairs() {
        setDisplayStatView(false);
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("."); //mira     
        ch.setColor(ColorRGBA.Green);
        ch.setLocalTranslation(settings.getWidth() / 2 - 5, settings.getHeight() / 2 + 28, 0); //center
        guiNode.attachChild(ch);
    }

    private void initScene() {
        getRootNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));
    }

    private void initMaterials() {
        stone_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
        key2.setGenerateMips(true);
        Texture tex2 = assetManager.loadTexture(key2);
        stone_mat.setTexture("ColorMap", tex2);

        gun_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture gun_text = assetManager.loadTexture("Textures/TexturaAK.png");
        gun_mat.setTexture("ColorMap", gun_text);

    }

    private void initAudio() {
        audio_gun = new AudioNode(assetManager, "Sounds/Gun_Shot.wav", DataType.Buffer);
        audio_gun.setPositional(false);
        audio_gun.setLooping(false);
        audio_gun.setVolume(2);
        rootNode.attachChild(audio_gun);

        audio_background = new AudioNode(assetManager, "Sounds/ZombieBackground.wav", DataType.Buffer);
        audio_background.setLooping(true);
        audio_background.setPositional(false);
        audio_background.setVolume(2);
        rootNode.attachChild(audio_background);
        audio_background.play();
    }

    private void initMap() {
        int map[][] = getMap();

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                //Floor
                criarParedeChao(i, j, -1, 0.1f);
                //Wall
                if (map[i][j] == 1) {
                    criarParedeChao(i, j, 1, 3f);
                }
                if (map[i][j] == 4) {
                    //criarInimigo(i, j, 4);
                }
            }
        }
    }

    private void criarParedeChao(int x, int z, int chao, float y) {
        Box boxMesh = new Box(1f, y, 1f);

        Geometry boxGeo;
        if (chao == -1) {
            boxGeo = new Geometry("BlocoChao", boxMesh);
            Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            Texture cube1Tex = assetManager.loadTexture("Textures/FloorTexture1.jpg");
            boxMat.setTexture("ColorMap", cube1Tex);
            boxGeo.setMaterial(boxMat);
            boxGeo.setLocalTranslation(0f, -1f, 0f);

        } else {         
            boxGeo = new Geometry("Bloco", boxMesh);
            Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            Texture cube3Tex = assetManager.loadTexture("Textures/WallTexture1.jpg");
            boxMat.setTexture("ColorMap", cube3Tex);
            boxGeo.setMaterial(boxMat);
            boxGeo.setMaterial(boxMat);
        }
        
        RigidBodyControl r = new RigidBodyControl(0);

        boxGeo.addControl(r);

        boxGeo.move(-40 + x * 2, chao, -40 + z * 2);
        r.setPhysicsLocation(boxGeo.getLocalTranslation());

        rootNode.attachChild(boxGeo);
        bulletAppState.getPhysicsSpace().add(r);
    }

    private void criarInimigo(int x, int z, int chao) {
        
    }

    private void initPhysics() {
        bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(false);
        stateManager.attach(bulletAppState);
    }

    private void setGun() {
        Vector3f vectorDifference = new Vector3f(cam.getLocation().subtract(gun.getWorldTranslation()));
        gun.setLocalTranslation(vectorDifference.addLocal(gun.getLocalTranslation()));

        Quaternion worldDiff = new Quaternion(cam.getRotation().mult(gun.getWorldRotation().inverse()));
        gun.setLocalRotation(worldDiff.multLocal(gun.getLocalRotation()));

        gun.move(cam.getDirection().mult(2.5f));
        gun.move(cam.getUp().mult(-0.5f));
        gun.move(cam.getLeft().mult(1.3f));
        gun.rotate(0.1f, FastMath.PI * 1.45f, 0.13f);
    }

    private void movePlayer() {
        camDir.set(cam.getDirection()).multLocal(0.12f);
        camLeft.set(cam.getLeft()).multLocal(0.12f);
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        walkDirection.multLocal(2f);
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());

        setGun();
    }

    private void colisaoBalaInimigo() {
        for (int i = 0; i < enemies.size(); i++) {
            for (int j = 0; j < balas.size(); j++) {
                balas.get(j).collideWith(enemies.get(i).getSpatial().getWorldBound(), results);
                if (results.size() > 0) {
                    enemies.get(i).getSpatial().removeFromParent();
                    balas.get(j).removeFromParent();
                    results = new CollisionResults();

                    score++;
                    initHUD();
                }
            }
        }
    }

    private void initHUD() {
        setDisplayStatView(false);
        hud.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        hud.setText("SCORE: " + score); //mira     
        hud.setColor(ColorRGBA.Blue);
        hud.setLocalTranslation(settings.getWidth() - 200, 50, 0);
        guiNode.attachChild(hud);
    }

    private void inimigoSeguePlayer(float speed) {
        for (Inimigo e : enemies) {
            if (e.getSpatial().getLocalTranslation().x < player.getPhysicsLocation().x) {
                e.getSpatial().getLocalTranslation().x += speed;
            }
            if (e.getSpatial().getLocalTranslation().x > player.getPhysicsLocation().x) {
                e.getSpatial().getLocalTranslation().x -= speed;
            }
            if (e.getSpatial().getLocalTranslation().z < player.getPhysicsLocation().z) {
                e.getSpatial().getLocalTranslation().z += speed;
            }
            if (e.getSpatial().getLocalTranslation().z > player.getPhysicsLocation().z) {
                e.getSpatial().getLocalTranslation().z -= speed;
            }

            //e.getControl(RigidBodyControl.class).setPhysicsLocation(e.getLocalTranslation());
            //RigidBodyControl rb = e.getControl(RigidBodyControl.class);
            //rb.setPhysicsLocation(e.getLocalTranslation());
            e.getSpatial().lookAt(player.getPhysicsLocation(), new Vector3f(0, 1, 0));
        }
    }
    
    private int[][] getMap() {
        //50x50
        int map[][] = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 1, 0, 4, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 11, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 10, 0, 1, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, -2, -2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, -2, -2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 1, 0, 6, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 7, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 8, 0, 1, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},};

        return map;
    }
}
