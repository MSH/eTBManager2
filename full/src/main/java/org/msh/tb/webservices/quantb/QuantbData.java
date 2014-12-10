package org.msh.tb.webservices.quantb;

import org.msh.tb.webservices.Response;

import java.util.List;

/**
 * Created by ricardo on 09/12/14.
 */
public class QuantbData extends Response {
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
