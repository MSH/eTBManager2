/**
 * Get_institutionsResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.msh.tb.md.symeta;

public class Get_institutionsResponse  implements java.io.Serializable {
    private org.msh.tb.md.symeta.Get_institutionsResponseGet_institutionsResult get_institutionsResult;

    public Get_institutionsResponse() {
    }

    public Get_institutionsResponse(
           org.msh.tb.md.symeta.Get_institutionsResponseGet_institutionsResult get_institutionsResult) {
           this.get_institutionsResult = get_institutionsResult;
    }


    /**
     * Gets the get_institutionsResult value for this Get_institutionsResponse.
     * 
     * @return get_institutionsResult
     */
    public org.msh.tb.md.symeta.Get_institutionsResponseGet_institutionsResult getGet_institutionsResult() {
        return get_institutionsResult;
    }


    /**
     * Sets the get_institutionsResult value for this Get_institutionsResponse.
     * 
     * @param get_institutionsResult
     */
    public void setGet_institutionsResult(org.msh.tb.md.symeta.Get_institutionsResponseGet_institutionsResult get_institutionsResult) {
        this.get_institutionsResult = get_institutionsResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Get_institutionsResponse)) return false;
        Get_institutionsResponse other = (Get_institutionsResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.get_institutionsResult==null && other.getGet_institutionsResult()==null) || 
             (this.get_institutionsResult!=null &&
              this.get_institutionsResult.equals(other.getGet_institutionsResult())));
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
        if (getGet_institutionsResult() != null) {
            _hashCode += getGet_institutionsResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Get_institutionsResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">get_institutionsResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("get_institutionsResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "get_institutionsResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">>get_institutionsResponse>get_institutionsResult"));
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
