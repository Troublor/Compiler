import java.util.ArrayList;
import java.util.Arrays;

public class Deriver{
    /**
     * 源符号
     */
    private String source;

    /**
     * 生成式
     */
    private ArrayList<String> destination;

    Deriver(String s, String[] d){
        source = s;
        destination = new ArrayList<>(Arrays.asList(d));
    }

    Deriver(String s, ArrayList<String> d){
        source = s;
        destination = new ArrayList<>(d);
    }

    /**
     * source访问器
     * @return source
     */
    public String getSource(){
        return source;
    }

    /**
     * destination访问器
     * @return destination
     */
    public ArrayList<String> getDestination(){
        return destination;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(source);
        s.append(" -> ");
        for (String ss : destination) {
            s.append(ss).append(" ");
        }
        return s.toString();
    }
}