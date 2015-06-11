package edu.gmu.hivgame.core;

import static playn.core.PlayN.*;

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

import playn.core.CanvasImage;
import playn.core.Canvas;
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

public class ReverseTranscriptase implements CollisionHandler {
  // for calculating interpolation
  private float prevX, prevY, prevA;
  final float radius = 3f;
  final float diameter = radius*2f;
  private Body body;
  private Fixture myBodyFixture;
  private ImageLayer myLayer;
  AidsAttack game;
  LevelTwo level;

  private ReverseTranscriptase(){}
  public static ReverseTranscriptase make(AidsAttack game, Level level, float x, float y, float ang){
    ReverseTranscriptase rt = new ReverseTranscriptase();
    rt.game = game;
    rt.initPhysicsBody(level.physicsWorld(), x, y, ang);
    rt.drawRTImage();

    GroupLayer testLayer = graphics().createGroupLayer();
    testLayer.setDepth(7f);
    graphics().rootLayer().add(testLayer);
    System.out.println("added testlayer");

    // Choose one of the following to add the image. worldLayer is not displaying at all.
    //testLayer.add(rt.myLayer);
    graphics().rootLayer().add(rt.myLayer);
    //level.addLayer(rt.myLayer);

    rt.level = (LevelTwo) level;
    rt.prevX = rt.x(); rt.prevY = rt.y(); rt.prevA = rt.ang();
    return rt;
  }

  void initPhysicsBody(World world, float x, float y, float angle) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.DYNAMIC;
    bodyDef.position = new Vec2(x, y);
    bodyDef.angle = angle;
    bodyDef.linearDamping = 1.0f;

    this.body = world.createBody(bodyDef);
    body.setSleepingAllowed(false);

    CircleShape shape = new CircleShape();
    shape.m_radius = radius;
    shape.m_p.set(0.0f, 0.0f);

    FixtureDef fd = new FixtureDef();
    fd.shape = shape;
    fd.friction = 0.1f;
    fd.restitution = 0.4f;
    fd.density = 1.0f;
    this.myBodyFixture = body.createFixture(fd);
    this.myBodyFixture.m_userData = this;
  }

  private void drawRTImage(){
    float imageSize = 100;
    CanvasImage image = graphics().createImage(imageSize, imageSize);
    Canvas canvas = image.canvas();
    canvas.setFillColor(0xff050505);
    canvas.fillCircle(image.width()/2f, image.height()/2f, imageSize/2f);
    this.myLayer = graphics().createImageLayer(image);
    myLayer.setOrigin(image.width()/2f, image.height()/2f);
    myLayer.setTranslation(x(), y());
    myLayer.setRotation(ang());
    myLayer.setDepth(6);
  }

  public void handleCollision(Fixture me, Fixture other){
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
  void update(int delta){
    prevX = x();
    prevY = y();
    prevA = ang();
  }
  void paint(float alpha){
    float x = (x() * alpha) + (prevX * (1f - alpha));
    float y = (y() * alpha) + (prevY * (1f - alpha));
    float a = (ang() * alpha) + (prevA * (1f - alpha));
    myLayer.setTranslation(x, y);
    myLayer.setRotation(a);
  }
}
