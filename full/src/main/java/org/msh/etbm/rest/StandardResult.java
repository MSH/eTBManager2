package org.msh.etbm.rest;

/**
 * Standard REST result from a call. Used throughout the system
 *
 * Created by ricardo on 24/11/14.
 */
public class StandardResult {
    private boolean success;
    private Object result;

    /**
     * Default constructor
     * @param success true if result was succeeded
     * @param result the result to be serialized
     */
    public StandardResult(boolean success, Object result) {
        this.success = success;
        this.result = result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
