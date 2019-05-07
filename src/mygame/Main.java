package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import javafx.scene.shape.Rectangle;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication implements AnimEventListener{

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
    
    //Material
    Material stone_mat;
    
    //Inimigo
    private Spatial sinbad;
    private AnimControl control1;
    private AnimChannel channel1;
    private AnimChannel channel2;
    
    //Fisica da aplicaçao
    private BulletAppState bulletAppState;
    //Fisica da bala, paredes e inimigo
    private RigidBodyControl bullet_phy;
    private static final Sphere bullet;
    private RigidBodyControl floor_phy;
    private static final Box floor;
    private RigidBodyControl enemy_phy;
    public static final BoundingBox enemy;
    
    //Dimensoes da sala(parede e chao)
    private static final float floorLargura = 1;
    private static final float floorComprimento = 1;
    private static final float floorAltura = 1;
    
    static {
        bullet = new Sphere(16, 16, 0.2f, true, false);
        bullet.setTextureMode(TextureMode.Projected);
        
        floor = new Box(floorLargura, floorAltura, floorComprimento);
        floor.scaleTextureCoordinates(new Vector2f(1f, 1f));
        
        enemy = new BoundingBox();
    }
    
    
    @Override
    public void simpleInitApp() {
        initKeys();
        initMaterials();
        //initSinbad();
        initLight();
        initMap();
        initCrosshairs();
        
        flyCam.setMoveSpeed(10);
        
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        //mudar localização do personagem ao iniciar o jogo
        cam.setLocation(new Vector3f(-20f, 0f, -20f));
        
        //System.out.println(control1.getAnimationNames());
    }
    
    private void initKeys() {
        inputManager.addMapping("SliceVertical",  new KeyTrigger(KeyInput.KEY_V));
        inputManager.addMapping("SliceHorizontal",  new KeyTrigger(keyInput.KEY_X));
        inputManager.addMapping("RunBase",  new KeyTrigger(keyInput.KEY_C));
        inputManager.addMapping("shoot", new MouseButtonTrigger(mouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "SliceVertical");
        inputManager.addListener(actionListener, "SliceHorizontal");
        inputManager.addListener(actionListener, "RunBase");
        inputManager.addListener(actionListener, "shoot");
    }

    private ActionListener actionListener = new ActionListener() { 
        public void onAction(String name, boolean keyPressed, float tpf) {
            if(name.equals("SliceVertical") && !keyPressed)
            {
                if(!channel1.getAnimationName().equals("SliceVertical"))
                {
                    channel1.setAnim("SliceVertical", 0.50f);
                    channel1.setLoopMode(LoopMode.Loop);
                }
            }
            if(name.equals("SliceHorizontal") && !keyPressed)
            {
                if(!channel1.getAnimationName().equals("SliceHorizontal"))
                {
                  channel1.setAnim("SliceHorizontal", 0.50f);
                  channel1.setLoopMode(LoopMode.Loop);
                }
            }
            if(name.equals("RunBase") && !keyPressed)
            { 
                if(!channel1.getAnimationName().equals("RunBase"))
                {
                    channel1.setAnim("RunBase", 0.50f);
                    channel1.setLoopMode(LoopMode.Loop);
                }
            }
            if (name.equals("shoot") && !keyPressed) {
                makeCannonBall();
            }
        }
    };
            
    
    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

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

    private void initSinbad() {
        sinbad = assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
        sinbad.setLocalScale(0.5f);
        sinbad.setLocalTranslation(0.0f, 0.0f, -2.0f);
        rootNode.attachChild(sinbad);
        
        control1 = sinbad.getControl(AnimControl.class);
        control1.addListener(this);
        
        channel1 = control1.createChannel();
        channel1.setAnim("RunTop");
    }

    private void initLight() {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(
                (new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
    }

    private void initCrosshairs() {
        setDisplayStatView(false);
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); //mira       
        ch.setLocalTranslation(settings.getWidth() / 2, settings.getHeight() / 2, 0); //center
        guiNode.attachChild(ch);
    }
    
    public void makeCannonBall() {
        /**
         * Create a cannon ball geometry and attach to scene graph.
         */
        Geometry ball_geo = new Geometry("cannon ball", bullet);
        ball_geo.setMaterial(stone_mat);
        rootNode.attachChild(ball_geo);
        /**
         * Position the cannon ball
         */
        ball_geo.setLocalTranslation(cam.getLocation());
        /**
         * Make the ball physical with a mass > 0.0f
         */
        bullet_phy = new RigidBodyControl(0.005f);
        /**
         * Add physical ball to physics space.
         */
        ball_geo.addControl(bullet_phy);
        bulletAppState.getPhysicsSpace().add(bullet_phy);
        /**
         * Accelerate the physical ball to shoot it.
         */
        bullet_phy.setLinearVelocity(cam.getDirection().mult(100));
    }

    

    private void initMaterials() {
        stone_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
        key2.setGenerateMips(true);
        Texture tex2 = assetManager.loadTexture(key2);
        stone_mat.setTexture("ColorMap", tex2);
    }

    private void initMap() {
        int map[][] = {
            {1, 1, 1, 1, 1, 1, 4, 4, 1, 1, 1, 1, 4, 4, 1, 1, 1, 1, 1, 1},
            {1, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1},
            {1, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 1, 0, 0, 1},
            {1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1},
            {1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1},
            {1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1},
            {1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1},
            {1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1},
            {1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1},
            {1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1},
            {1, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 1, 0, 0, 1},
            {1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };
        
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                //Floor
                criarParedeChao(i, j, -1);
                //Wall
                if (map[i][j] == 1) {
                    criarParedeChao(i, j, 1);
                }
                if (map[i][j] == 2) {
                    
                }
                /*if(map[i][j] == 4) { 
                  sinbad = assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
                  sinbad.setLocalScale(0.5f);
                  sinbad.setLocalTranslation(2.0f, 0.0f, 0.0f);
                  rootNode.attachChild(sinbad);
                  
                  //OrcsList.add(orcs);
                }*/
            }
        }
        
    }

    private void criarParedeChao(int x, int z, int chao) {
        Box boxMesh = new Box(1f, 1f, 1f);
        
        Geometry boxGeo;
        if (chao == -1) {
            boxGeo = new Geometry("BlocoChao", boxMesh);
            Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            Texture cube1Tex = assetManager.loadTexture("Textures/FloorTexture.jpg");
            boxMat.setTexture("ColorMap", cube1Tex);
            boxGeo.setMaterial(boxMat);
            boxGeo.move(0f, -2f, 0f);
            
            //Texture dirt = assetManager.loadTexture("Textures/Blood.png");
            //dirt.setWrap(WrapMode.Repeat);
            //boxMat.setTexture("ColorMap", dirt);
            //boxMat.setFloat("Tex2Scale", 32f);
        } else {
            boxGeo = new Geometry("Bloco", boxMesh); 
            Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            Texture cube2Tex = assetManager.loadTexture("Textures/WallTexture.jpg");
            boxMat.setTexture("ColorMap", cube2Tex);
            boxGeo.scale(1f, 3.0f, 1f);
            boxGeo.setMaterial(boxMat);
            boxGeo.setMaterial(boxMat);
        }

        rootNode.attachChild(boxGeo);

        //RigidBodyControl r = new RigidBodyControl(0);
        //boxGeo.addControl(r);

        boxGeo.move(-40 + x * 2, chao, -40 + z * 2);
        //r.setPhysicsLocation(boxGeo.getLocalTranslation());

        //state.getPhysicsSpace().add(r);
    }
}
