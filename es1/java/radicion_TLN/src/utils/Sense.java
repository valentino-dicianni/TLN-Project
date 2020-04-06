package utils;

import edu.mit.jwi.item.Synset;

public class Sense {
    private Synset synset;
    private String gloss;

    public Sense(Synset synset, String gloss) {
        this.synset = synset;
        this.gloss = gloss;
    }

    public Synset getSynset() {
        return synset;
    }

    public String getGloss() {
        return gloss;
    }


}
