/**
 * Created by troub on 2017/10/23.
 */
public class Word{
    String word;
    String token;
    Word(String w, String t) {
        word = w;
        token = t;
    }

    public String getToken() {
        return token;
    }

    public String getWord() {
        return word;
    }
}
