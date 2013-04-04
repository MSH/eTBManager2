package org.msh.tb.reports2.variables;

import java.util.List;

import org.jboss.seam.Component;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.enums.TbField;
import org.msh.tb.misc.FieldsOptions;
import org.msh.tb.reports2.VariableImpl;

/**
 * Standard variable that supports fields of type {@link FieldValue} 
 * 
 * @author Ricardo Memoria
 *
 */
public class FieldValueVariable extends VariableImpl {

	private TbField tbfield;
	private List<FieldValue> fields;
	
	public FieldValueVariable(String id, String keylabel, String fieldName, TbField tbfield) {
		super(id, keylabel, fieldName);
		this.tbfield = tbfield;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		Integer id = (Integer)key;

		FieldValue fld = getFieldValue(id);
		
		if (fld == null)
			return super.getDisplayText(null);
		
		return fld.getName().toString();
	}

	/**
	 * Return an instance of the {@link FieldValue} by its id
	 * @param id is the unique identification of the field value
	 * @return instance of the {@link FieldValue} corresponding to the id 
	 */
	public FieldValue getFieldValue(Integer id) {
		if (fields == null) {
			FieldsOptions options = (FieldsOptions)Component.getInstance("fieldOptions", true);
			if (options == null)
				return null;
			
			fields = options.getOptions(tbfield);
			if (fields == null)
				return null;
		}
		
		for (FieldValue fld: fields) {
			if (fld.getId().equals(id))
				return fld;
		}
		
		return null;
	}
}
