package de.tub.av.pe.db;

public class PolicyIdentifier {

	protected String id;
	protected String idType;
	protected String identity;
	protected String scope;
	protected String event;
	protected int priority = -1;

	/**
	 * Gets the value of the id property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the value of the id property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setId(String value) {
		this.id = value;
	}

	/**
	 * Gets the value of the idType property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getIdType() {
		return idType;
	}

	/**
	 * Sets the value of the idType property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setIdType(String value) {
		this.idType = value;
	}

	/**
	 * Gets the value of the identity property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getIdentity() {
		return identity;
	}

	/**
	 * Sets the value of the identity property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setIdentity(String value) {
		this.identity = value;
	}

	/**
	 * Gets the value of the scope property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * Sets the value of the scope property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setScope(String value) {
		this.scope = value;
	}

	/**
	 * Gets the value of the event property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getEvent() {
		return event;
	}

	/**
	 * Sets the value of the event property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setEvent(String value) {
		this.event = value;
	}

	/**
	 * Gets the value of the priority property.
	 * 
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Sets the value of the priority property.
	 * 
	 */
	public void setPriority(int value) {
		this.priority = value;
	}

}
