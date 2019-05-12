package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.scene.Spatial;

public class Orc {
    
    private Spatial spatial;
    private AnimChannel channel;
    private AnimControl control;
    private String name;

    public Spatial getSpatial() {
        return spatial;
    }

    public void setSpatial(Spatial spatial) {
        this.spatial = spatial;
    }

    public AnimChannel getChannel() {
        return channel;
    }

    public void setChannel(AnimChannel channel) {
        this.channel = channel;
    }

    public AnimControl getControl() {
        return control;
    }

    public void setControl(AnimControl control) {
        this.control = control;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}