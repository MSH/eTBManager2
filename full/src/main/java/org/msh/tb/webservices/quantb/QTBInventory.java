package org.msh.tb.webservices.quantb;

import java.util.Date;
import java.util.List;

/**
 * Created by ricardo on 09/12/14.
 */
public class QTBInventory {
    private Integer medicineId;
    private String batchNumber;
    private String manufacturer;
    private Date expiryDate;
    private Integer quantity;
    private List<QTBOrder> orders;


    public Integer getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Integer medicineId) {
        this.medicineId = medicineId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public List<QTBOrder> getOrders() {
        return orders;
    }

    public void setOrders(List<QTBOrder> orders) {
        this.orders = orders;
    }
}
