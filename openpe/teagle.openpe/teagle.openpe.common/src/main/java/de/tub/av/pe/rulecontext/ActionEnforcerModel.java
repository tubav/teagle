
package de.tub.av.pe.rulecontext;

public abstract class ActionEnforcerModel {

	private ActionDescriptionModel actionDescription;

	public abstract void executeAction(RuleContext ruleContext, PEAction action)
			throws ActionEnforcerException;

	public void setActionDescription(ActionDescriptionModel actionDescription) {
		this.actionDescription = actionDescription;
	}

	public ActionDescriptionModel getActionDescription() {
		return this.actionDescription;
	}
}
