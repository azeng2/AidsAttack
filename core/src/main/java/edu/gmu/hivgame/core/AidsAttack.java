package edu.gmu.hivgame.core;

import static playn.core.PlayN.*;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.ContactEdge;

import java.util.Random;
import java.math.*;

import playn.core.Game;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.GroupLayer;
import playn.core.Layer;
import static playn.core.PlayN.pointer;
import playn.core.Pointer;
import static playn.core.PlayN.keyboard;
import playn.core.Keyboard;
import playn.core.Key;
import playn.core.util.Callback;

public class AidsAttack extends Game.Default {

  // Scaling of meters / pixels ratio for drawing scales
  public static float screenUnitPerPhysUnit = 20.0f;
  //used for smooth zoom
  private static float zoomLevelGoal = 20.0f;
  private static int width = 24;
  private static int height = 18;
  public static final int UPDATE_RATE = 33; // call update every 33ms (30 times per second)

  World world;			// Box2d world
  GroupLayer worldLayer;	// Holds everything
  GroupLayer entityLayer;	// Add entities
  Camera camera;

  World physicsWorld(){ return this.world; }

  Virus theVirus;
  Antibody[] antibodies;

  public void addLayer(Layer l){
    this.entityLayer.add(l);
  }
  public void removeLayer(Layer l){
    this.entityLayer.remove(l);
  }

  public AidsAttack() {
    super(UPDATE_RATE); 
  }

  @Override
  public void init() {
    // create and add background image layer
    Image bgImage = assets().getImage("images/bg.png");
    ImageLayer bgLayer = graphics().createImageLayer(bgImage);
    graphics().rootLayer().add(bgLayer);

    // create our world layer (scaled to "world space")
    worldLayer = graphics().createGroupLayer();
    camera = new Camera(this);
    //FIXME: to be removed when camera is finished
    worldLayer.setScale(screenUnitPerPhysUnit);
    graphics().rootLayer().add(worldLayer);

    //group layer to hold entities
    entityLayer = graphics().createGroupLayer();
    worldLayer.add(entityLayer);

    // create the physics world
    Vec2 gravity = new Vec2(0.0f, 0.0f);
    world = new World(gravity);
    world.setWarmStarting(true);
    world.setAutoClearForces(true);
    world.setContactListener(Global.contactListener);

    //create the Virus object
    this.theVirus = Virus.make(this, 15f, 0f, .2f);

    //Random to distribute Antibodies on screen
    Random r = new Random(12345);
    antibodies = new Antibody[6];
    for(int i=0; i<antibodies.length; i++){
      float x = r.nextFloat();
      float y = r.nextFloat(); 
      Antibody a = Antibody.make(this, x*50, y*50, .2f);
      antibodies[i] = a;
    }


    // hook up our pointer listener
    pointer().setListener(new Pointer.Adapter() {
	    @Override
      public void onPointerStart(Pointer.Event event) {
      attractingVirus = true;
      virusTarget.set(1/camera.screenUnitPerPhysUnit * event.x(),
          1/camera.screenUnitPerPhysUnit * event.y());
      }
      @Override
      public void onPointerEnd(Pointer.Event event) {
        attractingVirus = false;
      }
      @Override
      public void onPointerDrag(Pointer.Event event) {
        attractingVirus = true;
        virusTarget.set(1/camera.screenUnitPerPhysUnit * event.x(),
            1/camera.screenUnitPerPhysUnit * event.y());
      }
    });

    //hook up key listener, for global scaling in-game
    keyboard().setListener(new Keyboard.Adapter() {
      @Override
      //Zoom keys: Up and Down arrows. Tried + and -, but did not work for +
      //I suspect this is because it required the shift key, but I'm not sure how to fix it.
      public void onKeyDown(Keyboard.Event event){
        if(event.key() == Key.valueOf("UP")){
          camera.zoomIn();
        }
        else if(event.key() == Key.valueOf("DOWN")){
          camera.zoomOut();
        }
        //Translation keys: h is left, j is down, k is up, l is right.
        //for now, movement keys used in Vim
        else if(event.key() == Key.valueOf("H")){
          camera.translateLeft();
        }
        else if(event.key() == Key.valueOf("J")){
          camera.translateDown();
        }
        else if(event.key() == Key.valueOf("K")){
          camera.translateUp();
        }
        else if(event.key() == Key.valueOf("L")){
          camera.translateRight();
        }
      }
      @Override
      public void onKeyUp(Keyboard.Event event){
        System.out.println("Key released!");
        //do I need to put anything here?
      }
    });

  }

  Vec2 virusTarget = new Vec2();
  boolean attractingVirus = false;
  float minLength = 1f;
  float forceScale = 10f;

  int time = 0;
  public int time(){ return this.time; }

  float zoomStep = 0.1f;

  @Override
  public void update(int delta) {
    time += delta;
    time = time < 0 ? 0 : time;
    theVirus.update(delta);

    if(this.attractingVirus){
      theVirus.attractTowards(virusTarget);
    }
    for(int i=0; i<antibodies.length; i++){
      antibodies[i].update(delta);
    }
    camera.updateZoom();
    worldLayer.setScale(camera.screenUnitPerPhysUnit);
    camera.updateTranslation();
    worldLayer.setTranslation(camera.translationX, camera.translationY);
    worldLayer.transform();

    //Handling Contacts between fixtures. m_userData of Virus and Antibodies is themselves,
    //and they implement the interface CollisionHandler.
    Contact contact = world.getContactList();
    while(contact != null){
      if(contact.isTouching()){
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        if(fixtureA.m_userData instanceof CollisionHandler){
          CollisionHandler ch = (CollisionHandler) fixtureA.m_userData;
          ch.handleCollision(fixtureA, fixtureB);
        }
        if(fixtureB.m_userData instanceof CollisionHandler){
          CollisionHandler ch = (CollisionHandler) fixtureB.m_userData;
          ch.handleCollision(fixtureB, fixtureA);
        }
      }
      contact = contact.getNext();
    }


    // the step delta is fixed so box2d isn't affected by framerate
    world.step(0.033f, 10, 10);
  }

  @Override
  public void paint(float alpha) {
    // the background automatically paints itself, so no need to do anything here!
    theVirus.paint(alpha);
    for(int i=0; i<antibodies.length; i++){
      antibodies[i].paint(alpha);
    }
  }
}
