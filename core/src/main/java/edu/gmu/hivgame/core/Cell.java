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

//This is the target of the first level.
//When the virus hits it, Congratulations message should display
//When hit by Antibody, they should bounce off each other.
public class Cell implements CollisionHandler{
  // for calculating interpolation
  private float prevX, prevY, prevA;
  private Body body;
  private Fixture myBodyFixture;
  private Fixture mySensor;
  private ImageLayer myLayer;
  AidsAttack game;

  public Body body(){ return this.body; }
  public Vec2 position(){ return this.body().getPosition(); }
  public Fixture bodyFixture(){ return this.myBodyFixture; }
  public ImageLayer layer() {return this.myLayer; }

  private Cell(){}
  public static Cell make(AidsAttack game, float x, float y, float ang){
    Cell c = new Cell();
    c.virusContact = false;
    c.game = game;
    c.initPhysicsBody(game.physicsWorld(), x, y, ang);
    c.drawCellImage();
    c.game.addLayer(c.myLayer);
    c.prevX = c.x(); c.prevY = c.y(); c.prevA = c.ang();
    return c;
  }

  void initPhysicsBody(World world, float x, float y, float angle){
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.DYNAMIC;
    bodyDef.position = new Vec2(x, y);
    bodyDef.angle = angle;
    bodyDef.linearDamping = 2f;
    Body body = world.createBody(bodyDef);
    body.setSleepingAllowed(false);

    CircleShape circleShape = new CircleShape();
    circleShape.m_radius = getRadius();
    // density is 1.0f   
    this.myBodyFixture = body.createFixture(circleShape, 1.0f);
    this.myBodyFixture.m_userData = this;

    this.body = body;
    this.body.m_userData = this;
  }

  private void drawCellImage(){
    Fixture fix = body.getFixtureList();
    CircleShape s = (CircleShape) fix.getShape();

    CanvasImage image = graphics().createImage(100, 100);

    Canvas canvas = image.canvas();
    canvas.setStrokeColor(0xff888800);
    canvas.fillCircle(50f,50f,50f);

    this.myLayer = graphics().createImageLayer(image);
    myLayer.setOrigin(image.width() / 2f, image.height() / 2f);
    myLayer.setScale(getWidth()/image.width(),getHeight()/image.height());

    myLayer.setTranslation(x(), y());
    myLayer.setRotation(ang());
  }

  float getWidth(){
    return getRadius()*2;
  }
  float getHeight(){
    return getRadius()*2;
  }
  float getRadius(){
    return 10f;
  }
  float x(){
    return body.getPosition().x;
  }
  float y(){
    return body.getPosition().y;
  }
  float ang(){
    return body.getAngle();
  }

  public void paint(float alpha){
    float x = (x() * alpha) + (prevX * (1f - alpha));
    float y = (y() * alpha) + (prevY * (1f - alpha));
    float a = (ang() * alpha) + (prevA * (1f - alpha));
    myLayer.setTranslation(x, y);
    myLayer.setRotation(a);

    float angle = (game.time() + game.UPDATE_RATE*alpha) * (float) Math.PI / 1000;
  }

  public void update(float delta){
    prevX = x();
    prevY = y();
    prevA = ang();
  }
  public void destroy(){
    // remove graphics
    this.game.removeLayer(this.myLayer);
    // remove physics body
    this.game.physicsWorld().destroyBody(this.body);
  }

  boolean virusContact = false;
  public void handleCollision(Fixture me, Fixture other){
    if(other.m_userData instanceof Virus && !virusContact){
      virusContact = true;
      Virus v = (Virus) other.m_userData;
      //System.out.println("Collided with virus!");
      //game.successLevelOne();
    }
  }      
}
