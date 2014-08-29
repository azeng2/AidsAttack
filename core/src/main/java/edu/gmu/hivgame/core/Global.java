package edu.gmu.hivgame.core;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;


// Contains globally relevant data
class Global {

  // Global contact listener.  Intention is to have bodies be linked
  // to objects that can handle their own contact events and dispatch
  // to them when contacts begin and end
  public static ContactListener contactListener = 
    new ContactListener(){
      // Called when two fixtures begin to touch
      public void beginContact(Contact contact){
	Object a = contact.getFixtureA().getUserData();
	Object b = contact.getFixtureB().getUserData();
	if(a!=null && a instanceof ContactListener){
	  ((ContactListener) a).beginContact(contact);
	}
	else if(b!=null && b instanceof ContactListener){
	  ((ContactListener) b).beginContact(contact);
	}
      }

      // Called when two fixtures cease to touch.
      public void endContact(Contact contact){
	Object a = contact.getFixtureA().getUserData();
	Object b = contact.getFixtureB().getUserData();
	if(a!=null && a instanceof ContactListener){
	  ((ContactListener) a).endContact(contact);
	}
	else if(b!=null && b instanceof ContactListener){
	  ((ContactListener) b).endContact(contact);
	}
      }

      public void postSolve(Contact contact, ContactImpulse impulse){}
      public void preSolve(Contact contact, Manifold oldManifold){}
      
    };
}



