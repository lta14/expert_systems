package expert_systems;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.util.FileManager;

public class OwlManager {
	
	private static String s;
	private static String s_Source = "http://www.semanticweb.org/koger/ontologies/2018/5/traffic";
	private static String s_SourcePrefix = s_Source + "#";

	static OntModel OpenOWL(String fileName) 
	{
		OntModel owlModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF);
		com.hp.hpl.jena.ontology.OntModelSpec spec= PelletReasonerFactory.THE_SPEC;
		
		OntModel owlModel2 = ModelFactory.createOntologyModel(spec, owlModel);

		InputStream in = FileManager.get().open(fileName);

		if (in == null) 
		{
			throw new IllegalArgumentException("cannot open ontology");
		}

		OntModel model = (OntModel) owlModel2.read(in, "");

		return model;
	}
	
	static Individual CreateIndividual(OntModel model, OntClass ontClass, String name)
	{
		Individual individual = model.createIndividual(s_SourcePrefix+name, ontClass);
		return individual;
	}
	
	static Individual GetIndividual(OntModel model, String name)
	{
		Individual individual = model.getIndividual(s_SourcePrefix+name);
		return individual;
	}
	
	static OntClass GetClass(OntModel model, String name)
	{
		OntClass ontClass = model.getOntClass(s_SourcePrefix+name);
		return ontClass;
	}
	
	static ObjectProperty GetObjectProperty(OntModel model, String name)
	{
		ObjectProperty property = model.getObjectProperty(s_SourcePrefix+name);
		return property;	
	}
	
	static DatatypeProperty GetDataTypeProperty(OntModel model, String name)
	{
		DatatypeProperty property = model.getDatatypeProperty(s_SourcePrefix+name);
		return property;	
	}
	
    static ResultSet ExecSparQl(String Query, OntModel model)
    {
        Query query = QueryFactory.create(Query);

        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();

        return results;
    }
	
    static String ExecSparQlString(String Query, OntModel model){
		try 
		{
			Query query = QueryFactory.create(Query);

			QueryExecution qe = QueryExecutionFactory.create(query, model);

			ResultSet results = qe.execSelect();

			if (results.hasNext()) 
			{
				ByteArrayOutputStream go = new ByteArrayOutputStream();
				ResultSetFormatter.out((OutputStream) go, results, query);
				s = new String(go.toByteArray(), "UTF-8");
			}
			// not okay
			else 
			{
				s = "error";
			}
		} 
		catch (UnsupportedEncodingException ex) 
		{

		}
		
		return s; // return jena.query.ResultSet as string
	}
   
}
