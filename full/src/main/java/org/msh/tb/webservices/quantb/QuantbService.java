package org.msh.tb.webservices.quantb;

import org.msh.tb.webservices.RemoteActionHandler;
import org.msh.tb.webservices.Response;
import org.msh.tb.webservices.quantb.QuantbData;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * Created by ricardo on 09/12/14.
 */
@WebService(name="quantbService", serviceName="quantbService")
@SOAPBinding(style= SOAPBinding.Style.RPC)
public class QuantbService {

    @WebMethod
    public QuantbData export(@WebParam String sessionId) {
        return (new RemoteActionHandler<QuantbData>(sessionId, null) {
            @Override
            protected Object execute(Object data) {
                return exportData(this);
            }
        }).run();
    }


    protected Object exportData(RemoteActionHandler<QuantbData> handler) {
        QuantbData data = handler.getResponse();
        System.out.println("Testing...");
        return null;
    }
}
