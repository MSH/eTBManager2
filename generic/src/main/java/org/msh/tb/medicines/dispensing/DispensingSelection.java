package org.msh.tb.medicines.dispensing;

import java.util.Calendar;
import java.util.Date;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.utils.date.DateUtils;

@Name("dispensingSelection")
@AutoCreate
@BypassInterceptors
public class DispensingSelection {

    private Integer month;
    private Integer year;
    private Integer dispensingId;


    /**
     * Initialize the month and year to the current date
     */
    public void initialize() {
        if ((month != null) && (year != null))
            return;
        Calendar c = Calendar.getInstance();
        month = c.get(Calendar.MONTH);
        year = c.get(Calendar.YEAR);
    }

    /**
     * Check if month and year are selected or if one of them is null
     * @return
     */
    public boolean isMonthYearSelected() {
        return (month != null) && (year != null);
    }


    /**
     * Return the initial date with the first day of the month and year selected
     * @return
     */
    public Date getIniDate() {
        if (!isMonthYearSelected())
            return null;
        return DateUtils.newDate(year, month, 1);
    }

    /**
     * Return the final date with the last day of the month and year selected
     * @return
     */
    public Date getEndDate() {
        if (!isMonthYearSelected())
            return null;
        return DateUtils.newDate(year, month, DateUtils.daysInAMonth(year, month));
    }

    /**
     * @return the month
     */
    public Integer getMonth() {
        return month;
    }
    /**
     * @param month the month to set
     */
    public void setMonth(Integer month) {
        this.month = month;
    }
    /**
     * @return the year
     */
    public Integer getYear() {
        return year;
    }
    /**
     * @param year the year to set
     */
    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getDispensingId() {
        return dispensingId;
    }

    public void setDispensingId(Integer dispensingId) {
        this.dispensingId = dispensingId;
    }

}
