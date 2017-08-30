package editor;
import javafx.scene.text.Text;

/**
 * Created by jonmigueljara on 8/30/17.
 */
public class FastLinkedList {
    int size = 0;
    Node head = null;
    Node tail = null;
    Node currentNode;


    public Node add(Text text){
        Node newNode = new Node(text);
        if(size == 0){
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

//    public Node addAfter(int data, Node prevNode){
//        if (prevNode == null){
//            return null;
//        } else if (prevNode == tail){//check if it a last node
//            return add(data);
//        } else {
//            //create a new node
//            Node newNode = new Node(data);
//
//            //store the next node of prevNode
//            Node nextNode = prevNode.next;
//
//            //make new node next points to prevNode
//            newNode.next = nextNode;
//
//            //make prevNode next points to new Node
//            prevNode.next = newNode;
//
//            //make nextNode previous points to new node
//            nextNode.previous = newNode;
//
//            //make  new Node previous points to prevNode
//            newNode.previous = prevNode;
//            size++;
//            return newNode;
//        }
//    }

    public void deleteHead() {
        if(size == 0){
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
    public int getSize(){
        return size;
    }


}

class Node {
    Text text;
    Node next;
    Node previous;
    public Node(Text text){
        this.text = text;
        next = null;
        previous = null;
    }
}
