package com.ripple.data;

import javafx.animation.Interpolator;

/**
 * Created by Ethan MacBrough on 2017-06-11.
 */
public class SmoothInterpolator extends Interpolator {
    @Override
    protected double curve(double t) {
        return 3*t*t-2*t*t*t;
    }
}
