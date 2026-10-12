package com.sportspulse.util;

/**
 * Result of an interactive or line-mode menu selection.
 */
public final class MenuResult {

    public enum Action {
        SELECT,
        CANCEL,
        EXIT
    }

    private final Action action;
    private final MenuItem selectedItem;
    private final int selectedIndex;

    private MenuResult(Action action, MenuItem selectedItem, int selectedIndex) {
        this.action = action;
        this.selectedItem = selectedItem;
        this.selectedIndex = selectedIndex;
    }

    public static MenuResult select(MenuItem item, int index) {
        return new MenuResult(Action.SELECT, item, index);
    }

    public static MenuResult cancel() {
        return new MenuResult(Action.CANCEL, null, -1);
    }

    public static MenuResult exit() {
        return new MenuResult(Action.EXIT, null, -1);
    }

    public Action getAction() {
        return action;
    }

    public boolean isSelected() {
        return action == Action.SELECT && selectedItem != null;
    }

    public boolean isCancelOrExit() {
        return action == Action.CANCEL || action == Action.EXIT;
    }

    public MenuItem getSelectedItem() {
        return selectedItem;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public String getSelectedId() {
        return selectedItem != null ? selectedItem.getId() : "";
    }
}
