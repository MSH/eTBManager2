package org.msh.tb.entities;

import org.hibernate.validator.NotNull;
import org.msh.etbm.transactionlog.mapping.PropertyLog;
import org.msh.tb.entities.enums.YesNoType;

import javax.persistence.*;

/**
 * Holds information about a side effect of a TB case
 * @author Ricardo Lima
 *
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="DISCRIMINATOR", discriminatorType=DiscriminatorType.STRING)
@DiscriminatorValue("gen")
@Table(name="casesideeffect")
public class CaseSideEffect implements Transactional, SyncKey {


	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@Embedded
	@AssociationOverrides({ @AssociationOverride(name = "value", joinColumns = @JoinColumn(name = "SIDEEFFECT_ID")) })
	@AttributeOverrides({ @AttributeOverride(name = "complement", column = @Column(name = "otherAdverseEffect")) })
	@NotNull
	private FieldValueComponent sideEffect;
	
	@ManyToOne
	@JoinColumn(name="CASE_ID")
	@NotNull
	private TbCase tbcase;
	
	private String medicines;
	
	@Column(name="SE_MONTH")
	private int month;
	
	private YesNoType resolved;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SUBSTANCE_ID")
	private Substance substance;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SUBSTANCE2_ID")
	private Substance substance2;
	
	@Lob
	private String comment;
		
	/**
	 * Point to the transaction log that contains information about the last time this entity was changed (updated or created)
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="lastTransaction_ID")
	@PropertyLog(ignore=true)
	private TransactionLog lastTransaction;
	
	// Ricardo: TEMPORARY UNTIL A SOLUTION IS FOUND. Just to attend a request from the XML data model to
	// map an XML node to a property in the model
	@Transient
	private Integer clientId;
	
	/**
	 * @return
	 */
	public Integer getClientId() {
		return clientId;
	}
	
	/**
	 * @param clientId
	 */
	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the comments
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comments the comments to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the resolved
	 */
	public YesNoType getResolved() {
		return resolved;
	}

	/**
	 * @param resolved the resolved to set
	 */
	public void setResolved(YesNoType resolved) {
		this.resolved = resolved;
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
	 * @return the sideEffect
	 */
	public FieldValueComponent getSideEffect() {
		if (sideEffect == null)
			sideEffect = new FieldValueComponent();
		return sideEffect;
	}

	/**
	 * @param sideEffect the sideEffect to set
	 */
	public void setSideEffect(FieldValueComponent sideEffect) {
		this.sideEffect = sideEffect;
	}

	/**
	 * @return the medicines
	 */
	public String getMedicines() {
		return medicines;
	}

	/**
	 * @param medicines the medicines to set
	 */
	public void setMedicines(String medicines) {
		this.medicines = medicines;
	}

	/**
	 * @return the month
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * @param month the month to set
	 */
	public void setMonth(int month) {
		this.month = month;
	}

	/**
	 * @return the tbcase
	 */
	public TbCase getTbcase() {
		return tbcase;
	}

	/**
	 * @param tbcase the tbcase to set
	 */
	public void setTbcase(TbCase tbcase) {
		this.tbcase = tbcase;
	}

	/**
	 * @return the substance
	 */
	public Substance getSubstance() {
		return substance;
	}

	/**
	 * @param substance the substance to set
	 */
	public void setSubstance(Substance substance) {
		this.substance = substance;
	}

	/**
	 * @return the substance2
	 */
	public Substance getSubstance2() {
		return substance2;
	}

	/**
	 * @param substance2 the substance2 to set
	 */
	public void setSubstance2(Substance substance2) {
		this.substance2 = substance2;
	}

	/**
	 * @return the lastTransaction
	 */
	@Override
	public TransactionLog getLastTransaction() {
		return lastTransaction;
	}

	/**
	 * @param lastTransaction the lastTransaction to set
	 */
	@Override
	public void setLastTransaction(TransactionLog lastTransaction) {
		this.lastTransaction = lastTransaction;
	}
}
