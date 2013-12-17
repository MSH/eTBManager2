package org.msh.tb.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.validator.NotNull;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.PropertyLog;

@Entity
@Table(name="userworkspace")
public class UserWorkspace extends WSObject implements Serializable{
	private static final long serialVersionUID = 8975350130212905881L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="HEALTHSYSTEM_ID")
	@PropertyLog(operations={Operation.NEW})
	private HealthSystem healthSystem;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TBUNIT_ID")
	@NotNull
	@PropertyLog(operations={Operation.NEW})
	private Tbunit tbunit;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USER_ID")
	@NotNull
	@PropertyLog(ignore=true)
	private User user;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PROFILE_ID")
	@NotNull
	@PropertyLog(operations={Operation.NEW})
	private UserProfile profile;
	
	@Column(name="USER_VIEW")
	@NotNull
	@PropertyLog(operations={Operation.NEW})
	private UserView view;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ADMINUNIT_ID")
	@PropertyLog(operations={Operation.NEW})
	private AdministrativeUnit adminUnit;
    
    private boolean playOtherUnits;

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (!(obj instanceof UserWorkspace))
			return false;

		return ((UserWorkspace)obj).getId().equals(getId());
	}

	
	/**
	 * Return the text to be displayed according to the view
	 * @return
	 */
	public String getDisplayView() {
		switch (getView()) {
		case COUNTRY:
			return getWorkspace().getName().toString();
		case ADMINUNIT:
			return (getAdminUnit() != null? adminUnit.getCountryStructure().getName().toString() + ": " + adminUnit.getName().toString(): null);
		case TBUNIT:
			return Messages.instance().get("UserView.TBUNIT") + ": " + getTbunit().getName().toString();
		default:
			return null;
		}
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Tbunit getTbunit() {
		return tbunit;
	}

	public void setTbunit(Tbunit tbunit) {
		this.tbunit = tbunit;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public UserProfile getProfile() {
		return profile;
	}

	public void setProfile(UserProfile profile) {
		this.profile = profile;
	}

	public UserView getView() {
		return view;
	}

	public void setView(UserView view) {
		this.view = view;
	}

	public boolean isPlayOtherUnits() {
		return playOtherUnits;
	}

	public void setPlayOtherUnits(boolean playOtherUnits) {
		this.playOtherUnits = playOtherUnits;
	}

	/**
	 * @param adminUnit the adminUnit to set
	 */
	public void setAdminUnit(AdministrativeUnit adminUnit) {
		this.adminUnit = adminUnit;
	}

	/**
	 * @return the adminUnit
	 */
	public AdministrativeUnit getAdminUnit() {
		return adminUnit;
	}


	/**
	 * @return the healthSystem
	 */
	public HealthSystem getHealthSystem() {
		return healthSystem;
	}


	/**
	 * @param healthSystem the healthSystem to set
	 */
	public void setHealthSystem(HealthSystem healthSystem) {
		this.healthSystem = healthSystem;
	}
}
