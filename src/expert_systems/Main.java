package expert_systems;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;

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
		
		lane.addProperty(hasSign, sign);
		
		if(lane.hasProperty(hasSign))
		{
			System.out.println("it worked");
		}
		
		ResIterator iter = model.listSubjectsWithProperty(hasSign);
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
