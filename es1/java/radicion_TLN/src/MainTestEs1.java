import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import utils.Couple;
import utils.Measure;
import utils.WordNet;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MainTestEs1 {


    private static ArrayList<Double> targetList = new ArrayList<>();

    private static ArrayList<Couple> getWordsFromFile(String path){
        ArrayList<Couple> tupleArray = new ArrayList<>();
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(path));
            while ((line = br.readLine()) != null) {
                String[] words = line.split(",");
                if(!words[2].startsWith("Human")){
                    Couple t = new Couple(words[0],words[1], Double.parseDouble(words[2]));
                    tupleArray.add(t);
                    targetList.add(Double.parseDouble(words[2]));
                }
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
        return tupleArray;


    }

    private static double pearsonCoefficient (ArrayList<Double> x, ArrayList<Double> y){
        Covariance c = new Covariance();
        StandardDeviation sd = new StandardDeviation();

        double[] dblX = x.stream()
                .mapToDouble(Double::doubleValue)
                .toArray();

        double[] dblY = y.stream()
                .mapToDouble(Double::doubleValue)
                .toArray();

        return c.covariance(dblX,dblY)/(sd.evaluate(dblX)*sd.evaluate(dblY));

    }

    private static double spearmanCoefficient (ArrayList<Double> x, ArrayList<Double> y){
        Covariance c = new Covariance();
        StandardDeviation sd = new StandardDeviation();

        HashMap<Double, Integer> rankX = getRank(x);
        HashMap<Double, Integer> rankY = getRank(y);

        double[] rgX = new double[x.size()];
        double[] rgY = new double[y.size()];

        for (int i = 0; i<x.size(); i++) {
            rgX[i] = rankX.get(x.get(i));
            rgY[i] = rankY.get(y.get(i));
        }
        return c.covariance(rgX,rgY)/(sd.evaluate(rgX)*sd.evaluate(rgY));
    }

    private static HashMap<Double, Integer> getRank(ArrayList<Double> list) {
        HashMap<Double, Integer> res = new HashMap<>();

        List<Double> listWithoutDuplicates =  list.stream().distinct().collect(Collectors.toList());

        for(int i = 0; i<listWithoutDuplicates.size(); i++){
            res.put(listWithoutDuplicates.get(i), i+1);
        }
        return res;
    }


    public static void main(String[] args) {
        String simPath = "res/WordSim353.csv";
        WordNet wordNet = new WordNet("/usr/local/Cellar/wordnet/3.1/dict");


        ArrayList<Couple> couples = getWordsFromFile(simPath);
        ArrayList<Double> wuAndPalmer = new ArrayList<>();
        ArrayList<Double> shortestPath = new ArrayList<>();
        ArrayList<Double> leakcockChodorow = new ArrayList<>();

        for(Couple c : couples){
            wuAndPalmer.add(wordNet.calculateSimilarity(c.getS1(),c.getS2(), Measure.WU_PALMER));
            leakcockChodorow.add(wordNet.calculateSimilarity(c.getS1(),c.getS2(), Measure.LEAKCOCK_CHODOROW));
            shortestPath.add(wordNet.calculateSimilarity(c.getS1(),c.getS2(), Measure.SHORTEST_PATH));
        }


        System.out.println("# Pearson Correlation coefficient for Wu & Palmer: "+pearsonCoefficient(targetList,wuAndPalmer));
        System.out.println("# Pearson Correlation coefficient for Leakcock & Chodorow: "+pearsonCoefficient(targetList,leakcockChodorow));
        System.out.println("# Pearson Correlation coefficient for Shortest Path: "+pearsonCoefficient(targetList,shortestPath));
        System.out.println("\n----------------------------------------------------\n");
        System.out.println("# Spearman's rank correlation coefficient for Wu & Palmer: "+spearmanCoefficient(targetList,wuAndPalmer));
        System.out.println("# Spearman's rank correlation coefficient for Leakcock & Chodorow: "+spearmanCoefficient(targetList,leakcockChodorow));
        System.out.println("# Spearman's rank correlation coefficient for Shortest Path: "+spearmanCoefficient(targetList,shortestPath));



    }

}
