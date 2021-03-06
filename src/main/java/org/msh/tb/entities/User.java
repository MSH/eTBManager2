/*
 * User.java
 *
 * Created on 29 de Janeiro de 2007, 13:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.msh.tb.entities;

import org.hibernate.validator.Email;
import org.hibernate.validator.NotNull;
import org.msh.etbm.commons.transactionlog.Operation;
import org.msh.etbm.commons.transactionlog.mapping.PropertyLog;
import org.msh.tb.entities.enums.UserState;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Store information about a user of the system
 * @author Ricardo Memoria
 */
@Entity
@Table(name="sys_user")
public class User implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
    
    @Column(length=30)
	@NotNull
	@PropertyLog(operations={Operation.NEW})
    private String login;
    
    @Column(length=80, name="user_name")
	@NotNull
	@PropertyLog(operations={Operation.NEW})
    private String name;

    @Column(length=32, name="user_password")
	@NotNull
	@PropertyLog(ignore=true)
    private String password;
    
    @Column(nullable=false, length=80)
	@PropertyLog(operations={Operation.NEW})
    @Email
    private String email;
    
	@PropertyLog(operations={Operation.NEW})
    private UserState state;
	
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="DEFAULTWORKSPACE_ID")
	@PropertyLog(operations={Operation.NEW}, logEntityFields=true)
    private UserWorkspace defaultWorkspace;
    
    @Column(length=6)
    private String language;
    
    @Column(length=50)
    private String timeZone;

	@OneToMany(mappedBy="user", cascade={CascadeType.ALL})
	private List<UserWorkspace> workspaces = new ArrayList<UserWorkspace>();
	
	@Column(length=200)
	private String comments;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PARENTUSER_ID")
	private User parentUser;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date registrationDate;

	@Column(length=50)
	@PropertyLog(messageKey="global.legacyId")
	private String legacyId;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="THEME_ID")
	private UITheme theme;
	
	private boolean sendSystemMessages;

	private boolean ulaAccepted;
	
	/**
	 * Check if password has expired
	 * @return
	 */
	public boolean isPasswordExpired() {
		return state == UserState.PASSWD_EXPIRED;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) 
			return true;
		
		if (!(obj instanceof User))
			return false;
		
		return ((User)obj).getId().equals(getId());
	}


	/**
	 * Search the user workspace by the workspace
	 * @param workspace instance of the workspace to search for the user workspace
	 * @return instance of {@link UserWorkspace}
	 */
	public UserWorkspace getUserWorkspaceByWorkspace(Workspace workspace) {
		for (UserWorkspace ws: getWorkspaces()) {
			if (ws.getWorkspace().equals(workspace)) {
				return ws;
			}
		}
		return null;
	}

	/**
	 * Return the user selected language
	 * @return language in the format accepted by the {@link Locale} class
	 */
	public String getLanguage() {
		return language;
	}

	
	/**
	 * Set the user language
	 * @param defaultLanguage the new language of the user 
	 */
	public void setLanguage(String defaultLanguage) {
		this.language = defaultLanguage;
	}

	@Override
	public String toString() {
		return login + " - " + name;
	}

    /**
     * Return the user ID
     * @return user ID
     */
    public Integer getId() {
        return id;
    }

    
    /**
     * Set the user id
     * @param id new user id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Return the user login. The login is used to enter in the system
     * @return the user login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Set a new user login
     * @param login new user login
     */
    public void setLogin(String login) {
    	this.login = login;
    }

    /**
     * Return the user name
     * @return name of the user
     */
    public String getName() {
        return name;
    }

    /**
     * Change the user name
     * @param name new name of the user
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return the user password hashed using the MD5 algorithm
     * @return the user password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the user password hashed using the MD5 algorithm
     * @param password
     */
    public  void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserState getState() {
        return state;
    }

    public void setState(UserState state) {
        this.state = state;
    }

	public UserWorkspace getDefaultWorkspace() {
		return defaultWorkspace;
	}

	public void setDefaultWorkspace(UserWorkspace defaultWorkspace) {
		this.defaultWorkspace = defaultWorkspace;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public List<UserWorkspace> getWorkspaces() {
		return workspaces;
	}

	public void setWorkspaces(List<UserWorkspace> workspaces) {
		this.workspaces = workspaces;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComments(String comment) {
		this.comments = comment;
	}

	/**
	 * @return the comment
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * @param parentUser the parentUser to set
	 */
	public void setParentUser(User parentUser) {
		this.parentUser = parentUser;
	}

	/**
	 * @return the parentUser
	 */
	public User getParentUser() {
		return parentUser;
	}

	/**
	 * @param registrationDate the registrationDate to set
	 */
	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	/**
	 * @return the registrationDate
	 */
	public Date getRegistrationDate() {
		return registrationDate;
	}


	/**
	 * @return the legacyId
	 */
	public String getLegacyId() {
		return legacyId;
	}


	/**
	 * @param legacyId the legacyId to set
	 */
	public void setLegacyId(String legacyId) {
		this.legacyId = legacyId;
	}


	/**
	 * @return the theme
	 */
	public UITheme getTheme() {
		return theme;
	}


	/**
	 * @param theme the theme to set
	 */
	public void setTheme(UITheme theme) {
		this.theme = theme;
	}


	/**
	 * @return the sendSystemMessages
	 */
	public boolean isSendSystemMessages() {
		return sendSystemMessages;
	}


	/**
	 * @param sendSystemMessages the sendSystemMessages to set
	 */
	public void setSendSystemMessages(boolean sendSystemMessages) {
		this.sendSystemMessages = sendSystemMessages;
	}

	public boolean isUlaAccepted() {
		return ulaAccepted;
	}

	public void setUlaAccepted(boolean ulaAccepted) {
		this.ulaAccepted = ulaAccepted;
	}
	
}
