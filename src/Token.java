/**
 * Created by troub on 2017/10/23.
 */
public class Token{


    private String word;

    //文法符号
    private String label;

    Token(String w, String l) {
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
}
