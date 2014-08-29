package edu.gmu.hivgame.core;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;

import static playn.core.PlayN.assets;
import static playn.core.PlayN.graphics;
import static playn.core.PlayN.log;

import playn.core.CanvasImage;
import playn.core.Canvas;
import playn.core.DebugDrawBox2D;
import playn.core.GroupLayer;
import playn.core.ImageLayer;
import playn.core.Image;
import playn.core.util.Callback;

public class Virus {
  // for calculating interpolation
  private float prevX, prevY, prevA;
  private Body body;
  private ImageLayer myLayer;
  AidsAttack game;

  boolean debugMe = true;       // set to true when debugging; draw additional info

  //private Magnet magnet;

  private Virus(){
  }
  public static Virus make(AidsAttack game, float x, float y, float ang){
    Virus v = new Virus();
    v.game = game;
    v.initPhysicsBody(game.physicsWorld(), x, y, ang);
    v.addVirusLayer();
    game.addLayer(v.myLayer);
    game.addLayer(v.myDebugLayer);
    v.prevX = v.x(); v.prevY = v.y(); v.prevA = v.ang();
    return v;
  }

  public Body body(){ return this.body; }
  public Vec2 position(){ return this.body().getPosition(); }
  public Fixture sensor(){
    Fixture fix = body.getFixtureList();
    while(!fix.isSensor()){
      fix = fix.getNext();
    }
    return fix;
  }

  void initPhysicsBody(World world, float x, float y, float angle) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.DYNAMIC;
    bodyDef.position = new Vec2(x, y);
    bodyDef.angle = angle;
    //adjusted angular damping because larger shape was spinning oddly. was 1.0f
    bodyDef.angularDamping = 2.0f;
    bodyDef.linearDamping = 1.0f;
    Body body = world.createBody(bodyDef);

    // Define a simple square for the internal virus
    PolygonShape polygonShape = new PolygonShape();
    Vec2[] polygon = new Vec2[4];
    polygon[0] = new Vec2(-getWidth()/2f, -getHeight()/2f);
    polygon[1] = new Vec2(getWidth()/2f, -getHeight()/2f);
    polygon[2] = new Vec2(getWidth()/2f, getHeight()/2f);
    polygon[3] = new Vec2(-getWidth()/2f, getHeight()/2f);
    polygonShape.set(polygon, polygon.length);

    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = polygonShape;
    fixtureDef.friction = 0.1f;
    fixtureDef.restitution = 0.4f;
    fixtureDef.density = 1.0f;

    body.createFixture(fixtureDef);
    // body.setLinearDamping(1.0f);
    this.body = body;

    //create sensor
    CircleShape circleShape = new CircleShape();
    circleShape.m_radius = this.getSensorRadius();
    circleShape.m_p.set(0.0f, 0.0f);

    FixtureDef fd = new FixtureDef();
    fd.shape = circleShape;
    fd.isSensor = true;
    body.createFixture(fd);
  }
  
  

  // Add the virus layers
  private void addVirusLayer(){
     this.loadVirusImage();
     //this.drawVirusImage();
     if(debugMe){
       this.drawDebugImage();
     }
  }


  // Debug scaling and image layer for the virus
  float debugScaleX, debugScaleY;
  ImageLayer myDebugLayer;

  // Draw a debugging image of the virus usually including its sensor
  private void drawDebugImage(){
    Fixture fix = body.getFixtureList();
    if(!fix.isSensor()){
      fix = fix.getNext();
    }
    CircleShape s = (CircleShape) fix.getShape();
    float physRad = s.getRadius();

    float screenRad = physRad / AidsAttack.physUnitPerScreenUnit;
    //System.out.printf("sensor physRad: %f\nscreenRad: %f\n",physRad,screenRad);
    //why random number?
    //screenRad = 50f;

    CanvasImage image = graphics().createImage(100, 100);
    Canvas canvas = image.canvas();
    canvas.setStrokeWidth(2);
    canvas.setStrokeColor(0xffffff00);
    canvas.strokeCircle(50f,50f,screenRad);

    this.myDebugLayer = graphics().createImageLayer(image);
    myDebugLayer.setOrigin(image.width() / 2f, image.height() / 2f);
    //this.debugScaleX = (screenRad) / image.width();
    //this.debugScaleY = (screenRad) / image.width();
    this.debugScaleX = (1f / AidsAttack.physUnitPerScreenUnit) / image.width();
    this.debugScaleY = (1f / AidsAttack.physUnitPerScreenUnit) / image.width();
    //    System.out.printf("scaleX: %f\nscaleY: %f",scaleX,scaleY);
    myDebugLayer.setScale(debugScaleX,debugScaleY);
    myDebugLayer.setTranslation(x(), y());
    myDebugLayer.setRotation(ang());
  }

  // Scale of image layer representing the virus
  float scaleX, scaleY;
    
  // Manually draw the image of the virus, currently as a red rectangle
  private void drawVirusImage(){
    CanvasImage image = graphics().createImage(100, 100);
    Canvas canvas = image.canvas();
    canvas.setStrokeWidth(2);
    canvas.setStrokeColor(0xffff0000);
    canvas.strokeRect(1, 1, 96, 96);

    myLayer = graphics().createImageLayer(image);
    myLayer.setOrigin(image.width() / 2f, image.height() / 2f);
    scaleX = (getWidth()  / AidsAttack.physUnitPerScreenUnit) / image.width();
    scaleY = (getHeight() / AidsAttack.physUnitPerScreenUnit) / image.height();
    //System.out.printf("scaleX: %f\nscaleY: %f",scaleX,scaleY);
    myLayer.setScale(scaleX,scaleY);
    myLayer.setTranslation(x(), y());
    myLayer.setRotation(ang());
  }

  // Create an image version of the virus loaded from a graphic
  private void loadVirusImage(){
    Image image = assets().getImage("images/HIV-virion.png");
    myLayer = graphics().createImageLayer(image);
    // Callback is required because image may not immediately load (?)
    image.addCallback(new Callback<Image>() {
      @Override
      public void onSuccess(Image image) {
  	myLayer.setOrigin(image.width() / 2f, image.height() / 2f);
        scaleX = (getWidth()  / AidsAttack.physUnitPerScreenUnit) / image.width();
        scaleY = (getHeight() / AidsAttack.physUnitPerScreenUnit) / image.height();
  	// System.out.printf("scaleX: %f\nscaleY: %f",scaleX,scaleY);
  	myLayer.setScale(scaleX,scaleY);
  	myLayer.setTranslation(x(), y());
  	myLayer.setRotation(ang());


        // // since the image is loaded, we can use its width and height
        // layer.setOrigin(image.width() / 2f, image.height() / 2f);
        // layer.setScale(getWidth() / image.width(), getHeight() / image.height());
        // layer.setTranslation(x, y);
        // layer.setRotation(angle);
        // initPostLoad(peaWorld);
      }

      @Override
      public void onFailure(Throwable err) {
        log().error("Error loading image: " + err.getMessage());
      }
    });

  }

  // Get the sensor radius in physics units
  float getSensorRadius(){
    return 5f;
  }

  // Get the widht/height of the virus in physics units
  float getWidth() {
    return 1.0f;
  }

  // Get the widht/height of the virus in physics units
  float getHeight() {
    return 1.0f;
  }

  // X/Y position and angle of the center of the virus in physics units
  float x(){
    return body.getPosition().x;
  }
  // X/Y position and angle of the center of the virus in physics units
  float y(){
    return body.getPosition().y;
  }
  // X/Y position and angle of the center of the virus in physics units
  float ang(){
    return body.getAngle();
  }

  // Find the closest vertex of the internal virus polygon to a given point
  public Vec2 closestVertex(Vec2 target){
    Fixture fix = body.getFixtureList();
    if(fix.isSensor()){
      fix = fix.getNext();
    }
    PolygonShape sh = (PolygonShape) fix.getShape();
    Vec2 closest = body.getWorldPoint(sh.getVertex(0));
    float closestDist2 = closest.sub(target).lengthSquared();
    for(int i=1; i<sh.getVertexCount(); i++){
      Vec2 v = body.getWorldPoint(sh.getVertex(i));
      float dist2 = v.sub(target).lengthSquared();
      if(dist2 < closestDist2*.98){ // scale a little to prevent sudden changes
	closest = v;
	closestDist2 = dist2;
      }
    }
    return closest;
  }

  // Parameters governing motion of virus
  float minLength = 1f;
  float forceScale = 10f;

  public void attractTowards(Vec2 target){
    Vec2 force = target.sub(position());
    float length = force.normalize(); // Alters force vector, length returned
    if(length > minLength){
      force.mulLocal(forceScale);
    }
    // body().applyForceToCenter(force);
    Vec2 cv = this.closestVertex(target);
    body().applyForce(force,cv);
  }


  public void paint(float alpha) {
    // interpolate based on previous state
    float x = (x() * alpha) + (prevX * (1f - alpha));
    float y = (y() * alpha) + (prevY * (1f - alpha));
    float a = (ang() * alpha) + (prevA * (1f - alpha));
    myLayer.setTranslation(x, y);
    myLayer.setRotation(a);

    float angle = (game.time() + game.UPDATE_RATE*alpha) * (float) Math.PI / 1000;
    float scale = (float) Math.sin(angle)*0.1f + 1f;
    myLayer.setScale(scaleX*scale,scaleY*scale);
    // myDebugLayer.setScale(scaleX*scale,scaleY*scale);
        
    if(debugMe){
      myDebugLayer.setTranslation(x, y);
      myDebugLayer.setRotation(a);
    }



    // float scale = (float) 0.1f;
    // myLayer.transform().setUniformScale(scale);    

    // float scale = 1.0f; // + 0.25f*((float) Math.sin(angle));
    // myLayer.setScale(scale,scale);
    // myLayer.transform().setUniformScale(scale);    
  }


  public void update(float delta) {
    // store state for interpolation in paint()
    prevX = x();
    prevY = y();
    prevA = ang();
  }


}
