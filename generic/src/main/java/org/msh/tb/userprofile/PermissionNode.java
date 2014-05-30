package org.msh.tb.userprofile;

import java.util.ArrayList;
import java.util.List;

import org.msh.tb.entities.UserPermission;
import org.msh.tb.entities.UserRole;
import org.msh.tb.entities.enums.CaseClassification;

/**
 * Represents a node in the list of permissions of the user. A node is used as a representative display of
 * the user profile, where permissions are displayed in a tree view
 * @author Ricardo Memoria
 *
 */
public class PermissionNode {

	private UserPermission permission;
	private PermissionNode parent;
	private CaseClassification caseClassification;
	private List<PermissionNode> children = new ArrayList<PermissionNode>();
	private boolean checked;


	public PermissionNode(PermissionNode parent, UserPermission permission,
			CaseClassification caseClassification) {
		super();
		this.permission = permission;
		this.caseClassification = caseClassification;
		this.parent = parent;
		if (this.parent != null)
			this.parent.getChildren().add(this);
	}


	/**
	 * Return unique name to be used in the XHTML form to display sub permissions
	 * @return
	 */
	public String getUniqueName() {
		String s = getUserRole().getCode();
		if (permission.getCaseClassification() != null)
			s += permission.getCaseClassification().toString();
		return s;
	}

	/**
	 * Return checked status 
	 * @return
	 */
	public boolean isChecked() {
		return checked;
	}

	
	/**
	 * Change checked status
	 * @param value
	 */
	public void setChecked(boolean value) {
		checked = value; 
	}

	
	public UserRole getUserRole() {
		return (permission != null? permission.getUserRole(): null);
	}
	
	/**
	 * @return the permission
	 */
	public UserPermission getPermission() {
		return permission;
	}
	/**
	 * @param permission the permission to set
	 */
	public void setPermission(UserPermission permission) {
		this.permission = permission;
	}
	/**
	 * @return the parent
	 */
	public PermissionNode getParent() {
		return parent;
	}
	/**
	 * @param parent the parent to set
	 */
	public void setParent(PermissionNode parent) {
		this.parent = parent;
	}
	/**
	 * @return the caseClassification
	 */
	public CaseClassification getCaseClassification() {
		return caseClassification;
	}
	/**
	 * @param caseClassification the caseClassification to set
	 */
	public void setCaseClassification(CaseClassification caseClassification) {
		this.caseClassification = caseClassification;
	}
	/**
	 * @return the children
	 */
	public List<PermissionNode> getChildren() {
		return children;
	}
	
}
