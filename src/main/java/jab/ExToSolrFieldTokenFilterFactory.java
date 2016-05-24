package jab;

import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

/**
 * A factory for creating ExToSolrFieldTokenFilter objects.
 */
public class ExToSolrFieldTokenFilterFactory extends TokenFilterFactory {

public ExToSolrFieldTokenFilterFactory(Map<String,String> args) {
	    super(args);
	}
	
	/* (non-Javadoc)
	 * @see org.apache.solr.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
	 */
	public TokenStream create(TokenStream input) {
		return new ExToSolrTokenFilter(input);
	}

}
