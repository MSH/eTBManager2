package org.msh.tb.ge.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.tb.entities.User;
import org.msh.tb.entities.WSObject;

@Entity
@Table(name="integration_history")
public class IntegrationHistory extends WSObject{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4162004129794272340L;
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	@Temporal(TemporalType.DATE)
	private Date lastIntegrationDate;
	
	private int noOfRecords;
	
	@OneToOne
	@JoinColumn(name="user_id")
	private User user;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getLastIntegrationDate() {
		return lastIntegrationDate;
	}

	public void setLastIntegrationDate(Date lastIntegrationDate) {
		this.lastIntegrationDate = lastIntegrationDate;
	}

	public int getNoOfRecords() {
		return noOfRecords;
	}

	public void setNoOfRecords(int noOfRecords) {
		this.noOfRecords = noOfRecords;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	
	

}
