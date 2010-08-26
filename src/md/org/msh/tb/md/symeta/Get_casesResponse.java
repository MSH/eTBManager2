/**
 * Get_casesResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.msh.tb.md.symeta;

public class Get_casesResponse  implements java.io.Serializable {
    private org.msh.tb.md.symeta.Get_casesResponseGet_casesResult get_casesResult;

    public Get_casesResponse() {
    }

    public Get_casesResponse(
           org.msh.tb.md.symeta.Get_casesResponseGet_casesResult get_casesResult) {
           this.get_casesResult = get_casesResult;
    }


    /**
     * Gets the get_casesResult value for this Get_casesResponse.
     * 
     * @return get_casesResult
     */
    public org.msh.tb.md.symeta.Get_casesResponseGet_casesResult getGet_casesResult() {
        return get_casesResult;
    }


    /**
     * Sets the get_casesResult value for this Get_casesResponse.
     * 
     * @param get_casesResult
     */
    public void setGet_casesResult(org.msh.tb.md.symeta.Get_casesResponseGet_casesResult get_casesResult) {
        this.get_casesResult = get_casesResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Get_casesResponse)) return false;
        Get_casesResponse other = (Get_casesResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.get_casesResult==null && other.getGet_casesResult()==null) || 
             (this.get_casesResult!=null &&
              this.get_casesResult.equals(other.getGet_casesResult())));
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
        if (getGet_casesResult() != null) {
            _hashCode += getGet_casesResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Get_casesResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">get_casesResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("get_casesResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "get_casesResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">>get_casesResponse>get_casesResult"));
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
