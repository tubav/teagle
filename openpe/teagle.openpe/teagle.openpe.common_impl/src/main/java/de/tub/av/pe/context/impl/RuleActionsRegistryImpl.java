package de.tub.av.pe.context.impl;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.av.pe.context.ConfigurationException;
import de.tub.av.pe.rulecontext.ActionReference;
import de.tub.av.pe.rulecontext.RuleActionsRegistry;

public class RuleActionsRegistryImpl implements RuleActionsRegistry {
	private Logger log = LoggerFactory.getLogger(RuleActionsRegistryImpl.class);

	private Map<String, ActionReference> actionTypes = new HashMap<String, ActionReference>();

	@Override
	public synchronized void config(Properties props)
			throws ConfigurationException {
		String ruleActions = (String) props.get("openpe.rule.actions");
		if (ruleActions == null) {
			log.debug("Just actions which explicit register will be taken into consideration");
			return;
		}
		String[] actionImpls = ruleActions.split(";");
		if (actionImpls.length > 0) {
			for (String actionImpl : actionImpls) {
				String[] s = actionImpl.split(",");
				if (s.length == 2) {
					try {
						Class<?> loadedaction = Class.forName(s[1]);
						Constructor<?> constructor = loadedaction
								.getConstructor(new Class<?>[0]);
						actionTypes.put(s[0],
								(ActionReference) constructor.newInstance());
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}

			}
		}
	}

	@Override
	public synchronized String getImplementationClass(String actionname) {
		ActionReference ref = actionTypes.get(actionname);
		if (ref != null)
			return ref.getClass().getName();
		return null;
	}

	@Override
	public synchronized ActionReference getActionReference(String actionname) {
		return actionTypes.get(actionname);
	}

	@Override
	public synchronized void addActionReference(String actionname, Class<?> clzz) {
		try {
			Class<?> loadedaction = Class.forName(clzz.getName());
			Constructor<?> constructor = loadedaction
					.getConstructor(new Class<?>[0]);
			this.actionTypes.put(actionname,
					(ActionReference) constructor.newInstance());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public synchronized void addActionReference(String actionname,
			ActionReference actionReference) {
		this.actionTypes.put(actionname, actionReference);
	}

	@Override
	public synchronized void removeActionReference(String actionname) {
		this.actionTypes.remove(actionname);
	}
}
