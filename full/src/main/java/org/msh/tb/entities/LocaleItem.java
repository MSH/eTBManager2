package org.msh.tb.entities;

import org.jboss.seam.international.Messages;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class LocaleItem {

	@Id
	private int id;
	
	@Column(length=50,name="name_id")
	private String name;
	
	public String toString() {
		if (id == 0)
			 return super.toString();
		else return Integer.toString(id);
	}

	public int getValue() {
		return id;
	}
	
	public String getKeyItemDisplay() {
		return Integer.toString(id) + " - " + getDisplayName();
	}
	
    /**
     * Retorna o nome de exibi��o da classe de acordo com a l�ngua selecionada 
     * @return
     */
    public String getDisplayName() {
    	String msg = getClass().getSimpleName() + "." + name;
    	return Messages.instance().get(msg);
    }
    
    public String getName() {
    	return name;
    }
}
