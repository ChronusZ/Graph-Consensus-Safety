package com.ripple;

import com.ripple.data.Arrow;
import com.ripple.data.Graph;
import com.ripple.data.Node;
import com.ripple.data.Vector;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.BoundsAccessor;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;


public class Main extends Application {
    public static Node currentNode;
    public static Graph graph;
    private Vector mousePrev;
    public static float width = 960, height = 640;
    private static Button updateButton;
    private static Slider lengthExponentSlider;
    private static Text lengthExponentText;
    private static Rectangle menuBox, innerMenuBox, bg;

    private Group vertexGroup, edgeGroup, textGroup;

        @Override
        public void start(Stage stage) {
            vertexGroup = new Group();
            edgeGroup = new Group();
            textGroup = new Group();

            graph = constructGraph("011 101 110", vertexGroup, edgeGroup);

            setUpMenu();
            bg = new Rectangle(0,0,width,height);
            bg.setFill(Color.BEIGE);

            Pane root = new Pane();

            root.getChildren().addAll(bg, edgeGroup, vertexGroup, textGroup, menuBox, innerMenuBox, updateButton, lengthExponentSlider, lengthExponentText);

            Scene scene = new Scene(root, width, height, Color.BEIGE);

            stage.setTitle("Graph Consensus");
            stage.setScene(scene);
            stage.getIcons().add(new Image("file:icon.png"));
            stage.show();
            updateButton.setLayoutX(width - 100 - updateButton.getWidth() / 2);
            lengthExponentText.setLayoutX(width - 100 - lengthExponentText.getLayoutBounds().getWidth() / 2);
            lengthExponentSlider.setLayoutX(width - 100 - lengthExponentSlider.getWidth() / 2);


            bg.setOnMouseDragged(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent me) {
                    Vector mouseDelta = new Vector ((float) me.getX(), (float) me.getY()).difference(mousePrev);
                    mousePrev = new Vector ((float) me.getX(), (float) me.getY());

                    for (Node node : graph.nodes) {
                        node.shiftPos(mouseDelta.negate());
                    }
                }
            });
            bg.setOnMousePressed(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent me) {
                    mousePrev = new Vector ((float) me.getX(), (float) me.getY());
                }
            });
            bg.setOnMouseClicked(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent me) {
                    if (me.isShiftDown()) {
                        if (me.getButton() == MouseButton.PRIMARY) {
                            if (graph.edgeQueue == null)
                                graph.addNode (new Vector((float) me.getX(), (float) me.getY()), vertexGroup, edgeGroup, textGroup);
                            else
                                graph.edgeQueue = null;
                        }
                    }
                }
            });
            scene.setOnMouseMoved(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent me) {
                    if (graph.edgeQueue != null) {
                        graph.edgeAlign.out = new Vector((float) me.getX(), (float) me.getY());
                        graph.edgeAlign.updatePosition();
                    }
                }
            });
        }

        private void setUpMenu ( ) {
            DropShadow dropShadow = new DropShadow();
            dropShadow.setRadius(5.0);
            dropShadow.setOffsetX(3.0);
            dropShadow.setOffsetY(3.0);
            dropShadow.setColor(Color.color(0.4, 0.5, 0.5));

            menuBox = new Rectangle(145,165);
            menuBox.setLayoutX(width - 172.5);
            menuBox.setLayoutY(25);
            menuBox.setEffect(dropShadow);
            menuBox.setFill(new Color (0.6, 0.6, 0.6, 1));
            innerMenuBox = new Rectangle(140,80);
            innerMenuBox.setLayoutX(width - 170);
            innerMenuBox.setLayoutY(105);
            innerMenuBox.setEffect(dropShadow);
            innerMenuBox.setFill(new Color (0.7, 0.7, 0.7, 1));

            updateButton = new Button("Compute Safety");
            updateButton.setLayoutY(50);
            updateButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    graph.updateSafety();
                }
            });

            lengthExponentText = new Text("Length Effect");
            lengthExponentText.setLayoutY(130);

            lengthExponentSlider = new Slider(0, 3, 1);
            lengthExponentSlider.setOrientation(Orientation.HORIZONTAL);
            lengthExponentSlider.setLayoutY(140);
            lengthExponentSlider.setShowTickLabels(true);
            lengthExponentSlider.setShowTickMarks(true);
            lengthExponentSlider.setMajorTickUnit(1);
            lengthExponentSlider.setMinorTickCount(5);

        }

        public static float getLengthExponent( ) {
            return (float) lengthExponentSlider.getValue();
        }

        private Graph constructGraph (String array, Group vertexGroup, Group edgeGroup) {
            String[] rows = array.split(" ");
            boolean[][] out = new boolean[rows.length][rows.length];

            for (int i = 0; i < rows.length; i++) {
                for (int j = 0; j < rows.length; j++) {
                    out[i][j] = (rows[i].charAt(j) == '1');
                }
            }

            return new Graph(out, rows.length, vertexGroup, edgeGroup, textGroup);
        }

        public static void main(String[] args) {
            launch(args);
        }
    }