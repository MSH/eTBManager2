package org.msh.tb.client.ui;

/**
 * Represent an option of the MultiSelectionBox
 * Created by rmemoria on 26/5/15.
 */
public class SelectionOption {
    private String label;
    private Object value;
    private boolean selected;

    public SelectionOption(String label, Object value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
