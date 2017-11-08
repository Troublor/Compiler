import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

/**
 * Created by troub on 2017/10/10.
 */
public class Parser extends Lang{

    /**
     * LL1分析表
     */
    private HashMap<String, HashMap<String, Integer>> analyseTable;

    /**
     * 分析栈
     */
    private Stack<Word> analyseStack;

    private Stack<Word> SEM;

    private ArrayList<QT> QT;

    /**
     * 输入表达式
     */
    private ArrayList<Word> input;
    private int i = 0;

    public Parser(){
        super();
        analyseTable = new HashMap<>();
        analyseStack = new Stack<>();
        SEM = new Stack<>();
        QT = new ArrayList<QT>();

        grammar.addVN(new HashSet<>(Arrays.asList(new String[]{"E", "T", "E1", "T1", "F"})));
        grammar.addVT(new HashSet<>(Arrays.asList(new String[]{"+", "-", "*", "/", "i", "(", ")"})));

        grammar.setStartVN("E");

        grammar.addDeriver(new Deriver("E", new String[]{"T", "E1"}));
        grammar.addDeriver(new Deriver("E1", new String[]{"+", "T", "_AC_GEQ_+_", "E1"}));
        grammar.addDeriver(new Deriver("E1", new String[]{"-", "T", "_AC_GEQ_-_", "E1"}));
        grammar.addDeriver(new Deriver("E1", new String[]{}));
        grammar.addDeriver(new Deriver("T", new String[]{"F", "T1"}));
        grammar.addDeriver(new Deriver("T1", new String[]{"*", "F", "_AC_GEQ_*_", "T1"}));
        grammar.addDeriver(new Deriver("T1", new String[]{"/", "F", "_AC_GEQ_/_", "T1"}));
        grammar.addDeriver(new Deriver("T1", new String[]{}));
        grammar.addDeriver(new Deriver("F", new String[]{"i", "_AC_PUSH_i_"}));
        grammar.addDeriver(new Deriver("F", new String[]{"(", "E", ")"}));

        /*grammar.addVN(new HashSet<String>(Arrays.asList(new String[]{"S", "A", "B"})));
        grammar.addVT(new HashSet<String>(Arrays.asList(new String[]{"a", "b", "c", "d"})));

        grammar.setStartVN("S");

        grammar.addDeriver(new Deriver("S", new String[]{"a", "A", "S", "b"}));
        grammar.addDeriver(new Deriver("S", new String[]{"B", "d"}));
        grammar.addDeriver(new Deriver("A", new String[]{"c", "S"}));
        grammar.addDeriver(new Deriver("A", new String[]{}));
        grammar.addDeriver(new Deriver("B", new String[]{"b", "B"}));
        grammar.addDeriver(new Deriver("B", new String[]{"d"}));*/

        try {
            grammar.generateLL1AnalyzeTable();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        analyseTable = grammar.getLL1AnalyseTable();

        HashMap<String, Integer> row1 = new HashMap<String, Integer>();
        row1.put("i", 1);
        row1.put("(", 1);
        HashMap<String, Integer> row2 = new HashMap<String, Integer>();
        row2.put("+", 2);
        row2.put("-", 3);
        row2.put(")", 4);
        row2.put("#", 4);
        HashMap<String, Integer> row3 = new HashMap<String, Integer>();
        row3.put("i", 5);
        row3.put("(", 5);
        HashMap<String, Integer> row4 = new HashMap<String, Integer>();
        row4.put("+", 8);
        row4.put("-", 8);
        row4.put("*", 6);
        row4.put("/", 7);
        row4.put(")", 8);
        row4.put("#", 8);
        HashMap<String, Integer> row5 = new HashMap<String, Integer>();
        row5.put("i", 9);
        row5.put("(", 10);

        analyseTable.put("E", row1);
        analyseTable.put("E1", row2);
        analyseTable.put("T", row3);
        analyseTable.put("T1", row4);
        analyseTable.put("F", row5);
    }

    public Parser(ArrayList<Word> i){
        this();
        input = i;
    }



    /**
     * 设置输入串
     * @param i 输入串
     */
    public void input(ArrayList<Word> i){
        input = i;
    }

    public boolean LL1AnalyzeToken(){

        for (int i = 0; i < input.size(); i++){
            Word s = input.get(i);
            if (s.token.equals("<00>") || s.token.equals("<03>")){
                s.token = "i";
                input.remove(i);
                input.add(i, s);
            }else if (s.token.equals("<24>")){
                s.token = "(";
                input.remove(i);
                input.add(i, s);
            }else if (s.token.equals("<25>")){
                s.token = ")";
                input.remove(i);
                input.add(i, s);
            }else if (s.token.equals("<16>")){
                s.token = "+";
                input.remove(i);
                input.add(i, s);
            }else if (s.token.equals("<17>")){
                s.token = "-";
                input.remove(i);
                input.add(i, s);
            }else if (s.token.equals("<18>")){
                s.token = "*";
                input.remove(i);
                input.add(i, s);
            }else if (s.token.equals("<19>")){
                s.token = "/";
                input.remove(i);
                input.add(i, s);
            }
        }
        return LL1Analyze();
    }

    public boolean LL1Analyze(){
        SEM.clear();
        QT.clear();
        analyseStack.clear();
        Word w;
        i=0;
        analyseStack.push(new Word("#", "#"));
        analyseStack.push(new Word("", "E"));
        input.add(new Word("#", "#"));
        w = next();
        while (!analyseStack.peek().token.equals("#")){
            if (w == null){
                return false;
            }
            Word x = analyseStack.pop();
            if (x.token.equals("PUSHi")) {
                SEM.push(new Word(x.word, "i"));
                continue;
            }
            if (x.token.equals("GEQ+")){
                Word n1, n2;
                n2 = SEM.pop();
                n1 = SEM.pop();
                QT newone = new QT("+", n1.word, n2.word);
                QT.add(newone);
                SEM.push(new Word(newone.getResult(), "i"));
                continue;
            } else if (x.token.equals("GEQ-")) {
                Word n1, n2;
                n2 = SEM.pop();
                n1 = SEM.pop();
                QT newone = new QT("-", n1.word, n2.word);
                QT.add(newone);
                SEM.push(new Word(newone.getResult(), "i"));
                continue;
            } else if (x.token.equals("GEQ*")) {
                Word n1, n2;
                n2 = SEM.pop();
                n1 = SEM.pop();
                QT newone = new QT("*", n1.word, n2.word);
                QT.add(newone);
                SEM.push(new Word(newone.getResult(), "i"));
                continue;
            } else if (x.token.equals("GEQ/")) {
                Word n1, n2;
                n2 = SEM.pop();
                n1 = SEM.pop();
                QT newone = new QT("/", n1.word, n2.word);
                QT.add(newone);
                SEM.push(new Word(newone.getResult(), "i"));
                continue;
            }
            if (x.token.equals("i") || x.token.equals("(") || x.token.equals(")") || x.token.equals("+") || x.token.equals("-") || x.token.equals("*") || x.token.equals("/")){

                //如果是终结符
                if (w.token.equals(x.token)){
                    if (x.token.equals("i")&&analyseStack.peek().token.equals("PUSHi")) {
                        analyseStack.peek().word = w.word;
                    }
                    w = next();
                    continue;
                }else {
                    return false;
                }
            }else{
                HashMap<String, Integer> row = analyseTable.get(x.token);
                if (row == null){
                    return false;
                }
                Integer index = row.get(w.token);
                if (index == null){
                    return false;
                }
                analyseStack.addAll(reverse(grammar.getDeriver(index).getDestination()));
            }
        }
        if (w == null || !w.token.equals("#")){
            return false;
        }
        analyseStack.clear();
        return true;
    }

    /**
     * 获取下一个单词
     * @return 单词
     */
    private Word next(){
        if (i >= input.size()){
            return null;
        }
        return input.get(i++);
    }

    /**
     * 翻转arrayList顺序
     * @param arrayList　源arrayList
     * @return 翻转后的arrayList
     */
    private ArrayList<Word> reverse(ArrayList<String> arrayList){
        ArrayList<Word> n = new ArrayList<>();
        for (int i = arrayList.size() - 1; i >= 0; i--){
            n.add(new Word("", arrayList.get(i)));
        }
        return n;
    }






    //递归下降方法
    private Word recursiveDescentWord;

    public boolean recursiveDescentAnalyzeToken(){
        for (int i = 0; i < input.size(); i++){
            Word s = input.get(i);
            if (s.token.equals("<00>") || s.token.equals("<03>")){
                s.token = "i";
                input.remove(i);
                input.add(i, s);
            }else if (s.token.equals("<24>")){
                s.token = "(";
                input.remove(i);
                input.add(i, s);
            }else if (s.token.equals("<25>")){
                s.token = ")";
                input.remove(i);
                input.add(i, s);
            }else if (s.token.equals("<16>")){
                s.token = "+";
                input.remove(i);
                input.add(i, s);
            }else if (s.token.equals("<17>")){
                s.token = "-";
                input.remove(i);
                input.add(i, s);
            }else if (s.token.equals("<18>")){
                s.token = "*";
                input.remove(i);
                input.add(i, s);
            }else if (s.token.equals("<19>")){
                s.token = "/";
                input.remove(i);
                input.add(i, s);
            }
        }
        return recursiveDescentAnalyze();
    }

    public boolean recursiveDescentAnalyze(){
        SEM.clear();
        QT.clear();
        i = 0;
        input.add(new Word("#", "#"));
        recursiveDescentWord = next();
        if (!E()){
            return false;
        }else {
            if (recursiveDescentWord.token.equals("#")){
                return true;
            }else {
                return false;
            }
        }
    }

    private boolean E(){
        if (!T()){
            return false;
        }else {
            return E1();
        }
    }

    private boolean E1(){
        if (recursiveDescentWord.token.equals("+") || recursiveDescentWord.token.equals("-")) {
            String temp = recursiveDescentWord.token;
            recursiveDescentWord = next();
            if (!T()) {
                return false;
            } else {
                if (temp.equals("+")) {
                    Word n1, n2;
                    n2 = SEM.pop();
                    n1 = SEM.pop();
                    QT newone = new QT("+", n1.word, n2.word);
                    QT.add(newone);
                    SEM.push(new Word(newone.getResult(), "i"));
                } else if (temp.equals("-")) {
                    Word n1, n2;
                    n2 = SEM.pop();
                    n1 = SEM.pop();
                    QT newone = new QT("-", n1.word, n2.word);
                    QT.add(newone);
                    SEM.push(new Word(newone.getResult(), "i"));
                }
                return E1();
            }
        } else {
            return true;
        }
    }

    private boolean T(){
        if (!F()){
            return false;
        }else {
            return T1();
        }
    }

    private boolean T1(){
        if (recursiveDescentWord.token.equals("*") || recursiveDescentWord.token.equals("/")) {
            String temp = recursiveDescentWord.token;
            recursiveDescentWord = next();
            if (!F()) {
                return false;
            } else {
                if (temp.equals("*")) {
                    Word n1, n2;
                    n2 = SEM.pop();
                    n1 = SEM.pop();
                    QT newone = new QT("*", n1.word, n2.word);
                    QT.add(newone);
                    SEM.push(new Word(newone.getResult(), "i"));
                } else if (temp.equals("/")) {
                    Word n1, n2;
                    n2 = SEM.pop();
                    n1 = SEM.pop();
                    QT newone = new QT("/", n1.word, n2.word);
                    QT.add(newone);
                    SEM.push(new Word(newone.getResult(), "i"));
                }
                return T1();
            }
        } else {
            return true;
        }
    }

    private boolean F(){
        if (recursiveDescentWord.token.equals("i")){
            SEM.push(recursiveDescentWord);
            recursiveDescentWord = next();
            return true;
        }else if (recursiveDescentWord.token.equals("(")){
            recursiveDescentWord = next();
            if (!E()){
                return false;
            }else {
                if (recursiveDescentWord.token.equals(")")){
                    recursiveDescentWord = next();
                    return true;
                }else {
                    return false;
                }
            }
        }else {
            return false;
        }
    }

    public void printQT(){
        for (QT qt : QT) {
            System.out.println(qt.toString());
        }
    }
}
