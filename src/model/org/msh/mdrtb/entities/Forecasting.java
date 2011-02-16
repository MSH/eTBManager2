package org.msh.mdrtb.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.mdrtb.entities.enums.MedicineLine;
import org.msh.mdrtb.entities.enums.UserView;
import org.msh.utils.date.DateUtils;


/**
 * Entity class to store the main information about a medicine forecasting 
 * @author Ricardo Memoria
 *
 */
@Entity
public class Forecasting extends WSObject implements Serializable {
	private static final long serialVersionUID = -4515050920327650318L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@Column(length=100, nullable=false)
	private String name;

	/**
	 * Date of reference, i.e, date where inventory was done
	 */
	private Date referenceDate;
	
	/**
	 * Initial date of the forecasting
	 */
	@Temporal(TemporalType.DATE)
	private Date iniDate;

	
	/**
	 * Final date of the forecasting
	 */
	@Temporal(TemporalType.DATE)
	private Date endDate;
	
	/**
	 * Buffer stock of the forecasting (in months) 
	 */
	private int bufferStock;

	/**
	 * Check if cases on treatment are read from the database
	 */
	private boolean casesFromDatabase = true;

	/**
	 * Medicine line to be included - first line or second line
	 */
	private MedicineLine medicineLine;
	
	/**
	 * Register the number of cases on treatment at the reference date 
	 */
	private int numCasesOnTreatment;
	
	
	private int leadTime;
	
	private LeadTimeMeasuring leadTimeMeasuring;


	@OneToMany(cascade={CascadeType.ALL}, mappedBy="forecasting")
	private List<ForecastingResult> results = new ArrayList<ForecastingResult>();
		
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="forecasting")
	private List<ForecastingRegimen> regimens = new ArrayList<ForecastingRegimen>();

	@OneToMany(cascade={CascadeType.ALL}, mappedBy="forecasting")
	private List<ForecastingMedicine> medicines = new ArrayList<ForecastingMedicine>();
	
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="forecasting")
	private List<ForecastingCasesOnTreat> casesOnTreatment = new ArrayList<ForecastingCasesOnTreat>();
	
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="forecasting")
	private List<ForecastingNewCases> newCases = new ArrayList<ForecastingNewCases>();
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date recordingDate;
	
	@Column(name="FORECASTING_VIEW")
	private UserView view = UserView.COUNTRY;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ADMINUNIT_ID")
	private AdministrativeUnit administrativeUnit;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TBUNIT_ID")
	private Tbunit tbunit;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USER_ID")
	private User user;
	
	@Lob
	private String comment;

	
	/**
	 * Check if forecasting has results, i.e, if it was executed before
	 * @return true if has results
	 */
	public boolean isHasResult() {
		return (results != null) && (results.size() > 0);
	}


	/**
	 * Return the initial date of forecasting considering the forecasting
	 * @return
	 */
	public Date getIniDateLeadTime() {
		if ((referenceDate == null) || (iniDate == null))
			return null;

		Date dt = DateUtils.incMonths(referenceDate, leadTime);
		if (dt.after(iniDate))
			 return dt;
		else return iniDate;
/*		int months = DateUtils.monthsBetween(referenceDate, iniDate);
		if (months > leadTime)
			 return iniDate;
		else return DateUtils.incMonths(iniDate, leadTime - months);
*/	}
	
	/**
	 * Clear all results
	 */
	public void clearResults() {
		results.clear();
		for (ForecastingMedicine med: medicines)
			med.refreshResults();
	}
	
	
	/**
	 * Forecasting is for first line medicines
	 * @return
	 */
	protected boolean isFirstLine() {
		return (( medicineLine == null) || ( medicineLine == MedicineLine.FIRST_LINE));
	}


	/**
	 * Forecasting is for second line medicines
	 * @return
	 */
	protected boolean isSecondLine() {
		return (( medicineLine == null) || ( medicineLine == MedicineLine.SECOND_LINE));
	}


	/**
	 * Return the number of months of the forecating (including lead time and review period)
	 * @return number of months
	 */
	public int getNumMonths() {
		Date endForecasting = DateUtils.incMonths(endDate, bufferStock);
		Calendar cini = Calendar.getInstance();
		cini.setTime(referenceDate);
		cini.set(Calendar.DAY_OF_MONTH, 1);
		
		Calendar cend = Calendar.getInstance();
		cend.setTime(endForecasting);
		cend.set(Calendar.DAY_OF_MONTH, 2);
		
		return DateUtils.monthsBetween(cini.getTime(), cend.getTime());
	}


	/**
	 * Initialize the results, clearing all values and creating list of objects {@link ForecastingResult} 
	 * according to the medicines and number of months of forecasting
	 */
	public void initializeResults() {
		getResults().clear();
		int num = getNumMonths();
		for (ForecastingMedicine med: getMedicines()) {
			med.setEstimatedQtyCases(0);
			med.setEstimatedQtyNewCases(0);
			med.setDispensingLeadTime(0);
			med.setQuantityExpired(0);
			med.setStockOnOrder(0);
			med.setStockOnOrderLT(0);
			med.setStockOnHand(0);
			med.setStockOnHandLT(0);

			for (int i = 0; i <= num; i++) {
				ForecastingResult res = new ForecastingResult();
				res.setForecasting(this);
				res.setMedicine(med.getMedicine());
				res.setMonthIndex(i);
				getResults().add(res);
			}
		}
	}


	/**
	 * Find a forecasting result by the medicine and month index
	 * @param med
	 * @param monthIndex
	 * @return
	 */
	public ForecastingResult findResult(Medicine med, int monthIndex) {
		for (ForecastingResult aux: getResults()) {
			if ((aux.getMedicine().equals(med)) && (aux.getMonthIndex() == monthIndex)) {
				return aux;
			}
		}
		return null;
	}


	/**
	 * Search for an instance of a {@link ForecastingMedicine} class by its medicine id
	 * @param id
	 * @return
	 */
	public ForecastingMedicine findMedicineById(Integer id) {
		for (ForecastingMedicine fm: getMedicines()) {
			if (fm.getMedicine().getId().equals(id)) {
				return fm;
			}
		}
		return null;
	}


	/**
	 * Search for instance of {@link ForecastingCasesOnTreat} class by its regimen and month index
	 * @param reg
	 * @param monthIndex
	 * @return instance of {@link ForecastingCasesOnTreat} class
	 */
	public ForecastingCasesOnTreat findCaseInfoByRegimen(Regimen reg, int monthIndex) {
		for (ForecastingCasesOnTreat c: getCasesOnTreatment()) {
			if ((c.getMonthIndex() == monthIndex) && (c.getRegimen().equals(reg))) {
				return c;
			}
		}
		
		return null;
	}


	/**
	 * Search for regimen information by a specific regimen
	 * @param reg
	 * @return instance of {@link ForecastingRegimen} class
	 */
	public ForecastingRegimen findRegimen(Regimen reg) {
		for (ForecastingRegimen aux: getRegimens()) {
			if (aux.getRegimen().equals(reg)) {
				return aux;
			}
		}
		return null;
	}


	/**
	 * Search for an instance of {@link ForecastingNewCases} class by its month index
	 * @param monthIndex
	 * @return
	 */
	public ForecastingNewCases findNewCasesInfo(int monthIndex) {
		for (ForecastingNewCases f: getNewCases()) {
			if (f.getMonthIndex() == monthIndex)
				return f;
		}
		return null;
	}

	public Date getIniDate() {
		return iniDate;
	}
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getRecordingDate() {
		return recordingDate;
	}

	public void setRecordingDate(Date recordingDate) {
		this.recordingDate = recordingDate;
	}

	public List<ForecastingRegimen> getRegimens() {
		return regimens;
	}

	public void setRegimens(List<ForecastingRegimen> regimens) {
		this.regimens = regimens;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public UserView getView() {
		return view;
	}

	public void setView(UserView view) {
		this.view = view;
	}

	public Tbunit getTbunit() {
		return tbunit;
	}

	public void setTbunit(Tbunit tbunit) {
		this.tbunit = tbunit;
	}

	public List<ForecastingMedicine> getMedicines() {
		return medicines;
	}

	public void setMedicines(List<ForecastingMedicine> medicines) {
		this.medicines = medicines;
	}


	/**
	 * @return the bufferStock
	 */
	public int getBufferStock() {
		return bufferStock;
	}

	/**
	 * @param bufferStock the bufferStock to set
	 */
	public void setBufferStock(int bufferStock) {
		this.bufferStock = bufferStock;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @return the medicineLine
	 */
	public MedicineLine getMedicineLine() {
		return medicineLine;
	}

	/**
	 * @param medicineLine the medicineLine to set
	 */
	public void setMedicineLine(MedicineLine medicineLine) {
		this.medicineLine = medicineLine;
	}

	/**
	 * @return the administrativeUnit
	 */
	public AdministrativeUnit getAdministrativeUnit() {
		return administrativeUnit;
	}

	/**
	 * @param administrativeUnit the administrativeUnit to set
	 */
	public void setAdministrativeUnit(AdministrativeUnit administrativeUnit) {
		this.administrativeUnit = administrativeUnit;
	}

	public Date getReferenceDate() {
		return referenceDate;
	}

	public void setReferenceDate(Date referenceDate) {
		this.referenceDate = referenceDate;
	}

	public boolean isCasesFromDatabase() {
		return casesFromDatabase;
	}

	public void setCasesFromDatabase(boolean casesFromDatabase) {
		this.casesFromDatabase = casesFromDatabase;
	}

	public void setIniDate(Date iniDate) {
		this.iniDate = iniDate;
	}

	public List<ForecastingResult> getResults() {
		return results;
	}

	public void setResults(List<ForecastingResult> results) {
		this.results = results;
	}

	public int getNumCasesOnTreatment() {
		return numCasesOnTreatment;
	}

	public void setNumCasesOnTreatment(int numCasesOnTreatment) {
		this.numCasesOnTreatment = numCasesOnTreatment;
	}

	public List<ForecastingCasesOnTreat> getCasesOnTreatment() {
		return casesOnTreatment;
	}

	public void setCasesOnTreatment(List<ForecastingCasesOnTreat> casesOnTreatment) {
		this.casesOnTreatment = casesOnTreatment;
	}

	public List<ForecastingNewCases> getNewCases() {
		return newCases;
	}

	public void setNewCases(List<ForecastingNewCases> newCases) {
		this.newCases = newCases;
	}

	/**
	 * Return the month index of the date relative to the reference date
	 * @param dt
	 * @return
	 */
	public int getMonthIndex(Date dt) {
		int numIni = DateUtils.monthOf(referenceDate) + (DateUtils.yearOf(referenceDate) * 12);
		int numEnd = DateUtils.monthOf(dt) + (DateUtils.yearOf(dt) * 12);
		return numEnd - numIni;
	}
	
	/**
	 * Return the initial date of the month index
	 * @param monthIndex
	 * @return
	 */
	public Date getIniDateMonthIndex(int monthIndex) {
		if ((referenceDate == null) || (endDate == null))
			return null;

		if (monthIndex == 0)
			return referenceDate;
		
		Date dt = DateUtils.incMonths(referenceDate, monthIndex);
		int month = DateUtils.monthOf(dt);
		int year = DateUtils.yearOf(dt);
		
		dt = DateUtils.newDate(year, month, 1);
		Date dtend = DateUtils.incMonths(endDate, bufferStock);
		return (dt.after(dtend)? null: dt);
	}


	/**
	 * Return the initial date of the month index
	 * @param monthIndex
	 * @return
	 */
	public Date getEndDateMonthIndex(int monthIndex) {
		if (referenceDate == null)
			return null;

		Date dt = DateUtils.incMonths(referenceDate, monthIndex);
		int month = DateUtils.monthOf(dt);
		int year = DateUtils.yearOf(dt);
		dt = DateUtils.newDate(year, month, DateUtils.daysInAMonth(year, month));
		Date dtend = DateUtils.incMonths(endDate, bufferStock);
		return (dt.after(dtend)? dtend: dt);
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	/**
	 * Lead time measure options
	 * @author Ricardo Memoria
	 *
	 */
	public enum LeadTimeMeasuring {
		DAYS,
		MONTHS;
		
		public String getKey() {
			if (DAYS.equals(this))
				 return "global.days";
			else return "global.months";
		}
	}

	public int getLeadTime() {
		return leadTime;
	}


	public void setLeadTime(int leadTime) {
		this.leadTime = leadTime;
	}


	public LeadTimeMeasuring getLeadTimeMeasuring() {
		return leadTimeMeasuring;
	}


	public void setLeadTimeMeasuring(LeadTimeMeasuring leadTimeMeasuring) {
		this.leadTimeMeasuring = leadTimeMeasuring;
	}
}
