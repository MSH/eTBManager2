/**
 * Get_usersResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.msh.tb.md.symeta;

public class Get_usersResponse  implements java.io.Serializable {
    private org.msh.tb.md.symeta.Get_usersResponseGet_usersResult get_usersResult;

    public Get_usersResponse() {
    }

    public Get_usersResponse(
           org.msh.tb.md.symeta.Get_usersResponseGet_usersResult get_usersResult) {
           this.get_usersResult = get_usersResult;
    }


    /**
     * Gets the get_usersResult value for this Get_usersResponse.
     * 
     * @return get_usersResult
     */
    public org.msh.tb.md.symeta.Get_usersResponseGet_usersResult getGet_usersResult() {
        return get_usersResult;
    }


    /**
     * Sets the get_usersResult value for this Get_usersResponse.
     * 
     * @param get_usersResult
     */
    public void setGet_usersResult(org.msh.tb.md.symeta.Get_usersResponseGet_usersResult get_usersResult) {
        this.get_usersResult = get_usersResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Get_usersResponse)) return false;
        Get_usersResponse other = (Get_usersResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.get_usersResult==null && other.getGet_usersResult()==null) || 
             (this.get_usersResult!=null &&
              this.get_usersResult.equals(other.getGet_usersResult())));
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
        if (getGet_usersResult() != null) {
            _hashCode += getGet_usersResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Get_usersResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">get_usersResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("get_usersResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "get_usersResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">>get_usersResponse>get_usersResult"));
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
