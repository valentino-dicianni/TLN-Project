package utils;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.WordnetStemmer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WordNet {

    private String dictPath;
    private IDictionary dict;
    private static final int DEPTH_MAX = 20;


    public WordNet(String path){
        this.dictPath = path;
        try {
            setUpDictionary();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpDictionary() throws IOException {
        URL url = new URL("file", null, dictPath);
        this.dict = new Dictionary(url);
    }

//    RETURNS an empty list if there is no synsetid in dictionary
    private ArrayList<Synset> getsynsetIDs(String w){
        ArrayList<Synset> sysIds = new ArrayList<>();
        try {
            ArrayList<String> stems = findStems(w);
            this.dict.open();
            for(String s : stems) {
                IIndexWord idxWord = dict.getIndexWord(s, POS.NOUN);
                if (idxWord != null) {
                    List<IWordID> wordIDs = idxWord.getWordIDs();
                    for (IWordID iw : wordIDs) {
                        IWord word = dict.getWord(iw);
                        sysIds.add((Synset) word.getSynset());
                    }
                }
            }
        } catch (Exception ignored) {}
        return sysIds;
    }

    private ArrayList<String> findStems(String s){
        List<String> res = new ArrayList<>();
        try {
            this.dict.open();
            WordnetStemmer stemmer = new WordnetStemmer(this.dict);
            res = stemmer.findStems(s, POS.NOUN);
        } catch (Exception ignored) {}
        return (ArrayList<String>) res;
    }

    private List<SynsetID> getHypernyms(Synset synset){

        List<SynsetID> hypernym_list = new ArrayList<>();
        List<ISynsetID> noHyper = synset.getRelatedSynsets(Pointer.HYPERNYM);

        if(!noHyper.isEmpty()) {
            SynsetID first_hp = (SynsetID) synset.getRelatedSynsets(Pointer.HYPERNYM).get(0);

            boolean flag = false;
            while (!flag) {
                hypernym_list.add(first_hp);
                List<ISynsetID> hypernymTmp = dict.getSynset(first_hp).getRelatedSynsets(Pointer.HYPERNYM);
                if (hypernymTmp.isEmpty()) {
                    flag = true;
                } else {
                    first_hp = (SynsetID) hypernymTmp.get(0);
                }
            }
        }
        return hypernym_list;
    }

    public double calculateSimilarity(String w1, String w2, Measure measure){
        double result = 0.0;

        ArrayList<Synset> synset1 = getsynsetIDs(w1);
        ArrayList<Synset> synset2 = getsynsetIDs(w2);
        ArrayList<Double> results = new ArrayList<>();

        // if there is no match in dictionary we don't consider the couple and return 0
        if(synset1.isEmpty() || synset2.isEmpty()){return result;}

        for (Synset c1 : synset1) {
            for (Synset c2 : synset2){

                switch (measure){
                    case WU_PALMER:
                        result = wuAndPalmerSim(c1, c2);
                        break;

                    case SHORTEST_PATH:
                        result = shortestPath(c1, c2);
                        break;

                    case LEAKCOCK_CHODOROW:
                        result = leakcockChodorow(c1, c2);
                        break;
                }
                results.add(result);
            }
        }
        return Collections.max(results);
    }

    private double wuAndPalmerSim(Synset c1, Synset c2){
        double res = 0.0;

        List<SynsetID> h1 = getHypernyms(c1);
        List<SynsetID> h2 = getHypernyms(c2);
        List<SynsetID> hlcs = new ArrayList<>();

        SynsetID lcs = getLCS(h1, h2);

        if(lcs != null){
            Synset lcsSynset = (Synset) dict.getSynset(lcs);
            hlcs= getHypernyms(lcsSynset);
        }

        if(h1.size() != 0 || h2.size() != 0)
            res = (double)(2 * hlcs.size()) / (h1.size() + h2.size());

        return normalizeResults(res, 0.0, 1.0);
    }

    private double shortestPath(Synset c1, Synset c2){
        double res = (double)((2 * DEPTH_MAX) - getLen(c1,c2));
        return normalizeResults(res,0.0, (double)2*DEPTH_MAX);
    }

    private double leakcockChodorow(Synset c1, Synset c2){
        double res;

        //To avoid log(0) we add 1
        if(getLen(c1,c2) == 0){
            res = -1*Math.log((double)(getLen(c1,c2)+1)/(2*DEPTH_MAX+1));
        }
        else {
            res = -1*Math.log((double)(getLen(c1,c2))/(2*DEPTH_MAX));

        }
        return normalizeResults(res, 0.0, Math.log(2*DEPTH_MAX+1));
    }

    // NORMALIZE data in a range between 0 and 10
    private double normalizeResults(double value, double min, double max){
        return (value-min)/(max-min) * 10;
    }

    private SynsetID getLCS(List<SynsetID> h1, List<SynsetID> h2){
        SynsetID common = null;
        for(SynsetID s : h1){
            if(h2.contains(s)){
                common = s;
                break;
            }
        }
        return common;
    }

    private int getLen(Synset s1, Synset s2){
        int len = 0;
        List<SynsetID> h1 = getHypernyms(s1);
        List<SynsetID> h2 = getHypernyms(s2);

        SynsetID lcs = getLCS(h1, h2);
        if(lcs != null)
            len = (h1.subList(0,h1.indexOf(lcs))).size() + (h2.subList(0,h2.indexOf(lcs))).size() + 1;
        return len;
    }

    public ArrayList<Sense> getSenses(String word){
        ArrayList<Synset> synsets = getsynsetIDs(word);
        ArrayList<Sense> res = new ArrayList<>();

        for (Synset s :synsets) {
            res.add(new Sense(s, s.getGloss()
                    .replaceAll("\"", "")
                    .replaceAll(";","")
                    .replaceAll(",", "")));
        }
        return res;
    }





}
