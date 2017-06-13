package com.ripple.data;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.shape.Line;
import javafx.util.Duration;

/**
 * Created by Ethan MacBrough on 2017-06-12.
 */
public class Arrow {
    public Vector in, out;
    public Group lineGroup;
    protected Line body, arrowLeft, arrowRight;

    public Arrow (Group edgeGroup, Vector in, Vector out) {
        this.in = in;
        this.out = out;

        body = new Line ( );
        arrowLeft = new Line ( );
        arrowRight = new Line ( );
        updatePosition();

        lineGroup = new Group ( );
        lineGroup.getChildren().addAll(body, arrowLeft, arrowRight);

        lineGroup.setOpacity(0.5);
        body.setStrokeWidth(2);
        arrowLeft.setStrokeWidth(2);
        arrowRight.setStrokeWidth(2);
        edgeGroup.getChildren().add (lineGroup);
    }

    public void updatePosition ( ) {
        double dist = in.distance(out);
        double difx = (out.x - in.x) / dist;
        double dify = (out.y - in.y) / dist;
        body.setStartX(in.x); body.setStartY(in.y);
        body.setEndX(out.x); body.setEndY(out.y);
        double midx = out.x * 0.4 + in.x * 0.6;
        double midy = out.y * 0.4 + in.y * 0.6;
        double leftWingx = midx - difx * 0.05 * dist - dify * 0.025 * dist;
        double leftWingy = midy - dify * 0.05 * dist + difx * 0.025 * dist;
        double rightWingx = midx - difx * 0.05 * dist + dify * 0.025 * dist;
        double rightWingy = midy - dify * 0.05 * dist - difx * 0.025 * dist;
        arrowLeft.setStartX(midx); arrowLeft.setStartY(midy);
        arrowLeft.setEndX(leftWingx); arrowLeft.setEndY(leftWingy);
        arrowRight.setStartX(midx); arrowRight.setStartY(midy);
        arrowRight.setEndX(rightWingx); arrowRight.setEndY(rightWingy);
    }
}
