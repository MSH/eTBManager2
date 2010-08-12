package org.msh.mdrtb.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.validator.NotNull;

@Entity
public class UserPermission implements Serializable, Comparable<UserPermission> {
	private static final long serialVersionUID = 7565244271956307412L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@ManyToOne
	@JoinColumn(name="ROLE_ID")
	@NotNull
	private UserRole userRole;

	@ManyToOne
	@JoinColumn(name="PROFILE_ID")
	@NotNull
	private UserProfile userProfile;

	private boolean canOpen;
	private boolean canChange;
	private boolean canExecute;
	private boolean grantPermission;

	public boolean isGrantPermission() {
		return grantPermission;
	}

	public void setGrantPermission(boolean grantPermission) {
		this.grantPermission = grantPermission;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public UserRole getUserRole() {
		return userRole;
	}

	public void setUserRole(UserRole userRole) {
		this.userRole = userRole;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public int compareTo(UserPermission userPermission) {
		return (((getUserRole() != null) && (userPermission.getUserRole() != null))? 
				userRole.compareTo(userPermission.getUserRole()): 
				0);
	}

	/**
	 * @return the canOpen
	 */
	public boolean isCanOpen() {
		return canOpen;
	}

	/**
	 * @param canOpen the canOpen to set
	 */
	public void setCanOpen(boolean canOpen) {
		this.canOpen = canOpen;
	}

	/**
	 * @return the canChange
	 */
	public boolean isCanChange() {
		return canChange;
	}

	/**
	 * @param canChange the canChange to set
	 */
	public void setCanChange(boolean canChange) {
		this.canChange = canChange;
	}

	/**
	 * @return the canExecute
	 */
	public boolean isCanExecute() {
		return canExecute;
	}

	/**
	 * @param canExecute the canExecute to set
	 */
	public void setCanExecute(boolean canExecute) {
		this.canExecute = canExecute;
	}

}
