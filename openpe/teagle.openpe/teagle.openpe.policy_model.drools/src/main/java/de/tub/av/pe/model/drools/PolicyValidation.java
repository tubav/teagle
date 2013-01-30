package de.tub.av.pe.model.drools;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.av.pe.rulecontext.ValidationErrorHandler;

public class PolicyValidation {

	private static Logger log = LoggerFactory.getLogger(PolicyValidation.class);
	
	public static boolean validate( String policyContent, ValidationErrorHandler validationHandler) {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newByteArrayResource(policyContent.getBytes()),ResourceType.XDRL);

        if(kbuilder.hasErrors())
        {
        	log.error(kbuilder.getErrors().toString());
        	validationHandler.error(kbuilder.getErrors().toString());
        	return false;
        }
		return true;
	}
}
