package org.msh.tb.transactionlog;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.faces.Converter;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.Messages;
import org.msh.utils.date.LocaleDateConverter;

@Converter(id="detailHtmlConverter")
@Name("detailHtmlConverter")
@BypassInterceptors
public class DetailHtmlConverter implements javax.faces.convert.Converter {

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
		if ((object == null) || (!(object instanceof String)))
			return null;

		return convertToString((String)object);
	}


	/**
	 * Convert a xml representation in a string to a html fragment
	 * @param xml
	 * @return
	 */
	public String convertToString(String xml) {
		Document el;
		try {
			el = DocumentHelper.parseText(xml);
		} catch (DocumentException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return convertToString(el.getRootElement());
	}


	/**
	 * Convert the XML representation to a HTML page
	 * @param doc
	 * @return
	 */
	public String convertToString(Element doc) {
		String s = "";
		if ("r".equals(doc.getName())) {
			for (Iterator<Element> i = doc.elementIterator(); i.hasNext() ;) {
				Element elem = i.next();
				s += renderElement(elem) + "</br>";
			}
		}
		else s = renderElement(doc);
		s = s.replace("\n", "<br/>");
		return s;
	}


	/**
	 * Render an XML node to a Html page
	 * @param el
	 * @return
	 */
	protected String renderElement(Element el) {
		String s = el.getName();
		if ("txt".equals(s))
			return renderTextElement(el);
		else
		if ("msg".equals(s))
			return renderMessageElement(el);
		else
		if ("tbl".equals(s))
			return renderTableElement(el);
		return "";
	}
	
	protected String renderTextElement(Element el) {
		String s = el.getText();
		return "<span class='tl-text'>" + StringEscapeUtils.escapeHtml(s) + "</span>";
	}
	
	
	protected String renderMessageElement(Element el) {
		String s = el.attributeValue("id");
		s = Messages.instance().get(s);
		
		MessageFormat mf = new MessageFormat(s);

		List<Element> lst = el.elements();
		List<Object> params = new ArrayList<Object>();
		for (Element param: lst) {
			Object val = readParameter(param);
			params.add(displayText(val));
		}

		s = mf.format(params.toArray());
		
		return "<span class='tl-message'>" + StringEscapeUtils.escapeHtml(s) + "</span>";
	}
	
	
	/**
	 * Render an element to a table
	 * @param el
	 * @return
	 */
	protected String renderTableElement(Element el) {
		int numCols = 0;
		// loop in the table rows
		String rowhtml = ""; 
		for (Iterator<Element> i = el.elementIterator(); i.hasNext(); ) {
			Element row = i.next();
			String key = row.attributeValue("id");
			// loop in the table parameters
			int counter = 1;
			rowhtml += "<tr><td class='tl-key'><span class='tl-key-text'>" + Messages.instance().get(key) + ":</span></td>"; 

			for (Iterator<Element> k = row.elementIterator(); k.hasNext(); ) {
				Element p = k.next();
				Object value = readParameter(p);
				rowhtml += "<td class='tl-value'>" + displayText(value) + "</td>";
				counter++;
			}
			
			rowhtml += "</tr>";
			
			if (counter > numCols)
				numCols = counter;
		}
		
		Map<String, String> msgs = Messages.instance();

		String tbl = "<table class='tl-table'>";
		if (numCols == 3)
			tbl += "<tr><th class='tlh-key'>" + msgs.get("LogValue.key") + "</th>" + 
				   "<th><span class='new-value'>" + msgs.get("LogValue.prevValue") + "</span></th>" +
				   "<th><span class='prev-value'>" + msgs.get("LogValue.newValue") + "</span></th>";
		
		tbl += rowhtml + "</table>";
		
		return tbl;
	}
	
	
	/**
	 * Return text to be displayed
	 * @param obj
	 * @return
	 */
	protected String displayText(Object obj) {
		if (obj == null)
			return "";

		if (obj instanceof String)
			return (String)obj;
		
		if (obj instanceof Date) {
			return LocaleDateConverter.getDisplayDate((Date)obj, false);
		}
		
		if (obj instanceof Double) {
			DecimalFormat df = new DecimalFormat("#0.#####");
			return df.format((Double)obj);
		}
		
		if (obj instanceof Enum) {
			String key = obj.getClass().getSimpleName() + '.' + ((Enum)obj).toString();
			return Messages.instance().get(key);
		}
		
		return obj.toString();
	}
	
	protected Object readParameter(Element p) {
		String type = p.attributeValue("type");
		String txt = p.getText();
		
		if (txt.isEmpty())
			return null;

		if (("string".equals(type)) || ("int".equals(type)) || ("long".equals(type)))
			return txt;
		else
		if (("double".equals(type)))
			return StringConverter.stringToDouble(txt);
		else
		if ("date".equals(type))
			return StringConverter.stringToDate(txt);
		else
		if ("bool".equals(type))
			return StringConverter.stringToBool(txt);
		else
		if ("enum".equals(type))
			return StringConverter.stringToEnum(txt);
		return txt;
	}
}
