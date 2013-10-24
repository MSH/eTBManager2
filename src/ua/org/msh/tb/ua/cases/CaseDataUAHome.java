package org.msh.tb.ua.cases;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang.ArrayUtils;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseCloseHome;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.ComorbidityHome;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.ExtraOutcomeInfo;
import org.msh.tb.entities.enums.Nationality;
import org.msh.tb.entities.enums.YesNoType;
import org.msh.tb.misc.FieldsQuery;
import org.msh.tb.ua.GlobalLists;
import org.msh.tb.ua.entities.CaseDataUA;


@Name("caseDataUAHome")
public class CaseDataUAHome extends EntityHomeEx<CaseDataUA> {
	private static final long serialVersionUID = -327682679191655122L;
	private Nationality nationality;
	private Integer colHospView;
	
	@In EntityManager entityManager;
	@In(create=true) CaseEditingHome caseEditingHome;
	@In(create=true) CaseHome caseHome;
	@In(create=true) CaseCloseHome caseCloseHome;
	@In(create=true) ComorbidityHome comorbidityHome;
	
	
	@Factory("caseDataUA")
	public CaseDataUA getCaseDataUA() {
		if ((!isManaged()) && (caseHome.isManaged())) {
			Integer id = (Integer)caseHome.getId();
			if (id != null) {
				try {
					setId(id);
				} catch (Exception e) {
					//e.printStackTrace();
					System.err.println("CaseDataUa with Id " + id + " not found. New initialised");
					setId(null);
				}
			}
		}
/*		try {
			List<CaseDataUA> entityManager.createQuery("from CaseDataUA c where c.tbcase.id = #{caseHome.id}").getResultList();
			setId(caseHome.getId());
		} catch (Exception e) {
			e.printStackTrace();
			setId(null);
		}
*/		return getInstance();
	}

	
	
	/**
	 * Save a new TB or MDR-TB case for the Ukraine version 
	 * @return "persisted" if successfully saved
	 */
	@Transactional
	public String saveNew() {
		if (!checkConstraints())
			return "error";

		String ret = caseEditingHome.saveNew();

		if (!ret.equals("persisted"))
			return ret;
		
		TbCase tbcase = caseEditingHome.getTbcase();
		CaseDataUA data = getInstance();
		tbcase.setNationality(nationality);
		data.setTbcase(tbcase);
		data.setId(tbcase.getId());
		PrevTBTreatmentUAHome prev = (PrevTBTreatmentUAHome) App.getComponent(PrevTBTreatmentUAHome.class);
		prev.persist();
		
		if (data.getPulmonaryMBT() == YesNoType.NO) {
			data.setMbtResult(null);
		}
		
		setDisplayMessage(false);
		return persist();
	}


	
	/**
	 * Save an existing TB or MDR-TB case of the Ukraine version
	 * @return "persisted" if successfully saved
	 */
	@Transactional
	public String saveEditing() {
		if (!checkConstraints())
			return "error";
		if (!caseEditingHome.saveEditing().equals("persisted"))
			return "error";
		
		CaseDataUA data = getInstance();
		TbCase tbcase = caseEditingHome.getTbcase();
		tbcase.setNationality(nationality);
		
		PrevTBTreatmentUAHome prev = (PrevTBTreatmentUAHome) App.getComponent(PrevTBTreatmentUAHome.class);
		prev.persist();
		
		if (data.getId() == null)
			data.setId(tbcase.getId());

		setDisplayMessage(false);
		return persist(); 
	}


	@Transactional
	public String saveAnnex() {
		comorbidityHome.save();
		CaseDataUA data = getInstance();
		if (data.getId() == null)
			data.setId((Integer)caseHome.getId());
		setDisplayMessage(false);
		return persist();
	}


	/**
	 * Close a case and save specific ukrainian data
	 * @return
	 */
	@Transactional
	public String closeCase() {
		String ret = caseCloseHome.closeCase(); 
		if (ret.equals("case-closed")) {
			setDisplayMessage(false);
			persist();
		}

		return "case-closed";
	}
	
	private boolean checkConstraints() {
		CaseDataUA data = getInstance();
		if (data.getPulmonaryMBT() == YesNoType.NO) {
			data.setMbtResult(null);
		}		
		return true;
	}
	
	/**
	 * Return list of extraoutcames for died and failed outcome 
	 * @return
	 */
	public ExtraOutcomeInfo[] getExtraInfo(){
		if (caseCloseHome.getState()==CaseState.FAILED){
			return GlobalLists.ocCuredFailed;
		}else if (caseCloseHome.getState()==CaseState.DIED){
			return  GlobalLists.ocDied;
		}
		return (ExtraOutcomeInfo[]) ArrayUtils.addAll(GlobalLists.ocDied, GlobalLists.ocCuredFailed);

	}
	
	public void setNationality(Nationality nationality) {
		this.nationality = nationality;
	}

	public Nationality getNationality() {
		if (caseEditingHome.getTbcase().getNationality()== null)
			nationality = Nationality.NATIVE;
		else
			nationality = caseEditingHome.getTbcase().getNationality();
		return nationality;
	}

	public Integer getColHospView() {
		if (colHospView == null){
			colHospView = 1;
			CaseDataUA ua = getCaseDataUA();
			if (ua.getHospitalizationDate2()!=null){
				colHospView++;
				if (ua.getHospitalizationDate3()!=null){
					colHospView++;
					if (ua.getHospitalizationDate4()!=null){
						colHospView++;
						if (ua.getHospitalizationDate5()!=null)
							colHospView++;
					}
				}
			}
		}
		return colHospView;
	}



	/**
	 * @param colHospView the colHospView to set
	 */
	public void setColHospView(Integer colHospView) {
		this.colHospView = colHospView;
	}
	/**
	 * rearrange DRTB risk case to TB case
	 */
	public void riskDRTBtoTB(){
		TbCase tbcase = caseHome.getTbCase();
		if (tbcase.getClassification() == CaseClassification.DRTB){
			if (tbcase.getDiagnosisType() == DiagnosisType.SUSPECT){
				tbcase.setClassification(CaseClassification.TB);
				tbcase.setDiagnosisType(DiagnosisType.CONFIRMED);
				CaseDataUA data = getInstance();
				data.setRegistrationCategory(null);
				App.getEntityManager().persist(tbcase);
				super.persist();
			}
		}
	}
	/**
	 * rearrange DRTB risk case to real DRTB
	 */
	public void riskDRTBtoDR(){
		TbCase tbcase = caseHome.getTbCase();
		if (tbcase.getClassification() == CaseClassification.DRTB){
			if (tbcase.getDiagnosisType() == DiagnosisType.SUSPECT){
				tbcase.setClassification(CaseClassification.DRTB);
				tbcase.setDiagnosisType(DiagnosisType.CONFIRMED);
				App.getEntityManager().persist(tbcase);
				super.persist();
			}
		}
	}
	
	/**
	 * Return true if case is DRTB-risk case
	 */
	public boolean isRiskDRTB(){
		TbCase tc = caseHome.getTbCase();
		if (CaseClassification.DRTB.equals(tc.getClassification()) && DiagnosisType.SUSPECT.equals(tc.getDiagnosisType()))
			return true;
		return false;
	}
	
	/**
	 * Return true if case is DRTB case
	 */
	public boolean isConfirmedDRTB(){
		TbCase tc = caseHome.getTbCase();
		if (CaseClassification.DRTB.equals(tc.getClassification()) && DiagnosisType.CONFIRMED.equals(tc.getDiagnosisType()))
			return true;
		return false;
	}
	
	public List<FieldValue> getRegistrationCategoriesDRTB(){
		TbCase tbcase = caseHome.getTbCase();
		FieldsQuery fq = (FieldsQuery) App.getComponent(FieldsQuery.class);
		List<FieldValue> cats = fq.getRegistrationCategories();
		List<FieldValue> res = new ArrayList<FieldValue>();
		if (tbcase.getClassification() == CaseClassification.DRTB){
			if (tbcase.getDiagnosisType() == DiagnosisType.SUSPECT){
				for (FieldValue val:cats)
					if (val.getName().getName1().contains(" 1 ") || val.getName().getName1().contains(" 2 "))
						res.add(val);
			}
			else{
				res = getCat4List();
			}
		}
		else
			return cats;
		return res;
	}

	public List<FieldValue> getCat4List() {
		FieldsQuery fq = (FieldsQuery) App.getComponent(FieldsQuery.class);
		List<FieldValue> cats = fq.getRegistrationCategories();
		List<FieldValue> res = new ArrayList<FieldValue>();
		for (FieldValue val:cats)
			if (val.getName().getName1().contains(" 4"))
				res.add(val);
		return res;
	}
	
	public String getSourcesString(){
		TbCase tc = caseHome.getTbCase();
		if (tc.getPrescribedMedicines()==null) return null;
		Set<Source> sources = new HashSet<Source>();
		for (PrescribedMedicine pm: tc.getPrescribedMedicines()){
			sources.add(pm.getSource());
		}
		if (sources.isEmpty()) return null;
		String res = "";
		for (Source s:sources){
			res += s.getName().getDefaultName()+", ";
		}
		res = res.substring(0, res.length()-2);
		return res;
		
	}
}
