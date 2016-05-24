package jab;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

/**
 * The Class ToSolrTokenFilter.
 */
public class ExToSolrTokenFilter extends TokenFilter {
	
	  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	  private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	
	protected ExToSolrTokenFilter(TokenStream input) {
		super(input);
	}

	@Override
	public boolean incrementToken() throws IOException  {
		if (!input.incrementToken()) return false;		
		int length = termAtt.length();
		char[] buffer = termAtt.buffer();
		String tokens[] = new String(buffer, 0, length).split(":");
		termAtt.setEmpty();
		termAtt.append(tokens[0]);
		if (tokens.length > 1) {
			posIncrAtt.setPositionIncrement(Integer.parseInt(tokens[1]));
			if (tokens.length > 2) {
				int startOffset =Integer.parseInt(tokens[2]);
				int endOffset = (tokens.length > 3)  ?
						Integer.parseInt(tokens[3]) : offsetAtt.endOffset();						
				offsetAtt.setOffset(startOffset, endOffset);
			}
		}
		return true;
	}
}


