/*
 * UserRole.java
 *
 * Created on 31 de Janeiro de 2007, 15:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.msh.mdrtb.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.jboss.seam.international.Messages;

/**
 *
 * @author Ricardo
 */

@Entity
public class UserRole implements java.io.Serializable, Comparable<UserRole> {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    @Column(length=80, nullable=false, name="Role_Name")
    private String name;

    private String code;

    /**
     * Describes if the role is for executing an operation
     */
    private boolean executable;

    /**
     * Describes if this role contains operations of read-write, like, for instance, insert-update-delete command
     */
    private boolean changeable;
    
    /**
     * Describes if the role is used internally by the system or is available to be assigned in a profile
     */
    private boolean internalUse;

    public int getLevel() {
		if ((code == null) || (code.isEmpty()))
			 return 0;
		
		if (code.endsWith("0000"))
			 return 1;
		else
		if (code.endsWith("00"))
			 return 2;
		else
		if (code.length() == 6)
			return 3;
		else return 0;
	}

    public String getDisplayName() {
    	String msg = "userrole." + name;
    	return Messages.instance().get(msg);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int compareTo(UserRole userRole) {
		return code.compareTo(userRole.getCode());
	}

	/**
	 * @return the executable
	 */
	public boolean isExecutable() {
		return executable;
	}

	/**
	 * @param executable the executable to set
	 */
	public void setExecutable(boolean executable) {
		this.executable = executable;
	}

	/**
	 * @return the changeable
	 */
	public boolean isChangeable() {
		return changeable;
	}

	/**
	 * @param changeable the changeable to set
	 */
	public void setChangeable(boolean changeable) {
		this.changeable = changeable;
	}

	/**
	 * @param internalUse the internalUse to set
	 */
	public void setInternalUse(boolean internalUse) {
		this.internalUse = internalUse;
	}

	/**
	 * @return the internalUse
	 */
	public boolean isInternalUse() {
		return internalUse;
	}
}
