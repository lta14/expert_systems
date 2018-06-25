package expert_systems;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
//import com.hp.hpl.jena.rdf.model.ResIterator;
//import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import org.json.*;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import org.json.JSONObject;
import org.apache.commons.io.FileUtils;

public class Main {
	
	private static String s_FileName = "traffic.owl";
	private static String s_StandardInput = ".\\inputs\\";
	private static String s_DefaultInput = s_StandardInput + "input.json";

	
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
			inputData = readJSON(args);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			return;
		}
		
		createSigns(inputData, model);
		createTrafficLights(inputData, model);
		createLanes(inputData, model);
		createCars(inputData, model);
		
		//check output
		String legalSituation = "LegalSingleSituation";
		String illegalSituation = "IllegalSingleSituation";
		
		ArrayList<Individual> legals = new ArrayList<Individual>();
		ArrayList<Individual> illegals = new ArrayList<Individual>();
		
		
		ExtendedIterator<Individual> iter = model.listIndividuals();
		while (iter.hasNext()) {
			Individual individual = iter.next();
			OntClass ontClass = individual.getOntClass();

			String uri = ontClass.getURI();
			
			System.out.println(individual.getLocalName());
			
			if(uri.contains(legalSituation))
			{
				legals.add(individual);
				//System.out.println(individual.getLocalName());
			}
			else if(uri.contains(illegalSituation))
			{
				illegals.add(individual);
				//System.out.println(individual.getLocalName());
			}
		}
		
		
		//Print Report:
		
		System.out.println("We have legal situations on the following lanes:" );
		
		for (Individual individual: legals) {
		    System.out.println(individual.getLocalName());
		}
		
		System.out.println("------------------");
		System.out.println("We have illegal situations on the following lanes:" );
		
		for (Individual individual: illegals) {
		    System.out.println(individual.getLocalName());
		}
		
		
		String fileName = "created.owl";
		FileWriter out;
		try {
			out = new FileWriter( fileName );
		    model.write( out);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		
		//SPARQL
		/*
		String rdf = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
		String rdfs = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>";
		String owl = "PREFIX owl: <http://www.semanticweb.org/koger/ontologies/2018/5/traffic>";
		
		String newQuery = rdf + rdfs + owl + "SELECT ?individual WHERE { ?individual rdf:type owl:LegalSingleSituation }";
		OwlManager.ExecSparQlString(newQuery, model);
		*/

		
		/*
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
		*/
	}
	
	private static void createSigns(JSONObject inputData, OntModel model)
	{
		OntClass signClass = OwlManager.GetClass(model, "Sign");
		
		JSONArray arr = inputData.getJSONArray("signs");
		for (int i = 0; i < arr.length(); i++)
		{
			JSONObject object = arr.getJSONObject(i);
			
		    String id = getString(object, "id");
		    String hasType = getString(object, "hasType");
					    
			Individual sign = OwlManager.CreateIndividual(model, signClass, id);
			
			DatatypeProperty hasTypeProperty = OwlManager.GetDataTypeProperty(model, "hasType");
			sign.addProperty(hasTypeProperty,hasType);
		}
	}
	
	private static void createTrafficLights(JSONObject inputData, OntModel model)
	{
		OntClass lightClass = OwlManager.GetClass(model, "TrafficLight");
		
		JSONArray arr = inputData.getJSONArray("lights");
		for (int i = 0; i < arr.length(); i++)
		{
			JSONObject object = arr.getJSONObject(i);
			
		    String id = getString(object, "id");
		    String hasColor = getString(object, "hasColor");
				    
			Individual light = OwlManager.CreateIndividual(model, lightClass, id);
			
			DatatypeProperty hasColorProperty = OwlManager.GetDataTypeProperty(model, "hasColor");
			light.addProperty(hasColorProperty,hasColor);
		}
	}
	
	private static void createLanes(JSONObject inputData, OntModel model)
	{
		OntClass laneClass = OwlManager.GetClass(model, "Lane");
		
		JSONArray arr = inputData.getJSONArray("lanes");
		for (int i = 0; i < arr.length(); i++)
		{
			JSONObject object = arr.getJSONObject(i);
			
		    String id = getString(object, "id");
		    String hasSpeedLimit = getString(object, "hasSpeedLimit");
		    String hasTrafficLight = getString(object, "hasTrafficLight");
		    String hasSign = getString(object, "hasSign");
					    
			Individual lane = OwlManager.CreateIndividual(model, laneClass, id);
			
			Individual no = OwlManager.GetIndividual(model, "No");
			
			// speedLimit
			if(hasSpeedLimit.isEmpty())
			{
				if(no != null)
				{
					ObjectProperty hasNoSpeedLimitProperty = OwlManager.GetObjectProperty(model, "hasNoSpeedLimit");
					lane.addProperty(hasNoSpeedLimitProperty, no);
				}
			}
			else
			{
				DatatypeProperty hasSpeedLimitProperty = OwlManager.GetDataTypeProperty(model, "hasSpeedLimit");		
				Literal speedLimit = model.createTypedLiteral(hasSpeedLimit, XSDDatatype.XSDinteger);
				Statement speedLimitStatement = model.createStatement(lane, hasSpeedLimitProperty, speedLimit);
				model.add(speedLimitStatement);
			}
			
			//find traffic light
			Individual light = OwlManager.GetIndividual(model, hasTrafficLight);
			
			if(light != null)
			{
				ObjectProperty hasTrafficLightProperty = OwlManager.GetObjectProperty(model, "hasTrafficLight");
				lane.addProperty(hasTrafficLightProperty, light);
			}
			else
			{
				if(no != null)
				{
					ObjectProperty hasNoTrafficLightProperty = OwlManager.GetObjectProperty(model, "hasNoTrafficLight");
					lane.addProperty(hasNoTrafficLightProperty, no);	
				}
			}
					
			//find sign
			Individual sign = OwlManager.GetIndividual(model, hasSign);
			
			if(sign != null)
			{
				ObjectProperty hasSignProperty = OwlManager.GetObjectProperty(model, "hasSign");
				lane.addProperty(hasSignProperty, sign);
			}
			else
			{
				if(no != null)
				{
					ObjectProperty hasNoSignProperty = OwlManager.GetObjectProperty(model, "hasNoSign");
					lane.addProperty(hasNoSignProperty, no);	
				}
			}
		}
	}
	
	private static void createCars(JSONObject inputData, OntModel model)
	{
		OntClass carClass = OwlManager.GetClass(model, "Car");
		
		JSONArray arr = inputData.getJSONArray("cars");
		for (int i = 0; i < arr.length(); i++)
		{
			JSONObject object = arr.getJSONObject(i);
			
		    String id = getString(object, "id");
		    String hasSpeed = getString(object, "hasSpeed");
		    String hasAction = getString(object, "hasAction");
		    String isOnLane = getString(object, "isOnLane");
		    
			Individual car = OwlManager.CreateIndividual(model, carClass, id);
						
			DatatypeProperty hasSpeedProperty = OwlManager.GetDataTypeProperty(model, "hasSpeed");		
			Literal speed = model.createTypedLiteral(hasSpeed, XSDDatatype.XSDinteger);
			Statement speedStatement = model.createStatement(car, hasSpeedProperty, speed);
			model.add(speedStatement);

			DatatypeProperty hasActionProperty = OwlManager.GetDataTypeProperty(model, "hasAction");
			car.addProperty(hasActionProperty, hasAction);
			
			//find lane
			Individual lane = OwlManager.GetIndividual(model, isOnLane);
			
			if(lane != null)
			{
				ObjectProperty isOnLanePropertry = OwlManager.GetObjectProperty(model, "isOnLane");
				car.addProperty(isOnLanePropertry, lane);
			}
		}
	}
	
	private static String getString(JSONObject object, String name)
	{
		String result = "";
		try
		{
			result = object.getString(name);	
		}
		catch(Exception e)
		{
			result = "";
		}
		
		return result;
	}
	
	private static JSONObject readJSON(String args[]) throws Exception {
		
		String fileName = s_DefaultInput;
		
		if(args.length == 1)
		{
			fileName = s_StandardInput + args[0];
		}
		
		File file = new File(fileName);
		
		if(!file.exists())
		{
			fileName = s_DefaultInput;
			System.out.println("File: " + fileName + " not found. Using default file: " + s_DefaultInput);
		}
		
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
