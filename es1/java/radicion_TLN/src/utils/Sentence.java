package utils;


public class Sentence {
    private String[] sentence;
    private String targetWord;


    public Sentence(String[] sentence, String targetWord) {
        this.sentence = sentence;
        this.targetWord = targetWord;
    }

    public String[] getContext() {
        return (sentence[0]+targetWord+sentence[2]).split(" ");
    }

    public String[] getSentence() {
        return sentence;
    }

    public String getTargetWord() {
        return targetWord;
    }

    @Override
    public String toString() {
        return sentence[0]+"-"+targetWord+"-"+sentence[2];
    }
}
