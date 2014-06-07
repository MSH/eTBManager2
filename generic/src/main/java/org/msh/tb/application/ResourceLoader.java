package org.msh.tb.application;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * Replace the SEAM {@link org.jboss.seam.core.ResourceLoader} to include property
 * files created to specific workspace customizations
 * 
 * @author Ricardo Memoria
 *
 */
@Scope(ScopeType.STATELESS)
@BypassInterceptors
@Name("org.jboss.seam.core.resourceLoader")
public class ResourceLoader extends org.jboss.seam.core.ResourceLoader {

	private String[] names;
	
	/* (non-Javadoc)
	 * @see org.jboss.seam.core.ResourceLoader#getBundleNames()
	 */
	@Override
	public String[] getBundleNames() {
		if (names == null) {
			names = super.getBundleNames();
			List<String> extraNames = getExtraBundleFiles();
			if (extraNames.size() > 0) {
				int index = names.length;
				names = Arrays.copyOf(names, names.length + extraNames.size());
				for (String name: extraNames) {
					names[index++] = name;
				}
			}
		}
		return names;
	}

	
	/**
	 * Return the extra list of bundle files added because of specific workspace customization
	 * @return
	 */
	public List<String> getExtraBundleFiles() {
		List<String> lst = new ArrayList<String>();
		try {
			String dir = "WEB-INF/classes";
			URL url = ResourceLoader.class.getClassLoader().getResource(dir);
			// url = ResourceLoader.class.getClassLoader().getResource("WEB-INF/classes/messages_en.properties");
			File folder = new File(url.toURI());
			for (File file: folder.listFiles()) {
				String fname = file.getName();
				if (fname.startsWith("messages-")) {
					int pos = fname.indexOf("_");
					if (pos > 0)
						fname = fname.substring(0, pos);
					if (!lst.contains(fname))
						lst.add(fname);
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return lst;
	}
}
