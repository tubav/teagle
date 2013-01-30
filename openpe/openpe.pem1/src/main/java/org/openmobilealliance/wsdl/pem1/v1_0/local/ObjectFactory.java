
package org.openmobilealliance.wsdl.pem1.v1_0.local;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.openmobilealliance.wsdl.pem1.v1_0.local package. 
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

    private final static QName _EvaluatePolicy_QNAME = new QName("http://www.openmobilealliance.org/wsdl/PEM1/v1_0/local.xsd", "evaluatePolicy");
    private final static QName _EvaluatePolicyResponse_QNAME = new QName("http://www.openmobilealliance.org/wsdl/PEM1/v1_0/local.xsd", "evaluatePolicyResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.openmobilealliance.wsdl.pem1.v1_0.local
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EvaluatePolicyResponseType }
     * 
     */
    public EvaluatePolicyResponseType createEvaluatePolicyResponseType() {
        return new EvaluatePolicyResponseType();
    }

    /**
     * Create an instance of {@link EvaluatePolicyRequestType }
     * 
     */
    public EvaluatePolicyRequestType createEvaluatePolicyRequestType() {
        return new EvaluatePolicyRequestType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EvaluatePolicyRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.openmobilealliance.org/wsdl/PEM1/v1_0/local.xsd", name = "evaluatePolicy")
    public JAXBElement<EvaluatePolicyRequestType> createEvaluatePolicy(EvaluatePolicyRequestType value) {
        return new JAXBElement<EvaluatePolicyRequestType>(_EvaluatePolicy_QNAME, EvaluatePolicyRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EvaluatePolicyResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.openmobilealliance.org/wsdl/PEM1/v1_0/local.xsd", name = "evaluatePolicyResponse")
    public JAXBElement<EvaluatePolicyResponseType> createEvaluatePolicyResponse(EvaluatePolicyResponseType value) {
        return new JAXBElement<EvaluatePolicyResponseType>(_EvaluatePolicyResponse_QNAME, EvaluatePolicyResponseType.class, null, value);
    }

}
