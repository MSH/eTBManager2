package org.msh.tb.md.symeta;

public class MDR_TBMISSSoapProxy implements MDR_TBMISSSoap {
  private String _endpoint = null;
  private MDR_TBMISSSoap mDR_TBMISSSoap = null;
  
  public MDR_TBMISSSoapProxy() {
    _initMDR_TBMISSSoapProxy();
  }
  
  public MDR_TBMISSSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initMDR_TBMISSSoapProxy();
  }
  
  private void _initMDR_TBMISSSoapProxy() {
    try {
      mDR_TBMISSSoap = (new MDR_TBMISSLocator()).getMDR_TBMISSSoap();
      if (mDR_TBMISSSoap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)mDR_TBMISSSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)mDR_TBMISSSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (mDR_TBMISSSoap != null)
      ((javax.xml.rpc.Stub)mDR_TBMISSSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public MDR_TBMISSSoap getMDR_TBMISSSoap() {
    if (mDR_TBMISSSoap == null)
      _initMDR_TBMISSSoapProxy();
    return mDR_TBMISSSoap;
  }
  
  public java.lang.String helloWorld() throws java.rmi.RemoteException{
    if (mDR_TBMISSSoap == null)
      _initMDR_TBMISSSoapProxy();
    return mDR_TBMISSSoap.helloWorld();
  }
  
  public Get_deleted_recordResponseGet_deleted_recordResult get_deleted_record(java.lang.String table_name, java.util.Calendar last_update) throws java.rmi.RemoteException{
    if (mDR_TBMISSSoap == null)
      _initMDR_TBMISSSoapProxy();
    return mDR_TBMISSSoap.get_deleted_record(table_name, last_update);
  }
  
  public Get_institutionsResponseGet_institutionsResult get_institutions(java.util.Calendar last_update) throws java.rmi.RemoteException{
    if (mDR_TBMISSSoap == null)
      _initMDR_TBMISSSoapProxy();
    return mDR_TBMISSSoap.get_institutions(last_update);
  }
  
  public Get_regionsResponseGet_regionsResult get_regions(java.util.Calendar last_update) throws java.rmi.RemoteException{
    if (mDR_TBMISSSoap == null)
      _initMDR_TBMISSSoapProxy();
    return mDR_TBMISSSoap.get_regions(last_update);
  }
  
  public Get_usersResponseGet_usersResult get_users(java.util.Calendar last_update) throws java.rmi.RemoteException{
    if (mDR_TBMISSSoap == null)
      _initMDR_TBMISSSoapProxy();
    return mDR_TBMISSSoap.get_users(last_update);
  }
  
  public Get_users_additional_unitsResponseGet_users_additional_unitsResult get_users_additional_units(java.util.Calendar last_update) throws java.rmi.RemoteException{
    if (mDR_TBMISSSoap == null)
      _initMDR_TBMISSSoapProxy();
    return mDR_TBMISSSoap.get_users_additional_units(last_update);
  }
  
  public Get_casesResponseGet_casesResult get_cases(java.util.Calendar last_update) throws java.rmi.RemoteException{
    if (mDR_TBMISSSoap == null)
      _initMDR_TBMISSSoapProxy();
    return mDR_TBMISSSoap.get_cases(last_update);
  }
  
  public Get_cases_unitsResponseGet_cases_unitsResult get_cases_units(java.util.Calendar last_update) throws java.rmi.RemoteException{
    if (mDR_TBMISSSoap == null)
      _initMDR_TBMISSSoapProxy();
    return mDR_TBMISSSoap.get_cases_units(last_update);
  }
  
  public Get_tables_updatedResponseGet_tables_updatedResult get_tables_updated(java.lang.String table_name, java.util.Calendar last_update) throws java.rmi.RemoteException{
    if (mDR_TBMISSSoap == null)
      _initMDR_TBMISSSoapProxy();
    return mDR_TBMISSSoap.get_tables_updated(table_name, last_update);
  }
  
  public Get_cases_classicResponseGet_cases_classicResult get_cases_classic(java.util.Calendar last_update) throws java.rmi.RemoteException{
    if (mDR_TBMISSSoap == null)
      _initMDR_TBMISSSoapProxy();
    return mDR_TBMISSSoap.get_cases_classic(last_update);
  }
  
  public Get_localitiesResponseGet_localitiesResult get_localities(java.util.Calendar last_update) throws java.rmi.RemoteException{
    if (mDR_TBMISSSoap == null)
      _initMDR_TBMISSSoapProxy();
    return mDR_TBMISSSoap.get_localities(last_update);
  }
  
  
}