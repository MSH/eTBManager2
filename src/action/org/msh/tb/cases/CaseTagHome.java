package org.msh.tb.cases;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.Tag;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.Workspace;
import org.msh.utils.ItemSelect;
import org.msh.utils.ItemSelectHelper;

@Name("caseTagHome")
public class CaseTagHome {

	@In(create=true) EntityManager entityManager;
	@In(required=true) CaseHome caseHome;

	private List<ItemSelect<Tag>> items;

	private String tagName;
	

	/**
	 * Save tags to the case
	 * @return
	 */
	public String saveTags() {
		if (items == null)
			return "error";
		
		List<Tag> tags = ItemSelectHelper.getSelectedItems(items, true);
		
		TbCase tbcase = caseHome.getInstance();
		tbcase.setTags(tags);
		
		caseHome.persist();
		
		return "tags-saved";
	}


	/**
	 * Save new tag in the system
	 * @return
	 */
	public String saveNewTag() {
		if ((tagName == null) || (tagName.isEmpty()))
			return "error";
		
		Workspace workspace = (Workspace)Component.getInstance("defaultWorkspace");

		Tag tag = new Tag();
		tag.setName(tagName);
		tag.setWorkspace(entityManager.find (Workspace.class, workspace.getId() ));
		
		entityManager.persist(tag);
		
		if (items != null)
			items.add(new ItemSelect<Tag>(tag, true));
		
		tagName = null;

		return "persisted";
	}

	/**
	 * @return the items
	 */
	public List<ItemSelect<Tag>> getItems() {
		if (items == null)
			createItems();
		return items;
	}

	
	/**
	 * Create a list of items to be selected
	 */
	private void createItems() {
		List<Tag> tags = entityManager
			.createQuery("from Tag where workspace.id = #{defaultWorkspace.id}")
			.getResultList();

		TbCase tbcase = caseHome.getInstance();
		items = ItemSelectHelper.createList(tags);
		
		ItemSelectHelper.selectItems(items, tbcase.getTags(), true);
	}


	/**
	 * @return the tagName
	 */
	public String getTagName() {
		return tagName;
	}


	/**
	 * @param tagName the tagName to set
	 */
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
}
