package com.ripple.data;

import com.ripple.Main;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Node {
    private List<Edge> inNeighbors, outNeighbors;
    protected Vector pos;
    private Vector vel;
    protected boolean infected, activated;
    private boolean clickLock;

    private Graph graph;
    protected Circle body;
    private Timeline activate, deactivate, infect, deinfect;

    protected Text healthText;
    protected Rectangle healthBox;
    private Color health;

    private long timePrev;

    public Node (Graph graph, Group vertexGroup, Group edgeGroup, Group textGroup, Vector pos) {
        health = new Color (0,1,0,1);
        this.graph = graph;
        infected = false;
        clickLock = false;
        this.pos = pos;
        vel = new Vector(0, 0);

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(3.0);
        dropShadow.setColor(Color.color(0.4, 0.5, 0.5));
        healthBox = new Rectangle(0,0);
        healthBox.setEffect(dropShadow);
        healthBox.setFill(new Color (0.9, 0.9, 0.9, 1));
        healthBox.setMouseTransparent(true);
        healthBox.setVisible(false);

        healthText = new Text ("");
        healthText.setMouseTransparent(true);
        healthText.setVisible(false);

        body = new Circle (pos.x, pos.y, 10);
        body.setFill (Color.PINK);
        body.setOpacity (0.75);
        body.setStroke (Color.BLACK);
        body.setStrokeWidth (2);
        body.setStrokeType(StrokeType.INSIDE);
        body.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (graph.stabilizer == null) {
                    graph.stabilizer = Node.this;
                    activate();
                }
                if (healthText.getText() != "") {
                    healthBox.setVisible(true);
                    healthText.setVisible(true);
                }
            }
        });
        body.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (!activated && (graph.stabilizer == Node.this || graph.stabilizer == null)) {
                    graph.stabilizer = null;
                    deactivate();
                }
                healthBox.setVisible(false);
                healthText.setVisible(false);
            }
        });
        body.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (me.isShiftDown()) {
                    if (me.getButton() == MouseButton.SECONDARY) {
                        if (graph.nodes.size() > 1) {
                            graph.removeNode (Node.this, vertexGroup, edgeGroup, textGroup);
                        }
                    }
                    if (me.getButton() == MouseButton.PRIMARY) {
                        if (graph.edgeQueue == null) {
                            graph.edgeQueue = Node.this;
                            health = Color.PINK;
                        } else {
                            if (graph.edgeQueue != Node.this) {
                                graph.addEdge(graph.edgeQueue, Node.this, edgeGroup);
                                health = Color.PINK;
                                graph.edgeQueue = null;
                            }
                        }
                    }
                } else {
                    if (me.getButton() == MouseButton.SECONDARY && !activated) {
                        infect();
                    }
                    if (me.getButton() == MouseButton.PRIMARY) {
                        graph.activateNode(Node.this);
                        if (infected) infect();
                    }
                }
            }
        });

        vertexGroup.getChildren().add(body);
        textGroup.getChildren().addAll(healthBox, healthText);

        inNeighbors = new ArrayList<>( );
        outNeighbors = new ArrayList<>( );
        constructAnimations();

        timePrev = System.nanoTime();
    }

    public void infect ( ) {
        if (!infected) infect.play(); else deinfect.play();

        infected = !infected;
    }

    public List<List<Edge>> findPaths (List<Edge> initialPath, Node target, int maxLength) {
        List<List<Edge>> paths = new ArrayList<>( );
        if (maxLength <= 0) return paths;

        for (Edge edge : outNeighbors) {
            if (initialPath.stream().filter(e -> e.compareVertices(edge)).count() > 0) continue;
            List<Edge> newPath = new ArrayList<>(initialPath);
            newPath.add(edge);
            if (edge.getOut() == target) {
                paths.add(newPath);
            } else {
                paths.addAll(edge.getOut().findPaths(newPath, target, maxLength - 1));
            }
        }

        return paths;
    }

    public float computeSafety (Node target, List<Node> infected, int maxLength, float lengthExponent) {
        if (target == this) return 0;

        List<List<Edge>> paths = findPaths(new ArrayList<>( ), target, maxLength);
        List<List<Edge>> pathsCulled = paths.stream().filter(p -> {
            List<Node> nodeList = new ArrayList<>();
            for (Edge edge : p) {
                nodeList.add(edge.getOut());
            }
            for (List<Edge> path : paths) {
                if (path.size() >= p.size()) continue;
                if (path.stream().filter(e -> nodeList.contains(e.getOut())).count() == path.size()) return false;
            }
            return true;
        }).collect(Collectors.toList());
        List<List<Edge>> infectedPaths = pathsCulled.stream().filter(p -> p.stream().filter(e -> infected.contains(e.getOut())).count() > 0).collect(Collectors.toList());

        System.out.println ("infected paths: " + infectedPaths.size() + "; total paths: " + pathsCulled.size());

        float numerator = 0;
        for (List<Edge> path : infectedPaths) {
            numerator += Math.pow (path.size(), lengthExponent);
        }

        float denomerator = 0;
        for (List<Edge> path : pathsCulled) {
            denomerator += Math.pow (path.size(), lengthExponent);
        }

        return numerator / denomerator;
    }

    public void updatePosition (List<Edge> edges) {
        float timeStep = (float) ((System.nanoTime() - timePrev) * 0.00000006); // time step in frames, where 60 frames = 1 second.
        timePrev = System.nanoTime();

        Vector force = new Vector(0, 0);

        for (Edge edge : inNeighbors) {
            Vector diff = edge.getIn().getPos().difference(pos);
            double magnitude = (edge.optimalLength - diff.length( )) * 0.01;
            //Vector relVel = vel.difference(edge.getIn().vel).scale(-0.5f);
            force = force.add ((diff.normalize()).scale ((float) magnitude));
            //System.out.println (relVel.x + ", " + relVel.y + ": " + relVel.length());
        }

        for (Edge edge : outNeighbors) {
            Vector diff = edge.getOut().getPos().difference(pos);
            double magnitude = (edge.optimalLength - diff.length( )) * 0.01;
            //Vector relVel = vel.difference(edge.getIn().vel).scale(-0.5f);
            force = force.add ((diff.normalize()).scale ((float) magnitude));
            //System.out.println (relVel.x + ", " + relVel.y + ": " + relVel.length());
        }

        //vel = vel.add(force.scale(timeStep));
        pos = pos.add(force.scale(timeStep));
        body.setCenterX(pos.x);
        body.setCenterY(pos.y);
        body.setFill(health);
        updateHealthText();
    }

    public void updateStatic ( ) {
        timePrev = System.nanoTime();
        body.setCenterX(pos.x);
        body.setCenterY(pos.y);
        body.setFill(Color.GOLD);
        updateHealthText();
    }

    private void updateHealthText ( ) {
        healthBox.setX(pos.x - 14);
        healthBox.setY(pos.y - 25);
        healthBox.setWidth(healthText.getLayoutBounds().getWidth() + 8);
        healthBox.setHeight(healthText.getLayoutBounds().getHeight());
        healthText.setX(pos.x - 10);
        healthText.setY(pos.y - 10);
    }

    public void updateColor (Node target, List<Node> infected, int maxLength, float lengthExponent) {
        if (this.infected || target == this) return;

        float safety = 1;
        if (graph.stabilizer != null) {
            safety = computeSafety(target, infected, maxLength, lengthExponent);
        }

        if (Float.isNaN(safety)) health = new Color (0,0,1,1);
        else health = new Color(Math.min(safety * 2, 1), Math.min((1 - safety) * 2, 1), 0, 1);

        body.setFill(health);
        healthText.setText(((int) safety) + "." + ("" + (1000 + (((int) (safety * 1000)) % 1000))).substring(1));
    }

    public void activate ( ) {
        activate.play();
        for (Edge edge : inNeighbors) {
            edge.activate();
        }
    }

    public void deactivate ( ) {
        deactivate.play();
        for (Edge edge : inNeighbors) {
            edge.deactivate();
        }
    }

    public Edge addEdge (Node in, Group edgeGroup) {
        Edge edge = new Edge (graph, edgeGroup, in, this);
        inNeighbors.add(edge);
        in.outNeighbors.add(edge);
        return edge;
    }

    public void removeEdge (Node in) {
        inNeighbors.removeIf(edge -> edge.getIn() == in);
    }

    public boolean isNeighbor (Node in) {
        return (inNeighbors.stream().filter(edge -> edge.getIn() == in).count() > 0);
    }

    public Vector getPos ( ) {
        return pos;
    }
    protected void setPos (Vector pos) {
        this.pos = pos;
    }
    public void shiftPos (Vector pos) { this.pos = this.pos.add(pos); }

    private void constructAnimations ( ) {
        activate = new Timeline();
        deactivate = new Timeline();
        infect = new Timeline();
        deinfect = new Timeline();

        activate.getKeyFrames().add(
                new KeyFrame(new Duration(200),
                        new KeyValue(body.opacityProperty(), 1, new SmoothInterpolator()),
                        new KeyValue(body.radiusProperty(), 20, new SmoothInterpolator())
                )
        );

        deactivate.getKeyFrames().add(
                new KeyFrame(new Duration(200),
                        new KeyValue(body.opacityProperty(), 0.75, new SmoothInterpolator()),
                        new KeyValue(body.radiusProperty(), 10, new SmoothInterpolator())
                )
        );

        infect.getKeyFrames().add(
                new KeyFrame(new Duration(500),
                        new KeyValue(body.strokeWidthProperty(), 10, new SmoothInterpolator())
                )
        );

        deinfect.getKeyFrames().add(
                new KeyFrame(new Duration(500),
                        new KeyValue(body.strokeWidthProperty(), 2, new SmoothInterpolator())
                )
        );
    }
}
