package jab;

import org.apache.lucene.index.Term;
import org.apache.solr.parser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SolrQueryParser;
import org.apache.solr.search.SyntaxError;

/**
 *  A Query Plugin for Annotated Queries
 *  
 *  needs a query or a query annotated (QUERY_ANNOTATED_PARAM) with a value:  type<QUERY_TOKEN_TYPE_SEPARATOR>token 
 *  
 */

public class ExParserPlugin extends QParserPlugin {
		
	@Override
	public void init(@SuppressWarnings("rawtypes") NamedList args) {
	}

	public QParser createParser(String qstr, SolrParams localParams,
			SolrParams params, SolrQueryRequest req) {
		return new LkParser(qstr, localParams, params, req);
	}

	
}

class LkParser extends QParser {
	/// Solr Field to store token and token annotation
	public static final String FIELD_TOKEN_ANNOT = "bio_annot";
	/// Solr Parameter to pass an annotated query
	private static final String QUERY_ANNOTATED_PARAM = "q.annot";
	private static final String QUERY_TOKEN_TYPE_SEPARATOR = "-";
	private static final String I_BIOPREFIX = "i-";
	private static final String B_BIOPREFIX = "b-";
	String sortStr;
	SolrQueryParser lparser;

	public LkParser(String qstr, SolrParams localParams, SolrParams params,
			SolrQueryRequest req) {
		super(qstr, localParams, params, req);
	}

	public Query parse() throws SyntaxError  {
		String qstr = getString();

		String annotationQuery = getParam(QUERY_ANNOTATED_PARAM);
		
		String defaultField = getParam(CommonParams.DF);
		if (defaultField == null) {
			defaultField = getReq().getSchema().getDefaultSearchFieldName();
		}
		
		lparser = new SolrQueryParser(this, defaultField);

		// these could either be checked & set here, or in the SolrQueryParser
		// constructor
		String opParam = getParam(QueryParsing.OP);
		if (opParam != null) {
			lparser
					.setDefaultOperator("AND".equals(opParam) ? QueryParser.Operator.AND
							: QueryParser.Operator.OR);
		} 

		
		if (annotationQuery != null) {
			BooleanClause.Occur op = BooleanClause.Occur.MUST;
			Query aQuery = getAnnotationQuery(annotationQuery);
			if(qstr!=null) {
				Query query = lparser.parse(qstr);
				BooleanQuery combined = new BooleanQuery();
				combined.add(query, op);
				combined.add(aQuery, op);
				return combined;
			}
			return aQuery;
		} else {
			Query query = lparser.parse(qstr);
			return query;
		}
	  
	}

	public Query getAnnotationQuery(String annotation) {
		
		int typePos = annotation.indexOf(QUERY_TOKEN_TYPE_SEPARATOR);
		String type = annotation.substring(0, typePos).toLowerCase();
		String entity = annotation.substring(typePos + 1).toLowerCase();

		String tokens[] = entity.split(" ");

		PhraseQuery q = new PhraseQuery();

		q.add(new Term(FIELD_TOKEN_ANNOT, tokens[0]));
		q.add(new Term(FIELD_TOKEN_ANNOT, B_BIOPREFIX + type), 0);

		for (int i = 1; i < tokens.length; i++) {
			q.add(new Term(FIELD_TOKEN_ANNOT, tokens[i]), i);
			q.add(new Term(FIELD_TOKEN_ANNOT, I_BIOPREFIX + type), i);
		}

		return q;
	}

	public String[] getDefaultHighlightFields() {
		return new String[] { lparser.getDefaultField() };
	}
}
