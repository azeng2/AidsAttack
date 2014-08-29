package edu.gmu.hivgame.java;

import playn.core.PlayN;
import playn.java.JavaPlatform;

import edu.gmu.hivgame.core.AidsAttack;

public class AidsAttackJava {

  public static void main(String[] args) {
    JavaPlatform.Config config = new JavaPlatform.Config();
    // use config to customize the Java platform, if needed
    JavaPlatform.register(config);
    PlayN.run(new AidsAttack());
  }
}
