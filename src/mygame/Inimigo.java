/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.scene.Spatial;
import java.util.ArrayList;

/**
 *
 * @author Vitor
 */
public class Inimigo implements AnimEventListener{
    private Spatial enemySpatial;
    private AnimControl control1;
    private AnimChannel channel1;
    private RigidBodyControl rb;
    
    public Inimigo(Spatial e, int x, int y, int z) {
        enemySpatial = e;
        e.setLocalTranslation(-40 + x * 2, y - 3.5f, -40 + z * 2);
        e.setLocalScale(0.5f);
        
        rb = new RigidBodyControl();
        e.addControl(rb);
        //r2.setPhysicsLocation(enemy.getLocalTranslation());

        control1 = e.getControl(AnimControl.class);
        control1.addListener(this);
        channel1 = control1.createChannel();
        channel1.setAnim("RunBase");

        
    }
    
    public Spatial getSpatial() {
        return enemySpatial;
    }
    
    public RigidBodyControl getRigidBodyControl() {
        return rb;
    }
    
    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
