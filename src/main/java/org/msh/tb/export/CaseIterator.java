package org.msh.tb.export;

import org.msh.tb.entities.TbCase;

public interface CaseIterator {

	/**
	 * Return the number of cases
	 * @return
	 */
	int getResultCount();
	
	/**
	 * Include the titles  
	 */
	void addTitles();

	/**
	 * Export the content and return the current case
	 * @param index
	 * @return
	 */
	TbCase exportContent(int index);
}
