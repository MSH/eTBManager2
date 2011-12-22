package org.msh.tb.transactionlog;

import java.util.Date;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.utils.date.DateUtils;

@Name("xmltest")
public class Xmltest {

	private String html;
	private String xml;
	
	public void generateXml() {
		DetailXMLWriter writer = new DetailXMLWriter();

		writer.addMessage("MovementException.NEGATIVE_STOCK", "12345", new Date());
		
		writer.addText("Este é um simples exemplo de texto.\nEle tem várias linhas e uma <tag> no meio.");
		
		writer.addTableRow("form.save", 213.4, "teste");
		
		xml = writer.asXML();

		
/*		DetailHtmlConverter c = new DetailHtmlConverter();
		html = c.convertToString(xml);
		System.out.println(html);
*/		
/*		try {
			Document doc = DocumentHelper.parseText(s);
			elem = doc.getRootElement();
			String val = elem.attribute("id").getValue();
			System.out.println(elem.getName() + " ... id=" + val);
			
			for (Iterator<Element> i = elem.elementIterator(); i.hasNext();) {
				Element aux = i.next();
				System.out.println(aux.getText());
			}

			System.out.println(doc.asXML());
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/	}
	
	
	public void test() {
		EntityManager em = (EntityManager)Component.getInstance("entityManager");
		TbCase aux = em.find(TbCase.class, 20581);

		TransactionLogService srv = (TransactionLogService)Component.getInstance("transactionLogService");
		srv.recordEntityState(aux, Operation.NEW);
		aux.setRegistrationDate( DateUtils.incMonths(aux.getRegistrationDate(), 1) ); 
		srv.save("CASE_DATA", RoleAction.EDIT, aux.toString(), aux.getId());
		
/*		EntityLogMapping map = EntityLogManager.instance().getEntityMapping(aux);
		
		List<PropertyValue> values = map.describeEntity(aux);
		
		DetailXMLWriter writer = new DetailXMLWriter();
		
		for (PropertyValue pv: values) {
			writer.addTableRow(pv.getMapping().getMessageKey(), pv.getValue());
		}
		
		xml = writer.asXML();
		
		System.out.println(xml);
*/		
/*		Class c = Hibernate.getClass(u);
		while (c != Object.class) {
			Field[] fields = c.getDeclaredFields();
			for (Field f: fields) {
				String s = f.getName();
				char ch = Character.toUpperCase( s.charAt(0) );
				String getname = "get" + ch + s.substring(1);
				boolean isProperty = true;
				
				try {
					Class[] params = null;
					c.getMethod(getname, params);	} 
				catch (Exception e) {
					isProperty = false;
				}

				if (isProperty)
					System.out.println(s);
			}
			c = c.getSuperclass();
		}
*/	}

	public String getXml() {
		if (xml == null)
			generateXml();
		
		return xml; 
	}
	
	/**
	 * @return the html
	 */
	public String getHtml() {
		return html;
	}

	/**
	 * @param html the html to set
	 */
	public void setHtml(String html) {
		this.html = html;
	}
}
