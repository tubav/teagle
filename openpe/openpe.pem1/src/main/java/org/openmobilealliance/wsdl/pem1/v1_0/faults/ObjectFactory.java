
package org.openmobilealliance.wsdl.pem1.v1_0.faults;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.openmobilealliance.wsdl.pem1.v1_0.faults package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ServiceException_QNAME = new QName("http://www.openmobilealliance.org/wsdl/PEM1/v1_0/faults", "serviceException");
    private final static QName _DenyPolicyResponseException_QNAME = new QName("http://www.openmobilealliance.org/wsdl/PEM1/v1_0/faults", "denyPolicyResponseException");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.openmobilealliance.wsdl.pem1.v1_0.faults
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DenyPolicyResponseExceptionType }
     * 
     */
    public DenyPolicyResponseExceptionType createDenyPolicyResponseExceptionType() {
        return new DenyPolicyResponseExceptionType();
    }

    /**
     * Create an instance of {@link ServiceExceptionType }
     * 
     */
    public ServiceExceptionType createServiceExceptionType() {
        return new ServiceExceptionType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.openmobilealliance.org/wsdl/PEM1/v1_0/faults", name = "serviceException")
    public JAXBElement<ServiceExceptionType> createServiceException(ServiceExceptionType value) {
        return new JAXBElement<ServiceExceptionType>(_ServiceException_QNAME, ServiceExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DenyPolicyResponseExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.openmobilealliance.org/wsdl/PEM1/v1_0/faults", name = "denyPolicyResponseException")
    public JAXBElement<DenyPolicyResponseExceptionType> createDenyPolicyResponseException(DenyPolicyResponseExceptionType value) {
        return new JAXBElement<DenyPolicyResponseExceptionType>(_DenyPolicyResponseException_QNAME, DenyPolicyResponseExceptionType.class, null, value);
    }

}
