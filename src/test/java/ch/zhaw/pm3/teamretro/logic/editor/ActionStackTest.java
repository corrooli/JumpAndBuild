package ch.zhaw.pm3.teamretro.logic.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.zhaw.pm3.teamretro.logic.editor.ActionStack.Node;

class ActionStackTest {

    static final Random random = new Random();

    private ActionStack<PlacementAction, Integer> stack;

    static final int stackEntries = 10;

    @BeforeEach
    void setup() {
        stack = new ActionStack<>();
        for (int i = 0; i < stackEntries; i += 1) {
            PlacementAction action = random.nextInt(2) == 0 ? PlacementAction.REMOVE : PlacementAction.ADD;
            int value = random.nextInt(10);
            stack.push(action, value);
        }
    }

    @Test
    void testSize() {
        assertEquals(stackEntries, stack.getUndoStackSize());
        assertEquals(0, stack.getRedoStackSize());
        for (int i = 1; i < 2; i += 1) {
            stack.undo();
            assertEquals(stackEntries - i, stack.getUndoStackSize());
            assertEquals(0 + i, stack.getRedoStackSize());
        }
    }

    @Test
    void testPush() {
        assertEquals(stackEntries, stack.getUndoStackSize());
        stack.push(PlacementAction.ADD, 0);
        assertEquals(stackEntries + 1, stack.getUndoStackSize());
    }

    @Test
    void testUndoList() {
        // check if correct values are returned
        {
            PlacementAction testAction = PlacementAction.ADD;
            Integer testNum = 42;
            stack.push(testAction, testNum);
            Optional<Node<PlacementAction, Integer>> node = stack.undo();
            assertTrue(node.isPresent(), "Checking if node is present.");
            assertEquals(node.get().getAction(), testAction, "Action returned was not correct.");
            assertEquals(node.get().getValue(), testNum, "Value returned was not correct.");
        }
        {
            assertEquals(10, stack.getUndoStackSize());
            Optional<Node<PlacementAction, Integer>> node = stack.undo();
            assertTrue(node.isPresent(), "Checking if node is present");
            // empty out the stack
            for (int i = stackEntries - 1; i > -1; i -= 1) {
                assertEquals(i, stack.getUndoStackSize());
                assertTrue(node.isPresent(), "Checking if node is present");
                node = stack.undo();
            }
            assertTrue(node.isEmpty(), "Checking if node is empty");
        }
    }

    @Test
    void testRedoList() {
        assertEquals(10, stack.getUndoStackSize());
        Optional<Node<PlacementAction, Integer>> node = stack.redo();
        assertTrue(node.isEmpty(), "Checking if node is empty");
        // empty out the stack
        int i;
        for (i = stackEntries - 1; i > stackEntries / 2; i -= 1) {
            node = stack.undo();
            assertEquals(stackEntries - i, stack.getRedoStackSize(), "Checking if size is correct.");
        }
        for (; i > 0; i -= 1) {
            assertTrue(node.isPresent(), "Checking if node is present");
            node = stack.redo();
        }
        assertTrue(node.isEmpty(), "Checking if node is empty");
    }
}
