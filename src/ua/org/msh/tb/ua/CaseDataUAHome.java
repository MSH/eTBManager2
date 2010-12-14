package org.msh.tb.ua;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.enums.YesNoType;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.cases.CaseCloseHome;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.ComorbidityHome;
import org.msh.tb.ua.entities.CaseDataUA;


@Name("caseDataUAHome")
public class CaseDataUAHome extends EntityHomeEx<CaseDataUA> {
	private static final long serialVersionUID = -327682679191655122L;

	
	@In EntityManager entityManager;
	@In(create=true) CaseEditingHome caseEditingHome;
	@In(create=true) CaseHome caseHome;
	@In(create=true) CaseCloseHome caseCloseHome;
	@In(create=true) ComorbidityHome comorbidityHome;
	
	
	@Factory("caseDataUA")
	public CaseDataUA getCaseDataUA() {
		try {
			entityManager.createQuery("from CaseDataUA c where c.tbcase.id = #{caseHome.id}").getSingleResult();
			setId(caseHome.getId());
		} catch (Exception e) {
			e.printStackTrace();
			setId(null);
		}
		return getInstance();
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
		
		data.setTbcase(tbcase);
		data.setId(tbcase.getId());
		
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
}
