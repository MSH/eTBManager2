package org.msh.tb.ua;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;

import org.jboss.seam.annotations.Name;
import org.msh.tb.adminunits.AdminUnitGroup;
import org.msh.tb.cases.HealthUnitInfo;
import org.msh.tb.cases.HealthUnitsQuery;

@Name("healthUnitsQueryUA")
public class HealthUnitsQueryUA extends HealthUnitsQuery {
	private static final long serialVersionUID = 4887501919593423214L;

	@Override
	protected void createAdminUnits() {
		super.createAdminUnits();
		for (AdminUnitGroup<HealthUnitInfo> adm:getAdminUnits()){
			Collections.sort(adm.getItems(),new Comparator<HealthUnitInfo>() {

				@Override
				public int compare(HealthUnitInfo o1, HealthUnitInfo o2) {
					String n1 = o1.getUnitName();
					String n2 = o2.getUnitName();
					Collator myCollator = Collator.getInstance();
					return myCollator.compare(n1, n2);
				}
				
			});
		}
		
	}
}
