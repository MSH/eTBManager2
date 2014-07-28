package org.msh.tb.client.indicators;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import org.msh.tb.client.shared.model.CIndicator;

/**
 * Panel that displays a set of indicator panels and adjust its layout
 * Created by ricardo on 28/07/14.
 */
public class IndicatorsPanel extends Composite {

    private FlowPanel pnlContent;

    /**
     * Default constructor
     */
    public IndicatorsPanel() {
        pnlContent = new FlowPanel();
        pnlContent.setStyleName("indicators-panel");
        initWidget(pnlContent);
    }

    /**
     * Update the styles used in the indicators according to its status
     */
/*
    public void updateIndicatorsStyles() {
        boolean prevSingle = false;
        boolean endOfLine = true;
        for (int i = 0; i < pnlContent.getWidgetCount(); i++) {
            IndicatorWrapperPanel indPanel = (IndicatorWrapperPanel)pnlContent.getWidget(i);

            // indicator style should not be applied ?
            if (indPanel.isProprietaryStyle()) {
                continue;
            }

            String style = "indicator-view";

            IndicatorController controller = indPanel.getController();
            CIndicator ind = controller.getIndicator();
            if (ind.isSingleValue()) {
                if (!prevSingle) {
                    style += " line-break";
                }
                prevSingle = true;
            }
            else {
                // is end of line ?
                if ((endOfLine) || (prevSingle)) {
                    style += " line-break";
                }
                // evaluate size
                if ((ind.getSize() == null) || (ind.getSize() == 100)) {
                    style += " ind100";
                    endOfLine = true;
                }
                else {
                    style += " ind50";
                    if (!endOfLine) {
                        style += " margin-right";
                    }
                    endOfLine = false;
                }
            }
            indPanel.setStyleName(style);
        }
    }
*/

    /**
     * Update the style of the indicator using its given position in the list of indicators
     * @param index indicator position
     */
    protected void updateIndicatorStyle(int index) {
        IndicatorWrapperPanel pnl = getIndicatorPanel(index);

        if (pnl.isProprietaryStyle()) {
            return;
        }

        boolean prevSingle = false;
        boolean prev50 = false;
        if (index > 0) {
            // get information about previous indicator
            IndicatorWrapperPanel prev = getIndicatorPanel(index - 1);
            if (prev.getController().getIndicator().isSingleValue()) {
                prevSingle = true;
            }
            else {
                String s = prev.getStyleName();
                prev50 = s.contains("ind50") && s.contains("line-break");
            }
        }

        // initial style name
        String style = "indicator-view";

        IndicatorController controller = pnl.getController();
        CIndicator ind = controller.getIndicator();
        if (ind.isSingleValue()) {
            style += " margin-right";
            if (!prevSingle) {
                style += " line-break";
            }
            prevSingle = true;
        }
        else {
            // is end of line ?
            if ((!prev50) || (prevSingle)) {
                style += " line-break";
            }
            // evaluate size
            if ((ind.getSize() == null) || (ind.getSize() == 100)) {
                style += " ind100";
                prev50 = true;
            }
            else {
                style += " ind50";
                if (!prev50) {
                    style += " margin-right";
                }
                prev50 = !prev50;
            }
        }
        pnl.setStyleName(style);
    }


    /**
     * Return the number of indicators being displayed
     * @return int value
     */
    public int getIndicatorsCount() {
        return pnlContent.getWidgetCount();
    }


    /**
     * Return the indicator panel at the given index position
     * @param index the indicator panel index, starting at 0
     * @return instance of IndicatorWrapperPanel
     */
    public IndicatorWrapperPanel getIndicatorPanel(int index) {
        return (IndicatorWrapperPanel)pnlContent.getWidget(index);
    }


    /**
     * Remove all indicators being displayed
     */
    public void clear() {
        pnlContent.clear();
    }

    /**
     * Insert an indicator at a given position
     * @param pnl instance of IndicatorWrapperPanel to be inserted
     * @param index position to be inserted among the existing indicators
     */
    public void insert(IndicatorWrapperPanel pnl, int index) {
        pnlContent.insert(pnl, index);
        updateIndicatorStyle(pnlContent.getWidgetIndex(pnl));
    }


    /**
     * Add a new indicator panel at the end of the indicators being displayed
     * @param pnl the IndicatorWrapperPanel to be added
     */
    public void add(IndicatorWrapperPanel pnl) {
        pnlContent.add(pnl);
        updateIndicatorStyle(pnlContent.getWidgetIndex(pnl));
    }

    /**
     * Return the index position of the indicator panel among the others
     * @param pnl instance of IndicatorWrapperPanel
     * @return index position
     */
    public int getIndicatorIndex(IndicatorWrapperPanel pnl) {
        return pnlContent.getWidgetIndex(pnl);
    }

    /**
     * Remove an indicator
     * @param pnl instance of IndicatorWrapperPanel to be removed
     */
    public void remove(IndicatorWrapperPanel pnl) {
        pnlContent.remove(pnl);
    }

    /**
     * Remove indicator panel by its index
     * @param index index position of the indicator panel
     */
    public void remove(int index) {
        pnlContent.remove(index);
    }
}
