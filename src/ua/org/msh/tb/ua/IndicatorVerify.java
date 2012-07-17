package org.msh.tb.ua;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.In;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorController;
import org.msh.tb.indicators.core.IndicatorFilters;

public abstract class IndicatorVerify extends Indicator2D {
	private static final long serialVersionUID = -2348070104654096945L;

	@In(create=true) IndicatorController indicatorController;
	
	/**
	 * List of errors and warnings in TbCase data, allowing in reports 
	 * Parameters of Map means category and list of types of errors
	 * */
	private Map<String,List<ErrItem>> verifyList;
	
	/**
	 * Define what type of SELECT in HQL query we need now
	 * */
	private boolean counting;
	/**
	 * @true if items in HQL query more than 4000
	 * */
	private boolean overflow;
	
	/**
	 * Class of elements of VerifyList
	 * */
	public class ErrItem {
		public ErrItem(List<TbCase> ltc,String t){
			super();
			this.caseList = ltc;
			this.title = t;
		}
		/**
		 * List of TbCase, allowing for current category and type of error
		 * */
		private List<TbCase> caseList;
		/**
		 * Title of current type of error
		 * */
		private String title;
		
		/**
		 * @return the caseList
		 * */
		public List<TbCase> getCaseList() {
			return caseList;
		}
		
		/**
		 * @param caseList the caseList to set
		 * */
		public void setCaseList(List<TbCase> caseList) {
			this.caseList = caseList;
		}
		
		/**
		 * @return the title
		 * */
		public String getTitle() {
			return title;
		}
		
		/**
		 * @param title the title to set
		 * */
		public void setTitle(String title) {
			this.title = title;
		}
	}

	public boolean isCounting() {
		return counting;
	}

	public void setCounting(boolean counting) {
		this.counting = counting;
	}

	public boolean isOverflow() {
		return overflow;
	}

	public void setOverflow(boolean overflow) {
		this.overflow = overflow;
	}
	
	public Map<String, List<ErrItem>> getVerifyList() {
		return verifyList;
	}
	
	/**
	 * @return VerifyList or create it, if it null 
	 * */
	public Map<String, List<ErrItem>> getList() {
		if (verifyList==null)
			if (indicatorController.isExecuting())
				createIndicators();
		return verifyList;
	}

	public void setVerifyList(Map<String, List<ErrItem>> verifyList) {
		this.verifyList = verifyList;
	}

	/**
	 * Sort all cases in caseList in elements of VerifyList allow for locale
	 * */
	protected void sortAllLists() {
		if (verifyList!=null)
			for (String key:verifyList.keySet()){
				for (ErrItem it:verifyList.get(key)){
					Collections.sort(it.getCaseList(), new Comparator<TbCase>() {
						  public int compare(TbCase o1, TbCase o2) {
							String name1, name2;
							name1 = o1.getPatient().getFullName();
							name2 = o2.getPatient().getFullName();
						
							if (name1.equals(name2)){
								name2 = name1+"_"+o2.getId();
							}
							Collator myCollator = Collator.getInstance();			    
							return myCollator.compare(name1,name2);
						  }
					});
				}
			}
		
	}
	
	/**
	 * Create new VerifyList
	 * @param cat1 set number of types in category1 (not allowing cases)
	 * @param cat2 set number of types in category2 (allowing cases, but with warnings)
	 * @param cat3 set number of types in category3 (cases, allowing in report)
	 * */
	protected void initVerifList(String errmes, int cat1,int cat2,int cat3){
		Map<String, List<ErrItem>>  verifyList = new LinkedHashMap<String,List<ErrItem>>();
		for (int i = 1; i < 4; i++) {
			verifyList.put(getMessage("verify.errorcat"+i),new ArrayList<ErrItem>());
			switch (i){
				case 1:{
					for (int j = 1; j <= cat1; j++) {
						verifyList.get(getMessage("verify.errorcat"+i)).add(new ErrItem(new ArrayList<TbCase>(), getMessage(errmes+i+j)));
					}
					break;
				}
				case 2:{
					for (int j = 1; j <= cat2; j++) {
						verifyList.get(getMessage("verify.errorcat"+i)).add(new ErrItem(new ArrayList<TbCase>(), getMessage(errmes+i+j)));
					}
					break;
				}
				case 3:{
					for (int j = 1; j <= cat3; j++) {
						verifyList.get(getMessage("verify.errorcat"+i)).add(new ErrItem(new ArrayList<TbCase>(), getMessage(errmes+i+j)));
					}
					break;
				}
			}
		}
		setVerifyList(verifyList);
	}
	
	/**
	 * Add TbCase to category2 (allowing cases, but with warnings)
	 * */
	protected abstract void addToAllowing(TbCase tc);
	
	/**
	 * Add TbCase to category3 (allowing cases), if it not contains in all subcategories of category2
	 * */
	protected void addToRepList(TbCase tc){
		addToAllowing(tc);
		for (ErrItem ls: verifyList.get(getMessage("verify.errorcat2"))) {
			if (ls.getCaseList().contains(tc))
				return;
		}
		addToVerList(tc,3,0);
	}
	
	@Override
	protected String getHQLFrom() {
		return "from TbCase c";
	}

	@Override
	protected String getHQLSelect() {
		if (isCounting()) 
			return "select count(*)";
		else
			return "select c";
	}
	
	@Override
	protected String getHQLJoin() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.core.CaseHQLBase#getHQLInfectionSite()
	 */
	@Override
	protected String getHQLInfectionSite() {
		IndicatorFilters filters = getIndicatorFilters();
		if  (filters.getInfectionSite() == InfectionSite.PULMONARY)
			return "c.infectionSite in (" + InfectionSite.PULMONARY.ordinal() + "," + InfectionSite.BOTH.ordinal() + ")";
		else return super.getHQLInfectionSite();
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.core.CaseHQLBase#getHQLValidationState()
	 */
	protected String getHQLValidationState() {
		return "c.validationState = "+ValidationState.VALIDATED.ordinal();
	}
	
	/**
	 * Fill tables of report by data
	 * */
	protected abstract void generateTables();
	
	/**
	 * @return true if case don't have necessary result of microscopy
	 * */
	protected boolean MicroscopyIsNull(TbCase tc){
		if (tc.getExamsMicroscopy()==null)	return true;
		else
			if (tc.getExamsMicroscopy().size()==0) return true;
		if (rightMcTest(tc)==null) return true;
		return false;
	}
	
	/**
	 * @return true if case don't have necessary result of culture
	 * */
	protected boolean CultureIsNull(TbCase tc){
		if (tc.getExamsCulture()==null)	return true;
		else
			if (tc.getExamsCulture().size()==0) return true;
		if (rightCulTest(tc)==null) return true;
		return false;
	}
	
	/**
	 * Add case in category cat in subcategory num_er
	 * @param cat key of category
	 * @param num_er index of subcategory
	 * */
	protected void addToVerList(TbCase tc, int cat,int num_er){
		if (!getVerifyList().get(getMessage("verify.errorcat"+cat)).get(num_er).getCaseList().contains(tc))
			getVerifyList().get(getMessage("verify.errorcat"+cat)).get(num_er).getCaseList().add(tc);
	}
	
	/**
	 * @return microscopy test, which is up to quality, namely first month of treatment and worst test from several 
	 */
	protected ExamMicroscopy rightMcTest(TbCase tc){
		Calendar dateTest = Calendar.getInstance();
		Calendar dateIniTreat = Calendar.getInstance();
		int res=0;
		ArrayList<ExamMicroscopy> lst = new ArrayList<ExamMicroscopy>();
		for (ExamMicroscopy ex: tc.getExamsMicroscopy()){
			dateTest.setTime(ex.getDateCollected());
			if (tc.getTreatmentPeriod()!=null)
				dateIniTreat.setTime(tc.getTreatmentPeriod().getIniDate());
			else
				dateIniTreat.setTime(tc.getDiagnosisDate());
			Calendar regDt = Calendar.getInstance();
			regDt.setTime(tc.getRegistrationDate());
			long testperiod = dateTest.getTimeInMillis()-regDt.getTimeInMillis();
			testperiod/=86400000;

			if (testperiod<=30) {
				res++;
				lst.add(ex);
			}	
		}

		switch (lst.size()) {
		case 0: 
			return null;
		case 1: 
			return lst.get(0);
		default:
			return WorstMcRes(lst);
		}
	}
	
	/**
	 * @return culture test, which is up to quality, namely first month of treatment and worst test from several 
	 */
	protected ExamCulture rightCulTest(TbCase tc){
		Calendar dateTest = Calendar.getInstance();
		Calendar dateIniTreat = Calendar.getInstance();
		int res=0;
		ArrayList<ExamCulture> lst = new ArrayList<ExamCulture>();
		for (ExamCulture ex: tc.getExamsCulture()){
			dateTest.setTime(ex.getDateCollected());
			if (tc.getTreatmentPeriod()!=null)
				dateIniTreat.setTime(tc.getTreatmentPeriod().getIniDate());
			else
				dateIniTreat.setTime(tc.getDiagnosisDate());
			Calendar regDt = Calendar.getInstance();
			regDt.setTime(tc.getRegistrationDate());
			long testperiod = dateTest.getTimeInMillis()-regDt.getTimeInMillis();
			testperiod/=86400000;

			if (testperiod<=30) {
				res++;
				lst.add(ex);
			}	
		}

		switch (lst.size()) {
		case 0: 
			return null;
		case 1: 
			return lst.get(0);
		default:
			return WorstCulRes(lst);
		}
	}

	/**
	 * @return dst-test, which is up to quality, namely first month of treatment and worst test from several 
	 */
	protected ExamDST rightDSTTest(TbCase tc){
		Calendar dateTest = Calendar.getInstance();
		Calendar dateIniTreat = Calendar.getInstance();
		int res=0;
		ArrayList<ExamDST> lst = new ArrayList<ExamDST>();
		for (ExamDST ex: tc.getExamsDST()){
			dateTest.setTime(ex.getDateCollected());
			if (tc.getTreatmentPeriod()!=null)
				dateIniTreat.setTime(tc.getTreatmentPeriod().getIniDate());
			else
				dateIniTreat.setTime(tc.getDiagnosisDate());
			Calendar regDt = Calendar.getInstance();
			regDt.setTime(tc.getRegistrationDate());
			long testperiod = dateTest.getTimeInMillis()-regDt.getTimeInMillis();
			testperiod/=86400000;

			if (testperiod<=30) {
				res++;
				lst.add(ex);
			}	
		}

		switch (lst.size()) {
		case 0: 
			return null;
		case 1: 
			return lst.get(0);
		default:
			return WorstDSTRes(lst);
		}
	}
	
	/**
	 * @return worst microscopy-test from several 
	 */
	private ExamMicroscopy WorstMcRes(List<ExamMicroscopy> lst) {
		int tmp = 0;
		int max = Integer.MIN_VALUE;
		ExamMicroscopy minEl = null;
		for (ExamMicroscopy el:lst){
			tmp = 0;
			tmp = el.getResult().ordinal();
			/*for (ExamDSTResult exr: el.getResults()){
				tmp += (exr.getResult().ordinal()==0 ? 4 : exr.getResult().ordinal());
			}*/
			if (tmp > max) {
				max = tmp; 
				minEl=el;
			}
		}
		return minEl;
	}
	
	/**
	 * @return worst culture-test from several 
	 */
	private ExamCulture WorstCulRes(List<ExamCulture> lst) {
		int tmp = 0;
		int max = Integer.MIN_VALUE;
		ExamCulture minEl = null;
		for (ExamCulture el:lst){
			tmp = 0;
			tmp = el.getResult().ordinal();
			/*for (ExamDSTResult exr: el.getResults()){
				tmp += (exr.getResult().ordinal()==0 ? 4 : exr.getResult().ordinal());
			}*/
			if (tmp > max) {
				max = tmp; 
				minEl=el;
			}
		}
		return minEl;
	}

	/**
	 * @return worst DST-test from several 
	 */
	private ExamDST WorstDSTRes(List<ExamDST> lst) {
		int tmp = 0;
		int max = Integer.MIN_VALUE;
		ExamDST minEl = null;
		for (ExamDST el:lst){
			tmp = 0;
			tmp = el.getNumResistant();
			/*for (ExamDSTResult exr: el.getResults()){
				tmp += (exr.getResult().ordinal()==0 ? 4 : exr.getResult().ordinal());
			}*/
			if (tmp > max) {
				max = tmp; 
				minEl=el;
			}
		}
		return minEl;
	}
}
