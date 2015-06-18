//Class to consolidate all zooming and translation of view during gameplay.
package edu.gmu.hivgame.core;

import static playn.core.PlayN.assets;
import static playn.core.PlayN.graphics;
import static playn.core.PlayN.log;

import java.math.*;

import playn.core.Game;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.GroupLayer;
import playn.core.Layer;
import static playn.core.PlayN.keyboard;
import playn.core.Keyboard;
import playn.core.Key;
import playn.core.util.Callback;
import playn.core.*;

public class Camera{
  AidsAttack game;
  public float screenUnitPerPhysUnit = 20.0f;
  public float screenPerPhysGoal = 20.0f;
  private float zoomStep = 0.1f;
  //translationX/Y in graphics coordinates
  //current translation of worldLayer
  //negative x means world layer has moved to the left, thus camera pans right
  //negative y means world layer has moved up, thus camera pans down
  public float translationX = 0.0f;
  public float translationY = 0.0f;
  //physical location of upper left corner
  public float currentPhysX;
  public float currentPhysY;
  public float tXGoal = 0.0f;
  public float tYGoal = 0.0f;
  private float tStep = 1f;
  private float tGoalStep = 10f;
  public boolean zoomingIn = false;
  public boolean zoomingOut = false;
  public final float halfWidth = graphics().width()/2f;
  public final float halfHeight = graphics().height()/2f;
  private Camera(){
  }
  public Camera(AidsAttack game){
    this.game = game;
    this.game.worldLayer.setScale(screenUnitPerPhysUnit);
    this.currentPhysX = 0.0f;
    this.currentPhysY = 0.0f;
  }
  public float physXToScreenX(float physX){
    return (physX*screenUnitPerPhysUnit) + translationX;
  }
  public float screenXToPhysX(float screenX){
    return (screenX - translationX) / screenUnitPerPhysUnit;
  }
  public float physYToScreenY(float physY){
    return (physY*screenUnitPerPhysUnit) + translationY;
  }
  public float screenYToPhysY(float screenY){
    return (screenY - translationY) / screenUnitPerPhysUnit;
  }
  public void zoomIn(){
    if(screenPerPhysGoal < 60f){
      screenPerPhysGoal += .1f;
    }
  }
  public void zoomOut(){
    if(screenPerPhysGoal > 1f){
      screenPerPhysGoal -= .1f;
    }
  }
  //to translate left is to push worldLayer right
  public void translateLeft(){
    tXGoal -=tGoalStep;
  }
  public void translateRight(){
    tXGoal +=tGoalStep;
  }
  //to translate up is to push worldLayer down
  public void translateUp(){
    tYGoal -=tGoalStep;
  }
  public void translateDown(){
    tYGoal +=tGoalStep;
  }
  void setCenter(float physX, float physY){
    float screenX = physXToScreenX(physX);
    float screenY = physYToScreenY(physY);
    this.tXGoal += halfWidth - screenX;
    this.tYGoal += halfHeight - screenY;
	int count = 0;
	while (count < 1000) {
	  updateTranslation();
	  count++;
	}
  }
  void trackVirus(){
    Virus v = this.game.theVirus;
    float vScreenXPos = physXToScreenX(v.x());
    float vScreenYPos = physYToScreenY(v.y());
    float centerX = this.translationX + halfWidth;
    float centerY = this.translationY + halfHeight;
    float vXFromCenter = vScreenXPos - centerX;
    float vYFromCenter = vScreenYPos - centerY;
    if(vXFromCenter > halfWidth){
      translateRight();
    }
    else if(vXFromCenter < -halfWidth){
      translateLeft();
    }
  }
  public void updateTranslation(){
    //trackVirus();
    //System.out.println("TranslationX is: "+translationX);
    //System.out.println("TranslationY is: "+translationY);
    float xDiff = Math.abs(tXGoal - translationX);
    if(xDiff > tStep){
      if(translationX < tXGoal){
        translationX += tStep;
      }
      else if(tXGoal < translationX){
        translationX -= tStep;
      }
    }
    float yDiff = Math.abs(tYGoal - translationY);
    if(yDiff > tStep){
      if(translationY < tYGoal){
        translationY += tStep;
      }
      else if(tYGoal < translationY){
        translationY -= tStep;
      }
    }
    game.worldLayer.setTranslation(translationX, translationY);
    //game.worldLayer.transform();
  }
  public void updateZoom(){
    if(zoomingIn){
      zoomIn();
    }
    else if(zoomingOut){
      zoomOut();
    }
    float diff = Math.abs(screenPerPhysGoal - screenUnitPerPhysUnit);
    if(diff > zoomStep){
      if(screenUnitPerPhysUnit < screenPerPhysGoal){
        screenUnitPerPhysUnit += zoomStep;
      }
      else if(screenPerPhysGoal < screenUnitPerPhysUnit){
        screenUnitPerPhysUnit -= zoomStep;
      }
    }
    game.worldLayer.setScale(screenUnitPerPhysUnit);
    //game.worldLayer.transform();
  }
  public void update(){
    updateZoom();
    updateTranslation();
    game.worldLayer.transform();
  }
}
