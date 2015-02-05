//Class to consolidate all zooming and translation of view during gameplay.
package edu.gmu.hivgame.core;

import static playn.core.PlayN.*;

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

public class Camera{
  AidsAttack game;
  public static float screenUnitPerPhysUnit = 20.0f;
  public static float screenPerPhysGoal = 20.0f;
  private static float zoomStep = 0.1f;
  public static float translationX = 0.0f;
  public static float translationY = 0.0f;
  public static float tXGoal = 0.0f;
  public static float tYGoal = 0.0f;
  private static float tStep = 1f;
  private static float tGoalStep = 10f;
  private Camera(){
  }
  //should Camera just be static? Could make zoom and translation functions static.
  //would require passing in arguments to translation functions.
  public Camera(AidsAttack game){
    this.game = game;
  }
  public void zoomIn(){
    if(screenPerPhysGoal < 60f){
      screenPerPhysGoal += 1f;
    }
  }
  public void zoomOut(){
    if(screenPerPhysGoal > 1f){
      screenPerPhysGoal -= 1f;
    }
  }
  public void translateLeft(){
    tXGoal -=tGoalStep;
  }
  public void translateRight(){
    tXGoal +=tGoalStep;
  }
  public void translateUp(){
    tYGoal -=tGoalStep;
  }
  public void translateDown(){
    tYGoal +=tGoalStep;
  }
  public void updateTranslation(){
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
  }
  public void updateZoom(){
    float diff = Math.abs(screenPerPhysGoal - screenUnitPerPhysUnit);
    if(diff > zoomStep){
      if(screenUnitPerPhysUnit < screenPerPhysGoal){
        screenUnitPerPhysUnit += zoomStep;
      }
      else if(screenPerPhysGoal < screenUnitPerPhysUnit){
        screenUnitPerPhysUnit -= zoomStep;
      }
    }
  }
}
