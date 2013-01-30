
package de.tub.av.pe.soap.intf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import oma.xml.fokus.pem1_input_template.InputTemplateType;
import oma.xml.fokus.pem1_input_template.PolicyInputData;
import oma.xml.fokus.soap_pem1_input_template.FOKUSSOAPInputTemplateType;

import org.openmobilealliance.schema.pem1.v1_0.DenyPolicyResponseException;
import org.openmobilealliance.schema.pem1.v1_0.InformationalException;
import org.openmobilealliance.schema.pem1.v1_0.PermanentErrorException;
import org.openmobilealliance.schema.pem1.v1_0.ProtocolErrorException;
import org.openmobilealliance.schema.pem1.v1_0.TransientErrorException;
import org.openmobilealliance.wsdl.pem1.v1_0.faults.DenyPolicyResponseExceptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.av.pe.context.OpenPEContextManager;
import de.tub.av.pe.pem1.input.utils.PEM1InputUtils;
import de.tub.av.pe.pem1.output.utils.PEM1OutputUtils;
import de.tub.av.pe.pem1.processing.CallableInterfaceThread;
import de.tub.av.pe.pem1.processing.EvaluationConfigAndInit;
import de.tub.av.pe.pem1.processing.EvaluationStatusHandler;

/**
 * Class that provides the implementation of the OpenPE exposed method
 * evaluatePolicy.
 * 
 * @author Irina Boldea
 */
@WebService(targetNamespace = "http://www.openmobilealliance.org/schema/PEM1/v1_0", serviceName = "PolicyEngineService", portName = "PolicyEnginePort", endpointInterface = "org.openmobilealliance.schema.pem1.v1_0.PolicyEnginePortType")
public class PolicyEngineServiceImpl {
	private final Logger log = LoggerFactory.getLogger(getClass());
	@Resource
	private WebServiceContext context;

	/**
	 * Evaluates and enforces associated policies from the domain policy
	 * repository against the input message.
	 * 
	 * @param callbackUrl
	 *            the url to which the message will be sent, input parameter
	 * @param timeStamp
	 *            the time when the evaluation was invoked, input parameter
	 * @param policyData
	 *            as input parameter contains the message that is evaluated, as
	 *            an output contains the evaluation decision load
	 * @param correlator
	 * @param statusCode
	 *            status code of the evaluation result, output parameter
	 * @param statusText
	 *            text concerning the status code, output parameter
	 * @throws DenyPolicyResponseException
	 * @throws InformationalException
	 * @throws PermanentErrorException
	 * @throws ProtocolErrorException
	 * @throws TransientErrorException
	 */
	public void evaluatePolicy(String callbackUrl,
			XMLGregorianCalendar timeStamp, Holder<String> policyData,
			Holder<String> correlator, Holder<Integer> statusCode,
			Holder<String> statusText) throws DenyPolicyResponseException,
			InformationalException, PermanentErrorException,
			ProtocolErrorException, TransientErrorException {

		ServletContext srvContext = (ServletContext) context
				.getMessageContext().get(MessageContext.SERVLET_CONTEXT);

		OpenPEContextManager ctxMng = (OpenPEContextManager) srvContext
				.getAttribute("PE_CONTEXT_MANAGER");

		try {

			EvaluationConfigAndInit configAndInit = new EvaluationConfigAndInit(
					ctxMng.getInstance());

			long startTime = System.currentTimeMillis();

			EvaluationStatusHandler handler = new EvaluationStatusHandler();
			boolean generalEvaluationResult = this
					.doParallelEvaluationOfMessage(policyData.value,
							configAndInit, handler);

			log.debug("The overall evaluation time took >>> {} ms",
					System.currentTimeMillis() - startTime);

			String status = handler.getMessage();

			if (generalEvaluationResult == false) {
				/* Message has been denied...throw exception */
				DenyPolicyResponseExceptionType faultInfo = new DenyPolicyResponseExceptionType();
				faultInfo.setStatusCode(2401);
				faultInfo.setStatusText(status == null ? "Message Denied"
						: status);
				throw new DenyPolicyResponseException(
						status == null ? "Message Denied" : status, faultInfo);
			}

			/* Accept Message or Successful Evaluation status codes */
			statusCode.value = 2101;
			statusText.value = status == null ? "Message Accepted, Evaluation Successfull"
					: status;
			policyData.value = PEM1OutputUtils.toString(configAndInit
					.getResultedPolicyData());
			log.info("Evaluation Response {}", policyData.value);
		} catch (Exception e) {
			log.debug("Evaluation Exception", e);
			if (e instanceof DenyPolicyResponseException)
				throw (DenyPolicyResponseException) e;
			else {
				DenyPolicyResponseExceptionType faultInfo = new DenyPolicyResponseExceptionType();
				faultInfo.setStatusCode(2401);
				faultInfo.setStatusText(e.getMessage());
				throw new DenyPolicyResponseException(
						"Input Template Exception", faultInfo);
			}
		}
	}

	private boolean doParallelEvaluationOfMessage(
			String policyEvaluationInputMessage,
			EvaluationConfigAndInit configAndInit, EvaluationStatusHandler handler) throws JAXBException,
			InterruptedException, ExecutionException {

		boolean evaluationResult = false;

		PolicyInputData policyInputData = PEM1InputUtils
				.toPolicyInputData(policyEvaluationInputMessage);

		List<InputTemplateType> inputTemplateList = policyInputData
				.getPolicyInputTemplate();

		if (inputTemplateList.size() == 1) {
			evaluationResult = configAndInit.evaluateInputTemplate(
					(FOKUSSOAPInputTemplateType) inputTemplateList.get(0), handler);			
		} else {
			handler.setMessage("This is a multiple input templates request. Please check the enforcement data for results for each request");
			ExecutorService threadsPool = Executors.newFixedThreadPool(20);

			ArrayList<FutureTask<Boolean>> futureTaskEvaluateRuleArray = new ArrayList<FutureTask<Boolean>>();
			for (int i = 0; i < inputTemplateList.size(); i++) {
				
				CallableInterfaceThread callIntfThread = new CallableInterfaceThread(
						configAndInit,
						(FOKUSSOAPInputTemplateType) inputTemplateList.get(i),
						i);
				FutureTask<Boolean> futureTask = new FutureTask<Boolean>(
						callIntfThread);
				futureTaskEvaluateRuleArray.add(futureTask);
				threadsPool.execute(futureTask);
			}

			// wait for all the threads to finish

			for (FutureTask<Boolean> futureTask : futureTaskEvaluateRuleArray) {
				evaluationResult |= futureTask.get().booleanValue();
			}
			log.info("All evaluation threads are finished {}",
					futureTaskEvaluateRuleArray.size());

			threadsPool.shutdown();
		}
		return evaluationResult;
	}
}