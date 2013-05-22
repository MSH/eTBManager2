package org.msh.tb.reports2;

import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.reports.IndicatorReport;
import org.msh.reports.ReportConfiguration;
import org.msh.reports.ReportResourceProvider;
import org.msh.tb.entities.Workspace;

@Name("indicatorReportFactory")
public class IndicatorReportFactory implements ReportResourceProvider {

	private Connection connection;
	
	/**
	 * Called when the component is created
	 */
	@Create
	public void create() {
		ReportConfiguration.instance().registerResourceProvider(this);
	}
	
	/**
	 * Called by SEAM when the component is to be released
	 */
	@Destroy
	public void destroy() {
		if (connection != null)
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
	
	/**
	 * Create a case 
	 * @return
	 */
	public IndicatorReport createCaseIndicator() {
		IndicatorReport rep = new IndicatorReport();
		rep.setTableName("tbcase");
		rep.addTableJoin("patient", "id", "patient_id");
		
		Workspace ws = (Workspace)Component.getInstance("defaultWorkspace");
		rep.addRestriction("patient.workspace_id = " + ws.getId());
		return rep;
	}
	

	/* (non-Javadoc)
	 * @see org.msh.reports.ReportResourceProvider#getConnection()
	 */
	@Override
	public Connection getConnection() {
		if (connection == null) {
			EntityManager em = (EntityManager)Component.getInstance("entityManager");
			Session session = (Session)em.getDelegate();
			SessionFactoryImplementor sfi = (SessionFactoryImplementor) session.getSessionFactory();
			try {
				connection = sfi.getConnectionProvider().getConnection();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		return connection;
	}

	/* (non-Javadoc)
	 * @see org.msh.reports.ReportResourceProvider#resolveName(java.lang.String)
	 */
	@Override
	public Object resolveName(String name) {
		return Messages.instance().get(name);
	}
	
	/**
	 * Return an instance of the {@link IndicatorReportFactory}
	 * @return
	 */
	public static IndicatorReportFactory instance() {
		return (IndicatorReportFactory)Component.getInstance("indicatorReportFactory");
	}
}
