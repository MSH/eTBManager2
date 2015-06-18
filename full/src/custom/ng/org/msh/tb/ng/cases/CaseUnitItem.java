package org.msh.tb.ng.cases;

/**
 * Store results of the {@link CasesUnitReport} report
 *
 * Created by rmemoria on 17/6/15.
 */
public class CaseUnitItem {

    public enum UnitType { ADMINUNIT, HEALTHFACILITY };

    private int id;
    private UnitType type;
    private String name;
    private int numSuspects;
    private int numTB;
    private int numDRTB;
    private int numNTM;
    private boolean node;

    public CaseUnitItem() {
        super();
    }

    public CaseUnitItem(String name, Integer numSuspects, Integer numTB, Integer numDRTB, Integer numNTM) {
        this.name = name;
        this.numSuspects = numSuspects;
        this.numTB = numTB;
        this.numDRTB = numDRTB;
        this.numNTM = numNTM;
    }

    /**
     * Return the total
     * @return
     */
    public int getTotal() {
        return numSuspects + numTB + numDRTB + numNTM;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumSuspects() {
        return numSuspects;
    }

    public void setNumSuspects(Integer numSuspects) {
        this.numSuspects = numSuspects;
    }

    public int getNumTB() {
        return numTB;
    }

    public void setNumTB(Integer numTB) {
        this.numTB = numTB;
    }

    public int getNumDRTB() {
        return numDRTB;
    }

    public void setNumDRTB(Integer numDRTB) {
        this.numDRTB = numDRTB;
    }

    public int getNumNTM() {
        return numNTM;
    }

    public void setNumNTM(Integer numNTM) {
        this.numNTM = numNTM;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UnitType getType() {
        return type;
    }

    public void setType(UnitType type) {
        this.type = type;
    }

    public boolean isNode() {
        return node;
    }

    public void setNode(boolean node) {
        this.node = node;
    }
}
