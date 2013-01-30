package de.fhg.fokus.ngni.openpe.pem1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oma.xml.fokus.pem1_output_template.EnforcementAction;
import oma.xml.fokus.pem1_output_template.EnforcementActionOperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolicyEvaluationTransformation {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	Map<String, Map<String, Transformation>> indexed = new HashMap<String, Map<String, Transformation>>();
	Map<String, Transformation> notindexed = new HashMap<String, Transformation>();
	
	public PolicyEvaluationTransformation(EnforcementAction enfAction) {

		List<EnforcementActionOperation> transfActionList = enfAction.getEnforcementActionOperation();	
				
		for (EnforcementActionOperation transformAction : transfActionList) {			
			if(transformAction.getName() != null)
			{
				log.debug("extracted action is {}", transformAction.getName());
				List<String>  parametersList = transformAction.getEnforcementActionOperationParameters();
				Transformation trans = new Transformation(Action.fromValue(transformAction.getName()), parametersList.size() > 1 ? parametersList.get(1) : "");

				log.debug("parsed action is {}", trans.getAction().value);

				String firstParameter = parametersList.get(0);	
				
				if (firstParameter.matches(".*\\[[0-9]\\]")) {
					String index = firstParameter.substring(firstParameter.indexOf('[') + 1, firstParameter.indexOf(']'));
					String param = firstParameter.substring(0, firstParameter.indexOf('['));
					if (!indexed.containsKey(param)) {
						indexed.put(param, new HashMap<String, Transformation>());
						log.debug("added {} to indexed map", param);
					}
					Map<String, Transformation> list = indexed.get(param);
					list.put(index, trans);
				} else {
					notindexed.put(firstParameter, trans);
					log.debug("added {} to not indexed map", firstParameter);
				}
			}
			else
			{
				log.warn("syntax error in transformation");
			}			
		}
	}

	public Action getAction(String parameter) {
		if (notindexed.containsKey(parameter)) {
			return notindexed.get(parameter).getAction();
		}
		return Action.NONE;
	}

	public String getValue(String parameter) {
		if (notindexed.containsKey(parameter)) {
			return notindexed.get(parameter).getValue();
		}
		return null;
	}
	
	public Action getAction(String parameter, int index) {
		if (indexed.containsKey(parameter)) {
			Map<String, Transformation> transformations = indexed.get(parameter);
			if (transformations.containsKey(String.valueOf(index))) {
				return transformations.get(String.valueOf(index)).getAction();
			}
		} else {
			log.debug("{} is not in indexed map", parameter);
		}
		return getAction(parameter);
	}

	public String getValue(String parameter, int index) {
		if (indexed.containsKey(parameter)) {
			Map<String, Transformation> transformations = indexed.get(parameter);
			if (transformations.containsKey(String.valueOf(index))) {
				return transformations.get(String.valueOf(index)).getValue();
			}
		} else {
			log.debug("{} is not in indexed map", parameter);
		}
		return getValue(parameter);
	}
	
	private class Transformation {
		private final Action action;
		private final String value;
		
		public Transformation(Action action, String value) {
			this.action = action;
			this.value = value;
		}
		
		public Action getAction() {
			return action;
		}
		
		public String getValue() {
			return value;
		}
	}
	
	public enum Action {
		SET("set"),
		DEL("del"),
		NONE("none");
		
		private final String value;

		Action(String v) {
			value = v;
		}
		
	    public static Action fromValue(String v) {
	        for (Action c: Action.values()) {
	            if (c.value.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }
		
	}
}
