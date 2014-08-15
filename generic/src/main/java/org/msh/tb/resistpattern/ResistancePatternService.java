package org.msh.tb.resistpattern;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.entities.ResistancePattern;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.entities.enums.XpertResult;
import org.msh.tb.entities.enums.XpertRifResult;
import org.msh.utils.date.DateUtils;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Update the resistance pattern of an specific case. This service must be
 * called every time a DST result or an Xpert result is posted, updated or deleted.
 * When the method <code>updateCase</code> is called, the user defined resistance 
 * patterns will be updated based on the medicine resistances of the case. 
 * 
 * @author Ricardo Memoria
 *
 */
@Name("resistancePatternService")
public class ResistancePatternService {

	@In EntityManager entityManager;
	
	private List<ResistancePattern> patterns;
	
	/**
	 * Update the resistance pattern of an specific case
	 * @param tbcase
	 */
	@Transactional
	public void updateCase(TbCase tbcase) {
		// delete resistance patterns
		entityManager.createQuery("delete from CaseResistancePattern p where p.tbcase.id = :id")
			.setParameter("id", tbcase.getId())
			.executeUpdate();

		// get resistances from DST 
		List<Object[]> lst = entityManager.createQuery("select r.substance.id, min(r.exam.sample.dateCollected) " +
				"from ExamDSTResult r where r.exam.tbcase.id = :id and result = :res " +
				"group by r.substance.id")
				.setParameter("res", DstResult.RESISTANT)
				.setParameter("id", tbcase.getId())
				.getResultList();

		// get Rifampicin resistances from xpert tests
		List<Date> lstxpert = entityManager.createQuery("select r.sample.dateCollected " +
				"from ExamXpert r where r.tbcase.id = :id and r.result = :res and r.rifResult = :res2")
				.setParameter("id", tbcase.getId())
				.setParameter("res", XpertResult.TB_DETECTED)
				.setParameter("res2", XpertRifResult.RIF_DETECTED)
				.getResultList();

		// include rifampicin in the list of results
		if (lstxpert.size() > 0) {
			Integer subid = (Integer)entityManager.createQuery("select id from Substance where abbrevName.name1 = :name " +
					"and workspace.id = #{defaultWorkspace.id}")
					.setParameter("name", "R")
					.getSingleResult();
			for (Date dt: lstxpert) {
				Object[] vals = new Object[2];
				vals[0] = subid;
				vals[1] = dt;
				lst.add(vals);
			}
		}

		// create list of diagnosis and current
		List<Integer> lstCurrent = new ArrayList<Integer>();
		List<Integer> lstDiagnosis = new ArrayList<Integer>();

		for (Object[] vals: lst) {
			Integer subid = (Integer)vals[0];
			Date dt = (Date)vals[1];
			if ((tbcase.getDiagnosisDate() != null) && (dt.before(DateUtils.incDays(tbcase.getDiagnosisDate(), 15))))
				lstDiagnosis.add(subid);
			lstCurrent.add(subid);
		}
		
		if (tbcase.getDiagnosisDate() != null)
			updatePatterns(tbcase, lstDiagnosis, true);
		updatePatterns(tbcase, lstCurrent, false);
	}
	
	
	/**
	 * Update the resistance patterns of a case
	 * @param tbcase is the case to be updated
	 * @param lstResistances is the list of substances that presented a resistance to the case
	 * @param bDiagnosis is true if it's a pattern of the diagnosis of the case, otherwise false if 
	 * it's a pattern of the whole treatment
	 */
	private void updatePatterns(TbCase tbcase, List<Integer> lstResistances, boolean bDiagnosis) {
		if (lstResistances.size() == 0)
			return;

		String s = "";
		for (Integer subid: lstResistances) {
			if (!s.isEmpty())
				s += ",";
			s += subid;
		}
		
		Integer id = tbcase.getPatient().getWorkspace().getId();
		
		String sql = "insert into caseresistancepattern (case_id, resistpattern_id, diagnosis) " +
				"select " + tbcase.getId() + ", id, " + (bDiagnosis? "true": "false") + " from resistancepattern a " +
				"where (select count(*) from substances_resistpattern b where b.resistancepattern_id = a.id " +
				"and substances_id in (" + s + ") ) = " + lstResistances.size() + 
				" and (select count(*) from substances_resistpattern b where b.resistancepattern_id=a.id) = " + lstResistances.size() + 
				" and a.workspace_id = " + id;
		
		entityManager.createNativeQuery(sql).executeUpdate();
	}


	/**
	 * @return
	 */
	List<ResistancePattern> getPatterns() {
		if (patterns == null) {
			patterns = entityManager
					.createQuery("from ResistancePattern where workspace.id = #{defaultWorkspace.id}")
					.getResultList();
		}
		return patterns;
	}
	
}
