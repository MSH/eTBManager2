package org.msh.tb.misc;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.entities.FieldValue;

@Name("fieldListScriptConverter")
@org.jboss.seam.annotations.faces.Converter(id="fieldListScriptConverter")
@BypassInterceptors
public class FieldListScriptConverter implements Converter {
	public Object getAsObject(FacesContext facesContext, UIComponent comp, String txt) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAsString(FacesContext facesContext, UIComponent comp, Object obj) {
		if (!(obj instanceof List))
			return null;
		
		UIParameter param = findParam(comp, "id");
		String prefix;
		if (param != null)
			prefix = param.getValue().toString();
		else prefix = "";
		
		List<FieldValue> lst = (List<FieldValue>)obj;
		
		String s;
		
		String variables = createJSarray(lst);

		if (variables == null) {
			s = "var elem = jQuery(cb).closest('#fieldedt').find('#divothers'); \n" +
				"elem.hide();";			
		}
		else {
			s = variables + "\n" + 
				"var n = cb.selectedIndex;\n"  +
				"var ldelay=(immediate?0:500); \n" +
				"n = cbindex.indexOf(n); \n" +
				"var elem = jQuery(cb).closest('#fieldedt'); \n" +
				"var inp = elem.find('#divothers'); \n" +
				"if (n==-1) { inp.hide(); return; } \n" + 
				"var name = cbnames[n];\n" +
				"elem.find('#labelothers').html(cbnames[n]); \n" +
				"inp.show(ldelay); ";			
		}
		
		String script = "<script type=\"text/javascript\">" +
				"function " + prefix + "checkFieldOthers(cb, immediate) {" +
				"\n" +
				s +
				"}" +
				"</script> ";
		return script;
	}

	public String createJSarray(List<FieldValue> lst) {
		String varindex = "";
		String varnames = "";
		int index = 1;

		for (FieldValue fld: lst) {
			if (fld.isOther()) {
				if (!varindex.isEmpty()) {
					varindex += ",";
					varnames += ",";
				}
				varindex += Integer.toString(index);
				varnames += "\"" + fld.getOtherDescription() + "\"";
			}
			index++;
		}

		if (varindex.isEmpty())
			return null;
		
		varindex = "var cbindex=[" + varindex + "];";
		varnames = "var cbnames=[" + varnames + "];";
		
		return varindex + "\n" + varnames;
	}
	
	public UIParameter findParam(UIComponent comp, String pname) {
		for (UIComponent c: comp.getChildren()) {
			if ((c instanceof UIParameter) && (((UIParameter)c).getName().equals(pname))) {
				return (UIParameter)c;
			}
		}
		return null;
	}
}
