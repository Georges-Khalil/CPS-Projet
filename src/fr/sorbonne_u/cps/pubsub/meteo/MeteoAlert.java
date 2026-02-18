package fr.sorbonne_u.cps.pubsub.meteo;

import fr.sorbonne_u.cps.meteo.interfaces.MeteoAlertI;
import fr.sorbonne_u.cps.meteo.interfaces.RegionI;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class MeteoAlert implements MeteoAlertI {

  public enum Level implements LevelI {
    GREEN, YELLOW, ORANGE, RED, SCARLET
  }

  private final AlertTypeI type;
  private final LevelI level;
  private final RegionI[] regions;
  private final Instant start_time;
  private final Duration duration;

  public MeteoAlert(AlertTypeI type, LevelI level, RegionI[] regions, Instant start_time, Duration duration) {
    if (type == null || level == null || start_time == null || duration == null || regions == null || regions.length == 0)
      throw new IllegalArgumentException();

    this.type = type;
    this.level = level;
    this.regions = regions;
    this.start_time = start_time;
    this.duration = duration;
  }

  @Override
  public AlertTypeI getAlertType() {
    return this.type;
  }

  @Override
  public LevelI getLevel() {
    return this.level;
  }

  @Override
  public RegionI[] getRegions() {
    return this.regions.clone();
  }

  @Override
  public Instant getStartTime() {
    return this.start_time;
  }

  @Override
  public Duration getDuration() {
    return this.duration;
  }

  @Override
  public String toString() {
    return "MeteoAlert{" + "type=" + type + ", level=" + level + ", regions=" + Arrays.toString(regions) + ", start_time=" + start_time + ", duration=" + duration + '}';
  }
}
