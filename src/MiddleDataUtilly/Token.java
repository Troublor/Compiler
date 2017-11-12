package MiddleDataUtilly;

/**
 * Created by troub on 2017/10/23.
 */
public class Token{
    //单词本身
    private String word;

    //文法符号
    private String label;

    public Token(String w, String l) {
        word = w;
        label = l;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public String toString() {
        return label + ": " + word;
    }

    public class WordType {
        public static final int NONE = 0;
        public static final int IDENTITY = 1;
        public static final int NUMBER = 2;
        public static final int DELIMITER = 3;
        public static final int STRING = 4;
        public static final int CHARACTER = 5;
        public static final int ANNOTATION = 6;
    }

}
