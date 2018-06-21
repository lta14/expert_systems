package expert_systems;

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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class OpenOwl {
	
	static String s;

	static OntModel OpenConnectOWL() {

		OntModel owlModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);

		java.io.InputStream in = FileManager.get()
				.open("C:\\Users\\tanne\\Desktop\\expert_systems\\expert_systems\\simpsons.owl");

		if (in == null) {
			throw new IllegalArgumentException("cannot open ontology");
		}

		OntModel model = (OntModel) owlModel.read(in, "");

		return model;
	}
	
    static ResultSet ExecSparQl(String Query)
    {
        Query query = QueryFactory.create(Query);

        QueryExecution qe = QueryExecutionFactory.create(query, OpenConnectOWL());
        ResultSet results = qe.execSelect();

        return results;
    }
	
    static String ExecSparQlString(String Query){
		try 
		{
			Query query = QueryFactory.create(Query);

			QueryExecution qe = QueryExecutionFactory.create(query, OpenConnectOWL());

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
