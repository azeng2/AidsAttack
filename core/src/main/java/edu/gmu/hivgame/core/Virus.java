package edu.gmu.hivgame.core;

import java.lang.Math;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

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
import playn.core.TextFormat;
import playn.core.AbstractTextLayout;
import playn.core.TextLayout;
import playn.core.TextWrap;
import playn.core.Font;
import playn.core.util.TextBlock;


public class Virus implements CollisionHandler {
  // for calculating interpolation
  private float prevX, prevY, prevA;
  private Body body;
  private Fixture myBodyFixture;
  private Fixture mySensor;
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
    game.addLayer(v.myHitCountLayer);
    v.prevX = v.x(); v.prevY = v.y(); v.prevA = v.ang();
    return v;
  }

  public Body body(){ return this.body; }
  public Vec2 position(){ return this.body().getPosition(); }
  public Fixture sensor(){
    return this.mySensor;
    // Fixture fix = body.getFixtureList();
    // while(!fix.isSensor()){
    //   fix = fix.getNext();
    // }
    // return fix;
  }
  public Fixture bodyFixture(){
    return this.myBodyFixture;
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

    this.myBodyFixture = body.createFixture(fixtureDef);
    this.myBodyFixture.m_userData = this;
    // body.setLinearDamping(1.0f);
    this.body = body;
    this.body.m_userData = this;

    //create sensor
    CircleShape circleShape = new CircleShape();
    circleShape.m_radius = this.getSensorRadius();
    circleShape.m_p.set(0.0f, 0.0f);

    FixtureDef fd = new FixtureDef();
    fd.shape = circleShape;
    fd.isSensor = true;
    this.mySensor = body.createFixture(fd);
    this.mySensor.m_userData = this;
  }
  
  
  public void handleCollision(Fixture me, Fixture other){
    if(me == this.myBodyFixture && other.m_userData instanceof Antibody){
      this.addHit();
      ((Antibody) other.m_userData).destroy();
    }
    if(me == this.mySensor){
      System.out.println("I've been spotted by "+other.m_userData);
    }

    
  }


  // Add the virus layers
  private void addVirusLayer(){
     //this.loadVirusImage();
     this.drawVirusImage();
     if(debugMe){
       this.drawDebugImage();
     }
     this.makeHitCountImage();
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

  //keep track of number of times the Virus has been hit by an Antibody
  int hitCount;
  float hitCountScaleX, hitCountScaleY;
  ImageLayer myHitCountLayer;
  
  public int getHitCount(){
    return this.hitCount;
  }

  public void addHit(){
    hitCount++;
    changeHitCountImage();     // Adjust the image
  }

  public void changeHitCountImage(){
    Font font = graphics().createFont("Courier", Font.Style.PLAIN, 16);
    String hits = Integer.toString(getHitCount());
    TextFormat fmt = new TextFormat().withFont(font);
    //look to playn showcase text example and model after that?
    //^Yes.
    TextLayout tl = graphics().layoutText(hits, fmt);
    //modeled after createTextLayer() in TextDemo.java in playn showcase
    //some code very much the same (really just changed 'layout' to 'tl').
    //how ok is this?
    CanvasImage image = graphics().createImage((int)Math.ceil(tl.width()),
                                                (int)Math.ceil(tl.height()));
    //took out a line here setting fill color. Default seems to be black.
    image.canvas().fillText(tl, 0, 0);
    myHitCountLayer.setImage(image);
  }

  //creates image of hit count to be displayed each time virus is hit by antibody.
  private void makeHitCountImage (){
    //CanvasImage image = graphics().createImage(100,100);
    //Canvas canvas = image.canvas();
    //canvas.setStrokeWidth(2);
    //canvas.setStrokeColor(0x5500ff00);
    //canvas.strokeRect(1, 1, 50, 50);
    Font font = graphics().createFont("Courier", Font.Style.PLAIN, 16);
    String hits = Integer.toString(getHitCount());
    TextFormat fmt = new TextFormat().withFont(font);
    //look to playn showcase text example and model after that?
    //^Yes.
    TextLayout tl = graphics().layoutText(hits, fmt);
    //modeled after createTextLayer() in TextDemo.java in playn showcase
    //some code very much the same (really just changed 'layout' to 'tl').
    //how ok is this?
    CanvasImage image = graphics().createImage((int)Math.ceil(tl.width()),
                                                (int)Math.ceil(tl.height()));
    //took out a line here setting fill color. Default seems to be black.
    image.canvas().fillText(tl, 0, 0);
    //^end of code from playn showcase file.
    myHitCountLayer = graphics().createImageLayer(image);
    myHitCountLayer.setScale(scaleX,scaleY);
    myHitCountLayer.setTranslation(x(), y());
    //does this really do anything?
    myHitCountLayer.setOrigin(image.width() / 2f, image.height() / 2f);

    //Can I get rid of these comments? Or should I keep them for reference?
    // canvas.fillText(tl, 0f, 0f);
    //TextWrap wrap = new TextWrap(200);
    //TextBlock block = new TextBlock(graphics().layoutText(hits, fmt, wrap));
    //myHitCountLayer = graphics().createImageLayer(block.toImage(TextBlock.Align.LEFT, 0xFF660000));
    //scaleX = (getWidth()  / AidsAttack.physUnitPerScreenUnit) / image.width();
    //scaleY = (getHeight() / AidsAttack.physUnitPerScreenUnit) / image.height();
    //myHitCountLayer.setRotation(ang());
    //String hits = Integer.toString(getHitCount());
    //typecast interface TextLayout to AbstractTextLayout
    //AbstractTextLayout tl = (AbstractTextLayout) graphics().layoutText(hits, 
    //                                                                  new TextFormat());
   // TextLayout tl = graphics().layoutText(hits, 
  //                                                                    new TextFormat());
    //canvas.fillText(tl, 0f, 0f);
  }
    
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
    Image image = assets().getImage("images/smiley.png");
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
    myHitCountLayer.setTranslation(20,20);



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
