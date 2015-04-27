package org.msh.etbm.services.quantb;

import java.util.Date;

/**
 * Created by ricardo on 09/12/14.
 */
public class QTBOrder {
    private Date arrivalDate;
    private int quantity;

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
