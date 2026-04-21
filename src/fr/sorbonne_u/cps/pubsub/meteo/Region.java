package fr.sorbonne_u.cps.pubsub.meteo;

import fr.sorbonne_u.cps.meteo.interfaces.PositionI;
import fr.sorbonne_u.cps.meteo.interfaces.RegionI;

/**
 * @author Jules Ragu, Côme Lance-Perlick and Georges Khalil
 */
public class Region implements RegionI {

    private final int x, y, width, height;

    public Region(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean in(PositionI p) {
        if (!(p instanceof Position))
            throw new IllegalArgumentException();
        return ((Position) p).isIn(this.x, this.y, this.width, this.height);
    }

    @Override
    public String toString() {
        return "Region{" + "x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + '}';
    }
}
