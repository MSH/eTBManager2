package org.msh.tb.laboratories;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.ExamStatus;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.laboratories.SampleFilters.SearchCriteria;
import org.msh.utils.EntityQuery;

@Name("samplesQuery")
public class SamplesQuery extends EntityQuery<SamplesQuery.Item> {
	private static final long serialVersionUID = -4416912124856801078L;

	private List<Item> resultList;
	
	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getCountEjbql()
	 */
	@Override
	protected String getCountEjbql() {
		return "select count(*) from PatientSample a join a.tbcase c join c.patient p ".concat(getConditions());
	}


	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getEjbql()
	 */
	@Override
	public String getEjbql() {
		return "select c.id, a.id, a.dateCollected, a.sampleNumber, p.birthDate, p.gender, p.name, p.middleName, p.lastName " +
				"from PatientSample a join a.tbcase c join c.patient p ".concat(getConditions());
	}


	/**
	 * Generate the sample conditions based on filters in component {@link org.msh.tb.laboratories.SampleFilters}
	 * @return
	 */
	protected String getConditions() {
		SampleFilters filters = SampleFilters.instance();
		
		String cond = " where p.workspace.id = #{defaultWorkspace.id} ";
		
		if (filters.getSearchCriteria() == SearchCriteria.TABLE)
			return cond + getTableConditions();
		else return cond + getPatientConditions();
	}


	/**
	 * Return HQL conditions of the query to be applied based on filters for table results
	 * @return
	 */
	protected String getTableConditions() {
		SampleFilters filters = SampleFilters.instance();
		
		String hql;
		if (ExamStatus.PERFORMED.equals(filters.getExamStatus()))
			 hql = "and month(a.dateCollected) = (#{sampleFilters.month} + 1) and year(a.dateCollected) = #{sampleFilters.year} ";
		else hql = "";

		String table;
		switch (filters.getExamType()) {
		case CULTURE: table = "ExamCulture";
			break;
		case MICROSCOPY: table = "ExamMicroscopy";
			break;
		case DST: table = "ExamDST";
			break;
		case IDENTIFICATION: table = "ExamIdentification";
			break;
		default: throw new IllegalArgumentException("Exam type cannot be null or is not supported");
		}

		String sqlstatus;
		if (filters.getExamStatus() != null)
			 sqlstatus = " and exam.status = #{sampleFilters.examStatus}";
		else sqlstatus = "";

		hql += "and exists(select exam.id from " + table + 
				" exam where exam.sample.id=a.id and exam.laboratory.id = #{userSession.laboratory.id}" + sqlstatus + ")";

		return hql;
	}
	
	
	/**
	 * Return HQL conditions of the query based on patient search by key word
	 * @return
	 */
	public String getPatientConditions() {
		String pat = SampleFilters.instance().getPatient();
		
		String cond = "";
		if ((pat != null) && (!pat.isEmpty())) {
			String[] names = pat.split(" ");
			if (names.length > 0) {
				String s="(";
				for (String name: names) {
					name = name.replaceAll("'", "''");
					if (s.length() > 1)
						s += " and ";
					s += "((upper(p.name) like '%" + name.toUpperCase() + 
						 "%') or (upper(p.middleName) like '%" + name.toUpperCase() + 
						 "%') or (upper(p.lastName) like '%" + name.toUpperCase() + "%') or " +
						 		"(upper(a.sampleNumber) = '" + name.toUpperCase() + "'))";
				}
				s += ")";
				
				cond += " and " + s;
			}
		}
		
		return cond;
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getMaxResults()
	 */
	@Override
	public Integer getMaxResults() {
		return 20;
	}


	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getOrder()
	 */
	@Override
	public String getOrder() {
		Integer order = SampleFilters.instance().getNewOrder();
		if (order == null)
			order = 2;

		String pref;
		if (SampleFilters.instance().isInverseOrder())
			 pref = " desc";
		else pref = "";
		
		switch (order) {
		case 0: return getPatientNameOrder(pref);
		case 1: return "a.sampleNumber" + pref;
		default: return "a.dateCollected" + pref + ", " + getPatientNameOrder("");
		}
	}
	
	private String getPatientNameOrder(String desc) {
		Workspace w = (Workspace)Component.getInstance("defaultWorkspace");
		
		switch (w.getPatientNameComposition()) {
		case FIRST_MIDDLE_LASTNAME: return "p.name" + desc + ", p.middleName, p.lastName";
		case FIRSTSURNAME: return "p.name" + desc + ", p.middleName";
		case LAST_FIRST_MIDDLENAME: return "p.lastName" + desc + ", p.name, p.middleName";
		case SURNAME_FIRSTNAME: return "p.middleName" + desc + ", p.name";
		default: return "p.name" + desc;
		}
	}


	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.EntityQuery#getResultList()
	 */
	@Override
	public List<Item> getResultList() {
		if (resultList == null)
		{
			javax.persistence.Query query = createQuery();
	        List<Object[]> lst = query==null ? null : query.getResultList();
	        fillResultList(lst);
	    }
		return resultList;
	}


	/**
	 * @param lst
	 */
	private void fillResultList(List<Object[]> lst) {
		resultList = new ArrayList<Item>();
		for (Object[] vals: lst) {
			Item item = new Item();
			item.setCaseId( (Integer)vals[0] );
			item.setSampleId( (Integer)vals[1]);
			item.setDateCollected((Date)vals[2]);
			item.setSampleNumber( (String) vals[3] );

			Patient p = item.getPatient();
			p.setBirthDate( (Date)vals[4]);
			p.setGender( (Gender)vals[5]);
			p.setName( (String)vals[6] );
			p.setMiddleName( (String)vals[7] );
			p.setLastName( (String)vals[8] );

			resultList.add(item);
		}
	}
	
	/**
	 * @author Ricardo Memoria
	 *
	 */
	public class Item {
		private Integer caseId;
		private Integer sampleId;
		private Patient patient = new Patient();
		private Date dateCollected;
		private String sampleNumber;

		/**
		 * @return the dateCollected
		 */
		public Date getDateCollected() {
			return dateCollected;
		}
		/**
		 * @param dateCollected the dateCollected to set
		 */
		public void setDateCollected(Date dateCollected) {
			this.dateCollected = dateCollected;
		}
		/**
		 * @return the sampleNumber
		 */
		public String getSampleNumber() {
			return sampleNumber;
		}
		/**
		 * @param sampleNumber the sampleNumber to set
		 */
		public void setSampleNumber(String sampleNumber) {
			this.sampleNumber = sampleNumber;
		}
		/**
		 * @return the caseId
		 */
		public Integer getCaseId() {
			return caseId;
		}
		/**
		 * @param caseId the caseId to set
		 */
		public void setCaseId(Integer caseId) {
			this.caseId = caseId;
		}
		/**
		 * @return the sampleId
		 */
		public Integer getSampleId() {
			return sampleId;
		}
		/**
		 * @param sampleId the sampleId to set
		 */
		public void setSampleId(Integer sampleId) {
			this.sampleId = sampleId;
		}
		/**
		 * @return the patient
		 */
		public Patient getPatient() {
			return patient;
		}
		/**
		 * @param patient the patient to set
		 */
		public void setPatient(Patient patient) {
			this.patient = patient;
		}
	}
}
