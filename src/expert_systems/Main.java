package expert_systems;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
//import com.hp.hpl.jena.rdf.model.ResIterator;
//import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import org.json.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONObject;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;
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
		boolean createdCrossing = createCrossing(inputData, model);
		createCars(inputData, model, createdCrossing);
		
		//reload model to apply swrl rules		
		model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, model);
		model.prepare();
		
		//Print Report:
		
		System.out.println("The current situation is:" );
		
		String legalSituation = "LegalSituation";
		OntClass legalClass = OwlManager.GetClass(model, legalSituation);
		
		boolean isLegal = false;
		Iterator<?> iterator = legalClass.listInstances();
		
		while( iterator.hasNext() ) {

		    isLegal = true;
		    break;
		}
		
		if(isLegal)
		{		 
			System.out.println("---LEGAL---");
		}
		else
		{
			System.out.println("---ILLEGAL---");
		}
		
		String fileName = "created.owl";
		System.out.println("Writing output file: " + fileName);


		FileWriter out;
		try {
			out = new FileWriter( fileName );
		    model.write( out, "RDF/XML");
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
		    String hasSignType = getString(object, "hasSignType");
					    
			Individual sign = OwlManager.CreateIndividual(model, signClass, id);
			
			DatatypeProperty hasSignTypeProperty = OwlManager.GetDataTypeProperty(model, "hasSignType");
			sign.addProperty(hasSignTypeProperty,hasSignType);
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
			
			if(!light.hasProperty(hasColorProperty))
			{
				System.out.println("No property");
			}
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
		    String hasNoCar = getString(object, "hasNoCar");
					    
			Individual lane = OwlManager.CreateIndividual(model, laneClass, id);
			
			Individual no = OwlManager.GetIndividual(model, "No");
			
			// speedLimit
			if(hasSpeedLimit.isEmpty())
			{
				if(no != null)
				{
					ObjectProperty hasNoSpeedLimitProperty = OwlManager.GetObjectProperty(model, "hasNoSpeedLimit");
					lane.addProperty(hasNoSpeedLimitProperty, no);
					
					if(!lane.hasProperty(hasNoSpeedLimitProperty))
					{
						System.out.println("No property");
					}
				}
			}
			else
			{
				DatatypeProperty hasSpeedLimitProperty = OwlManager.GetDataTypeProperty(model, "hasSpeedLimit");		
				Literal speedLimit = model.createTypedLiteral(hasSpeedLimit, XSDDatatype.XSDinteger);
				Statement speedLimitStatement = model.createStatement(lane, hasSpeedLimitProperty, speedLimit);
				model.add(speedLimitStatement);
				
				if(!lane.hasProperty(hasSpeedLimitProperty))
				{
					System.out.println("No property");
				}
				
			}
			
			//find traffic light
			Individual light = OwlManager.GetIndividual(model, hasTrafficLight);
			
			if(light != null)
			{
				ObjectProperty hasTrafficLightProperty = OwlManager.GetObjectProperty(model, "hasTrafficLight");
				lane.addProperty(hasTrafficLightProperty, light);
				
				if(!lane.hasProperty(hasTrafficLightProperty))
				{
					System.out.println("No property");
				}
			}
			else
			{
				if(no != null)
				{
					ObjectProperty hasNoTrafficLightProperty = OwlManager.GetObjectProperty(model, "hasNoTrafficLight");
					lane.addProperty(hasNoTrafficLightProperty, no);	
					
					if(!lane.hasProperty(hasNoTrafficLightProperty))
					{
						System.out.println("No property");
					}
				}
			}
					
			//find sign
			Individual sign = OwlManager.GetIndividual(model, hasSign);
			
			if(sign != null)
			{
				ObjectProperty hasSignProperty = OwlManager.GetObjectProperty(model, "hasSign");
				lane.addProperty(hasSignProperty, sign);
				
				if(!lane.hasProperty(hasSignProperty))
				{
					System.out.println("No property");
				}
				
			}
			else
			{
				if(no != null)
				{
					ObjectProperty hasNoSignProperty = OwlManager.GetObjectProperty(model, "hasNoSign");
					lane.addProperty(hasNoSignProperty, no);	
					
					if(!lane.hasProperty(hasNoSignProperty))
					{
						System.out.println("No property");
					}
				}
			}
			
			//hasNoCar
			if(!hasNoCar.isEmpty())
			{
				ObjectProperty hasNoCarProperty = OwlManager.GetObjectProperty(model, "hasNoCar");
				lane.addProperty(hasNoCarProperty, no);
			}
		}
	}
	
	private static boolean createCrossing(JSONObject inputData, OntModel model)
	{
		boolean created = false;
		
		OntClass crossingClass = OwlManager.GetClass(model, "Crossing");
		
		JSONArray arr = inputData.getJSONArray("crossings");
		for (int i = 0; i < arr.length(); i++)
		{
			JSONObject object = arr.getJSONObject(i);
			
		    String id = getString(object, "id");
		    String hasLeftLane = getString(object, "hasLeftLane");
		    String hasRightLane = getString(object, "hasRightLane");
		    String hasForwardLane = getString(object, "hasForwardLane");
				    
			Individual crossing = OwlManager.CreateIndividual(model, crossingClass, id);
			created = true;
			
			// find and add lanes
			Individual leftLane = OwlManager.GetIndividual(model, hasLeftLane);
			
			if(leftLane != null)
			{
				ObjectProperty hasLaneProperty = OwlManager.GetObjectProperty(model, "hasLeftLane");
				crossing.addProperty(hasLaneProperty, leftLane);
			}
			
			Individual rightLane = OwlManager.GetIndividual(model, hasRightLane);
			
			if(rightLane != null)
			{
				ObjectProperty hasLaneProperty = OwlManager.GetObjectProperty(model, "hasRightLane");
				crossing.addProperty(hasLaneProperty, rightLane);
			}
			
			Individual forwardLane = OwlManager.GetIndividual(model, hasForwardLane);
			
			if(leftLane != null)
			{
				ObjectProperty hasLaneProperty = OwlManager.GetObjectProperty(model, "hasForwardLane");
				crossing.addProperty(hasLaneProperty, forwardLane);
			}
		}
		
		return created;
	}
	
	private static void createCars(JSONObject inputData, OntModel model, boolean createdCrossing)
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
		    String isAtCrossing = getString(object, "isAtCrossing");
		    
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
				ObjectProperty isOnLaneProperty = OwlManager.GetObjectProperty(model, "isOnLane");
				car.addProperty(isOnLaneProperty, lane);
			}
			
			
			//find crossing
			if(!isAtCrossing.isEmpty())
			{
				Individual crossing = OwlManager.GetIndividual(model, isAtCrossing);
				
				if(crossing != null)
				{
					ObjectProperty isAtCrossingProperty = OwlManager.GetObjectProperty(model, "isAtCrossing");
					car.addProperty(isAtCrossingProperty, crossing);
				}
			}
			
			
			// check if no crossing exists
			if(!createdCrossing)
			{
				Individual no = OwlManager.GetIndividual(model, "No");
				if(no != null)
				{
					ObjectProperty isNotAtCrossingProperty = OwlManager.GetObjectProperty(model, "isNotAtCrossing");
					car.addProperty(isNotAtCrossingProperty, no);	
				}
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
			file = new File(s_DefaultInput);
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
