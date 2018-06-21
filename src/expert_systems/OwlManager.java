package expert_systems;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class OwlManager {
	
	private static String s;
	private static String s_Source = "http://www.semanticweb.org/koger/ontologies/2018/5/traffic";
	private static String s_SourcePrefix = s_Source + "#";

	static OntModel OpenOWL(String fileName) 
	{
		OntModel owlModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);

		InputStream in = FileManager.get().open(fileName);

		if (in == null) 
		{
			throw new IllegalArgumentException("cannot open ontology");
		}

		OntModel model = (OntModel) owlModel.read(in, "");

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
	
    static ResultSet ExecSparQl(String Query, String fileName)
    {
        Query query = QueryFactory.create(Query);

        QueryExecution qe = QueryExecutionFactory.create(query, OpenOWL(fileName));
        ResultSet results = qe.execSelect();

        return results;
    }
	
    static String ExecSparQlString(String Query, String fileName){
		try 
		{
			Query query = QueryFactory.create(Query);

			QueryExecution qe = QueryExecutionFactory.create(query, OpenOWL(fileName));

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
