package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TreeIterator implements Iterator<Node> {

    private Deque<Node> queue;

    public TreeIterator(Node root) {
        queue = new ConcurrentLinkedDeque<>();
        if (root != null) {
            queue.add(root);
        }
    }

    @Override
    public boolean hasNext() {//must do synchronized ?????
        return !queue.isEmpty();
    }

    @Override
    public Node next() {
        Node current = queue.poll();
        if (current != null) {
            for (Node child : current.getChildren()) {
                queue.add(child);
            }
        }
        return current;
    }

}
