package editor;
import com.sun.org.apache.bcel.internal.generic.NEW;
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


    public Node addAtCurrentNode(Text text) {
        Node newNode = new Node(text);
        if (size == 0) {
            head = newNode;
            tail = newNode;
        } else {
            // if not last
            if (currentNode.next != null) {
                currentNode.next.previous = newNode;
            }
            newNode.next = currentNode.next;
            newNode.previous = currentNode;
            currentNode.next = newNode;
        }
        currentNode = newNode;
        size++;
        return newNode;
    }


    public void deleteCurrentNode() {
        // base case, there is nothing to delete
        if (size == 0) {
            return;
        }
       /* If node to be deleted is head node */
        if (head == currentNode) {
            head = currentNode.next;
        }

        /* Change next only if node to be deleted is NOT the last node */
        if (currentNode.next != null) {
            currentNode.next.previous = currentNode.previous;
        }

        /* Change prev only if node to be deleted is NOT the first node */
        if (currentNode.previous != null) {
            currentNode.previous.next = currentNode.next;
        }
        // move current node 1 spot back
        currentNode = currentNode.previous;

        /* Finally, free the memory occupied by CurrentNode */
        return;
    }

    public void deleteLast() {
        if (size == 0) {
        } else {
            tail = tail.previous;
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