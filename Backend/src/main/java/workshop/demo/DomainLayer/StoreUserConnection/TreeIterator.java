package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeIterator implements Iterator<Node> {

    private static final Logger logger = LoggerFactory.getLogger(TreeIterator.class);

    private Deque<Node> queue;

    public TreeIterator(Node root) {
        queue = new ConcurrentLinkedDeque<>();
        if (root != null) {
            queue.add(root);
            logger.debug("TreeIterator initialized with root ");
        } else {
            logger.debug("TreeIterator initialized with null root");
        }
    }

    @Override
    public boolean hasNext() {// must do synchronized ?????
        boolean hasNext = !queue.isEmpty();
        logger.debug("hasNext called: {}", hasNext);
        return hasNext;
    }

    @Override
    public Node next() {
        Node current = queue.poll();
        if (current != null) {
            logger.debug("next called: returning node {}");

            for (Node child : current.getChildren()) {
                queue.add(child);
                logger.debug("Child node added to queue");

            }
        }
        return current;
    }

}
