package expert_systems;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.Individual;

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
		
		OntClass laneClass = OwlManager.GetClass(model, "Lane");
		Individual lane = OwlManager.CreateIndividual(model, laneClass, "lane2");
		
		OntClass signClass = OwlManager.GetClass(model, "Sign");
		Individual sign = OwlManager.CreateIndividual(model, signClass, "sign3");
		
		ObjectProperty hasSign = OwlManager.GetObjectProperty(model, "hasSign");
		
		Individual myStreet = OwlManager.CreateIndividual(model, laneClass, "MyStreet");
		
		ObjectProperty isLegal = OwlManager.GetObjectProperty(model, "isLegal");
		Statement staement = myStreet.getProperty(isLegal);
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
