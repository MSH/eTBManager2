package org.msh.tb.test;

import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.mdrtb.entities.enums.PatientType;
import org.msh.utils.date.DateUtils;


/**
 * Generate data test for case management
 * @author Ricardo Memoria
 *
 */
@Name("caseManagementTest")
public class CaseManagementTest {

	@In(create=true) EntityManager entityManager;
	@In(create=true) ExecuteAsyncAction executeAsyncAction;
	@In(create=true) FacesMessages facesMessages;
	
	/**
	 * Fills patient type with random values (just for cases with no patient value information)
	 */
	public void fillPatientType() {
		List<Integer> cases = entityManager.createQuery("select c.id from TbCase c where c.patientType is null and c.patient.workspace = #{defaultWorkspace}")
			.getResultList();
		
		Random random = new Random();
		
		
		for (Integer id: cases) {
			int val = random.nextInt(7) - 1;

			// leave some of them as NULL to check system results
			if (val >= 0) {
				PatientType pt = PatientType.values()[val];

				entityManager.createNativeQuery("update TbCase c set c.patientType = :p where c.id = :id")
					.setParameter("p", pt.ordinal())
					.setParameter("id", id)
					.executeUpdate();
			}
		}
	}


	/**
	 * Acerta a idade dos pacientes para cada caso
	 */
	public void fillAge() {
		List<Object[]> lst = entityManager.createQuery("select c.registrationDate, c.patient.birthDate, c.id " +
				"from TbCase c where c.age is null")
				.getResultList();
		for (Object[] vals: lst) {
			Date reg = (Date)vals[0];
			Date birth = (Date)vals[1];
			Integer id = (Integer)vals[2];
			if ((reg != null) && (birth != null)) {
				int age = DateUtils.yearsBetween(reg, birth);
				entityManager.createQuery("update TbCase set age = :age where id = :id")
					.setParameter("age", age)
					.setParameter("id", id)
					.executeUpdate();
			}
		}
	}

	public void adjustNumDaysPlanned() {
		executeAsyncAction.adjustDaysPlanned();
		facesMessages.add("Execution is under progress... Don't click on the update button again");
	}

}
