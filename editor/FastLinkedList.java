package editor;
import javafx.scene.text.Text;
import java.util.*;

/**
 * Created by jonmigueljara on 8/30/17.
 */
public class FastLinkedList implements Iterable<FastLinkedList.Node> {
    int size = 0;
    Node head = null;
    Node tail = null;
    Node currentNode;


    public Node add(Text text) {
        Node newNode = new Node(text);
        if (size == 0) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.previous = tail;
            tail = newNode;
        }
        size++;
        return newNode;
    }


    public void deleteHead() {
        if (size == 0) {
        } else {
            head = head.next;
            size--;
        }
    }

    public boolean isEmpty() {
        if (size == 0) {
            return true;
        } else {
            return false;
        }
    }

    public Text getLast() {
        return tail.text;
    }


    //get Size
    public int getSize() {
        return size;
    }


    public class Node {
        Text text;
        Node next;
        Node previous;

        public Node(Text text) {
            this.text = text;
            next = null;
            previous = null;
        }
    }


    public Iterator<FastLinkedList.Node> iterator()
    {
        return new LinkedListIterator();
    }

    private class LinkedListIterator  implements Iterator<FastLinkedList.Node>
    {
        private Node nextNode;

        public LinkedListIterator()
        {
            nextNode = head;
        }

        public boolean hasNext()
        {
            return nextNode != null;
        }

        public Node next()
        {
            if (!hasNext()) throw new NoSuchElementException();
            Node res = nextNode;
            nextNode = nextNode.next;
            return res;
        }

        public void remove() { throw new UnsupportedOperationException(); }
    }

}