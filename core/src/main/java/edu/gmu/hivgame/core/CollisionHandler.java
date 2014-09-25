package edu.gmu.hivgame.core;

// Implemented by objects which want to handle collisions with other
// objects

import org.jbox2d.dynamics.Fixture;
public interface CollisionHandler{
  public void handleCollision(Fixture me, Fixture other);
}
