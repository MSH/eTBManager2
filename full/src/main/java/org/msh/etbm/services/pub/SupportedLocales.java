package org.msh.etbm.services.pub;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by rmemoria on 5/5/15.
 */
@Name("supportedLocales")
@Scope(ScopeType.APPLICATION)
public class SupportedLocales {
    private String defaultLocaleCode;
    private List<Locale> locales;

    private List<String> localeCodes = new ArrayList<String>();

    public List<Locale> getLocales() {
        if (locales == null) {
            createLocales();
        }
        return locales;
    }

    public void createLocales() {
        locales = new ArrayList<Locale>();
        if (localeCodes == null) {
            return;
        }

        for (String code: localeCodes) {
            locales.add(new Locale(code));
        }
    }

    public Locale getDefaultLocale() {
        return new Locale(defaultLocaleCode);
    }

    public List<String> getLocaleCodes() {
        return localeCodes;
    }

    public void setLocaleCodes(List<String> codes) {
        localeCodes = codes;
        locales = null;
    }

    public String getDefaultLocaleCode() {
        return defaultLocaleCode;
    }

    public void setDefaultLocaleCode(String defaultLocaleCode) {
        this.defaultLocaleCode = defaultLocaleCode;
    }
}
