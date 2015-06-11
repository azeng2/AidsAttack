package edu.gmu.hivgame.core;

import static playn.core.PlayN.*;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.ContactEdge;

import playn.core.Game;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.GroupLayer;
import playn.core.CanvasImage;
import playn.core.Canvas;
import playn.core.SurfaceImage;
import playn.core.Layer;
import static playn.core.PlayN.pointer;
import playn.core.Pointer;
import static playn.core.PlayN.keyboard;
import playn.core.Keyboard;
import playn.core.Key;
import playn.core.util.Callback;
import playn.core.*;

//Abstract class to be implemented by each of the three levels of gameplay.
public abstract class Level{
  AidsAttack game;
  GroupLayer worldLayer;	// Holds everything for this level's world.
  public Camera camera; // used for translating coordinates between physics and screen units
  public World m_world; // The world specific to this level. AidsAttack holds a reference to this.
  public boolean gameOver;
  public boolean success;

  void initLevel(Camera camera){
    // create and add background image layer
    Image bgImage = assets().getImage("images/bg.png");
    ImageLayer bgLayer = graphics().createImageLayer(bgImage);
    bgLayer.setDepth(0f);
    graphics().rootLayer().add(bgLayer);

    // each Level has its own world, and its own worldLayer
    // will need endLevel() method to destroy worldLayer
    worldLayer = graphics().createGroupLayer();

    // for debugging of worldLayer
    System.out.println("RootLayer size is: "+graphics().rootLayer().size());
    graphics().rootLayer().add(worldLayer);
    System.out.println("World Layer added");
    System.out.println("RootLayer size is: "+graphics().rootLayer().size());
    worldLayer.setInteractive(true);
    System.out.println("World layer destroyed? "+worldLayer.destroyed());

    // create the physics world
    Vec2 gravity = new Vec2(0.0f, 0.0f);
    m_world = new World(gravity);
    m_world.setWarmStarting(true);
    m_world.setAutoClearForces(true);
    m_world.setContactListener(Global.contactListener);
    this.camera = camera;
  }

  void endLevel(){
    worldLayer.destroyAll();
    worldLayer.destroy();
  }

  public void addLayer(Layer l){
    this.worldLayer.add(l);
  }
  public void removeLayer(Layer l){
    this.worldLayer.remove(l);
  }

  abstract void update(int delta, int time);

  abstract void paint(float alpha);

  World physicsWorld(){ return this.m_world; }

  abstract String levelName();
}
