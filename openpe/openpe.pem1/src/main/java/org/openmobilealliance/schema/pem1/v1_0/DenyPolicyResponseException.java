
package org.openmobilealliance.schema.pem1.v1_0;

import javax.xml.ws.WebFault;
import org.openmobilealliance.wsdl.pem1.v1_0.faults.DenyPolicyResponseExceptionType;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.3-b02-
 * Generated source version: 2.1
 * 
 */
@WebFault(name = "denyPolicyResponseException", targetNamespace = "http://www.openmobilealliance.org/wsdl/PEM1/v1_0/faults")
public class DenyPolicyResponseException
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private DenyPolicyResponseExceptionType faultInfo;

    /**
     * 
     * @param message
     * @param faultInfo
     */
    public DenyPolicyResponseException(String message, DenyPolicyResponseExceptionType faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public DenyPolicyResponseException(String message, DenyPolicyResponseExceptionType faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: org.openmobilealliance.wsdl.pem1.v1_0.faults.DenyPolicyResponseExceptionType
     */
    public DenyPolicyResponseExceptionType getFaultInfo() {
        return faultInfo;
    }

}