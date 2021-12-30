package ch.zhaw.pm3.teamretro.logic.editor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Undo and redo stack class. Abstracts the stack of undo/redo operations.
 *
 * @param <T> Action the user has made is set to only accept enums
 * @param <U> Value of modified object.
 */
public class ActionStack<T extends Enum<T>, U> {
    /**
     * List of undo actions.
     */
    private final Deque<Node<T, U>> undoList = new ArrayDeque<>();

    /**
     * List of redo actions.
     */
    private final Deque<Node<T, U>> redoList = new ArrayDeque<>();

    /**
     * Constructor, deliberately empty
     */
    public ActionStack() {
        // Empty because of clean code
    }

    /**
     * Will return the current stack size.
     *
     * @return the current stack size.
     */
    public int getUndoStackSize() {
        return undoList.size();
    }

    /**
     * Will return the current redo stack size.
     *
     * @return the current stack size.
     */
    public int getRedoStackSize() {
        return redoList.size();
    }

    /**
     * Undoes an action. This will return an immutable node that was undone, with
     * the state at the given position.
     *
     * @return Node to be undone.
     */
    public Optional<Node<T, U>> undo() {
        return work(undoList, redoList);
    }

    /**
     * Redoes an action. This will return an immutable node that was redone, with
     * the state at the given position,
     *
     * @return Node to be redone.
     */
    public Optional<Node<T, U>> redo() {
        return work(redoList, undoList);
    }

    /**
     * Will to the actual work needed for the shuffling of the stacks
     *
     * @param <T>  any type that extends an enum
     * @param <U>  any type really
     * @param from the deque to pop from
     * @param to   the deque to push to
     * @return an optional node<T,U>
     */
    private static <T extends Enum<T>, U> Optional<Node<T, U>> work(Deque<Node<T, U>> from, Deque<Node<T, U>> to) {
        if (from.isEmpty()) {
            return Optional.empty();
        }
        Node<T, U> node = from.pop();
        to.push(node);
        return Optional.of(node);
    }

    /**
     * Creates a new undo/redo node, clears the redo list and pushes the new node to
     * the undo stack.
     * <p>
     * Attention, this method will clear the redo stack.
     * <p>
     * 
     * @param action the action that was done at the given time
     * @param value  the value that was used at that given time
     */
    public void push(T action, U value) {
        Node<T, U> node = new Node<>(action, value);
        redoList.clear();
        undoList.push(node);
    }

    /**
     * Node inner class, containing undo or redo actions and the accompanying value.
     *
     * @param <V> Action type.
     * @param <W> Value of modified object.
     */
    public static class Node<V extends Enum<V>, W> {
        private final V action;
        private final W value;

        Node(V action, W value) {
            this.action = action;
            this.value = value;
        }

        public V getAction() {
            return action;
        }

        public W getValue() {
            return value;
        }
    }
}
