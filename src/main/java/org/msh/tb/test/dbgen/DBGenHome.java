package org.msh.tb.test.dbgen;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Workspace;

@Name("dbgenHome")
public class DBGenHome {

	@In(create=true) PreferencesReader preferencesReader;
	@In(create=true) CaseGenerator caseGenerator;
	@In(required=true) Workspace defaultWorkspace;

	private GeneratorPreferences preferences = new GeneratorPreferences();

	/**
	 * Read the preference file and execute the database generator based on the preferences in the 
	 * preferenceGenerator property
	 * @return "success" if executed successfully
	 */
	public String readAndExecute() {
		// read the preferences file
		String s = preferencesReader.read(preferences);
		
		if (!s.equals("success"))
			return "error";
		
		return execute();
	}


	/**
	 * Execute the database generator based on the preferences in the preferenceGenerator property
	 * @return "success" if executed successfully
	 */
	public String execute() {
		caseGenerator.execute(defaultWorkspace, preferences);
		return "success";
	}


	/**
	 * Returns the preferences to be used during executing of the database generator
	 * @return GeneratorPreferences instance
	 */
	public GeneratorPreferences getPreferences() {
		return preferences;
	}


	/**
	 * @param preferences the preferences to set
	 */
	public void setPreferences(GeneratorPreferences preferences) {
		this.preferences = preferences;
	}

}
