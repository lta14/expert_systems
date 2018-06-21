package expert_systems;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;

public class Main {
	
	private static String s_FileName = "traffic.owl";
	
	public static void main(String [] args)
	{
		org.apache.log4j.BasicConfigurator.configure();
		
		OntModel model = LoadModel();
		
		if(model == null)
		{
			System.out.println("test 1234");
			return;
		}
		

	
		
		
		Individual lisa = model.getIndividual("http://www.semanticweb.org/tanne/ontologies/2018/4/untitled-ontology-2#Lisa");
		
		if(lisa == null)
		{
			System.out.println("sorry it is null");
		}
		else
		{
			System.out.println("it worked");
		}
	}
	
	private static OntModel LoadModel()
	{
		OntModel model = null;
		
		try
		{
			model = OpenOwl.OpenOWL(s_FileName);
		}
		catch(IllegalArgumentException e)
		{
			System.out.println(e.getMessage());
		}
		return model;
	}
}
