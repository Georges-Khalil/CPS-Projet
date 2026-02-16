package fr.sorbonne_u.cps.pubsub.meteo;

import fr.sorbonne_u.cps.meteo.interfaces.PositionI;
import fr.sorbonne_u.cps.meteo.interfaces.WindDataI;

/**
 * @author Jules Ragu
 */
public class WindData implements WindDataI {

  private final PositionI position;
  private final double x, y;

  public WindData(final PositionI position, double x, double y) {
    this.position = position;
    this.x = x;
    this.y = y;
  }

  @Override
  public PositionI getPosition() {
    return this.position;
  }

  @Override
  public double xComponent() {
    return this.x;
  }

  @Override
  public double yComponent() {
    return this.y;
  }

  @Override
  public double force() {
    return Math.sqrt(this.x * this.x + this.y * this.y);
  }

  @Override
  public String toString() {
    return "WindData{position=" + this.position.toString() + ", x=" + this.x + ", y=" + this.y + "}";
  }
}
