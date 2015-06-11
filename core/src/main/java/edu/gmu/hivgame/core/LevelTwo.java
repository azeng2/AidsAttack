package edu.gmu.hivgame.core;

import static playn.core.PlayN.*;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.ContactEdge;

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

//Level Two: Reverse Transcriptase
public class LevelTwo extends Level{
  ReverseTranscriptase theRT;

  static LevelTwo make(AidsAttack game){
    LevelTwo lt = new LevelTwo();
    lt.game = game;
    return lt;
  }

  @Override
  void initLevel(Camera camera){
    super.initLevel(camera);
    gameOver = false;
    success = false;

    // message to display when moving to level two.
    // this is added directly to graphics().rootLayer(), like the gameOver message,
    // so that it is not affected by scaling of worldLayer
    CanvasImage image = graphics().createImage(200,200);
    Canvas canvas = image.canvas();
    canvas.setFillColor(0xff050505);
    canvas.drawText("Level Two!",100,100);
    ImageLayer welcomeLayer = graphics().createImageLayer(image);
    welcomeLayer.setDepth(3f);
    graphics().rootLayer().add(welcomeLayer);
    System.out.println("welcomelayer added");
    System.out.println("RootLayer size is: "+graphics().rootLayer().size());

    // make the ReverseTranscriptase
    // This should create the image as well as the physics body
    this.theRT = ReverseTranscriptase.make(game, this, 100f, 100f, 0f);

    // hook up pointer listener
    // currently just for testing of worldLayer
    pointer().setListener(new Pointer.Adapter() {
      @Override
      public void onPointerStart(Pointer.Event event) {
        Point p = new Point(event.x(), event.y());
        Layer hit = worldLayer.hitTest(p);
        if(hit == null){
          System.out.println("no worldLayer here.");
        }
        else{
          System.out.println("worldLayer hit!");
        }
        hit = game.currentLevel.worldLayer.hitTest(p);
        if(hit != null){
          System.out.println("WorldLayer found through game.");
        }
      }
    });
  }
  void update(int delta, int time){
    theRT.update(delta);

    // the step delta is fixed so box2d isn't affected by framerate
    m_world.step(0.033f, 10, 10);
  }
  void paint(float alpha){
    theRT.paint(alpha);
  }
  String levelName(){
    return "Level Two";
  }
}
