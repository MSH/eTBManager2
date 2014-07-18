package org.msh.tb.client.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;

/**
 * Create a panel that wraps its elements inside divs
 * Created by Ricardo on 17/07/2014.
 */
public class DivPanel extends ComplexPanel implements InsertPanel.ForIsWidget {

    /**
     * Default constructor
     */
    public DivPanel() {
        setElement((Element)DOM.createDiv());
    }
    @Override
    public void add(Widget child) {
        Element div = addDiv();
        add(child, div);
    }

    /**
     * Add a new div to the panel
     * @return div element
     */
    protected Element addDiv() {
        Element div = DOM.createDiv();
        DOM.appendChild(getElement(), div);
        return div;
    }

    /**
     * Add a widget specifying the style class of the div that wraps it
     * @param child the widget to be included
     * @param styleClass the style class to be applied to the div
     */
    public void add(Widget child, String styleClass) {
        Element div = addDiv();
        add(child, div);

        if (styleClass != null) {
            div.setAttribute("class", styleClass);
        }
    }

    /**
     * Add a div with a text inside it
     * @param text the text to be inserted inside the div
     * @param styleClass the style class of the div
     */
    public Label addText(String text, String styleClass) {
        Element div = addDiv();
        if (styleClass != null) {
            div.setAttribute("class", styleClass);
        }
        Label lbl = new Label(text);
        add(lbl, div);
        return lbl;
    }


    @Override
    public boolean remove(Widget w) {
        Element div = DOM.getParent((Element)w.getElement());
        boolean removed = super.remove(w);
        if (removed) {
            getElement().removeChild(div);
        }
        return removed;
    }

    @Override
    public void insert(Widget w, int beforeIndex) {
        checkIndexBoundsForInsertion(beforeIndex);

        Element div = DOM.createDiv();
        DOM.insertChild(getElement(), div, beforeIndex);
        insert(w, div, beforeIndex, false);
    }

    @Override
    public void insert(IsWidget w, int beforeIndex) {
        insert(asWidgetOrNull(w), beforeIndex);
    }
}
