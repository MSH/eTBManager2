/*
 * UserLogin.java
 *
 * Created on 29 de Janeiro de 2007, 13:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.msh.mdrtb.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.NotNull;
import org.jboss.seam.core.Locale;

/**
 *
 * @author Ricardo
 */

@Entity
public class UserLogin implements java.io.Serializable {
    
	private static final long serialVersionUID = -6513121479803870524L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name="USER_ID")
	@NotNull
    private User user;
    
    @Temporal(value = TemporalType.TIMESTAMP)
	@NotNull
    private Date loginDate;
    
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date logoutDate;
    
    @Column(length=200)
    private String Application;
    
    @Column(length=16)
    private String IpAddress;

    @ManyToOne
    @JoinColumn(name="WORKSPACE_ID")
	@NotNull
    private Workspace workspace;
    
    public Workspace getDefaultWorkspace() {
    	return (user != null? getUser().getDefaultWorkspace().getWorkspace(): null);
    }

    public String getDisplayLocale() {
    	return Locale.instance().getDisplayName(Locale.instance());
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }

    public Date getLogoutDate() {
        return logoutDate;
    }

    public void setLogoutDate(Date logoutDate) {
        this.logoutDate = logoutDate;
    }

    public String getApplication() {
        return Application;
    }

    public void setApplication(String Application) {
        this.Application = Application;
    }

    public String getIpAddress() {
        return IpAddress;
    }

    public void setIpAddress(String IpAddress) {
        this.IpAddress = IpAddress;
    }

	/**
	 * @param workspace the workspace to set
	 */
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	/**
	 * @return the workspace
	 */
	public Workspace getWorkspace() {
		return workspace;
	}
}
