package org.msh.tb.entities;

import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Store information about a UI theme selected by the user 
 * @author Ricardo Memoria
 *
 */
@Entity
@Table(name="uitheme")
public class UITheme implements Serializable {
	private static final long serialVersionUID = 1209318241644185683L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@Column(length=100)
	@NotNull
	private String name;
	
	@Column(length=250)
	@NotNull
	private String path;
	
	private boolean systemTheme;
	
	private boolean defaultTheme;

	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) 
			return true;
		
		if (!(obj instanceof UITheme))
			return false;

		Integer objid = ((UITheme)obj).id;
		
		if ((objid == null) || (id == null))
			return false;
		
		return ((UITheme)obj).id.equals(id);
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return the systemTheme
	 */
	public boolean isSystemTheme() {
		return systemTheme;
	}

	/**
	 * @param systemTheme the systemTheme to set
	 */
	public void setSystemTheme(boolean systemTheme) {
		this.systemTheme = systemTheme;
	}

	/**
	 * @return the defaultTheme
	 */
	public boolean isDefaultTheme() {
		return defaultTheme;
	}

	/**
	 * @param defaultTheme the defaultTheme to set
	 */
	public void setDefaultTheme(boolean defaultTheme) {
		this.defaultTheme = defaultTheme;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
}
