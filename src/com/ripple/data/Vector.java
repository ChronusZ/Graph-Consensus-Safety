package com.ripple.data;

/**
 * Created by Ethan MacBrough on 2017-06-11.
 */
public class Vector {
    public float x, y;

    public Vector (float x, float y) {
        this.x = x;
        this.y = y;
    }

    public double distance (Vector v) {
        return Math.sqrt((v.y - y) * (v.y - y) + (v.x - x) * (v.x - x));
    }

    public double distanceSquared (Vector v) {
        return ((v.y - y) * (v.y - y) + (v.x - x) * (v.x - x));
    }

    public double length ( ) {
        return Math.sqrt(y * y + x * x);
    }

    public double lengthSquared (Vector v) {
        return (y * y + x * x);
    }

    public Vector add (Vector v) {
        return new Vector (x + v.x, y + v.y);
    }

    public Vector negate ( ) {
        return new Vector(-x, -y);
    }

    public Vector difference (Vector v) {
        return new Vector(v.x - x, v.y - y);
    }

    public Vector scale (float d) {
        return new Vector(d * x, d * y);
    }

    public Vector normalize ( ) {
        return this.scale (1 / ((float) this.length()));
    }
}
