package edu.gmu.hivgame.html;

import playn.core.PlayN;
import playn.html.HtmlGame;
import playn.html.HtmlPlatform;

import edu.gmu.hivgame.core.AidsAttack;

public class AidsAttackHtml extends HtmlGame {

  @Override
  public void start() {
    HtmlPlatform.Config config = new HtmlPlatform.Config();
    // use config to customize the HTML platform, if needed
    HtmlPlatform platform = HtmlPlatform.register(config);
    platform.assets().setPathPrefix("aidsattack/");
    PlayN.run(new AidsAttack());
  }
}
