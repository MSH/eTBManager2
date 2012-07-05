package org.msh.tb.ua;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.indicators.core.IndicatorController;

@Name("indicatorControllerUA")
@Scope(ScopeType.PAGE)
public class IndicatorControllerUA extends IndicatorController {
	private boolean verifing;
	
	public void verify(){
		verifing = !verifing;
		}

	public boolean isVerifing() {
		return verifing;
	}

	public void setVerifing(boolean verifing) {
		this.verifing = verifing;
	}
	
	@Override
	public void execute() {
		super.execute();
		verifing = false;
	}
}
