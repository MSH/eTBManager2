package org.msh.tb.client.tableview;

/**
 * Store information about a table cell coordinate
 *
 * Created by Ricardo on 23/07/2014.
 */
public class Cell {

    private int column;
    private int row;

    /**
     * Default constructor
     * @param column information about the column
     * @param row information about the row
     */
    public Cell(int column, int row) {
        this.column = column;
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }
}
