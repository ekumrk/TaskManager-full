package managers;

import tasks.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private static class Node {
        final private Task item;
        private Node next;
        private Node prev;

        Node(Node prev,Task element, Node next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }

    final private Map<Integer, Node> viewsHistory = new HashMap<>();
    private Node first;
    private Node last;


    public InMemoryHistoryManager(){
    }


    @Override
    public void addToHistoryTask(Task task) {
        if (viewsHistory.containsKey(task.getId())) {
            Node node = viewsHistory.get(task.getId());
            removeNode(node);
        }
        linkLast(task);
    }

    @Override
    public List<Task> getHistory() {
        List<Task> list = new ArrayList<>();
        Node current = first;
        while (current != null) {
            list.add(current.item);
            current = current.next;
        }
        return list;
    }

    @Override
    public void remove(int id) {
        if (!viewsHistory.isEmpty()) {
            Node node = viewsHistory.get(id);
            removeNode(node);
        }
    }

    void linkLast(Task task) {
        final Node prevlast = last;
        final Node newNode = new Node(prevlast, task, null);
        last = newNode;
        if (prevlast == null) {
            first = newNode;
        } else {
            prevlast.next = newNode;
        }
        viewsHistory.put(task.getId(), newNode);
    }

    private void removeNode(Node node) {

        if (first == node) {
            first = node.next;
            first.prev = null;
            return;
        }
        if (last == node) {
            last = node.prev;
            last.next = null;
            return;
        }

        if (node != null) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            viewsHistory.remove(node.item.getId());
        }
    }
}
