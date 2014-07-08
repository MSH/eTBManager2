package org.msh.tb.indicators;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorController;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.utils.date.DateUtils;

/**
 * Base class supporting TB/MDR-TB indicator generation in a 2-dimension table with possibilities 
 * <br/> of verifying data in the process of constructing of indicator
 * A base class must be created and override abstracts methods {@link #createIndicators()}, 
 * {@link #generateTables()}, {@link #addToAllowing(TbCase)}
 * @author A.M.
 * @param <E>
 *
 */
public abstract class IndicatorVerify<E> extends Indicator2D {
	private static final long serialVersionUID = -2348070104654096945L;

	@In(create=true) IndicatorController indicatorController;
	
	/**
	 * List of errors and warnings in TbCase data, allowing in org.msh.reports
	 * Parameters of Map means category and list of types of errors
	 * */
	private Map<String,List<ErrItem>> verifyList;
	
	private Set<E> recordsInReport = new HashSet<E>();
	
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
	 * Add cases to category3 (allowing cases), if it not contains in all subcategories of category2
	 * */
	protected void generateRepList(List<E> lst){
		Iterator<E> it = lst.iterator();
		while(it.hasNext()){
			E tc = it.next();
			if(!recordsInReport.contains(tc))
				continue;
			boolean inWarn = false; 
			for (ErrItem ls: verifyList.get(getMessage("verify.errorcat1"))) {
				if (ls.getCaseList().contains(tc)){
					inWarn = true;
					continue;
				}
			}
			for (ErrItem ls: verifyList.get(getMessage("verify.errorcat2"))) {
				if (ls.getCaseList().contains(tc)){
					inWarn = true;
					continue;
				}
			}
			if (!inWarn)
				addToVerList((TbCase)tc,3,0);
		}
	}
	
	@Override
	protected String getHQLWhere() {
		String hql = super.getHQLWhere();
		UserWorkspace uw = (UserWorkspace) Component.getInstance("userWorkspace");
		if (uw!=null){
/*			if (UserView.ADMINUNIT.equals(uw.getView())){
				hql += " and (c.notificationUnit.adminUnit.id = "+uw.getAdminUnit().getId() + " or c.ownerUnit.adminUnit.id = "+uw.getAdminUnit().getId()+")";
			}
			else if (UserView.TBUNIT.equals(uw.getView())){
				hql += " and (c.notificationUnit.id = "+uw.getTbunit().getId() + " or c.ownerUnit.id = "+uw.getTbunit().getId()+")";
			} AK 23.01.2013 wrong selection by adminunit*/
			if (UserView.ADMINUNIT.equals(uw.getView())){
				hql += " and (c.notificationUnit.adminUnit.code like '"+uw.getAdminUnit().getCode() + "%' or c.ownerUnit.adminUnit.code like '"+uw.getAdminUnit().getCode()+"%')";
			}
			else if (UserView.TBUNIT.equals(uw.getView())){
				hql += " and (c.notificationUnit.id = "+uw.getTbunit().getId() + " or c.ownerUnit.id = "+uw.getTbunit().getId()+")";
			}
		}
		return hql;
		
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
	 * @return microscope test, which is up to quality, strictly namely first month of treatment and worst test from several
	 * It is "ideal" period, not maybe for "real life" 
	 */
	protected ExamMicroscopy rightMcTestStrict(TbCase tc){
		Date dateIniTreat = tc.getRegistrationDate();
		int res=0;
		ArrayList<ExamMicroscopy> lst = new ArrayList<ExamMicroscopy>();
		
		if (tc.getTreatmentPeriod()!=null)
			if (tc.getTreatmentPeriod().getIniDate()!=null)
			dateIniTreat = tc.getTreatmentPeriod().getIniDate();

		for (ExamMicroscopy ex: tc.getExamsMicroscopy()){
			Date dateTest = ex.getDateCollected();
			int testperiod = DateUtils.daysBetween(dateTest,dateIniTreat);
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
	 * @return worst microscope test between first visit date and diagnosis date
	 * It is UA specific "real life" - not first month!
	 */
	protected ExamMicroscopy rightMcTest(TbCase tc){
		Date diagDate = new Date(); // today, because no diagnosis
		int res=0;
		if (tc.getDiagnosisDate()!=null)
			diagDate = tc.getDiagnosisDate();		// real diagnosis	
		
		ArrayList<ExamMicroscopy> lst = new ArrayList<ExamMicroscopy>();
		for (ExamMicroscopy ex: tc.getExamsMicroscopy()){
			Date dateTest = ex.getDateCollected();
			if (diagDate.before(dateTest)) {
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
	 * @return worst microscope test before start treatment. If it is null - first exist
	 */
	protected ExamMicroscopy rightMcTestBeforeTreat(TbCase tc){
		ExamMicroscopy result = null;;
		Date iniTreatDate = new Date(); 
		int res=0;
		if (tc.getTreatmentPeriod()!=null)
			if (tc.getTreatmentPeriod().getIniDate()!=null)
				iniTreatDate = tc.getTreatmentPeriod().getIniDate();		
		
		ArrayList<ExamMicroscopy> lst = new ArrayList<ExamMicroscopy>();
		for (ExamMicroscopy ex: tc.getExamsMicroscopy()){
			Date dateTest = ex.getDateCollected();
			if (dateTest.before(iniTreatDate)) {
				res++;
				lst.add(ex);
			}	
		}

		switch (lst.size()) {
		case 0: 
			result = null;
			break;
		case 1: 
			result = lst.get(0);
			break;
		default:
			result = WorstMcRes(lst);
			break;
		}
		
		if (result == null)
			result = firstMcTest(tc);
		return result;
	}
	
	/**
	 * @return worst culture test before start treatment. If it is null - first exist
	 */
	protected ExamCulture rightCulTestBeforeTreat(TbCase tc){
		ExamCulture result = null;
		Date iniTreatDate = new Date(); 
		int res=0;
		if (tc.getTreatmentPeriod()!=null)
			if (tc.getTreatmentPeriod().getIniDate()!=null)
				iniTreatDate = tc.getTreatmentPeriod().getIniDate();		
		
		ArrayList<ExamCulture> lst = new ArrayList<ExamCulture>();
		for (ExamCulture ex: tc.getExamsCulture()){
			Date dateTest = ex.getDateCollected();
			if (dateTest.before(iniTreatDate)) {
				res++;
				lst.add(ex);
			}	
		}

		switch (lst.size()) {
		case 0: 
			result = null;
			break;
		case 1: 
			result = lst.get(0);
			break;
		default:
			result = WorstCulRes(lst);
			break;
		}
		if (result == null)
			result = firstCulTest(tc);
		return result;
	}
	
	/**
	 * Return the most early exam culture in case
	 */
	private ExamCulture firstCulTest(TbCase tc) {
		if (tc.getExamsCulture().size() == 0) return null;
		Date d = tc.getExamsCulture().get(0).getDateCollected();
		ExamCulture res = tc.getExamsCulture().get(0);
		for (ExamCulture ex:tc.getExamsCulture()){
			if (ex.getDateCollected().before(d))
				res = ex;
		}
		return res;
	}
	
	/**
	 * Return the most early exam microscopy in case
	 */
	private ExamMicroscopy firstMcTest(TbCase tc) {
		if (tc.getExamsMicroscopy().size() == 0) return null;
		Date d = tc.getExamsMicroscopy().get(0).getDateCollected();
		ExamMicroscopy res = tc.getExamsMicroscopy().get(0);
		for (ExamMicroscopy ex:tc.getExamsMicroscopy()){
			if (ex.getDateCollected().before(d))
				res = ex;
		}
		return res;
	}

	/**
	 * @return culture test, which is up to quality, strictly namely first 14 days of treatment 
	 * (if no treatment, then all results) and worst test from several 
	 */
	protected ExamCulture rightCulTestDuring14daysTreat(TbCase tc){
		Date dateIniTreat=null;
		int res=0;
		ArrayList<ExamCulture> lst = new ArrayList<ExamCulture>();
		if (tc.getTreatmentPeriod()!=null)
				dateIniTreat = tc.getTreatmentPeriod().getIniDate();
		
		if (dateIniTreat!=null)
			for (ExamCulture ex: tc.getExamsCulture()){
				Date dateTest = ex.getDateCollected();
				int testperiod = DateUtils.daysBetween(dateTest, dateIniTreat);
				
				if (testperiod<=14) {
					res++;
					lst.add(ex);
				}	
			}
		else
			lst.addAll(tc.getExamsCulture());

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
	 * @return microscopy test, which is up to quality, strictly namely first 14 days of treatment 
	 * (if no treatment, then all results) and worst test from several 
	 */
	protected ExamMicroscopy rightMcTestDuring14daysTreat(TbCase tc){
		Date dateIniTreat=null;
		int res=0;
		ArrayList<ExamMicroscopy> lst = new ArrayList<ExamMicroscopy>();
		if (tc.getTreatmentPeriod()!=null)
				dateIniTreat = tc.getTreatmentPeriod().getIniDate();
		
		if (dateIniTreat!=null)
			for (ExamMicroscopy ex: tc.getExamsMicroscopy()){
				Date dateTest = ex.getDateCollected();
				int testperiod = DateUtils.daysBetween(dateTest, dateIniTreat);
				
				if (testperiod<=14) {
					res++;
					lst.add(ex);
				}	
			}
		else
			lst.addAll(tc.getExamsMicroscopy());

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
	 * @return culture test, which is up to quality, strictly namely first month of treatment and worst test from several 
	 * not for UA "real life", maybe
	 */
	protected ExamCulture rightCulTestStrict(TbCase tc){
		Date dateIniTreat = tc.getRegistrationDate();
		int res=0;
		ArrayList<ExamCulture> lst = new ArrayList<ExamCulture>();
		
		if (tc.getTreatmentPeriod()!=null)
			if (tc.getTreatmentPeriod().getIniDate()!=null)
			dateIniTreat = tc.getTreatmentPeriod().getIniDate();

		for (ExamCulture ex: tc.getExamsCulture()){
			Date dateTest = ex.getDateCollected();
			int testperiod = DateUtils.daysBetween(dateTest,dateIniTreat);
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
	 * @return worst culture test before diagnosis date
	 * It is UA specific "real life" - not first month!
	 */
	protected ExamCulture rightCulTest(TbCase tc){
		Date diagDate = new Date(); // today, because no diagnosis
		int res=0;
		if (tc.getDiagnosisDate()!=null)
			diagDate = tc.getDiagnosisDate();		// real diagnosis	
		
		ArrayList<ExamCulture> lst = new ArrayList<ExamCulture>();
		for (ExamCulture ex: tc.getExamsCulture()){
			Date dateTest = ex.getDateCollected();
			if (diagDate.before(dateTest)) {
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
		Date dateIniTreat = tc.getDiagnosisDate();
		int res=0;
		ArrayList<ExamDST> lst = new ArrayList<ExamDST>();
		if (tc.getTreatmentPeriod()!=null)
			if (tc.getTreatmentPeriod().getIniDate()!=null)
				dateIniTreat = tc.getTreatmentPeriod().getIniDate();
		for (ExamDST ex: tc.getExamsDST()){
			Date dateTest = ex.getDateCollected();
			Date regDt = tc.getRegistrationDate();
			int testperiod = DateUtils.daysBetween(dateTest, regDt);
			
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
	protected ExamDST WorstDSTRes(List<ExamDST> lst) {
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

	protected void addToRecordsInReport(E tc) {
		recordsInReport.add(tc);
	}
}
