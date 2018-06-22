package expert_systems;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.Individual;
import org.json.*;
import java.io.File;
import org.json.JSONObject;
import org.apache.commons.io.FileUtils;

public class Main {
	
	private static String s_FileName = "traffic.owl";

	
	public static void main(String [] args)
	{
		org.apache.log4j.BasicConfigurator.configure();
		
		OntModel model = LoadModel();
		
		if(model == null)
		{
			return;
		}
		
		// read json file
		
		JSONObject inputData;

		try
		{
			inputData = readJSON();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			return;
		}
		
		
		String pageName = inputData.getJSONObject("pageInfo").getString("pageName");

		JSONArray arr = inputData.getJSONArray("posts");
		for (int i = 0; i < arr.length(); i++)
		{
		    String post_id = arr.getJSONObject(i).getString("post_id");
		}
		
		
		
		OntClass laneClass = OwlManager.GetClass(model, "Lane");
		Individual lane = OwlManager.CreateIndividual(model, laneClass, "lane2");
		
		OntClass signClass = OwlManager.GetClass(model, "Sign");
		Individual sign = OwlManager.CreateIndividual(model, signClass, "sign3");
		
		ObjectProperty hasSign = OwlManager.GetObjectProperty(model, "hasSign");
		
		Individual myStreet = OwlManager.CreateIndividual(model, laneClass, "MyStreet");
		
		ObjectProperty isLegal = OwlManager.GetObjectProperty(model, "isLegal");
		lane.addProperty(hasSign, sign);
		
		if(lane.hasProperty(hasSign))
		{
			System.out.println("it worked");
		}
		
		ResIterator iter = model.listSubjectsWithProperty(isLegal);
		while (iter.hasNext()) {
		    Resource r = iter.nextResource();
		    System.out.println(r.getLocalName());
		}
	}
	
	private static JSONObject readJSON() throws Exception {
	    File file = new File(".\\inputs\\input.json");
	    
	    String content = FileUtils.readFileToString(file, "utf-8");
	    
	    // Convert JSON string to JSONObject
	    JSONObject jsonObject = new JSONObject(content);    
	    
	    return jsonObject;
	}
	
	private static OntModel LoadModel()
	{
		OntModel model = null;
		
		try
		{
			model = OwlManager.OpenOWL(s_FileName);
		}
		catch(IllegalArgumentException e)
		{
			System.out.println(e.getMessage());
		}
		
		return model;
	}
}
