package org.msh.tb.forecasting;

import java.util.Arrays;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.Forecasting;
import org.msh.utils.EntityQuery;


@Name("forecastings")
public class ForecastingQuery extends EntityQuery<Forecasting> {
	private static final long serialVersionUID = 3609366685898863832L;

	private boolean loading;
	
	private static final String[] restrictions = {
		"f.user.id = #{userLogin.user.id}"
	};
	
	
	@Override
	protected String getCountEjbql() {
		return "select count(*) from Forecasting f";
	}

	@Override
	public String getEjbql() {
		return "from Forecasting f";
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
