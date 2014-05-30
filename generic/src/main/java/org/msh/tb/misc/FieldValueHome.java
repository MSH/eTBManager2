package org.msh.tb.misc;

import java.util.Map;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.enums.TbField;
import org.msh.tb.transactionlog.LogInfo;



@Name("fieldValueHome")
@LogInfo(roleName="FIELDS", entityClass=FieldValue.class)
public class FieldValueHome extends EntityHomeEx<FieldValue> {
	private static final long serialVersionUID = 3191726641746083855L;

	private TbField field;
	
	@In(required=false) FieldsQuery fieldsQuery;
	@In(create=true) FacesMessages facesMessages;
	
	@Factory("fieldValue")
	public FieldValue getFieldValue() {
		return getInstance();
	}

	/* (non-Javadoc)
	 * @see com.rmemoria.utils.EntityHomeEx#persist()
	 */
	@Override
	public String persist() {
		FieldValue fld = getInstance();
		if (fld.getField() == null)
			fld.setField(field);
		
		if (fld.isOther()) {
			String val = fld.getOtherDescription();
			if ((val == null) || (val.isEmpty())) {
				Map<String, String> msgs = Messages.instance();
				facesMessages.add(msgs.get("FieldValue.otherDescription") + ": " + msgs.get("javax.faces.component.UIInput.REQUIRED"));
				return "error";
			}
		}
		
		return super.persist();
	}

	@Override
	public String remove() {
		String ret = super.remove();
		
		if (ret.equals("removed")) {
			getEntityManager().flush();
			if (fieldsQuery != null)
				fieldsQuery.refreshLists();
		}
		
		return ret;
	}
	
	/**
	 * @param field the field to set
	 */
	public void setField(TbField field) {
		this.field = field;
	}

	/**
	 * @return the field
	 */
	public TbField getField() {
		return field;
	}
}
