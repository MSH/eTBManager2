package org.msh.tb.client.reports.filters;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.msh.tb.client.commons.StandardCallback;
import org.msh.tb.client.shared.model.CFilter;
import org.msh.tb.client.shared.model.CItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmemoria on 16/11/15.
 */
public class AdminUnitFilter extends FilterWidget {
    private VerticalPanel panel;
    private List<ListBox> lst;

    public AdminUnitFilter() {
        panel = new VerticalPanel();

        // create list boxes for administrative units
        lst = new ArrayList<ListBox>();
        for (int i = 0; i < 5; i++) {
            ListBox lb = createListBox();
            // the first list box is visible
            lb.setVisible( i == 0 );
            lst.add(lb);
            panel.add(lb);
        }

        initWidget(panel);
    }

    @Override
    public void initialize(CFilter filterData, String iniValue) {
        super.initialize(filterData, iniValue);
        loadAdminUnits(lst.get(0), true);
    }

    /**
     * Crete a list box
     * @return
     */
    protected ListBox createListBox() {
        ListBox lb = new ListBox();
        lb.setVisibleItemCount(1);
        lb.setWidth("300px");
        lb.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                ListBox lb = (ListBox)changeEvent.getSource();
                adminUnitChanged(lb);
            }
        });

        return lb;
    }


    protected String getId(int index) {
        String id = null;
        for (int i = 0; i <= index; i++) {
            ListBox lb = lst.get(i);
            if (lb.isVisible() && lb.getSelectedIndex() > 0) {
                id = lb.getValue(lb.getSelectedIndex());
            }
            else {
                break;
            }
        }
        return id;
    }

    /**
     * Called when the selection in the list box with administrative units is changed
     */
    protected void adminUnitChanged(ListBox lb) {
        loadAdminUnits(lb, false);
        notifyFilterChange();
    }

    /**
     * Load administrative units based on the given list box
     * @param lb the list box
     */
    protected void loadAdminUnits(ListBox lb ,boolean initializing) {
        int index = lst.indexOf(lb);

        if (index == 4) {
            return;
        }

        if (index < 5) {
            ListBox lb2 = lst.get(index + 1);
            boolean b = lb.getSelectedIndex() > 0;
            lb2.setVisible(b);
            lb2.clear();

            // hide the other list boxes
            for (int i = index + 2; i < 5; i++) {
                lst.get(i).setVisible( false );
            }
        }

        int i = lb.getSelectedIndex();
        String id = i > 0? lb.getValue(i): null;

        if (id != null || initializing) {
            // update TB unit list
            final String fid = id;
            final ListBox sellb = initializing? lb : lst.get(index + 1);
            loadServerOptions(fid, new StandardCallback<ArrayList<CItem>>() {
                @Override
                public void onSuccess(ArrayList<CItem> result) {
                    if (result != null) {
                        fillListOptions(sellb, result, null);
                    }
                }
            });
        }
    }

    @Override
    public void setValue(String value) {
        // TODO
    }

    @Override
    public String getValue() {
        String val = getId(4);
        return val;
    }
}
