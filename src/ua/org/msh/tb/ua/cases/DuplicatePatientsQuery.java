package org.msh.tb.ua.cases;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.application.App;
import org.msh.tb.entities.Patient;
import org.msh.utils.EntityQuery;
import org.msh.utils.date.DateUtils;
import org.msh.tb.ua.cases.DuplicatePatientsQuery.PatientDup;

/**
 * Class for generating list of duplicate patients
 * @author A.M.
 */
@Name("duplicatePatientsQuery")
public class DuplicatePatientsQuery extends EntityQuery<PatientDup>{
	private static final long serialVersionUID = -3813698910442988755L;

	private List<PatientDup> resultList;
	private Long resultCount;
	
	/**
	 * Generate list of duplicates patients 
	 */
	private void generateDuplicateList() {
		String sql = generateSql();
		javax.persistence.Query query = App.getEntityManager().createNativeQuery(sql);
		
		if ( getFirstResult()!=null) query.setFirstResult( getFirstResult() );
		if ( getMaxResults()!=null) query.setMaxResults( getMaxResults()+1 ); //add one, so we can tell if there is another page
		
		List<Object[]> lst = query.getResultList();
		if (lst.size()>0){
			resultList = new ArrayList<PatientDup>();
			for (Object[] vals:lst){
				Patient p = (Patient)App.getEntityManager().find(Patient.class, (Integer)vals[0]);
				PatientDup pd = new PatientDup();
				pd.getPatients().add(p);
				pd.getPatients().addAll(DuplicatePatients.getOtherPatients(p,false));
				pd.setBirthDate(p.getBirthDate());
				pd.setFio(p.getFullName());
				resultList.add(pd);
			}
			
		}
	}
	
	@Override
	public void validate() {
		return;
	}
	
	@Override
	public List<PatientDup> getResultList() {
		if (resultList==null)
			generateDuplicateList();
		return resultList;
	}
	
	
	@Override
	public Long getResultCount() {
		if (resultCount==null){
			String sql = generateSql();
			resultCount = (long) App.getEntityManager().createNativeQuery(sql).getResultList().size();
		}
		return resultCount;
	}
	
	/**
	 * Generate SQL for query
	 */
	private String generateSql() {
		String sql = "select p.id, count(*) as ct " +
		"from patient p " +
		"where exists(select * " +
			"from tbcase c " +
			DuplicatePatients.getSqlJoinForCases()+
			"where c.patient_id=p.id "+
			DuplicatePatients.generateSQLConditionByUserView()+")"+
		"group by p.lastName,p.patient_name,p.middleName,p.birthDate " +
		"having ct>1 " +
		"order by p.lastName,p.patient_name,p.middleName";
		return sql;
	}

	@Override
	public Integer getMaxResults() {
		Integer max = super.getMaxResults();
		if (max != null)
			return max;
		else 
			return 50;
	}
	
	@Override
	@Transactional
	public boolean isNextExists()
	{
		boolean b = (resultList!=null) && (resultList.size() > getMaxResults());
		return b;
	}

	@Override
	public void refresh() {
		resultList = null;

		super.refresh();
	}
	
	/**
	 * Class for all necessary data of duplicate patients
	 * @author A.M.
	 */
	public class PatientDup{
		private List<Patient> patients = new ArrayList<Patient>();
		private String fio;
		private Date birthDate;
		
		public void setPatients(List<Patient> patients) {
			this.patients = patients;
		}
		public List<Patient> getPatients() {
			return patients;
		}
		public void setFio(String fio) {
			this.fio = fio;
		}
		public String getFio() {
			return fio;
		}
		public void setBirthDate(Date birthDate) {
			this.birthDate = birthDate;
		}
		public Date getBirthDate() {
			return birthDate;
		}
		
	}
}
