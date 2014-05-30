package org.msh.tb.forecasting;

import java.util.Arrays;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Forecasting;
import org.msh.utils.EntityQuery;


@Name("forecastings")
public class ForecastingQuery extends EntityQuery<Forecasting> {
	private static final long serialVersionUID = 3609366685898863832L;
	private static final String staticCondition = " where (f.user.id = #{userLogin.user.id} or f.publicView = true)";

	private boolean loading;
	
	private static final String[] restrictions = {
		"f.workspace.id = #{defaultWorkspace.id}"
	};
	
	
	@Override
	protected String getCountEjbql() {
		return "select count(*) from Forecasting f" + staticCondition;
	}

	@Override
	public String getEjbql() {
		return "from Forecasting f" + staticCondition;
	}

	@Override
	public String getOrder() {
		return "f.recordingDate";
	}

	/**
	 * @return the loading
	 */
	public boolean isLoading() {
		return loading;
	}

	public void initLoading() {
		refresh();
		loading = true;
	}

	/* (non-Javadoc)
	 * @see com.rmemoria.utils.EntityQuery#getStringRestrictions()
	 */
	@Override
	protected List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}
}
