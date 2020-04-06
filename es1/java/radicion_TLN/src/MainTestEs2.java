import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.Synset;
import utils.Sense;
import utils.Sentence;
import utils.WordNet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class MainTestEs2 {

    public static ArrayList<Sentence> parseSentences(String path){
        ArrayList<Sentence> res = new ArrayList<>();
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(path));
            while ((line = br.readLine()) != null) {
                if(line.startsWith("#") || line.equals("")){continue;}

                String[] parts = line.substring(1).split("\\*\\*");
                Sentence s = new Sentence(parts,parts[1]);
                res.add(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return res;

    }

    public static Synset leskAlgorithm(ArrayList<Sense> word, Sentence sentence){

        Synset bestSense = word.get(0).getSynset();
        String[] context = sentence.getContext();
        int maxOverlap = 0;

        for(Sense s : word){
            String[] signature = s.getGloss().split(" ");
            int overlap = computeOverlap(signature, context);

            if(overlap > maxOverlap){
                maxOverlap = overlap;
                bestSense = s.getSynset();
            }
        }
        return bestSense;
    }

    private static int computeOverlap(String[] signature, String[] context){
        int overlap = 0;
        for (String aSignature : signature) {
            for (String aContext : context) {
                if (aSignature.equals(aContext)) {
                    overlap++;
                }
            }
        }
        return overlap;
    }

    public static void main(String[] args) {

        WordNet wordNet = new WordNet("/usr/local/Cellar/wordnet/3.1/dict");


        ArrayList<Sentence> sentences = parseSentences("res/sentences.txt");



        for (Sentence snt : sentences) {

            ArrayList<Sense> word = wordNet.getSenses(snt.getTargetWord());
//            for (Sense s : word) {
//                System.out.println(s.getSynset().getID()+" " +s.getGloss());
//
//            }
//            System.out.println("--------");

            if(word.size() > 0) {
                Synset synset = leskAlgorithm(word, snt);

                String synonyms = "[";
                for(IWord w : synset.getWords()) {
                    synonyms = synonyms.concat(w.getLemma()+",");
                }
                synonyms =synonyms.substring(0, synonyms.length()-1)+"]";


                System.out.println("##" + snt.toString());
                System.out.println("Synset: " +synset.getID());
                System.out.println(snt.getSentence()[0]+synonyms+snt.getSentence()[2]);
                System.out.println("\n---------\n");
            }
        }





    }
}
