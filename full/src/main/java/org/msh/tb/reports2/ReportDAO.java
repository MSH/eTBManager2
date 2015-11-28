/**
 * 
 */
package org.msh.tb.reports2;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.entities.Report;
import org.msh.tb.entities.User;
import org.msh.tb.login.UserSession;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

/**
 * Handle CRUD operations with a report
 * 
 * @author Ricardo Memoria
 *
 */
@Name("reportDAO")
public class ReportDAO {

	@In EntityManager entityManager;
	
	/**
	 * Load a report into memory from it ID
	 * @param id the report ID
	 * @return instance of {@link org.msh.tb.entities.Report}, or null if not found
	 */
	public Report getReport(Integer id) {
		return entityManager.find(Report.class, id);
	}
	
	
	/**
	 * Return a list of org.msh.reports available for the current user (their org.msh.reports and
	 * the public org.msh.reports)
	 * @return {@link List} of {@link Report} objects
	 */
	public List<Report> getReportList() {
		return entityManager
			.createQuery("from Report where (published = true or owner.id = #{userLogin.user.id}) "
					+ "and (workspace.id = #{defaultWorkspace.id}) "
					+ "order by title")
			.getResultList();
	}
	
	/**
	 * Return the list of dashboard indicators available for the current user
	 * @return list of {@link Report} objects
	 */
	public List<Report> getDashboardIndicators() {
        return entityManager
                .createQuery("from Report where dashboard = true and published=true and workspace.id = #{defaultWorkspace.id} order by title")
                .getResultList();
	}
	
	@Transactional
	public Report saveReport(Report report) {
		// load the current user
		User user = UserSession.getUser();

		// is this a new report ?
		if (report.getId() == null) {
			// save a new report
			report.setRegistrationDate(new Date());
			report.setOwner(user);
			report.setWorkspace(UserSession.getWorkspace());
		}
		else {
			report = entityManager.merge(report);
		}
	
		// save data
		entityManager.persist(report);
		entityManager.flush();
		return report;
	}
	
	
	/**
	 * Delete a report by its report ID. If the report is not found or the user
	 * is not the owner of the report, and exception will be thrown
	 * @param reportId is the report ID
	 */
	@Transactional
	public void deleteReport(Integer reportId) {
		User user = UserSession.getUser();
		
		Report report = getReport(reportId);
		
		if (report == null) {
			throw new RuntimeException("Report not found");
		}
		
		if (!report.getOwner().getId().equals(user.getId())) {
			throw new RuntimeException("Not authorized");
		}
		
		entityManager.remove(report);
		entityManager.flush();
	}
}
