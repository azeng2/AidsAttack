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
  private Camera(){
  }
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
