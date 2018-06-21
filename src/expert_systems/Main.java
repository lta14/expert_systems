package expert_systems;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;

public class Main {
	
	public static void main(String [] args)
	{
		OntModel model = OpenOwl.OpenConnectOWL();
		Individual lisa = model.getIndividual("http://www.semanticweb.org/tanne/ontologies/2018/4/untitled-ontology-2#Lisa");
		
		if(lisa == null)
		{
			System.out.println("sorry it is null");
		}
	}
}
