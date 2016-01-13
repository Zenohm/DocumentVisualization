package internal.static_util;

import internal.analyzers.search.StemmingTermAnalyzer;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

/**
 * Class designed to stem a single term.
 * Created by chris on 1/5/16.
 */
public class TermStemmer {
    private TermStemmer(){} // Static Class

    public static String stemTerm(String term) throws ParseException {
        Analyzer stemmer = new StemmingTermAnalyzer();
        QueryParser parser = new QueryParser("test", stemmer);
        return StringUtils.replaceOnce(parser.parse(term).toString(), "test:", "");
    }
}
