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
import pythagoras.f.Point;

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

public class AidsAttack extends Game.Default {

  private static int width = 24; //width of drawable area on screen
  private static int height = 18; //height of drawable area on screen
  public static final int UPDATE_RATE = 33; // call update every 33ms (30 times per second)

  World world;			// Box2d world
  GroupLayer worldLayer;	// Holds everything
  GroupLayer entityLayer;	// Add entities
  GroupLayer buttonLayer; // contain buttons which do not scale with image
  public Camera camera; //used to control zooming and panning

  World physicsWorld(){ return this.world; }

  Virus theVirus;
  Cell theCell;
  Antibody[] antibodies;

  public void addLayer(Layer l){
    this.entityLayer.add(l);
  }
  public void removeLayer(Layer l){
    this.entityLayer.remove(l);
  }
  public void addButton(Layer l){
    this.buttonLayer.add(l);
  }

  public AidsAttack() {
    super(UPDATE_RATE); 
  }

  //returns x or y coordinate of center of drawable area, in screen units
  public static float getCenterX(){
    return width/2f;
  }
  public static float getCenterY(){
    return height/2f;
  }

  @Override
  public void init(){
    startLevelOne();
  }

  public void startLevelOne() {
    // create and add background image layer
    Image bgImage = assets().getImage("images/bg.png");
    ImageLayer bgLayer = graphics().createImageLayer(bgImage);
    bgLayer.setDepth(0f);
    graphics().rootLayer().add(bgLayer);

    // create our world layer (scaled to "world space")
    worldLayer = graphics().createGroupLayer();
    worldLayer.setDepth(2f);
    camera = new Camera(this);
    graphics().rootLayer().add(worldLayer);

    //group layer to hold entities
    entityLayer = graphics().createGroupLayer();
    worldLayer.add(entityLayer);

    //group layer to hold non-scaling layers
    //intended for manually-created buttons
    buttonLayer = graphics().createGroupLayer();
    buttonLayer.setDepth(4f);
    graphics().rootLayer().add(buttonLayer);

    //make(game, xpos, ypos, label)
    Button zoomInButton = Button.make(this,10f,10f,"+");
    zoomInButton.buttonImage.addListener(new Pointer.Adapter() {
          @Override
          public void onPointerStart(Pointer.Event event) {
            camera.zoomingIn = true;
            camera.zoomingOut = false;
          }
          @Override
          public void onPointerDrag(Pointer.Event event){
            camera.zoomingIn = true;
            camera.zoomingOut = false;
          }
          @Override
          public void onPointerEnd(Pointer.Event event){
            camera.zoomingIn = false;
          }
    });
    Button zoomOutButton = Button.make(this,10f,40f,"-");
    zoomOutButton.buttonImage.addListener(new Pointer.Adapter() {
      @Override
      public void onPointerStart(Pointer.Event event) {
        camera.zoomingOut = true;
        camera.zoomingIn = false;
      }
      @Override
      public void onPointerDrag(Pointer.Event event){
        camera.zoomingOut = true;
        camera.zoomingIn = false;
      }
      @Override
      public void onPointerEnd(Pointer.Event event){
        camera.zoomingOut = false;
      }
    });
    Button resetButton = Button.make(this,10f,70f,"reset");
    resetButton.buttonImage.addListener(new Pointer.Adapter() {
      @Override
      public void onPointerStart(Pointer.Event event){
        graphics().rootLayer().destroyAll();
        //worldLayer.destroyAll();
        //buttonLayer.destroyAll();
        startLevelOne();
      }
    });
    // Add centering function to center camera on virus
    Button centerButton = Button.make(this,10f,100f,"center");
    centerButton.buttonImage.addListener(new Pointer.Adapter() {
      @Override
      public void onPointerStart(Pointer.Event event){
        camera.setCenter(theVirus.x(), theVirus.y());
      }
    });

    System.out.println("bgLayer's depth: "+bgLayer.depth());
    System.out.println("worldLayer's depth: "+worldLayer.depth());
    System.out.println("buttonLayer's depth: "+buttonLayer.depth());

    // create the physics world
    Vec2 gravity = new Vec2(0.0f, 0.0f);
    world = new World(gravity);
    world.setWarmStarting(true);
    world.setAutoClearForces(true);
    world.setContactListener(Global.contactListener);

    //create the Virus object
    this.theVirus = Virus.make(this, 5f, 0f, .2f);

    //create the Cell object
    this.theCell = Cell.make(this, 50f, 60f, .2f);

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
        Point p = new Point(event.x(), event.y());
        System.out.printf("Point p is at: %f, %f.\n",p.x(), p.y());
        System.out.println("Physical x of point p: "+camera.screenXToPhysX(p.x()));
        System.out.println("Physical y of point p: "+camera.screenYToPhysY(p.y()));
        Layer hit = buttonLayer.hitTest(p);
        if(hit != null){
          System.out.println("Hit a button!");
        }
        else{
          attractingVirus = true;
          virusScreenTarget.set(event.x(),
              event.y());
        }
      }
      @Override
      public void onPointerEnd(Pointer.Event event) {
        attractingVirus = false;
      }
      @Override
      public void onPointerDrag(Pointer.Event event) {
        Point p = new Point(event.x(), event.y());
        System.out.printf("Point p is at: %f, %f.\n",p.x(), p.y());
        Layer hit = buttonLayer.hitTest(p);
        if(hit != null){
          System.out.println("Hit a button!");
        }
        else{
          attractingVirus = true;
          virusScreenTarget.set(event.x(),
            event.y());
        }
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
        //Translation keys: a is left, s is down, w is up, d is right.
        else if(event.key() == Key.valueOf("A")){
          camera.translateRight();
        }
        else if(event.key() == Key.valueOf("S")){
          camera.translateUp();
        }
        else if(event.key() == Key.valueOf("W")){
          camera.translateDown();
        }
        else if(event.key() == Key.valueOf("D")){
          camera.translateLeft();
        }
        else if(event.key() == Key.valueOf("SPACE")){
          System.out.println("Spacebar pressed!");
          camera.setCenter(theVirus.x(), theVirus.y());
        }
      }
      @Override
      public void onKeyUp(Keyboard.Event event){
        System.out.println("Key released!");
        //do I need to put anything here?
      }
    });

    //method in Interface Platform, same place we call graphics(), keyboard(), etc from
    //setPropagateEvents(false);
  }


  //called when the virus has too many hits on it
  public void gameOver(){
    //create surface layer with 'game over'
    CanvasImage image = graphics().createImage(200,200);
    Canvas canvas = image.canvas();
    canvas.setFillColor(0xff050505);
    canvas.drawText("Game Over!",100,100);
    ImageLayer gameOverLayer = graphics().createImageLayer(image);
    gameOverLayer.setDepth(6);
    //Game over message does not display because it is on the bottom of rootLayer, under background
    graphics().rootLayer().add(gameOverLayer);
    pointer().setListener(null);
    keyboard().setListener(null);
    //layer should be translucent background color w/ opaque text in center.
    //pointer listener should be null so mouse clicks don't continue to move virus.
  }

  Vec2 virusScreenTarget = new Vec2();
  boolean attractingVirus = false;
  float minLength = 1f;
  float forceScale = 10f;

  int time = 0;
  public int time(){ return this.time; }
  Random gravity = new Random(54321);

  float zoomStep = 0.1f;

  @Override
  public void update(int delta) {
    time += delta;
    time = time < 0 ? 0 : time;
    if(time%100 == 0){
      float r1 = (gravity.nextFloat() - 0.5f)*5f;
      float r2 = (gravity.nextFloat() - 0.5f)*5f;
      //float r1 = 0f;
      //float r2 = 100f;
      Vec2 ng = new Vec2(r1,r2);
      //System.out.printf("New gravity is: %f, %f\n",r1,r2);
      world.setGravity(ng);
    }
    theVirus.update(delta);
    theCell.update(delta);

    if(this.attractingVirus){
      Vec2 virusPhysTarget = new Vec2();
      virusPhysTarget.set(camera.screenXToPhysX(virusScreenTarget.x),
        camera.screenYToPhysY(virusScreenTarget.y));
      theVirus.attractTowards(virusPhysTarget);
    }
    for(int i=0; i<antibodies.length; i++){
      antibodies[i].update(delta);
    }
    camera.update();

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
    theCell.paint(alpha);
    for(int i=0; i<antibodies.length; i++){
      antibodies[i].paint(alpha);
    }
  }
}
