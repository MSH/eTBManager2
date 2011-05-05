package org.msh.tb.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.validator.NotNull;
import org.msh.utils.date.DateUtils;


@Entity
public class MedicineReceiving implements Serializable {
	private static final long serialVersionUID = -4879291745573056893L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@ManyToOne
	@JoinColumn(name="UNIT_ID")
	@NotNull
	private Tbunit tbunit;
	
	@Temporal(TemporalType.DATE)
	@NotNull
	private Date receivingDate;
	
	@ManyToOne
	@JoinColumn(name="SOURCE_ID")
	@NotNull
	private Source source;
	
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="medicineReceiving")
	private List<MedicineReceivingItem> medicines = new ArrayList<MedicineReceivingItem>();

	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return (source != null? source.toString() + " - " + DateUtils.formatDate(receivingDate, "dd-MMM-yyyy"): super.toString());
	}

	@Transient
	public float getTotalPrice() {
		float tot = 0;
		
		for (MedicineReceivingItem item: medicines) {
			tot += item.getTotalPrice();
		}
		
		return tot;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getReceivingDate() {
		return receivingDate;
	}

	public void setReceivingDate(Date receivingDate) {
		this.receivingDate = receivingDate;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public List<MedicineReceivingItem> getMedicines() {
		return medicines;
	}

	public void setDrugs(List<MedicineReceivingItem> medicines) {
		this.medicines = medicines;
	}

	public Tbunit getTbunit() {
		return tbunit;
	}

	public void setTbunit(Tbunit tbunit) {
		this.tbunit = tbunit;
	}

}
