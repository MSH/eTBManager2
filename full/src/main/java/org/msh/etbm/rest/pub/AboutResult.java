package org.msh.etbm.rest.pub;


import org.msh.etbm.commons.apidoc.annotations.ApiDocField;

/**
 * Created by ricardo on 15/12/14.
 */
public class AboutResult {
    @ApiDocField(description = "System build date")
    private String buildDate;

    @ApiDocField(description = "System build number")
    private String buildNumber;

    @ApiDocField(description = "Country code in use. Null if it's a generic version, " +
            "or using a two-letter code representing the country. Example: br, us, etc")
    private String countryCode;

    @ApiDocField(description = "Implementation version, in the format x.x.x.x")
    private String implementationVersion;

    @ApiDocField(description = "Implementation title, usually the full system name")
    private String implementationTitle;

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
}
