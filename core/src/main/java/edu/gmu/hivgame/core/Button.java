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

//public enum ButtonFunction{
  //ZOOM_IN, ZOOM_OUT, RESET
//}
public class Button{
  public enum ButtonFunction{
    ZOOM_IN, ZOOM_OUT, RESET
  }
  //in screen units. All buttons have same width and height.
  private float width;
  private float height;
  private String label;
  AidsAttack game;
  //need an ImageLayer to hold the button itself
  //on ImageLayer, need pointer listener. Should call whatever the button does.
  ImageLayer buttonImage;
  private Button(){}
  public static Button make(AidsAttack game, float xPos, float yPos, String label){
    Button b = new Button();
    b.game = game;
    b.width = 50f;
    b.height = 25f;
    b.label = label;
    b.build(xPos, yPos);
    return b;
  }
  private void build(float xPos, float yPos){
    CanvasImage image = graphics().createImage(this.width, this.height);
    Canvas canvas = image.canvas();
    canvas.setStrokeWidth(2);
    canvas.setFillColor(0xffffff00);
    canvas.fillRect(0,0,this.width,this.height);
    canvas.setFillColor(0xffff0000);
    canvas.drawText(this.label,this.width/3f,this.height/2f);
    this.buttonImage = graphics().createImageLayer(image);
    this.buttonImage.setTranslation(xPos,yPos);
    game.addButton(this.buttonImage);
  }
}
