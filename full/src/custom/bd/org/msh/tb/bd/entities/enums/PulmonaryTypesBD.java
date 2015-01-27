package org.msh.tb.bd.entities.enums;

public enum PulmonaryTypesBD {
     POSITIVE,
     NEGATIVE;

     public String getKey() {
            return getClass().getSimpleName().concat("." + name());
        }
}
