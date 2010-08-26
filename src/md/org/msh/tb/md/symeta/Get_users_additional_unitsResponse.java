/**
 * Get_users_additional_unitsResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.msh.tb.md.symeta;

public class Get_users_additional_unitsResponse  implements java.io.Serializable {
    private org.msh.tb.md.symeta.Get_users_additional_unitsResponseGet_users_additional_unitsResult get_users_additional_unitsResult;

    public Get_users_additional_unitsResponse() {
    }

    public Get_users_additional_unitsResponse(
           org.msh.tb.md.symeta.Get_users_additional_unitsResponseGet_users_additional_unitsResult get_users_additional_unitsResult) {
           this.get_users_additional_unitsResult = get_users_additional_unitsResult;
    }


    /**
     * Gets the get_users_additional_unitsResult value for this Get_users_additional_unitsResponse.
     * 
     * @return get_users_additional_unitsResult
     */
    public org.msh.tb.md.symeta.Get_users_additional_unitsResponseGet_users_additional_unitsResult getGet_users_additional_unitsResult() {
        return get_users_additional_unitsResult;
    }


    /**
     * Sets the get_users_additional_unitsResult value for this Get_users_additional_unitsResponse.
     * 
     * @param get_users_additional_unitsResult
     */
    public void setGet_users_additional_unitsResult(org.msh.tb.md.symeta.Get_users_additional_unitsResponseGet_users_additional_unitsResult get_users_additional_unitsResult) {
        this.get_users_additional_unitsResult = get_users_additional_unitsResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Get_users_additional_unitsResponse)) return false;
        Get_users_additional_unitsResponse other = (Get_users_additional_unitsResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.get_users_additional_unitsResult==null && other.getGet_users_additional_unitsResult()==null) || 
             (this.get_users_additional_unitsResult!=null &&
              this.get_users_additional_unitsResult.equals(other.getGet_users_additional_unitsResult())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getGet_users_additional_unitsResult() != null) {
            _hashCode += getGet_users_additional_unitsResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Get_users_additional_unitsResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">get_users_additional_unitsResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("get_users_additional_unitsResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "get_users_additional_unitsResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">>get_users_additional_unitsResponse>get_users_additional_unitsResult"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
