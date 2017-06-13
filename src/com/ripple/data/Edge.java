package com.ripple.data;

import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;

import java.awt.*;

/**
 * Created by Ethan MacBrough on 2017-06-11.
 */
public class Edge {
    private Node in, out;
    protected Group lineGroup;
    protected Line body, arrowLeft, arrowRight;
    private Timeline activate, deactivate;
    protected float optimalLength;
    private Graph graph;

    public Edge (Graph graph, Group edgeGroup, Node in, Node out) {
        this.in = in;
        this.out = out;
        this.graph = graph;

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

        constructAnimations ( );
    }

    public boolean compareVertices (Edge edge) {
        return ((in == edge.getIn() && out == edge.getOut()) || (in == edge.getOut() && out == edge.getIn()));
    }

    protected void activate ( ) {
        activate.play();
    }

    protected void deactivate ( ) {
        deactivate.play();
    }

    public Node getOut ( ) {
        return out;
    }

    public Node getIn ( ) {
        return in;
    }

    protected void updatePosition ( ) {
        if (graph.stabilizer == in || graph.stabilizer == out) optimalLength = 200;
        else optimalLength = 100;

        double dist = in.pos.distance(out.pos);
        double difx = (out.pos.x - in.pos.x) / dist;
        double dify = (out.pos.y - in.pos.y) / dist;
        body.setStartX(in.pos.x + difx * in.body.getRadius()); body.setStartY(in.pos.y + dify * in.body.getRadius());
        body.setEndX(out.pos.x - difx * out.body.getRadius()); body.setEndY(out.pos.y - dify * out.body.getRadius());
        double midx = out.pos.x * 0.4 + in.pos.x * 0.6;
        double midy = out.pos.y * 0.4 + in.pos.y * 0.6;
        double leftWingx = midx - difx * 0.05 * dist - dify * 0.025 * dist;
        double leftWingy = midy - dify * 0.05 * dist + difx * 0.025 * dist;
        double rightWingx = midx - difx * 0.05 * dist + dify * 0.025 * dist;
        double rightWingy = midy - dify * 0.05 * dist - difx * 0.025 * dist;
        arrowLeft.setStartX(midx); arrowLeft.setStartY(midy);
        arrowLeft.setEndX(leftWingx); arrowLeft.setEndY(leftWingy);
        arrowRight.setStartX(midx); arrowRight.setStartY(midy);
        arrowRight.setEndX(rightWingx); arrowRight.setEndY(rightWingy);
    }

    private void constructAnimations ( ) {
        activate = new Timeline();
        deactivate = new Timeline();
        activate.getKeyFrames().add(
                new KeyFrame(new Duration(200),
                        new KeyValue(lineGroup.opacityProperty(), 1, new SmoothInterpolator()),
                        new KeyValue(body.strokeWidthProperty(), 4, new SmoothInterpolator()),
                        new KeyValue(arrowLeft.strokeWidthProperty(), 4, new SmoothInterpolator()),
                        new KeyValue(arrowRight.strokeWidthProperty(), 4, new SmoothInterpolator())
                )
        );

        deactivate.getKeyFrames().add(
                new KeyFrame(new Duration(200),
                        new KeyValue(lineGroup.opacityProperty(), 0.5, new SmoothInterpolator()),
                        new KeyValue(body.strokeWidthProperty(), 2, new SmoothInterpolator()),
                        new KeyValue(arrowLeft.strokeWidthProperty(), 2, new SmoothInterpolator()),
                        new KeyValue(arrowRight.strokeWidthProperty(), 2, new SmoothInterpolator())
                )
        );
    }
}
