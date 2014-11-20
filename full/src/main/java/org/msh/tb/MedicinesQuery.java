package org.msh.tb;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.ProductGroup;
import org.msh.utils.EntityQuery;

import java.util.Arrays;
import java.util.List;


@Name("medicines")
public class MedicinesQuery extends EntityQuery<Medicine> {
	private static final long serialVersionUID = -185168625962305029L;

	private static final String[] restrictions = {"m.workspace.id = #{defaultWorkspace.id}",
			"m.group.id = #{medicines.productGroup.id}"};
	
	private ProductGroup productGroup;


	@Override
	public String getEjbql() {
		return "from Medicine m";
	}


	@Override
	public String getOrder() {
		String s = super.getOrder();
		
		if (s == null)
			return "m.genericName";
		else return s;
	}


	@Override
	protected String getCountEjbql() {
		return "select count(*) from Medicine m";
	}
	
	@Override
	public List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}
	
	public ProductGroup getProductGroup() {
		return productGroup;
	}


	public void setProductGroup(ProductGroup productGroup) {
		this.productGroup = productGroup;
	}
}
