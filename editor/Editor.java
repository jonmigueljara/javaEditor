package editor;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.shape.Rectangle;
import java.util.*;

import javafx.util.Duration;
import jdk.nashorn.internal.runtime.regexp.joni.encoding.CharacterType;

import java.util.LinkedList;
/**

 */
public class Editor extends Application {
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 500;
    Group root = new Group();
    FastLinkedList textList = new FastLinkedList();
    private final Rectangle cursor;
    int cursorX;
    int cursorY;


    public Editor() {
        //initilaize rectangle for cursor
        cursor = new Rectangle(0, 0);
    }


    /** An EventHandler to handle keys that get pressed. */
    private class KeyEventHandler implements EventHandler<KeyEvent> {
        int textCenterX;
        int textCenterY;

        private static final int STARTING_FONT_SIZE = 20;
        private static final int STARTING_TEXT_POSITION_X = 250;
        private static final int STARTING_TEXT_POSITION_Y = 250;

        /** The Text to display on the screen. */
        private Text displayText = new Text(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y, "");
        private int fontSize = STARTING_FONT_SIZE;
        private String fontName = "Verdana";

        public int charHeight;

        KeyEventHandler(final Group root, int windowWidth, int windowHeight) {
            //Initialize blank text so the we can use it's dimension for the cursor
            displayText.setFont(Font.font(fontName, fontSize));
            //get character height for the cursor rectangle
            charHeight = (int) displayText.getLayoutBounds().getHeight();
            int cusorPos = (int) displayText.getLayoutBounds().getHeight()/2;
            cursor.setWidth(1);
            cursor.setHeight(charHeight);

            // initialize cursor at the beginning
            cursor.setX(0);
            cursor.setY(0);
            root.getChildren().add(cursor);
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
                if (code == KeyCode.BACK_SPACE) {
                    deleteChar();
                    RenderObj.render(charHeight);
                    keyEvent.consume();

                } else if (code == KeyCode.LEFT) {
                    //move the current node left
                    cursorX = cursorMoveLeft();

                    cursorY = (int) textList.currentNode.previous.text.getY();
                    textList.currentNode = textList.currentNode.previous;
                    RenderObj.render(charHeight);
                }

            }
        }


        private void deleteChar() {
            // new cursor position is the previous position + the previous character width
            cursorX =  cursorMoveLeft();

            root.getChildren().remove(textList.currentNode.text);
            textList.deleteCurrentNode();
        }


        private int cursorMoveLeft() {
            int xPos;
            xPos = (int) textList.currentNode.previous.text.getX()
                    + (int) textList.currentNode.previous.text.getLayoutBounds().getWidth();
            return xPos;
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

           int newCharWidth = (int) nextText.getLayoutBounds().getWidth();
            // add to the text list
            textList.addAtCurrentNode(nextText);

            // add to the root
            root.getChildren().add(nextText);
            //cursorX += newCharWidth;
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
         */
        public void render(int charHeight) {
            // loop through every node in the textList
            for (FastLinkedList.Node n : textList) {
                // set wordlength, wordstart, previous and new character widths
                setRenderParams(n);

                // increment the xPosition at the wordLength
                xPos += prevCharWidth;
                wordLegnth += newCharWidth;

                // call method to handle word wrap
                handleWordWrap(charHeight, n);

                //set n's xPos and yPos
                n.text.setX(xPos);
                n.text.setY(yPos);
            }
            System.out.println(cursorX);
            if (textList.currentNode.previous != null ) {
                cursor.setX((int) textList.currentNode.text.getX()
                        + (int) textList.currentNode.text.getLayoutBounds().getWidth());
            } else {
                cursor.setX((int) textList.currentNode.text.getX() + (int) textList.currentNode.text.getLayoutBounds().getWidth());
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
            if (n.previous == null){
                prevCharWidth = 0;
            } else {
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
        private void handleWordWrap(int charHeight, FastLinkedList.Node n) {
            // if the next character is too long
            if (xPos + newCharWidth > WINDOW_WIDTH) {
                yPos += charHeight;
                xPos = 0;

                // if the word fits in the next line
                if (wordLegnth + newCharWidth < WINDOW_WIDTH) {
                    // call bringWordDown method
                    xPos = bringWordDown(wordStart, n, xPos, yPos);
                }
            }
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
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, Color.WHITE);

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


        primaryStage.setTitle("Jon Miguel Editor");

        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}