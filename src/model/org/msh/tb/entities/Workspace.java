package org.msh.tb.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.jboss.seam.international.LocaleSelector;
import org.msh.tb.entities.enums.DisplayCaseNumber;
import org.msh.tb.entities.enums.NameComposition;
import org.msh.tb.log.FieldLog;

@TypeDefs({@TypeDef(name="weeklyFrequency", typeClass=WeeklyFrequencyType.class)})

@Entity
public class Workspace implements Serializable {
	private static final long serialVersionUID = -7496421288607921489L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@Embedded
	@FieldLog(key="form.name")
	private LocalizedNameComp name = new LocalizedNameComp();

	@OneToMany(mappedBy="workspace", cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@FieldLog(ignore=true)
	private List<UserWorkspace> users = new ArrayList<UserWorkspace>();

	@Column(length=150)
	private String description;

	@Column(length=10)
	private String defaultLocale;
	
	@Column(length=10)
	private String alternateLocale;
	
	@Column(length=200)
	private String defaultTimeZone;

	// frequency of doses in a weekly basis
	@Type(type="org.msh.tb.entities.WeeklyFrequencyType")
	private WeeklyFrequency weekFreq1 = new WeeklyFrequency();
	@Type(type="org.msh.tb.entities.WeeklyFrequencyType")
	private WeeklyFrequency weekFreq2 = new WeeklyFrequency();
	@Type(type="org.msh.tb.entities.WeeklyFrequencyType")
	private WeeklyFrequency weekFreq3 = new WeeklyFrequency();
	@Type(type="org.msh.tb.entities.WeeklyFrequencyType")
	private WeeklyFrequency weekFreq4 = new WeeklyFrequency();
	@Type(type="org.msh.tb.entities.WeeklyFrequencyType")
	private WeeklyFrequency weekFreq5 = new WeeklyFrequency();
	@Type(type="org.msh.tb.entities.WeeklyFrequencyType")
	private WeeklyFrequency weekFreq6 = new WeeklyFrequency();
	@Type(type="org.msh.tb.entities.WeeklyFrequencyType")
	private WeeklyFrequency weekFreq7 = new WeeklyFrequency();

	@Column(length=10)
	private String extension;

	@OneToOne(cascade={CascadeType.REFRESH, CascadeType.REMOVE}, fetch=FetchType.LAZY)
	@PrimaryKeyJoinColumn
	private WorkspaceView view;


	/**
	 * Setup the patient name composition to be used in data entry forms and displaying
	 */
	private NameComposition patientNameComposition;
	
	/**
	 * Setup if treatment may start before or after validation of the case 
	 */
	private boolean startTBTreatBeforeValidation;
	
	private boolean startDRTBTreatBeforeValidation;
	
	/**
	 * Setup the case number to be displayed for the cases
	 */
	private DisplayCaseNumber displayCaseNumber;
	
	/**
	 * Required levels of administrative unit for patient address
	 */
	private Integer patientAddrRequiredLevels;
	
	@Column(length=200)
	private String url;
	
	


	public WeeklyFrequency[] getWeeklyFrequencies() {
		WeeklyFrequency[] lst = new WeeklyFrequency[7];

		lst[0] = weekFreq1;
		lst[1] = weekFreq2;
		lst[2] = weekFreq3;
		lst[3] = weekFreq4;
		lst[4] = weekFreq5;
		lst[5] = weekFreq6;
		lst[6] = weekFreq7;
		
		return lst;
	}
	
	public void setWeeklyFrequency(WeeklyFrequency[] vals) {
		weekFreq1 = vals[0];
		weekFreq2 = vals[1];
		weekFreq3 = vals[2];
		weekFreq4 = vals[3];
		weekFreq5 = vals[4];
		weekFreq6 = vals[5];
		weekFreq7 = vals[6];
	}
	
	public WeeklyFrequency getWeeklyFrequency(int frequency) {
		switch (frequency) {
		case 1: return weekFreq1;
		case 2: return weekFreq2;
		case 3: return weekFreq3;
		case 4: return weekFreq4;
		case 5: return weekFreq5;
		case 6: return weekFreq6;
		case 7: return weekFreq7;
		}
		return null;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (!(obj instanceof Workspace))
			return false;

		return ((Workspace)obj).getId().equals(getId());
	}

	
	@Override
	public String toString() {
		return getName().toString();
	}
	
	public boolean isHasAlternateLocale() {
		return ((getAlternateLocale() != null) && (!alternateLocale.isEmpty()));
	}
	
	/**
	 * Returns the name of the language according to the locale
	 * @param s
	 * @return
	 */
	protected String getLocaleDisplayName(String s) {
		if ((s == null) || (s.isEmpty()))
			return null;
		
		int p = s.indexOf("_");
		Locale loc = null;
		if (p != -1) {
			String lan = s.substring(0, p);
			String lc = s.substring(p+1);
			loc = new Locale(lan, lc);
		}
		else  loc = new Locale(s);
		
		return loc.getDisplayName(LocaleSelector.instance().getLocale());		
	}

	public String getDefaultDisplayLocale() {
		String s = getDefaultLocale();
		return getLocaleDisplayName(s);
	}
	
	public String getAlternateDisplayLocale() {
		return getLocaleDisplayName(getAlternateLocale());
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}


	/**
	 * @return the name
	 */
	public LocalizedNameComp getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(LocalizedNameComp name) {
		this.name = name;
	}

	public List<UserWorkspace> getUsers() {
		return users;
	}

	public void setUsers(List<UserWorkspace> users) {
		this.users = users;
	}

	public String getDefaultLocale() {
		return defaultLocale;
	}

	public void setDefaultLocale(String defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	public String getDefaultTimeZone() {
		return defaultTimeZone;
	}

	public void setDefaultTimeZone(String defaultTimeZone) {
		this.defaultTimeZone = defaultTimeZone;
	}

	public WeeklyFrequency getWeekFreq1() {
		return weekFreq1;
	}

	public void setWeekFreq1(WeeklyFrequency weekFreq1) {
		this.weekFreq1 = weekFreq1;
	}

	public WeeklyFrequency getWeekFreq2() {
		return weekFreq2;
	}

	public void setWeekFreq2(WeeklyFrequency weekFreq2) {
		this.weekFreq2 = weekFreq2;
	}

	public WeeklyFrequency getWeekFreq3() {
		return weekFreq3;
	}

	public void setWeekFreq3(WeeklyFrequency weekFreq3) {
		this.weekFreq3 = weekFreq3;
	}

	public WeeklyFrequency getWeekFreq4() {
		return weekFreq4;
	}

	public void setWeekFreq4(WeeklyFrequency weekFreq4) {
		this.weekFreq4 = weekFreq4;
	}

	public WeeklyFrequency getWeekFreq5() {
		return weekFreq5;
	}

	public void setWeekFreq5(WeeklyFrequency weekFreq5) {
		this.weekFreq5 = weekFreq5;
	}

	public WeeklyFrequency getWeekFreq6() {
		return weekFreq6;
	}

	public void setWeekFreq6(WeeklyFrequency weekFreq6) {
		this.weekFreq6 = weekFreq6;
	}

	public WeeklyFrequency getWeekFreq7() {
		return weekFreq7;
	}

	public void setWeekFreq7(WeeklyFrequency weekFreq7) {
		this.weekFreq7 = weekFreq7;
	}

	public String getAlternateLocale() {
		return alternateLocale;
	}

	public void setAlternateLocale(String alternateLocale) {
		this.alternateLocale = alternateLocale;
	}

	/**
	 * Returns the root path of the custom pages for the country. Ex: /brazil or /ukraine 
	 * @return
	 */
/*	public String getRootPath() {
		return ((customPath == null)||(customPath.isEmpty()) ? "/custom/generic": customPath);
	}
*/

	/**
	 * @return the patientNameComposition
	 */
	public NameComposition getPatientNameComposition() {
		return patientNameComposition;
	}

	/**
	 * @param patientNameComposition the patientNameComposition to set
	 */
	public void setPatientNameComposition(NameComposition patientNameComposition) {
		this.patientNameComposition = patientNameComposition;
	}

	/**
	 * @return the displayCaseNumber
	 */
	public DisplayCaseNumber getDisplayCaseNumber() {
		return displayCaseNumber;
	}

	/**
	 * @param displayCaseNumber the displayCaseNumber to set
	 */
	public void setDisplayCaseNumber(DisplayCaseNumber displayCaseNumber) {
		this.displayCaseNumber = displayCaseNumber;
	}

	/**
	 * @return the patientAddrRequiredLevels
	 */
	public Integer getPatientAddrRequiredLevels() {
		return patientAddrRequiredLevels;
	}

	/**
	 * @param patientAddrRequiredLevels the patientAddrRequiredLevels to set
	 */
	public void setPatientAddrRequiredLevels(Integer patientAddrRequiredLevels) {
		this.patientAddrRequiredLevels = patientAddrRequiredLevels;
	}

	/**
	 * @return the extension
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * @param extension the extension to set
	 */
	public void setExtension(String extension) {
		this.extension = extension;
	}

	/**
	 * @return the view
	 */
	public WorkspaceView getView() {
		return view;
	}

	/**
	 * @param view the view to set
	 */
	public void setView(WorkspaceView view) {
		this.view = view;
	}

	public boolean isStartTBTreatBeforeValidation() {
		return startTBTreatBeforeValidation;
	}

	public void setStartTBTreatBeforeValidation(boolean startTBTreatBeforeValidation) {
		this.startTBTreatBeforeValidation = startTBTreatBeforeValidation;
	}

	public boolean isStartDRTBTreatBeforeValidation() {
		return startDRTBTreatBeforeValidation;
	}

	public void setStartDRTBTreatBeforeValidation(
			boolean startDRTBTreatBeforeValidation) {
		this.startDRTBTreatBeforeValidation = startDRTBTreatBeforeValidation;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

}
