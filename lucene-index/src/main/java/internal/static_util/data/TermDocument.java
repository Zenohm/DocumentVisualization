package internal.static_util.data;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Created by chris on 1/4/16.
 */
public class TermDocument{
    private final int docId;
    private final String term;
    private TermDocument(int docId, String term){
        this.docId = docId;
        this.term = term;
    }
    public static TermDocument of(int docId, String term){
        return new TermDocument(docId, term);
    }
    @Override
    public int hashCode(){
        HashFunction hf = Hashing.sha1();
        return hf.newHasher().putInt(docId).putString(term, Charsets.UTF_8).hash().asInt();
    }
}
