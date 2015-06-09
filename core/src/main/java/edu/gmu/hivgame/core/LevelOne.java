package edu.gmu.hivgame.core;

import static playn.core.PlayN.*;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
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

//First level of game: Cell approach
public class LevelOne extends Level{
  boolean gameOver;
  boolean success;
  boolean attractingVirus;
  Virus theVirus;
  Cell theCell;
  Antibody[] antibodies;
  Vec2 virusScreenTarget = new Vec2();
  float minLength = 1f;
  float forceScale = 10f;

  static LevelOne make(AidsAttack game){
    LevelOne lv = new LevelOne();
    lv.game = game;
    return lv;
  }

  void initLevel(){
    super.initLevel();
    gameOver = false;
    success = false;

    //create the Virus object
    this.theVirus = Virus.make(this.game, 5f, 0f, .2f);

    //create the Cell object
    this.theCell = Cell.make(this.game, 30f, 30f, .2f);

    //Random to distribute Antibodies on screen
    Random r = new Random(12345);
    antibodies = new Antibody[6];
    for(int i=0; i<antibodies.length; i++){
      float x = r.nextFloat();
      float y = r.nextFloat(); 
      Antibody a = Antibody.make(this.game, x*50, y*50, .2f);
      antibodies[i] = a;
    }

    pointer().setListener(new Pointer.Adapter() {
	    @Override
      public void onPointerStart(Pointer.Event event) {
        Point p = new Point(event.x(), event.y());
        System.out.printf("Point p is at: %f, %f.\n",p.x(), p.y());
        Layer hit = game.buttonLayer.hitTest(p);
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
        Layer hit = game.buttonLayer.hitTest(p);
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
  }

  String levelName() {
    return "Level One";
  }
  void update(int delta){}
  void paint(float alpha){}
}
