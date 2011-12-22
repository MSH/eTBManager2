package org.msh.utils;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

@Name("convertHtmlText")
@org.jboss.seam.annotations.faces.Converter(id="rmemoria.TextConverter")
@BypassInterceptors
public class ConvertHtmlText implements Converter {

	public Object getAsObject(FacesContext facesContext, UIComponent comp, String value) throws ConverterException {
		return value;
	}

	public String getAsString(FacesContext facesContext, UIComponent comp, Object object) throws ConverterException {
		String s = object.toString();
		s = escapeHtml(s);
		return s.replaceAll("\n", "<br/>");
	}
	
	protected String escapeHtml(String text) {
		final StringBuilder result = new StringBuilder();
	     final StringCharacterIterator iterator = new StringCharacterIterator(text);
	     char character =  iterator.current();
	     while (character != CharacterIterator.DONE ){
	       if (character == '<') {
	         result.append("&lt;");
	       }
	       else if (character == '>') {
	         result.append("&gt;");
	       }
	       else if (character == '&') {
	         result.append("&amp;");
	      }
	       else if (character == '\"') {
	         result.append("&quot;");
	       }
	       else if (character == '\'') {
	         result.append("&#039;");
	       }
	       else if (character == '(') {
	         result.append("&#040;");
	       }
	       else if (character == ')') {
	         result.append("&#041;");
	       }
	       else if (character == '#') {
	         result.append("&#035;");
	       }
	       else if (character == '%') {
	         result.append("&#037;");
	       }
	       else if (character == ';') {
	         result.append("&#059;");
	       }
	       else if (character == '+') {
	         result.append("&#043;");
	       }
	       else if (character == '-') {
	         result.append("&#045;");
	       }
	       else {
	         //the char is not a special one
	         //add it to the result as is
	         result.append(character);
	       }
	       character = iterator.next();
	     }
	     return result.toString();
	}
}
