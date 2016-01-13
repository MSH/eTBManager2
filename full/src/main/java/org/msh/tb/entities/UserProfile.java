package org.msh.tb.entities;

import org.hibernate.validator.NotNull;
import org.msh.etbm.commons.transactionlog.mapping.PropertyLog;
import org.msh.tb.entities.enums.CaseClassification;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="userprofile")
public class UserProfile extends WSObject implements Serializable, Comparable<UserProfile>, SyncKey {
	private static final long serialVersionUID = -1798192936426485144L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

	@Column(length=100)
	@NotNull
	@PropertyLog(messageKey="form.name")
	private String name;

	@OneToMany(fetch=FetchType.LAZY, mappedBy="userProfile",cascade={CascadeType.ALL})
	@PropertyLog(alwaysLog = true)
    private List<UserPermission> permissions = new ArrayList<UserPermission>();

	@Column(length=50)
	@PropertyLog(messageKey="global.legacyId")
	private String legacyId;

	@Transient
	// Ricardo: TEMPORARY UNTIL A SOLUTION IS FOUND. Just to attend a request from the XML data model to
	// map an XML node to a property in the model
	private Integer clientId;

	@Override
	public boolean equals(Object obj) {
		if (obj == this) 
			return true;
		
		if (!(obj instanceof UserProfile))
			return false;
		
		Integer objId = ((UserProfile)obj).getId();
		
		if (objId == null) 
			return false;
		
		return objId.equals(getId());
	}

	@Override
	public String toString() {
		return (getName() != null? name: super.toString());
	}
	
	public UserPermission permissionByRole(UserRole role, CaseClassification classif) {
		for (UserPermission up: getPermissions()) {
			if (up.getUserRole().equals(role)) {
				if ((classif == null) || ((classif != null) && (classif == up.getCaseClassification())))
					return up;
			}
		}
		return null;
	}
	
	public Integer getId() {
		return id;
	}

	@Override
	public Integer getClientId() {
		return clientId;
	}

	@Override
	public void setClientId(Integer clientId) {
		this.clientId = clientId;
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


	public int compareTo(UserProfile userProfile) {
		return name.compareTo(userProfile.getName());
	}

	public List<UserPermission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<UserPermission> permissions) {
		this.permissions = permissions;
	}

	/**
	 * @param legacyId the legacyId to set
	 */
	public void setLegacyId(String legacyId) {
		this.legacyId = legacyId;
	}

	/**
	 * @return the legacyId
	 */
	public String getLegacyId() {
		return legacyId;
	}
}
