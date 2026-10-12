package com.sportspulse.util;

/**
 * Represents a discrete selectable entry in a SportsPulse terminal menu.
 */
public final class MenuItem {

    private final String id;
    private final String label;
    private final String description;
    private final String badge;

    public MenuItem(String id, String label, String description, String badge) {
        this.id = id != null ? id : "";
        this.label = label != null ? label : "";
        this.description = description != null ? description : "";
        this.badge = badge != null ? badge : "";
    }

    public MenuItem(String id, String label, String description) {
        this(id, label, description, "");
    }

    public MenuItem(String id, String label) {
        this(id, label, "", "");
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getBadge() {
        return badge;
    }

    @Override
    public String toString() {
        if (description.isEmpty()) {
            return id + ". " + label;
        }
        return id + ". " + label + " (" + description + ")";
    }
}
