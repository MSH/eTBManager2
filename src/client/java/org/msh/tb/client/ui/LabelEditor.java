package org.msh.tb.client.ui;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.msh.tb.client.commons.StandardEventHandler;

/**
 * Created by ricardo on 16/07/14.
 */
public class LabelEditor extends Composite {
    private FlowPanel pnl;

    private TextBox edtLabel;
    private Label txtLabel;

    private StandardEventHandler eventHandler;

    /**
     * Default constructor
     */
    public LabelEditor() {
        pnl = new FlowPanel();
        edtLabel = new TextBox();
        txtLabel = new Label();

        pnl.add(edtLabel);
        pnl.add(txtLabel);

        initWidget(pnl);

        setEditing(false);
        setStyleName("label-editor");

        txtLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setEditing(true);
            }
        });
        edtLabel.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                applyChange();
            }
        });
        edtLabel.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                editLabelKeyDown(event);
            }
        });
    }


    /**
     * Return the text being displayed
     * @return String value
     */
    public String getText() {
        return edtLabel.isVisible()? edtLabel.getText(): txtLabel.getText();
    }


    /**
     * Called when the user press a key when editing the title
     * @param event
     */
    protected void editLabelKeyDown(KeyDownEvent event) {
        // user pressed the enter key ?
        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
            applyChange();
            return;
        }

        // user pressed the esc key ?
        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
            setEditing(false);
        }
    }


    /**
     * turn off editor and set the text to the display mode
     */
    protected void applyChange() {
        String s = edtLabel.getText();
        if (s.isEmpty()) {
            edtLabel.setFocus(true);
            return;
        }

        txtLabel.setText(edtLabel.getText());
        setEditing(false);
        if (eventHandler != null) {
            eventHandler.handleEvent(this, edtLabel.getText());
        }
    }


    /**
     * Set the text being displayed or edited
     * @param text is the new text
     */
    public void setText(String text) {
        txtLabel.setText(text);
        edtLabel.setText(text);
    }

    /**
     * Return true if the editor is enabled
     * @return
     */
    public boolean isEditing() {
        return edtLabel.isVisible();
    }

    /**
     * Hide or show the editing box of the text
     * @param value true to set the editor visible
     */
    public void setEditing(boolean value) {
        edtLabel.setVisible(value);
        txtLabel.setVisible(!value);
        if (value) {
            edtLabel.setText(txtLabel.getText());
            edtLabel.setFocus(true);
        }
    }

    public StandardEventHandler getEventHandler() {
        return eventHandler;
    }

    public void setEventHandler(StandardEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

}
