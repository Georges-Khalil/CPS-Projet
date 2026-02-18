package fr.sorbonne_u.cps.pubsub.meteo;

import com.sun.corba.se.impl.io.TypeMismatchException;
import fr.sorbonne_u.cps.meteo.interfaces.PositionI;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class Position implements PositionI {

  public final int x, y;

  public Position(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(PositionI p) {
    if (p instanceof Position)
      return ((Position)p).getX() == x && ((Position)p).getX() == y;
    throw new TypeMismatchException();
  }

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }

  @Override
  public String toString() {
    return "Position{" + "x=" + x +", y=" + y +'}';
  }
}
