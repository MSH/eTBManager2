package org.msh.tb.client.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

/**
 * GWT Widget that displays a drop down menu. Must be inside a popup panel
 * Created by ricardo on 15/07/14.
 */
public class MenuPanel extends ComplexPanel implements InsertPanel.ForIsWidget {

    private final Element root = DOM.createElement("ul");

    private List<Element> menuOptions;

    /**
     * Default constructor
     */
    public MenuPanel() {
        setElement(root);
        setStyleName("dropdown-menu");
    }

    @Override
    public void add(Widget child) {
        Element li = DOM.createElement("li");
        DOM.appendChild(getElement(), li);
        add(child, li);
    }

    @Override
    public boolean remove(Widget w) {
        Element li = DOM.getParent((Element)w.getElement());
        boolean removed = super.remove(w);
        if (removed) {
            root.removeChild(li);
        }
        return removed;
    }

    @Override
    public void insert(Widget w, int beforeIndex) {
        checkIndexBoundsForInsertion(beforeIndex);

        Element li = DOM.createElement("li");
        DOM.insertChild(root, li, beforeIndex);
        insert(w,li, beforeIndex, false);
    }

    @Override
    public void insert(IsWidget w, int beforeIndex) {
        insert(asWidgetOrNull(w), beforeIndex);
    }
}
