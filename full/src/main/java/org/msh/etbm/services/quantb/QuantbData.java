package org.msh.etbm.services.quantb;

import org.msh.etbm.commons.apidoc.annotations.ApiDocField;

import java.util.List;

/**
 * Data to be returned to QuanTB (or any other service) with information to generate
 * a new forecasting
 *
 * Created by ricardo on 09/12/14.
 */
public class QuantbData {
    @ApiDocField(description = "List of medicines registered in the workspace")
    private List<QTBMedicine> medicines;

    @ApiDocField(description = "List of treatment regimens registered in the workspace")
    private List<QTBRegimen> regimens;

    @ApiDocField(description = "Consolidated number of cases on treatment by month and regimen")
    private List<QTBCases> cases;

    @ApiDocField(description = "Available quantity of medicines in the workspace")
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
