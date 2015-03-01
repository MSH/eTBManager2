package org.msh.tb.ng.entities.enums;

/**
 * Created by Mauricio on 25/02/2015.
 */
public enum HIVPositionDetail {
    BEFORE_REGISTRATION,
    AFTER_REGISTRATION;

    public String getKey(){
        return this.getClass().getSimpleName()+"."+this.toString();
    }

}
