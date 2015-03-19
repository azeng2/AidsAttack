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

  public void handleCollision(Fixture me, Fixture other){
  }
}
