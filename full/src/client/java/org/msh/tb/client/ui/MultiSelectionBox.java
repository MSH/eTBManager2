package org.msh.tb.client.ui;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Multi selection box. Display a list of options where user can select several of them
 * Created by rmemoria on 1/7/15.
 */
public class MultiSelectionBox extends Composite {

    public interface ChangeHandler {
        void onChange(MultiSelectionBox box);
    }

    // popup is singleton
    private static final DecoratedPopupPanel popup = new DecoratedPopupPanel();

    private FlowPanel pnlBox;
    private FlowPanel pnlSels;
    private Anchor btnDropDown;
    private MenuPanel pnlMenu;
    private List<SelectionOption> options = new ArrayList<SelectionOption>();
    private ArrayList<ChangeHandler> changeHandlers = new ArrayList<ChangeHandler>();


    /**
     * Default constructor
     */
    public MultiSelectionBox() {
        // initialize the popup
        initPopup();

        pnlBox = new FlowPanel();
        pnlBox.setStyleName("msbox");

        btnDropDown = new Anchor(SafeHtmlUtils.fromTrustedString("<div class='caret-down'></div>"));
        btnDropDown.setStyleName("option-button");
        btnDropDown.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                toggleOptions();
            }
        });
        pnlBox.add(btnDropDown);

        pnlSels = new FlowPanel();
        pnlBox.add(pnlSels);

        initWidget(pnlBox);
    }

    /**
     * Add change handler
     * @param handler
     */
    public void addChangeHandler(ChangeHandler handler) {
        changeHandlers.add(handler);
    }

    /**
     * Remove a change handler previously added
     * @param handler
     */
    public void removeChangeHandler(ChangeHandler handler) {
        changeHandlers.remove(handler);
    }

    /**
     * Notify about changes in the selection box
     */
    protected void notifyChangeEvent() {
        for (ChangeHandler handler: changeHandlers) {
            handler.onChange(this);
        }
    }

    protected void toggleOptions() {
        if (popup.isVisible()) {
            popup.hide();
        }

        updateDropDownItems();

        if (pnlMenu.getWidgetCount() == 0) {
            popup.hide();
            return;
        }

        popup.setAutoHideEnabled(true);
        popup.showRelativeTo(this);
    }

    /**
     * Update the list of drop down options
     */
    protected void updateDropDownItems() {
        pnlMenu.clear();

        for (SelectionOption opt: options) {
            // just include selected options
            if (!opt.isSelected()) {
                AnchorData link = new AnchorData(opt.getLabel(), opt);
                link.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        AnchorData link = (AnchorData)clickEvent.getSource();
                        dropDownClick(link);
                    }
                });
                pnlMenu.add(link);
            }
        }
    }

    protected void updateSelectedOptions() {
        pnlSels.clear();
        for (SelectionOption opt: options) {
            if (opt.isSelected()) {
                pnlSels.add( createSelectedOption(opt) );
            }
        }
    }

    /**
     * Create the item to be displayed when it is selected
     * @param opt
     * @return
     */
    protected Widget createSelectedOption(SelectionOption opt) {
        FlowPanel sel = new FlowPanel();
        sel.setStyleName("sel-opt");

        Label txt = new Label(opt.getLabel());
        txt.setStyleName("sel-opt-label");

        AnchorData lnk = new AnchorData();
        lnk.setData(opt);
        lnk.setStyleName("close-btn");
        lnk.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                AnchorData lnk = (AnchorData)clickEvent.getSource();
                SelectionOption opt = (SelectionOption)lnk.getData();
                removeSelection(opt);
            }
        });

        sel.add(lnk);
        sel.add(txt);

        return sel;
    }

    /**
     * Remove the selected item from the content area. Called when user clicks on the close buton
     * in the selected item
     * @param opt
     */
    protected void removeSelection(SelectionOption opt) {
        opt.setSelected(false);
        updateSelectedOptions();
        notifyChangeEvent();
    }

    /**
     * Called when user select an option
     * @param lnk
     */
    protected void dropDownClick(AnchorData lnk) {
        SelectionOption opt = (SelectionOption)lnk.getData();
        opt.setSelected(true);
        updateDropDownItems();
        updateSelectedOptions();

        // if there is no more items to display, just hide it
        if (pnlMenu.getWidgetCount() == 0) {
            popup.hide();
            return;
        }

        if (popup.isVisible()) {
            popup.showRelativeTo(this);
        }
        notifyChangeEvent();
    }

    /**
     * Initialize the popup (just do it once in the whole client)
     */
    protected void initPopup() {
        if (popup.getWidget() != null) {
            pnlMenu = (MenuPanel)popup.getWidget();
            return;
        }
        popup.setStyleName("dropdown");
        pnlMenu = new MenuPanel();
        popup.add(pnlMenu);
    }

    /**
     * Add a new item to the list
     * @param label
     * @param value
     */
    public void add(String label, Object value) {
        options.add(new SelectionOption(label, value));
    }

    public List<Object> getSelectedValues() {
        List<Object> lst = new ArrayList<Object>();
        for (SelectionOption opt: options) {
            if (opt.isSelected()) {
                lst.add(opt.getValue());
            }
        }
        return lst;
    }

    /**
     * Remove all items of the selection
     */
    public void clearSelection() {
        for (SelectionOption opt: options) {
            opt.setSelected(false);
        }
        updateSelectedOptions();
    }

    /**
     * Select the options by the given values
     * @param values
     */
    public void selectValues(Object[] values) {
        for (SelectionOption opt: options) {
            opt.setSelected(false);
        }

        for (Object val: values) {
            for (SelectionOption opt: options) {
                if (opt.getValue().equals(val)) {
                    opt.setSelected(true);
                    break;
                }
            }
        }
        updateSelectedOptions();
    }
}
