package org.msh.tb;

import java.util.List;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.entities.AgeRange;
import org.msh.tb.transactionlog.LogInfo;



/**
 * Handle Age Ranges in the system
 * @author Ricardo Memoria
 *
 */
@Name("ageRangeHome")
@LogInfo(roleName="AGERANGES")
public class AgeRangeHome extends EntityHomeEx<AgeRange> {
	private static final long serialVersionUID = 1L;

	@In(create=true) FacesMessages facesMessages;
	
	private List<AgeRange> items;


	@Factory("ageRange")
	public AgeRange getAgeRange() {
		return getInstance();
	}
	
	/**
	 * Return the list of age rages
	 * @return List of {@link AgeRange} objects
	 */
	public List<AgeRange> getItems() {
		if (items == null)
			createItems();
		return items;
	}

	@Override
	public String persist() {
		AgeRange range = getInstance();
		
		// check ranges
		if (range.getIniAge() >= range.getEndAge()) {
			facesMessages.addToControlFromResourceBundle("iniage", "admin.ageranges.msgerror1");
			return "error";
		}
		
		// verifica se ja existe faixa etaria igual
		AgeRange instance = getInstance();
		for (AgeRange age: getItems()) {
			if ((age != instance) &&
				(age.getIniAge() == instance.getIniAge()) &&
				(age.getEndAge() == instance.getEndAge())) {
				facesMessages.addFromResourceBundle("admin.ageranges.msgerror2");
				return "error";
			}
		}
		
		adjustAgeRanges();
		
		boolean newInstance = !isManaged();
		
		String ret = super.persist();
		if ((ret.equals("persisted")) && (newInstance) && (items != null))
			items.add(instance);
		
		return ret;
	}
	
	
	@Override
	public String remove() {
		if (items != null)
			items.remove(instance);
		return super.remove();
	}


	
	/**
	 * Adjust the age ranges according to the instance being created/edited
	 */
	protected void adjustAgeRanges() {
		List<AgeRange> ages = getItems();
		int index = 0;
		AgeRange instance = getInstance();
		
		while (index < ages.size()) {
			AgeRange range = ages.get(index);
			if (range != instance) {
				if ((range.getIniAge() >= instance.getIniAge()) && 
					(range.getEndAge() <= instance.getEndAge())) {
					getEntityManager().remove(range);
					index--;
				}
				else
				if ((range.getIniAge() < instance.getIniAge()) &&
					(range.getEndAge() > instance.getEndAge())) 
				{
					int end = range.getEndAge();
					range.setEndAge(instance.getIniAge() - 1);
					AgeRange aux = new AgeRange();
					aux.setIniAge(instance.getEndAge() + 1);
					aux.setEndAge(end);
					getEntityManager().persist(aux);
					ages.add(aux);
				}
				else
				if ((range.getIniAge() >= instance.getIniAge()) && (range.getIniAge() <= instance.getEndAge())) {
					range.setIniAge(instance.getEndAge() + 1);
				}
				else
				if ((range.getEndAge() <= instance.getEndAge()) && (range.getEndAge() >= instance.getIniAge())) {
					range.setEndAge(instance.getIniAge() - 1);
				}
			}
			index++;
		}		
	}
	
	/**
	 * Create the list of age range objects
	 */
	protected void createItems() {
		items = getEntityManager().createQuery("from AgeRange r where r.workspace.id = #{defaultWorkspace.id} order by r.iniAge").getResultList();
	}


	/**
	 * Search for an specific are range by the age
	 * @param age Age to search for range
	 * @return {@link AgeRange} of the age informed
	 */
	public AgeRange findRange(int age) {
		AgeRange res = null;
		for (AgeRange range: getItems()) {
			if ((res == null) || ((age >= range.getIniAge()) && (range.getIniAge() > res.getIniAge())))
				res = range;
		}
		
		return res;
	}
}
