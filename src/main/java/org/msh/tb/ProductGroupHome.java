package org.msh.tb;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.ProductGroup;


@Name("productGroupHome")
public class ProductGroupHome extends EntityHomeEx<ProductGroup>{
	private static final long serialVersionUID = 8115642079943580898L;

	@Factory("productGroup")
	public ProductGroup getProductGroup() {
		return getInstance();
	}
	
	@Override
	public String persist() {
		getInstance().updateLevel();
		return super.persist();
	}
}
