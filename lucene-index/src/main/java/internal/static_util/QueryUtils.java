package internal.static_util;

import common.Constants;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;

import java.util.Arrays;

/**
 * Created by chris on 1/7/16.
 */
public class QueryUtils {
    /**
     * Creates a query that must contain all the words.
     * @param words The list of words that must be contained
     * @return A query to get all the words on the TERM_CONTENTS.
     */
    public static Query mustContainWords(String... words){
        // The words ALL MUST contain the terms.
        BooleanQuery q = new BooleanQuery();
        for (String word : words) {
            PhraseQuery query = new PhraseQuery();
            Arrays.asList(word.split(" ")).stream()
                    .map(term -> new Term(Constants.FIELD_CONTENTS, term))
                    .forEach(query::add);
            query.setSlop(0);
            q.add(query, BooleanClause.Occur.MUST);
        }
        return q;
    }
}
