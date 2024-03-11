package com.example.tetris;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Tetris extends Application {
    public static final int MOVE = 25; //distance we move the rectangles
    public static final int SIZE = 25;
    public static int XMAX = SIZE * 12;
    public static int YMAX = SIZE * 24;
    public static int[][] MESH = new int[XMAX / SIZE][YMAX / SIZE];
    private static Pane group = new Pane();
    private static Shape object;
    private static Scene scene = new Scene(group, XMAX + 150, YMAX);
    public static int score = 0;
    private static int top = 0;
    private static boolean game = true;
    private static Shape nextObj = Controller.makeRect();
    private static int linesNr = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {   // Start app
        for (int[] a : MESH) {
            Arrays.fill(a, 0);
        }
        Image image = null;
        try {
            image = new Image(Tetris.class.getResourceAsStream("/background_image.png"));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        BackgroundImage backgroundImage =
                new BackgroundImage(
                        image,
                        BackgroundRepeat.NO_REPEAT,  // repeat X
                        BackgroundRepeat.NO_REPEAT,  // repeat Y
                        BackgroundPosition.DEFAULT,   // position
                        new BackgroundSize(
                                100,   // width  = 100%
                                100,   // height = 100%
                                true,  // width is percentage
                                true,  // height is percentage
                                true,  // contain image within bounds
                                true   // cover all of Region content area
                        )
                );

        group.setBackground(new Background(backgroundImage));

        Line line = new Line(XMAX, 0, XMAX, YMAX);
        Text scoretext = new Text("Score: ");
        scoretext.setStyle("-fx-font: 20 arial;");
        scoretext.setY(50);
        scoretext.setX(XMAX + 5);
        Text level = new Text("Lines: ");
        level.setStyle("-fx-font: 20 arial;");
        level.setY(100);
        level.setX(XMAX + 5);
        group.getChildren().addAll(scoretext, line, level);

        Shape a = nextObj;
        group.getChildren().addAll(a.a, a.b, a.c, a.d);
        moveOnKeyPress(a);
        object = a;
        nextObj = Controller.makeRect();
        stage.setScene(scene);
        stage.setTitle("TETRIS");
        stage.show();

        Timer fall = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                Platform.runLater(new Runnable() {
                    public void run() {
                        if (object.a.getY() == 0 || object.b.getY() == 0 || object.c.getY() == 0
                                || object.d.getY() == 0)
                            top++;
                        else
                            top = 0;

                        if (top == 2) {
                            // GAME OVER
                            Text over = new Text("GAME OVER");
                            over.setFill(Color.RED);
                            over.setStyle("-fx-font: 70 arial;");
                            over.setY(250);
                            over.setX(10);
                            group.getChildren().add(over);
                            game = false;
                        }
                        // Exit
                        if (top == 15) {
                            System.exit(0);
                        }

                        if (game) {
                            MoveDown(object);
                            scoretext.setText("Score: " + Integer.toString(score));
                            level.setText("Lines: " + Integer.toString(linesNr));
                        }
                    }
                });
            }
        };
        fall.schedule(task, 0, 300);
    }

    private void moveOnKeyPress(Shape form) {     //Handle user input
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case RIGHT:
                        Controller.MoveRight(form);
                        break;
                    case DOWN:
                        MoveDown(form);
                        break;
                    case LEFT:
                        Controller.MoveLeft(form);
                        break;
                    case UP:
                        Controller.rotate(form);
                        break;
                }
            }
        });
    }
    private void RemoveRows(Pane pane) {  //Check if there are any completed lines and if yes delete them
        ArrayList<Node> rects = new ArrayList<Node>();
        ArrayList<Integer> lines = new ArrayList<Integer>();
        ArrayList<Node> newrects = new ArrayList<Node>();
        int full = 0;
        for (int i = 0; i < MESH[0].length; i++) {
            for (int j = 0; j < MESH.length; j++) {
                if (MESH[j][i] == 1)
                    full++;
            }
            if (full == MESH.length)
                lines.add(i);
            full = 0;
        }
        if (lines.size() > 0) {
            int multiplier=0;
            do {
                for (Node node : pane.getChildren()) {
                    if (node instanceof Rectangle)
                        rects.add(node);
                }
                linesNr++;
                multiplier++;
                for (Node node : rects) {
                    Rectangle a = (Rectangle) node;
                    if (a.getY() == lines.get(0) * SIZE) {
                        MESH[(int) a.getX() / SIZE][(int) a.getY() / SIZE] = 0;
                        pane.getChildren().remove(node);
                    } else
                        newrects.add(node);
                }

                for (Node node : newrects) {
                    Rectangle a = (Rectangle) node;
                    if (a.getY() < lines.get(0) * SIZE) {
                        MESH[(int) a.getX() / SIZE][(int) a.getY() / SIZE] = 0;
                        a.setY(a.getY() + SIZE);
                    }
                }
                lines.remove(0);
                rects.clear();
                newrects.clear();
                for (Node node : pane.getChildren()) {
                    if (node instanceof Rectangle)
                        rects.add(node);
                }
                for (Node node : rects) {
                    Rectangle a = (Rectangle) node;
                    try {
                        MESH[(int) a.getX() / SIZE][(int) a.getY() / SIZE] = 1;
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                }
                rects.clear();
            } while (!lines.isEmpty());
            score += multiplier*(50*multiplier);
        }
    }

    private void MoveDown(Shape form) { //Move shape down
        if (form.a.getY() == YMAX - SIZE || form.b.getY() == YMAX - SIZE || form.c.getY() == YMAX - SIZE
                || form.d.getY() == YMAX - SIZE || moveA(form) || moveB(form) || moveC(form) || moveD(form)) { //if piece is donw
            MESH[(int) form.a.getX() / SIZE][(int) form.a.getY() / SIZE] = 1;
            MESH[(int) form.b.getX() / SIZE][(int) form.b.getY() / SIZE] = 1;
            MESH[(int) form.c.getX() / SIZE][(int) form.c.getY() / SIZE] = 1;
            MESH[(int) form.d.getX() / SIZE][(int) form.d.getY() / SIZE] = 1;
            RemoveRows(group);

            Shape a = nextObj;  //create next shape
            nextObj = Controller.makeRect();
            object = a;
            group.getChildren().addAll(a.a, a.b, a.c, a.d);
            moveOnKeyPress(a);
        }

        if (form.a.getY() + MOVE < YMAX && form.b.getY() + MOVE < YMAX && form.c.getY() + MOVE < YMAX
                && form.d.getY() + MOVE < YMAX) {  //if piece can still move
            int movea = MESH[(int) form.a.getX() / SIZE][((int) form.a.getY() / SIZE) + 1];
            int moveb = MESH[(int) form.b.getX() / SIZE][((int) form.b.getY() / SIZE) + 1];
            int movec = MESH[(int) form.c.getX() / SIZE][((int) form.c.getY() / SIZE) + 1];
            int moved = MESH[(int) form.d.getX() / SIZE][((int) form.d.getY() / SIZE) + 1];
            if (movea == 0 && movea == moveb && moveb == movec && movec == moved) {
                form.a.setY(form.a.getY() + MOVE);
                form.b.setY(form.b.getY() + MOVE);
                form.c.setY(form.c.getY() + MOVE);
                form.d.setY(form.d.getY() + MOVE);
            }
        }
    }

    private boolean moveA(Shape form) {  //check if rectangle a can be moved
        return (MESH[(int) form.a.getX() / SIZE][((int) form.a.getY() / SIZE) + 1] == 1);
    }

    private boolean moveB(Shape form) {  //check if rectangle b can be moved
        return (MESH[(int) form.b.getX() / SIZE][((int) form.b.getY() / SIZE) + 1] == 1);
    }

    private boolean moveC(Shape form) {  //check if rectangle c can be moved
        return (MESH[(int) form.c.getX() / SIZE][((int) form.c.getY() / SIZE) + 1] == 1);
    }

    private boolean moveD(Shape form) {  //check if rectangle d can be moved
        return (MESH[(int) form.d.getX() / SIZE][((int) form.d.getY() / SIZE) + 1] == 1);
    }
}