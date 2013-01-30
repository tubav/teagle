package de.fhg.fokus.teaglewebsite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import teagle.vct.model.ConfigParamAtomic;
import teagle.vct.model.ModelManager;

public class ConfigFieldCreator {

	public static List<? extends ConfigParamAtomic> generateConfigFields(HttpServletRequest req) throws Exception{
		List<ConfigParamAtomic> configFields = new ArrayList<ConfigParamAtomic>();
				
		String provider = req.getParameter("provider");
		String resource = req.getParameter("resource");

		if (provider==null || resource==null)
			throw new Exception("missing request parameters");
		
		Map<Integer, Map<String, String>> fields = new HashMap<Integer, Map<String, String>>();
		int num_fields = 0;

		System.out.println("Parameters:");
		// look for all field names in the parameters
		for (Object entry: req.getParameterMap().keySet()) {
			String name = (String)entry;
			String value = req.getParameter(name);

			if (! name.startsWith("field"))
				continue;

			int underscore = name.indexOf("_");
			if (underscore < 0 || underscore < 6)
				throw new Exception("unexpected field name: " + name);

			int id = Integer.valueOf(name.substring(5, underscore));
			Map<String, String> field = fields.containsKey(id) ? fields.get(id) : 
				new HashMap<String, String>();
			fields.put(id, field);

			String suffix = name.substring(underscore + 1);
			field.put(suffix, value);

			if (num_fields < id+1)
				num_fields = id+1;
		}
		
		if (num_fields != fields.size())
			throw new Exception(String.format("num_fields=%d but #fields=%d", 
					num_fields, fields.size()));
		// validate name, fields

		String re_identifier = "[a-zA-Z_][a-zA-Z0-9_]*";

		//if (! provider.matches(re_identifier))
		//	throw new WizardError("Provider ('$provider') must look like a valid identifier");

		if (! resource.matches(re_identifier))
			throw new Exception("Resource name ('$resource') must look like a valid identifier");

		Set<String> names = new HashSet<String>();

		for (Map<String, String> field: fields.values()) {
			String name   = field.get("name");
			String type   = field.get("type");
			String defval = field.get("default");
			//String description = field.get("description");

			if (names.contains(name)) 
				throw new Exception("Duplicate field: " + name);

			names.add(name);

			if (type.equals("string")) {
				// this is weird. why does php escape the _POST values ?
				//$from = Array("\n",  "\r",  "\"",   "\t");
				//$to   = Array("\\n", "\\r", "\\\"", "\\t");
				//$field["default"] = str_replace($from, $to, $default);
			} else if (type.equals("int")) {
				try{
					if (! defval.equals("")) field.put("default", String.valueOf(Integer.valueOf(defval)));
				}
				catch(Exception e){
					throw new Exception("Wrong value for parameter type int");
				}
			} else if (type.equals("double")) {
				try{
					if (! defval.equals("")) field.put("default", String.valueOf(Double.valueOf(defval)));
				}
				catch(Exception e){
					throw new Exception("Wrong value for parameter type double");
				}
			} else if (type.equals("boolean")) {
				try{
					if (! defval.equals("")) field.put("default", defval.equals("true") ? "true" : "false");
				}
				catch(Exception e){
					throw new Exception("Wrong value for parameter type boolean");
				}
			} else if (type.equals("reference")) {
				try{
					if (! defval.equals("")) field.put("default", defval);
				}
				catch(Exception e){
					throw new Exception("Wrong value for parameter type reference");
				}
			} else if (type.equals("reference array")) {
				try{
					if (! defval.equals("")) field.put("default", defval);
				}
				catch(Exception e){
					throw new Exception("Wrong value for parameter type reference array");
				}
			} else {
				throw new Exception("unknown type: " + type);
			}
		}
		
		// generate ConfigFields

		for (int id=0; id<num_fields; id++) {
			Map<String, String> field = fields.get(id);
			if (field==null) throw new Exception("null field #" + id);

			String name   = field.get("name");
			String type   = field.get("type");
			String defval = field.get("default");
			String description = field.get("description");
			
			ConfigParamAtomic cpa = ModelManager.getInstance().createConfigParamAtomic();
			cpa.setCommonName(name);
			cpa.setType(type);
			cpa.setDefaultValue(defval);
			cpa.setDescription(description);
			configFields.add(cpa);
		}
		return configFields;
	}
}
