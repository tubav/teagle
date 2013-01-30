package de.tub.av.pe.model.drools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.drools.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.av.pe.rulecontext.PERule;
import de.tub.av.pe.rulecontext.PERuleset;

public class DrlPERulesetImpl implements PERuleset{

	private String content;
	private String id;
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private Collection<KnowledgePackage> pkgs = null;
	private String packageName;
	private List<String> ruleids; 
	
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public List<PERule> getPERules() {
		return new ArrayList<PERule>();
	}
	

	public void setId(String id)
	{
		this.id = id;
	}
	
	public String getContent()
	{
		return this.content;		
	}
	
	public void setContent(String content)
	{
		this.content = content;
		if(this.content != null)
		{
			System.setProperty("drools.schema.validating", "false");
			KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
	        kbuilder.add( ResourceFactory.newByteArrayResource(this.content.getBytes()),ResourceType.XDRL);
	        if(kbuilder.hasErrors())
	        {
	        	log.error(kbuilder.getErrors().toString());
	        }
	        this.pkgs = kbuilder.getKnowledgePackages();			
	        Iterator<KnowledgePackage> it = this.pkgs.iterator();
	        if(it.hasNext())
	        {
	        	KnowledgePackage kpcg = it.next();	        	
	        	if(this.packageName == null)
	        		this.packageName = kpcg.getName();
	        	Iterator<Rule> rit = kpcg.getRules().iterator();
	        	while(rit.hasNext())
	        	{
	        		Rule rule = rit.next();
	        		this.getRuleIds().add(rule.getName());
	        	}
	        }
		}
	}
	
	public Collection<KnowledgePackage> getKnowledgePackage()
	{
		return this.pkgs;
	}
	
	public String getPackage()
	{
		return this.packageName;
	}
	
	public List<String> getRuleIds()
	{
		if (this.ruleids == null)
		{
			this.ruleids = new ArrayList<String>();
		}
		return this.ruleids;		
	}
}
