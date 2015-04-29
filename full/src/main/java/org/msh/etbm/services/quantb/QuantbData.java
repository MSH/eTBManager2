package org.msh.etbm.services.quantb;

import java.util.List;

/**
 * Data to be returned to QuanTB (or any other service) with information to generate
 * a new forecasting
 *
 * Created by ricardo on 09/12/14.
 */
public class QuantbData {
    private List<QTBMedicine> medicines;
    private List<QTBRegimen> regimens;

    private List<QTBCases> cases;
    private List<QTBInventory> inventory;

    public List<QTBMedicine> getMedicines() {
        return medicines;
    }

    public void setMedicines(List<QTBMedicine> medicines) {
        this.medicines = medicines;
    }

    public List<QTBRegimen> getRegimens() {
        return regimens;
    }

    public void setRegimens(List<QTBRegimen> regimens) {
        this.regimens = regimens;
    }

    public List<QTBCases> getCases() {
        return cases;
    }

    public void setCases(List<QTBCases> cases) {
        this.cases = cases;
    }

    public List<QTBInventory> getInventory() {
        return inventory;
    }

    public void setInventory(List<QTBInventory> inventory) {
        this.inventory = inventory;
    }
}
