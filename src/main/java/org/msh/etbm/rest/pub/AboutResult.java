package org.msh.etbm.rest.pub;


import org.msh.etbm.commons.apidoc.annotations.ApiDocField;

import java.util.List;

/**
 * Created by ricardo on 15/12/14.
 */
public class AboutResult {
    @ApiDocField(description = "System build date")
    private String buildDate;

    @ApiDocField(description = "System build number")
    private String buildNumber;

    @ApiDocField(description = "Null if it's a generic version, or country code in the format ZZ")
    private String countryCode;

    @ApiDocField(description = "Implementation version, in the format x.x.x.x")
    private String implementationVersion;

    @ApiDocField(description = "Implementation title, usually the full system name")
    private String implementationTitle;

    @ApiDocField(description = "Base bath including server name and initial path")
    private String basePath;

    @ApiDocField(description = "List of supported languages (ISO format)")
    private List<LocaleInfo> suportedLocales;

    public String getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(String buildDate) {
        this.buildDate = buildDate;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getImplementationVersion() {
        return implementationVersion;
    }

    public void setImplementationVersion(String implementationVersion) {
        this.implementationVersion = implementationVersion;
    }

    public String getImplementationTitle() {
        return implementationTitle;
    }

    public void setImplementationTitle(String implementationTitle) {
        this.implementationTitle = implementationTitle;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public List<LocaleInfo> getSuportedLocales() {
        return suportedLocales;
    }

    public void setSuportedLocales(List<LocaleInfo> suportedLocales) {
        this.suportedLocales = suportedLocales;
    }
}
