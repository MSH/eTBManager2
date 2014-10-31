package org.msh.tb.application.update;

/**
 * Created by ricardo on 31/10/14.
 */
public class HttpResult {
    private int responseCode;
    private String result;
    private String errorMessage;

    public HttpResult(int responseCode, String result, String errorMessage) {
        this.responseCode = responseCode;
        this.result = result;
        this.errorMessage = errorMessage;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
