package test.xcap;

import gen.openpe.identifiers.policy.PolicyIdentifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.drools.compiler.DrlParser;
import org.drools.lang.descr.PackageDescr;
import org.drools.xml.XmlDumper;
import org.junit.Before;
import org.junit.Test;

import de.tu.av.openpe.xcapclient.PoliciesDBManagerWithXcap;
import de.tu.av.openpe.xcapclient.PolicyObject;

public class PEXcapTesT {

	private PoliciesDBManagerWithXcap polMng;

	@Before
	public void setUp()
	{
		List<String> proposedEventsList = new ArrayList<String>();
		proposedEventsList.add("Global");
		proposedEventsList.add("testEvent");		
		polMng = new PoliciesDBManagerWithXcap("127.0.0.1", 8080, "openpe/xcap", proposedEventsList);
	}
	
	@Test
	public void addPolicy() throws Exception
	{		
		PolicyIdentifier pi = new PolicyIdentifier();
		pi.setEvent("testEvent");
		pi.setIdentity("Alice");
		pi.setScope("Originator");
		pi.setIdType("user");
		pi.setPriority(0);
		String policyContent = readPolicyContent("AliceOriginatorTestEvent.drl");
		polMng.addPolicy(pi, policyContent);

		boolean found = false;
		List<PolicyIdentifier> pilst = polMng.getPoliciesIdentifiers("user");
		for (PolicyIdentifier pil:pilst)
		{
			if(pil.getEvent().equals(pi.getEvent()) && pil.getIdentity().equals(pi.getIdentity()) &&
					pil.getIdType().equals(pi.getIdType()) && pil.getScope().equals(pi.getScope())&&
					pil.getPriority() == pi.getPriority())
			{
				found = true;
			}
		}
				
		Assert.assertTrue(found);
		polMng.deletePolicy(pi);
		found = false;
		List<PolicyIdentifier> pilst2 = polMng.getPoliciesIdentifiers("user");
		for (PolicyIdentifier pil:pilst2)
		{
			if(pil.getEvent().equals(pi.getEvent()) && pil.getIdentity().equals(pi.getIdentity()) &&
					pil.getIdType().equals(pi.getIdType()) && pil.getScope().equals(pi.getScope())&&
					pil.getPriority() == pi.getPriority())
			{
				found = true;
			}
		}		
		Assert.assertFalse(found);
	}

	@Test
	public void updatePolicy() throws Exception
	{		
		PolicyIdentifier pi = new PolicyIdentifier();
		pi.setEvent("testEvent");
		pi.setIdentity("Alice");
		pi.setScope("Originator");
		pi.setIdType("user");
		pi.setPriority(0);
		String policyContent = readPolicyContent("AliceOriginatorTestEvent.drl");
		polMng.addPolicy(pi, policyContent);

		boolean found = false;
		List<PolicyIdentifier> pilst = polMng.getPoliciesIdentifiers("user");
		for (PolicyIdentifier pil:pilst)
		{
			if(pil.getEvent().equals(pi.getEvent()) && pil.getIdentity().equals(pi.getIdentity()) &&
					pil.getIdType().equals(pi.getIdType()) && pil.getScope().equals(pi.getScope())&&
					pil.getPriority() == pi.getPriority())
			{
				pi.setId(pil.getId());
				found = true;
			}
		}
				
		Assert.assertTrue(found);
		
		polMng.updatePolicy(pi, policyContent);
				
		polMng.deletePolicy(pi);
		found = false;
		List<PolicyIdentifier> pilst2 = polMng.getPoliciesIdentifiers("user");
		for (PolicyIdentifier pil:pilst2)
		{
			if(pil.getEvent().equals(pi.getEvent()) && pil.getIdentity().equals(pi.getIdentity()) &&
					pil.getIdType().equals(pi.getIdType()) && pil.getScope().equals(pi.getScope())&&
					pil.getPriority() == pi.getPriority())
			{
				found = true;
			}
		}		
		Assert.assertFalse(found);
		
	}
	
	
	private String readPolicyContent(String path) throws Exception {
		InputStream stream = this.getClass().getResourceAsStream(path);
		Reader in = new InputStreamReader(stream, "UTF-8");

		DrlParser parser = new DrlParser();
		PackageDescr pckd = parser.parse(in);
		System.setProperty("drools.schema.validating", "false");
		XmlDumper dumper = new XmlDumper();
		String str = dumper.dump(pckd);

		return str;
	}

}

