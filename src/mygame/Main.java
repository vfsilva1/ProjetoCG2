package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

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
    private Spatial sinbad;
    private AnimControl control1;
    private AnimChannel channel1;
    private AnimChannel channel2;
    
    @Override
    public void simpleInitApp() {
        initKeys();
        
        sinbad = assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
        sinbad.setLocalScale(0.5f);
        sinbad.setLocalTranslation(0.0f, 0.0f, -2.0f);
        rootNode.attachChild(sinbad);
        
        control1 = sinbad.getControl(AnimControl.class);
        control1.addListener(this);
        
        channel1 = control1.createChannel();
        channel1.setAnim("RunTop");
        System.out.println(control1.getAnimationNames());
        
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(
                (new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
    }
    
    private void initKeys() {
        inputManager.addMapping("SliceVertical",  new KeyTrigger(KeyInput.KEY_V));
        inputManager.addMapping("SliceHorizontal",  new KeyTrigger(keyInput.KEY_X));
        inputManager.addMapping("RunBase",  new KeyTrigger(keyInput.KEY_C));
        inputManager.addListener(actionListener, "SliceVertical");
        inputManager.addListener(actionListener, "SliceHorizontal");
        inputManager.addListener(actionListener, "RunBase");
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
}
