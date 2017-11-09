/**
 * Created by troub on 2017/9/12.
 */
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 语言类
 * 语言的一些定义
 */
public class Lang {
    /**
     * 关键字集合
     */
    protected ArrayList<String> keyWordTable;

    protected ArrayList<String> delimiterTable;

    /**
     * 语法规则
     */
    protected Grammar grammar;

    /**
     * 默认的构造方法
     */
    public Lang(){
        keyWordTable=new ArrayList<>();
        delimiterTable=new ArrayList<>();
        grammar = new Grammar();

        //添加关键字
        keyWordTable.add("int");
        keyWordTable.add("main");
        keyWordTable.add("void");
        keyWordTable.add("double");
        keyWordTable.add("char");
        keyWordTable.add("struct");
        keyWordTable.add("func");
        keyWordTable.add("return");
        keyWordTable.add("if");
        keyWordTable.add("else");
        keyWordTable.add("while");
        keyWordTable.add("var");

        //添加界符
        String[] ds = {">=", "<=", "==", "=", ">", "<", "+", "-", "*", "/", "{", "}", ",", ";", "(",
            ")", "\"", "\'", "[", "]", "//", "&&", "||", "+=", "-=", "%", "!=", "."};
        delimiterTable.addAll(Arrays.asList(ds));
    }

    /**
     * 给出关键字集合的构造方法
     * @param k 关键字集合
     */
    public Lang(ArrayList<String> k){
        keyWordTable=new ArrayList<>(k);
    }

    /**
     * 关键字集合访问器
     * @return ArrayList 关键字集合
     */
    public ArrayList<String> getKeyWords(){
        return keyWordTable;
    }

    /**
     * 判断是否是关键字
     * @param k 待判断的单词
     * @return boolean
     */
    public boolean isKeyWord(String k){
        return keyWordTable.contains(k);
    }

    /**
     * 查找关键字
     * @param k 待查找的
     * @return int 关键字的序号
     */
    public int getKeyWord(String k){
        return keyWordTable.indexOf(k);
    }

    /**
     * keyWordTable访问器
     * @return ArrayList<String>
     */
    public ArrayList<String> getKeyWordTable(){
        return keyWordTable;
    }

    /**
     * 判断是否是界符
     * @param d 待判断的
     * @return boolean
     */
    public boolean isDelimiter(String d){
        return delimiterTable.contains(d);
    }

    /**
     * 查找界符
     * @param d 待查找的
     * @return int 界符的序号
     */
    public int getDelimiter(String d){
        return keyWordTable.indexOf(d);
    }

    /**
     * delimiterTable访问器
     * @return ArrayList<String>
     */
    public ArrayList<String> getDelimiterTable(){
        return delimiterTable;
    }

    /**
     * 添加关键字——单个
     * @param k 关键字
     * @return boolean
     * 不管是否重复均不返回false
     * 有重复则不添加
     */
    public boolean addKeyWord(String k){
        if (keyWordTable.contains(k)){
            return true;
        }
        keyWordTable.add(k);
        return true;
    }

    /**
     * 添加关键字——多个
     * @param ks 关键字集合
     * @return boolean
     * 不管是否重复均不返回false
     * 有重复则不添加
     */
    public boolean addKeyWords(String[] ks){
        for (String k:ks){
            if (keyWordTable.contains(k)){
                return true;
            }
            keyWordTable.add(k);
        }
        return true;
    }

    /**
     * 添加界符——单个
     * @param d 界符
     * @return boolean
     * 不管是否重复均不返回false
     * 有重复则不添加
     */
    public boolean addDelimiter(String d){
        if (delimiterTable.contains(d)){
            return true;
        }
        delimiterTable.add(d);
        return true;
    }

    /**
     * 添加界符——多个
     * @param ds 界符集合
     * @return boolean
     * 不管是否重复均不返回false
     * 有重复则不添加
     */
    public boolean addDelimiters(String[] ds){
        for (String d:ds){
            if (delimiterTable.contains(d)){
                return true;
            }
            delimiterTable.add(d);
        }
        return true;
    }
}
