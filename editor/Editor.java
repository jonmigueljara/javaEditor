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
            if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                // Use the KEY_TYPED event rather than KEY_PRESSED for letter keys, because with
                // the KEY_TYPED event, javafx handles the "Shift" key and associated
                // capitalization.
                String characterTyped = keyEvent.getCharacter();
                if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8) {
                    // Ignore control keys, which have non-zero length, as well as the backspace
                    // key, which is represented as a character of value = 8 on Windows.

                    addChar(characterTyped);
                    render(charHeight);
                    keyEvent.consume();
                }

            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                // Arrow keys should be processed using the KEY_PRESSED event, because KEY_PRESSED
                // events have a code that we can check (KEY_TYPED events don't have an associated
                // KeyCode).
                KeyCode code = keyEvent.getCode();
                if (code == KeyCode.UP) {
                    fontSize += 5;
                    displayText.setFont(Font.font(fontName, fontSize));
                    centerText();
                } else if (code == KeyCode.DOWN) {
                    fontSize = Math.max(0, fontSize - 5);
                    displayText.setFont(Font.font(fontName, fontSize));
                    centerText();
                }
            }
        }

        private void centerText() {
            // Figure out the size of the current text.
            double textHeight = displayText.getLayoutBounds().getHeight();
            double textWidth = displayText.getLayoutBounds().getWidth();

            // Calculate the position so that the text will be centered on the screen.
            double textTop = textCenterY - textHeight / 2;
            double textLeft = textCenterX - textWidth / 2;

            // Re-position the text.
            displayText.setX(textLeft);
            displayText.setY(textTop);

            // Make sure the text appears in front of any other objects you might add.
            displayText.toFront();
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
            textList.add(nextText);


            // add to the root
            root.getChildren().add(nextText);
        }
    }

    /**
     * Method used for rendering from the textList LinkedList
     * loop through each text object and see if the word needs to move down
     */
    private void render(int charHeight) {
        int xPos = 0;
        int yPos = 0;
        int lineNum = 0;

        int prevX = 0;
        int prevCharWidth  = 0;
        int newCharWidth = 0;

        FastLinkedList.Node prevNode;

        // loop through every node in the textList
        for (FastLinkedList.Node n : textList) {
            // keep track of the previous Node
            newCharWidth = (int) n.text.getLayoutBounds().getWidth();
            if (n.previous == null){
                prevX = 0;
                prevCharWidth = 0;
            } else {
                prevNode = n.previous;
                // get the previous X position and previous length
                prevX = (int) (prevNode.text.getX());
                prevCharWidth = (int) prevNode.text.getLayoutBounds().getWidth();
            }
            xPos += prevCharWidth;
            if (xPos + newCharWidth > WINDOW_WIDTH) {
                // if we are the edge of the window
                // increment yPos by charHeight
                yPos += charHeight;
                //reset the xPos
                xPos = 0;
            }
            n.text.setX(xPos);
            n.text.setY(yPos);

            // reset the cursor
            cursor.setX(xPos + newCharWidth);
            cursor.setY(yPos);
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


        primaryStage.setTitle("Multiple Letter Display Simple");

        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}