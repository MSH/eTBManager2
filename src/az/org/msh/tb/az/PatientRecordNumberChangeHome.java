package org.msh.tb.az;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.SequenceInfo;
import org.msh.tb.entities.Workspace;

@Name("patientRecordNumberChangeHome")
@Scope(ScopeType.PAGE)
public class PatientRecordNumberChangeHome {
	
	@In(required=false) Workspace defaultWorkspace;
	@In(create=true) FacesMessages facesMessages;
	
	private Integer numDefault;
	private SequenceInfo sequenceInfo;
	/**
	 * Change recordNumber of patient in current instance in numDefault
	 */
	public void changeNumber(){
		String res="";
		try{
			if (!validateChange()){
				return;
				/*SequenceGenerator sequenceGenerator = (SequenceGenerator) App.getComponent("sequenceGenerator");
				sequenceGenerator.generateNewNumber("CASE_NUMBER");*/
			}
			CaseHome caseHome = (CaseHome) App.getComponent("caseHome");
			Patient p = caseHome.getTbCase().getPatient();
			p.setRecordNumber(numDefault);
			App.getEntityManager().persist(p);
			App.getEntityManager().flush();
			res=App.getMessage("default.entity_updated");
			sequenceInfo = null;
		}
		catch (Exception e) {
			res="ERROR. In train of correcting number arised a problem: "+e.getLocalizedMessage();
		}
		facesMessages.add(res);
	}
	/**
	 * Return true if we can change number without problems
	 * */
	private boolean validateChange() {
		CaseHome caseHome = (CaseHome) App.getComponent("caseHome");
		Patient p = caseHome.getTbCase().getPatient();
		int num = p.getRecordNumber().intValue();
		if (num == numDefault)
			return false;
		long duplicate = (Long)App.getEntityManager().createQuery("select count(*) from Patient p " +
											"where p.id != " +p.getId() +
											  "and p.recordNumber="+numDefault +
											  "and p.workspace.id=#{defaultWorkspace.id}")
							  .getResultList().get(0);
		if (duplicate!=0){
			facesMessages.add(App.getMessage("az_AZ.changeNumber.alreadyExist"));
			return false;
		}
		if (numDefault>getSequenceInfo().getNumber()){
			facesMessages.add(App.getMessage("az_AZ.changeNumber.maxOverflow"));
			return false;
		}
		
		return true;
	}

	/**
	 * Return SequenceInfo with max caseNumber for Azerbaijan
	 */
	private SequenceInfo getSequenceInfo() {
		if (sequenceInfo==null){
			String seq = "CASE_NUMBER";
			try {
				sequenceInfo = (SequenceInfo) App.getEntityManager().createQuery("from SequenceInfo s where s.sequence = :seq and s.workspace.id = :id")
				.setParameter("seq", seq)
				.setParameter("id", defaultWorkspace.getId())
				.getSingleResult();			
			} catch (Exception e) {
				sequenceInfo = null;
				e.printStackTrace();
			}
			
			if (sequenceInfo == null) {
				sequenceInfo = new SequenceInfo();
				sequenceInfo.setSequence(seq);
				sequenceInfo.setWorkspace(defaultWorkspace);
			}
		}
		return sequenceInfo;
	}
	
	public Integer getNumDefault(){
		//numDefault = getSequenceInfo().getNumber()+1;
		CaseHome caseHome = (CaseHome) App.getComponent("caseHome");
		Patient p = caseHome.getTbCase().getPatient();
		numDefault = p.getRecordNumber();
		return numDefault;
	}

	public void setNumDefault(Integer numDefault) {
		this.numDefault = numDefault;
	}

	
}
