/**
 * MDR_TBMISSLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.msh.tb.md.symeta;

public class MDR_TBMISSLocator extends org.apache.axis.client.Service implements MDR_TBMISS {

    public MDR_TBMISSLocator() {
    }


    public MDR_TBMISSLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public MDR_TBMISSLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for MDR_TBMISSSoap12
    private java.lang.String MDR_TBMISSSoap12_address = "http://monitoring.mednet.md/MDR.TBMIS.Export.WebService/MDR_TBMISS.asmx";

    public java.lang.String getMDR_TBMISSSoap12Address() {
        return MDR_TBMISSSoap12_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String MDR_TBMISSSoap12WSDDServiceName = "MDR_TBMISSSoap12";

    public java.lang.String getMDR_TBMISSSoap12WSDDServiceName() {
        return MDR_TBMISSSoap12WSDDServiceName;
    }

    public void setMDR_TBMISSSoap12WSDDServiceName(java.lang.String name) {
        MDR_TBMISSSoap12WSDDServiceName = name;
    }

    public MDR_TBMISSSoap getMDR_TBMISSSoap12() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(MDR_TBMISSSoap12_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getMDR_TBMISSSoap12(endpoint);
    }

    public MDR_TBMISSSoap getMDR_TBMISSSoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            MDR_TBMISSSoap12Stub _stub = new MDR_TBMISSSoap12Stub(portAddress, this);
            _stub.setPortName(getMDR_TBMISSSoap12WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setMDR_TBMISSSoap12EndpointAddress(java.lang.String address) {
        MDR_TBMISSSoap12_address = address;
    }


    // Use to get a proxy class for MDR_TBMISSSoap
    private java.lang.String MDR_TBMISSSoap_address = "http://monitoring.mednet.md/MDR.TBMIS.Export.WebService/MDR_TBMISS.asmx";

    public java.lang.String getMDR_TBMISSSoapAddress() {
        return MDR_TBMISSSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String MDR_TBMISSSoapWSDDServiceName = "MDR_TBMISSSoap";

    public java.lang.String getMDR_TBMISSSoapWSDDServiceName() {
        return MDR_TBMISSSoapWSDDServiceName;
    }

    public void setMDR_TBMISSSoapWSDDServiceName(java.lang.String name) {
        MDR_TBMISSSoapWSDDServiceName = name;
    }

    public MDR_TBMISSSoap getMDR_TBMISSSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(MDR_TBMISSSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getMDR_TBMISSSoap(endpoint);
    }

    public MDR_TBMISSSoap getMDR_TBMISSSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            MDR_TBMISSSoapStub _stub = new MDR_TBMISSSoapStub(portAddress, this);
            _stub.setPortName(getMDR_TBMISSSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setMDR_TBMISSSoapEndpointAddress(java.lang.String address) {
        MDR_TBMISSSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     * This service has multiple ports for a given interface;
     * the proxy implementation returned may be indeterminate.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (MDR_TBMISSSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                MDR_TBMISSSoap12Stub _stub = new MDR_TBMISSSoap12Stub(new java.net.URL(MDR_TBMISSSoap12_address), this);
                _stub.setPortName(getMDR_TBMISSSoap12WSDDServiceName());
                return _stub;
            }
            if (MDR_TBMISSSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                MDR_TBMISSSoapStub _stub = new MDR_TBMISSSoapStub(new java.net.URL(MDR_TBMISSSoap_address), this);
                _stub.setPortName(getMDR_TBMISSSoapWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("MDR_TBMISSSoap12".equals(inputPortName)) {
            return getMDR_TBMISSSoap12();
        }
        else if ("MDR_TBMISSSoap".equals(inputPortName)) {
            return getMDR_TBMISSSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/", "MDR_TBMISS");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/", "MDR_TBMISSSoap12"));
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/", "MDR_TBMISSSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("MDR_TBMISSSoap12".equals(portName)) {
            setMDR_TBMISSSoap12EndpointAddress(address);
        }
        else 
if ("MDR_TBMISSSoap".equals(portName)) {
            setMDR_TBMISSSoapEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
