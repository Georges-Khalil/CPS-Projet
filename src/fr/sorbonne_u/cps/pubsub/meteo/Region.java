package fr.sorbonne_u.cps.pubsub.meteo;

import com.sun.corba.se.impl.io.TypeMismatchException;
import fr.sorbonne_u.cps.meteo.interfaces.PositionI;
import fr.sorbonne_u.cps.meteo.interfaces.RegionI;

/**
 * @author Jules Ragu
 */
public class Region implements RegionI {

  private final int x, y, width, height;

  public Region(int x, int y, int width, int height) {
    assert width > 0 && height > 0;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  @Override
  public boolean in(PositionI p) {
    if (!(p instanceof Position))
      throw new TypeMismatchException();
    Position pos = ((Position) p);
    return pos.getX() >= this.x && pos.getX() < this.x + this.width &&
           pos.getY() >= this.y && pos.getY() < this.y + this.height;
  }

  @Override
  public String toString() {
    return "Region{" + "x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + '}';
  }
}
