import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import javax.script.ScriptEngine;

/**
 * Created by troub on 2017/10/10.
 */
public class Parser extends Lang{
    /**
     * 词法分析器
     */
    private Lexer lexer;

    /**
     * LL1分析表
     */
    private HashMap<String, HashMap<String, Integer>> analyseTable;

    /**
     * 分析栈
     */
    private Stack<Token> analyseStack;

    private Stack<Token> SEM;

    private ArrayList<QT> QT;

    /**
     * 输入表达式,当前正在处理的就是input的最后一个
     * 也就是处理一个读一个，读一个放进来一个
     */
    private ArrayList<Token> input;
    private int i = 0;

    public Parser(){
        super();
        lexer = new Lexer();
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
        grammar.addDeriver(new Deriver("F", new String[]{"i", "_AC_PUSH_"}));
        grammar.addDeriver(new Deriver("F", new String[]{"(", "E", ")"}));

        try {
            grammar.generateLL1AnalyzeTable();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        analyseTable = grammar.getLL1AnalyseTable();
    }

    public Parser(ArrayList<Token> i){
        this();
        input = i;
    }


    /**
     * 设置源代码串
     *
     * @param s 源代码串
     */
    public void setSourceCode(String s) {
        lexer.setSourceCode(s);
    }

    public boolean LL1Analyze() throws InvalidLabelException{
        SEM.clear();
        QT.clear();
        analyseStack.clear();
        Token w;//从input取来的token
        Token x;//从栈里取来的token
        Token y;
        i=0;
        analyseStack.push(new Token("#","#"));
        analyseStack.push(new Token(null,"E"));
        input.add(new Token("#","#"));
        w = next();
        if (w == null) {
            return false;
        }
        while (!analyseStack.isEmpty()) {
            x = analyseStack.pop();
            if (grammar.isVT(x.getLabel())) {
                //如果栈顶是终结符
                if (!w.getLabel().equals(x.getLabel())) {
                    //如果与input不匹配
                    return false;
                }
                //如果匹配则input取下一个
                w = next();
                if (w == null) {
                    return false;
                }
            } else if (grammar.isVN(x.getLabel())) {
                //如果栈顶是非终结符
                //通过分析表获取产生式序号
                Integer index = analyseTable.get(x.getLabel()).get(w.getLabel());
                if (index == null) {
                    //如果没找到对应的产生式
                    return false;
                }
                //产生式逆序压栈
                for (Token t : this.reverse(grammar.getDeriver(index).getDestination())) {
                    analyseStack.push(t);
                }
            } else if (grammar.isAction(x.getLabel())) {
                //如果栈顶是动作
                this.action(x.getLabel());
            }
        }

        analyseStack.clear();
        return true;
    }

    /**
     * 调用动作函数
     * @param a 动作字符串
     * @throws InvalidLabelException 非法符号异常
     */
    private void action(String a) throws InvalidLabelException{
        String[] split = a.split("_");
        if (split.length < 3 || !split[1].equals("AC")) {
            throw new InvalidLabelException("InvalidLabelException: " + a + " is not an action\n");
        }
        //反射调用方法
        try {
            Class cls = Class.forName(this.getClass().getName());
            Method method;
            switch (split.length - 3) {
                case 0:
                    method = cls.getDeclaredMethod(split[2]);
                    method.invoke(this);
                    break;
                case 1:
                    method = cls.getDeclaredMethod(split[2], String.class);
                    method.invoke(this, split[3]);
                    break;
                case 2:
                    method = cls.getDeclaredMethod(split[2], String.class, String.class);
                    method.invoke(this, split[3], split[4]);
                    break;
                case 3:
                    method = cls.getDeclaredMethod(split[2], String.class, String.class,
                        String.class);
                    method.invoke(this, split[3], split[4], split[5]);
                    break;
                case 4:
                    method = cls.getDeclaredMethod(split[2], String.class, String.class,
                        String.class, String.class);
                    method.invoke(this, split[3], split[4], split[5], split[6]);
                    break;
                case 5:
                    method = cls.getDeclaredMethod(split[2], String.class, String.class,
                        String.class, String.class, String.class);
                    method.invoke(this, split[3], split[4], split[5], split[6], split[7]);
                    break;
            }
        } catch (Exception e) {
            throw new InvalidLabelException("InvalidLabelException: " + a + " action failure\n");
        }
    }

    /**
     * 获取下一个单词
     * @return 单词
     */
    private Token next(){
        if (i >= input.size()){
            return null;
        }
        return input.get(i++);
    }

    /**
     * 获取上一个单词
     * @return 单词
     */
    private Token last() {
        if (i == 0) {
            return null;
        }
        return input.get(i - 2);
    }

    /**
     * 翻转arrayList顺序
     * @param arrayList　源arrayList
     * @return 翻转后的arrayList
     */
    private ArrayList<Token> reverse(ArrayList<String> arrayList){
        ArrayList<Token> n = new ArrayList<>();
        for (int i = arrayList.size() - 1; i >= 0; i--){
            n.add(new Token(null,arrayList.get(i)));
        }
        return n;
    }

    //一下都是动作函数的定义

    private void PUSH() {
        SEM.push(this.last());
    }

    private void GEQ(String w) {
        Token n1, n2;
        n1 = SEM.pop();
        n2 = SEM.pop();
        QT qt=new QT(w, n2.getWord(), n1.getWord());
        QT.add(qt);
        SEM.push(new Token(qt.getResult(), "i"));
    }


    public void printQT(){
        for (QT qt : QT) {
            System.out.println(qt.toString());
        }
    }
}
