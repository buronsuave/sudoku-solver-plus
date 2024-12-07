package utils;

import java.util.ArrayList;

public class SynchronizedStack {
    private final ArrayList<Integer> stack;

    public SynchronizedStack() {
        stack = new ArrayList<>();
    }

    public synchronized void push(int item) {
        stack.add(item);
    }

    public synchronized int pop() {
        if (stack.isEmpty()) return -1;
        int index = stack.get(0);
        stack.remove(0);
        return index;
    }

    public ArrayList<Integer> getStackCopy() {
        return (ArrayList<Integer>) stack.clone();
    }

    public void loadStack(ArrayList<Integer> s) {
        stack.clear();
        stack.addAll(s);
    }

    public int size() {
        return stack.size();
    }
}
