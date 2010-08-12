package org.msh.mdrtb.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
public class ResistancePattern extends WSObject {
	private static final long serialVersionUID = -2663077939894708009L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
	
	@Column(length=100, name="PATTERN_NAME")
	private String name;

	@ManyToMany
	@JoinTable(name="SUBSTANCES_RESISTPATTERN")
	private List<Substance> substances = new ArrayList<Substance>();
	
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
	 * @return the substances
	 */
	public List<Substance> getSubstances() {
		return substances;
	}

	/**
	 * @param substances the substances to set
	 */
	public void setSubstances(List<Substance> substances) {
		this.substances = substances;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}
