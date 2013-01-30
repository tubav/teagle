package de.tub.av.pe.editor;

public class EditorDataItem {

	private String name;

	private Object value;

	private ItemHtmlElementEnum htmlElement = ItemHtmlElementEnum.SELECT;

	private String oldInput;

	private EditorDataItem editorItemTriggered = null;

	public EditorDataItem(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public EditorDataItem(String name, Object value,
			ItemHtmlElementEnum htmlElement) {
		this.name = name;
		this.value = value;
		this.htmlElement = htmlElement;
	}

	public EditorDataItem(String name, Object value,
			ItemHtmlElementEnum htmlElement, String oldInput) {
		this.name = name;
		this.value = value;
		this.htmlElement = htmlElement;
		this.oldInput = oldInput;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getName() {
		return this.name;
	}

	public Object getValue() {
		return this.value;
	}

	public void setHtmlElement(ItemHtmlElementEnum el) {
		htmlElement = el;
	}

	public ItemHtmlElementEnum getHtmlElement() {
		return htmlElement;
	}

	public EditorDataItem getTrigeredEditorItem() {
		return this.editorItemTriggered;
	}

	public void setTrigeredEditorItem(EditorDataItem edi) {
		this.editorItemTriggered = edi;
	}

	public String getOldInput() {
		return this.oldInput;
	}

}
