package edu.gmu.hivgame.android;

import playn.android.GameActivity;
import playn.core.PlayN;

import edu.gmu.hivgame.core.AidsAttack;

public class AidsAttackActivity extends GameActivity {

  @Override
  public void main(){
    PlayN.run(new AidsAttack());
  }
}
