package com.ripple.data;

import com.ripple.Main;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.util.Duration;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.stream.Collectors;

public class Graph {
    public List<Node> nodes;
    public List<Edge> edges;
    private Timeline update;
    protected Node stabilizer;
    public Node edgeQueue;
    public Arrow edgeAlign;

    public Graph (boolean[][] adjacencyMatrix, int nodeCount, Group vertexGroup, Group edgeGroup, Group textGroup) {
        Random random = new Random( );
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        edgeAlign = new Arrow(edgeGroup, new Vector(0, 0), new Vector(0, 0));

        for (int i = 0; i < nodeCount; i++) {
            nodes.add(new Node (this, vertexGroup, edgeGroup, textGroup, new Vector(Main.width / 2 + (random.nextFloat() - 0.5f) * 5, Main.height / 2 + (random.nextFloat() - 0.5f) * 5)));
        }

        stabilizer = null;

        for (int i = 0; i < nodeCount; i++) {
            for (int j = 0; j < nodeCount; j++) {
                if (adjacencyMatrix[i][j]) {
                    edges.add(nodes.get(i).addEdge(nodes.get(j), edgeGroup));
                }
            }
        }

        update = new Timeline( );
        update.setCycleCount(Animation.INDEFINITE);
        update.getKeyFrames().add(new KeyFrame(new Duration(25), event -> {
            updatePositions();
            if (edgeQueue != null) {
                edgeAlign.in = edgeQueue.getPos();
                edgeAlign.updatePosition();
                edgeAlign.lineGroup.setVisible(true);
            } else {
                edgeAlign.lineGroup.setVisible(false);
            }
        }));
        update.play();
    }

    public void activateNode (Node activatedNode) {
        for (Node node : nodes) {
            if (node.activated && node != activatedNode) {
                node.activated = false;
                node.deactivate();
            }
        }

        if (activatedNode.activated) {
            stabilizer = null;
            activatedNode.activated = false;
            activatedNode.deactivate();
        } else {
            stabilizer = activatedNode;
            activatedNode.activated = true;
            activatedNode.activate();
        }
    }

    private void updatePositions ( ) {
        for (Node node : nodes) {
            if (node != stabilizer) node.updatePosition(edges);
            else node.updateStatic();
        }

        for (Edge edge : edges) {
            edge.updatePosition();
        }
    }

    public void updateSafety ( ) {
        List<Node> infected = new ArrayList<>();
        for (Node node : nodes) {
            if (node.infected) infected.add(node);
        }

        for (Node node : nodes) {
            node.updateColor(stabilizer, infected, 10, -Main.getLengthExponent());
        }
    }




    public void addNode (Vector pos, Group vertexGroup, Group edgeGroup, Group textGroup) {
        nodes.add(new Node (this,vertexGroup, edgeGroup, textGroup, pos));
    }

    public void removeNode (Node node, Group vertexGroup, Group edgeGroup, Group textGroup) {
        nodes.remove(node);
        vertexGroup.getChildren().remove(node.body);
        textGroup.getChildren().remove(node.healthBox);
        textGroup.getChildren().remove(node.healthText);

        List<Edge> neighbors = edges.stream().filter(e -> e.getOut() == node || e.getIn() == node).collect(Collectors.toList());
        for (Edge edge : neighbors) {
            edges.remove(edge);
            edgeGroup.getChildren().remove(edge.lineGroup);
        }
    }

    public void addEdge (Node in, Node out, Group edgeGroup) {
        if (edges.stream().filter(e -> e.getIn() == in && e.getOut() == out).count() == 0)
            edges.add(out.addEdge(in, edgeGroup));
    }
}
