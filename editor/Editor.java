package editor;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.shape.Rectangle;
import java.util.*;

import javafx.util.Duration;




public class Editor extends Application {
    private static  int WINDOW_WIDTH;
    private static  int WINDOW_HEIGHT;
    private Group root = new Group();
    Group textRoot = new Group();
    FastLinkedList textList = new FastLinkedList();
    private final Rectangle cursor;
    public int charHeight;
    int scrollAdjust;

    int currentLine;
    int maxLine;
    int cursorLine;
    private HashMap<Integer, FastLinkedList.Node> lineMap = new HashMap<Integer, FastLinkedList.Node>();



    public Editor() {
        //initilaize rectangle for cursor
        cursor = new Rectangle(0, 0);
        WINDOW_WIDTH = 500;
        WINDOW_HEIGHT = 500;
    }


    /** An EventHandler to handle keys that get pressed. */
    private class KeyEventHandler implements EventHandler<KeyEvent> {


        private static final int STARTING_FONT_SIZE = 20;
        private static final int STARTING_TEXT_POSITION_X = 0;
        private static final int STARTING_TEXT_POSITION_Y = 0;


        /** The Text to display on the screen. */
        private Text displayText = new Text(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y, "");
        private int fontSize = STARTING_FONT_SIZE;
        private String fontName = "Verdana";




        KeyEventHandler(final Group root, int windowWidth, int windowHeight) {
            //Initialize blank text so the we can use it's dimension for the cursor
            displayText.setFont(Font.font(fontName, fontSize));
            //get character height for the cursor rectangle
            charHeight = (int) displayText.getLayoutBounds().getHeight();
            textList.addAtCurrentNode(displayText);

            lineMap.put(0, textList.head);

            cursor.setWidth(1);
            cursor.setHeight(charHeight);

            // initialize cursor at the beginning
            cursor.setX(0);
            cursor.setY(0);
            textRoot.getChildren().add(cursor);

        }

        @Override
        public void handle(KeyEvent keyEvent) {
           RenderClass RenderObj = new RenderClass();


            if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                String characterTyped = keyEvent.getCharacter();
                if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8) {
                    //Backspace is represented as a character of value = 8 on Windows.
                    addChar(characterTyped);
                    RenderObj.render(charHeight);
                    keyEvent.consume();
                }

            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                KeyCode code = keyEvent.getCode();
                if (code == KeyCode.COMMAND) {
                    System.out.println("Current Node: " + textList.currentNode.text.getText());
                    for (int i = 0; i <= maxLine; i++) {
                        System.out.println("i: " + i + " " + lineMap.get(i).text.getText());
                    }
                }
                if (code == KeyCode.UP) {
                    if (cursorLine != 0) {
                        cursorLine--;
                        searchline(cursorLine, (int) textList.currentNode.text.getX());
                        setCursorToCurrentNode();
                    }

                } else if (code == KeyCode.DOWN) {
                    if (cursorLine != maxLine) {
                        cursorLine++;
                        searchline(cursorLine, (int) textList.currentNode.text.getX());
                        setCursorToCurrentNode();
                    }
                }


                if (textList.currentNode.previous != null) {
                    if (code == KeyCode.BACK_SPACE) {
                        deleteChar();
                        RenderObj.render(charHeight);
                        keyEvent.consume();

                    } else if (code == KeyCode.LEFT) {
                        // if the right node is at X = 0, then we jumped down a line
                        if (textList.currentNode.text.getX() == 0) {
                            cursorLine--;
                        }
                        //move the current node left
                        textList.currentNode = textList.currentNode.previous;
                        setCursorToCurrentNode();
                        keyEvent.consume();
                    }
                }

                if (textList.currentNode.next != null) {
                    if (code == KeyCode.RIGHT) {
                        // if the right node is at X = 0, then we jumped down a line

                        if (textList.currentNode.next.text.getX() == 0) {
                            cursorLine++;
                        }
                        //move the current node Right
                        textList.currentNode = textList.currentNode.next;
                        setCursorToCurrentNode();
                        keyEvent.consume();
                    }

                }
            }
        }



        private void deleteChar() {
            // new cursor position is the previous position + the previous character width

            textRoot.getChildren().remove(textList.currentNode.text);
            textList.deleteCurrentNode();

        }



        /**
         * Method used for rendering from the textList LinkedList
         * @param characterTyped
         */
        private void addChar(String characterTyped) {

            /* get pos of previous character */
            Text nextText = new Text(characterTyped);
            nextText.setTextOrigin(VPos.TOP);
            nextText.setFont(Font.font(fontName, fontSize));

            // add to the text list
            textList.addAtCurrentNode(nextText);

            // add to the root
            textRoot.getChildren().add(nextText);
        }
    }




    public class RenderClass {
        private int xPos;
        private int yPos;
        private int prevCharWidth;
        private int newCharWidth;
        private int wordLegnth;
        FastLinkedList.Node wordStart;

        //constructor
        RenderClass()   {
            // inititialize all variables used for rendering
            xPos = 0;
            yPos = 0;
            prevCharWidth  = 0;
            newCharWidth = 0;
            wordLegnth = 0;
            FastLinkedList.Node wordStart = textList.head;
        }

        /**
         * Method used for rendering from the textList LinkedList
         * loop through each text object and see if the word needs to move down
         * @param charHeight
         */
        public void render(int charHeight) {
            currentLine = 0;
            cursorLine = 0;
            maxLine = 0;
            boolean foundCursor = false;
            // loop through every node in the textList
            for (FastLinkedList.Node n : textList) {
                // set wordlength, wordstart, previous and new character widths
                setRenderParams(n);

                // increment the xPosition at the wordLength
                xPos += prevCharWidth;
                wordLegnth += newCharWidth;

                // call method to handle word wrap
                // if the next character is too long
                if (n == textList.currentNode) {
                    foundCursor = true;
                }

                if (n.text.getText().hashCode() == 13) {

                    if (!foundCursor) {
                        cursorLine++;
                    }
                    maxLine++;
                    xPos = 0;
                    yPos += charHeight;
                    lineMap.put(++currentLine, n.next);
                }


                if (xPos + newCharWidth > WINDOW_WIDTH) {
                    maxLine++;
                    if (!foundCursor) {
                        cursorLine++;
                    }
                   xPos = handleWordWrap(charHeight, n);
                }

                //set n's xPos and yPos
                n.text.setX(xPos);
                n.text.setY(yPos);
            }


            if (textList.currentNode.previous != null ) {
                cursor.setX((int) textList.currentNode.text.getX()
                        + (int) textList.currentNode.text.getLayoutBounds().getWidth());
            } else {
                cursor.setX(0);
            }
            cursor.setY((int)textList.currentNode.text.getY());
        }

        /**
         * Resets word based of Xpos and Ypos and returns the final Xpos
         * @param wordStart pointer to start of the Node
         * @param n current Node in the loop
         * @param xPos current xPos in the rendering
         * @param yPos current yPos in the rendering
         * @return
         */
        private int bringWordDown(FastLinkedList.Node wordStart, FastLinkedList.Node n, int xPos, int yPos) {
            while (wordStart != n) {
                wordStart.text.setY(yPos);
                wordStart.text.setX(xPos);
                xPos += wordStart.text.getLayoutBounds().getWidth();
                wordStart = wordStart.next;
            }
            return xPos;
        }

        /**
         * Method for setting paramters needed for renderer
         * @param n - the current Node in the loop
         */
        private void setRenderParams (FastLinkedList.Node n) {
            FastLinkedList.Node prevNode;
            // keep track of the current Node width
            newCharWidth = (int) n.text.getLayoutBounds().getWidth();
            // if empty
            if (n.previous == null){
                prevCharWidth = 0;
            } else {
                // restart word count
                if (n.previous.text.getText().equals(" ")) {
                    wordLegnth = 0;
                    wordStart = n;
                }
                prevNode = n.previous;
                // get the previous X width
                prevCharWidth = (int) prevNode.text.getLayoutBounds().getWidth();
            }
        }

        /**
         * Handles wrapping functionality
         * @param charHeight height of the current font
         * @param n current node in the loop
         */
        private int handleWordWrap(int charHeight, FastLinkedList.Node n) {
                yPos += charHeight;
                xPos = 0;
                // if the word fits in the next line
                if (wordLegnth + newCharWidth < WINDOW_WIDTH) {
                    // call bringWordDown method
                    lineMap.put(++currentLine, wordStart);
                    xPos = bringWordDown(wordStart, n, xPos, yPos);
                } else {
                    lineMap.put(++currentLine, n);
                }
            //keep track of max line
            return xPos;
        }
    }


    /** An EventHandler to handle changing the color of the rectangle. */
    private class RectangleBlinkEventHandler implements EventHandler<ActionEvent> {
        private int currentColorIndex = 0;
        private Color[] boxColors =
                {Color.LIGHTPINK, Color.ORANGE, Color.BLACK,
                    Color.GREEN, Color.DARKBLUE, Color.PURPLE};

        RectangleBlinkEventHandler() {
            // Set the color to be the first color in the list.
            changeColor();
        }

        private void changeColor() {
            cursor.setFill(boxColors[currentColorIndex]);
            currentColorIndex = (currentColorIndex + 1) % boxColors.length;
        }

        @Override
        public void handle(ActionEvent event) {

            changeColor();
        }
    }

    private class MouseClickEventHandler implements EventHandler<MouseEvent> {
        /** A Text object that will be used to print the current mouse position. */
        Text positionText;

        MouseClickEventHandler(Group root) {
        }


        @Override
        public void handle(MouseEvent mouseEvent) {
            // Because we registered this EventHandler using setOnMouseClicked, it will only called
            // with mouse events of type MouseEvent.MOUSE_CLICKED.  A mouse clicked event is
            // generated anytime the mouse is pressed and released on the same JavaFX node.
            double mousePressedX = mouseEvent.getX();
            double mousePressedY = mouseEvent.getY();

            double line;


            line = Math.floor((mousePressedY + scrollAdjust)/charHeight);
            // Display text right above the click.

            if (line <= maxLine) {
                searchline((int) line, (int) mousePressedX);
                setCursorToCurrentNode();
                cursorLine = (int) line;
            }

        }
    }


    // helper functions
    /**
     * scans through the line until xPos is reached and set's current node to that closest position
     * @param line line to jump to
     * @param xPos horizontal distance to jump to
     */
    private void searchline (int line, int xPos) {
        FastLinkedList.Node nodePtr = lineMap.get(line);
        while (nodePtr.next != null && nodePtr.next.text.getText().hashCode() != 13 && nodePtr.next.text.getX() < xPos) {
            // if the next node is closer
            if (Math.abs(nodePtr.next.text.getX() - xPos) < Math.abs(nodePtr.text.getX() - xPos)) ;
                nodePtr = nodePtr.next;
            }
        textList.currentNode = nodePtr;
    }

    /**
     * used for setting the cursor to the current node position
     */
    private void setCursorToCurrentNode() {
        cursor.setX((int) textList.currentNode.text.getX()
                + (int) textList.currentNode.text.getLayoutBounds().getWidth());
        cursor.setY((int) textList.currentNode.text.getY());
    }


    /** Makes the text bounding box change color periodically. */
    public void makeRectangleColorChange() {
        // Create a Timeline that will call the "handle" function of RectangleBlinkEventHandler
        // every 1 second.
        final Timeline timeline = new Timeline();
        // The rectangle should continue blinking forever.
        timeline.setCycleCount(Timeline.INDEFINITE);
        Editor.RectangleBlinkEventHandler cursorChange = new Editor.RectangleBlinkEventHandler();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), cursorChange);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }





    @Override
    public void start(Stage primaryStage) {
        // Create a Node that will be the parent of all things displayed on the screen.

        // The Scene represents the window: its height and width will be the height and width
        // of the window displayed.

        root.getChildren().add(textRoot);

        Scene scene = new Scene(root, WINDOW_WIDTH + 20, WINDOW_HEIGHT, Color.WHITE);

        // Make a vertical scroll bar on the right side of the screen.
        ScrollBar scrollBar = new ScrollBar();
        scrollBar.setOrientation(Orientation.VERTICAL);
        // Set the height of the scroll bar so that it fills the whole window.
        scrollBar.setPrefHeight(WINDOW_HEIGHT);

        // Set the range of the scroll bar.
        scrollBar.setMin(0);
        scrollBar.setMax(WINDOW_HEIGHT);

        scrollBar.setLayoutX(WINDOW_WIDTH);

        // Add the scroll bar to the scene graph, so that it appears on the screen.
        root.getChildren().add(scrollBar);


        /** When the scroll bar changes position, change the height of Josh. */
        scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldValue,
                    Number newValue) {
                // newValue describes the value of the new position of the scroll bar. The numerical
                // value of the position is based on the position of the scroll bar, and on the min
                // and max we set above. For example, if the scroll bar is exactly in the middle of
                // the scroll area, the position will be:
                //      scroll minimum + (scroll maximum - scroll minimum) / 2
                // Here, we can directly use the value of the scroll bar to set the height of Josh,
                // because of how we set the minimum and maximum above.
                scrollAdjust = (int) newValue.doubleValue();
                textRoot.setLayoutY(-newValue.doubleValue());
            }
        });

        // Resize listeners
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenWidth,
                    Number newScreenWidth) {
                // Re-compute Allen's width.
                WINDOW_WIDTH = newScreenWidth.intValue();
                scrollBar.setLayoutX(WINDOW_WIDTH - 20);
                RenderClass RenderObj = new RenderClass();
                RenderObj.render(charHeight);


            }
        });

        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenHeight,
                    Number newScreenHeight) {
                WINDOW_HEIGHT = newScreenHeight.intValue();
                RenderClass RenderObj = new RenderClass();
                RenderObj.render(charHeight);

            }
        });




        // To get information about what keys the user is pressing, create an EventHandler.
        // EventHandler subclasses must override the "handle" function, which will be called
        // by javafx.

        EventHandler<KeyEvent> keyEventHandler =
                new KeyEventHandler(root, WINDOW_WIDTH, WINDOW_HEIGHT);


        // Register the event handler to be called for all KEY_PRESSED and KEY_TYPED events.
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);


        //call the color change
        makeRectangleColorChange();

        scene.setOnMouseClicked(new MouseClickEventHandler(root));


        primaryStage.setTitle("Jon Miguel Editor");

        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }
}