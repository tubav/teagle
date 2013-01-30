package de.tub.av.pe.pem1.processing;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oma.xml.fokus.soap_pem1_input_template.FOKUSSOAPInputTemplateType;

public class CallableInterfaceThread implements Callable<Boolean>{
	private static final Logger log = LoggerFactory.getLogger(CallableInterfaceThread.class);
	
	private EvaluationConfigAndInit configAndInit;
	
	private FOKUSSOAPInputTemplateType inputTemplate;
	
	private int id;
	
	public CallableInterfaceThread(EvaluationConfigAndInit configAndInit,  FOKUSSOAPInputTemplateType inputTemplate, int inputUniqueId)
	{
		this.configAndInit = configAndInit;
		this.id = inputUniqueId;
		this.inputTemplate = inputTemplate;
	}
	
	
	public Boolean call() throws Exception {
			
		boolean result = this.configAndInit.evaluateInputTemplate(this.inputTemplate, this.id);
		
		log.debug("Thread for CallableInterface finished");
		return new Boolean(result);
	}

}
