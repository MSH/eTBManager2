package org.msh.tb.client.ui;

import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi selection box. Display a list of options where user can select several of them
 * Created by rmemoria on 1/7/15.
 */
public class MultiSelectionBox extends Composite {

    private HTMLPanel pnlContent;
    private List<Option> options = new ArrayList<Option>();
    private static final String htmlContent = "<div id='btn' class='msb_link'></><div id='content' class='msb_content'></div>";

    public MultiSelectionBox() {
        pnlContent = new HTMLPanel(htmlContent);
        initWidget(pnlContent);
    }

    /**
     * Add a new item to the list
     * @param label
     * @param value
     */
    public void add(String label, Object value) {
        options.add(new Option(label, value));
    }

    public List<Option> getOptions() {
        return options;
    }

    /**
     * Store an option of the list
     */
    public class Option {
        private String label;
        private Object value;
        private boolean selected;

        public Option(String label, Object value) {
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
}
