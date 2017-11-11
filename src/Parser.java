import DFA.LexicalErrorException;
import TranslatorPackage.QT;
import java.lang.reflect.Method;
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

    private ArrayList<TranslatorPackage.QT> QT;

    /**
     * 输入表达式,当前正在处理的就是input的最后一个
     * 也就是处理一个读一个，读一个放进来一个
     */
    private ArrayList<Token> input;
    private int i = 0;

    public Parser(){
        super();
        lexer = new Lexer();
        input = new ArrayList<>();
        analyseTable = new HashMap<>();
        analyseStack = new Stack<>();
        SEM = new Stack<>();
        QT = new ArrayList<QT>();

        grammar.addVN(new HashSet<>(Arrays.asList(
            new String[]{"bool", "join", "BOOL", "equality", "JOIN", "EQUALITY", "rel", "expr",
                "expr1", "EXPR", "term", "TERM", "unary", "值", "值成分", "常量", "S", "P", "S", "F",
                "structure", "function", "L", "形参列表", "形参列表1", "类型", "proc", "形参",
                "形参类型", "类型标识符", "复合语句", "声明语句", "标识符表", "类型", "循环", "条件",
                "顺序", "语句成分", "赋值", "函数", "条件其他", "数组下标", "非负整数", "值列表",
                "值列表1"}
        )));

        grammar.addVT(new HashSet<>(Arrays.asList(
            new String[]{"||", "&&", "==", "!=", "<", ">", "<=", ">=", "+", "-", "*", "/", "=", "!", "I",
                "[", "]", "(", ")", "const int", "const double", "const char", "struct", "{", "}",
                "func", ";", ",", ".", "var", "return", "while", "if", "else"}
        )));

        grammar.setStartVN("P");

        grammar.addDeriver(new Deriver("P", new String[]{"S", "F"}));
        grammar.addDeriver(new Deriver("S", new String[]{"structure", "S"}));
        grammar.addDeriver(new Deriver("S", new String[]{}));
        grammar.addDeriver(new Deriver("F", new String[]{"function", "F"}));
        grammar.addDeriver(new Deriver("F", new String[]{}));
        grammar.addDeriver(new Deriver("structure", new String[]{"struct", "I", "{", "L", "}"}));
        grammar.addDeriver(new Deriver("function",
            new String[]{"func", "I", "(", "形参列表", ")", "类型", "{", "proc", "}"}));
        grammar.addDeriver(new Deriver("形参列表", new String[]{"形参", "形参列表1"}));
        grammar.addDeriver(new Deriver("形参列表", new String[]{}));
        grammar.addDeriver(new Deriver("形参列表1", new String[]{",", "形参", "形参列表1"}));
        grammar.addDeriver(new Deriver("形参列表1", new String[]{}));
        grammar.addDeriver(new Deriver("形参", new String[]{"I", "形参类型"}));
        grammar.addDeriver(new Deriver("形参类型", new String[]{"I"}));
        grammar.addDeriver(new Deriver("形参类型", new String[]{"[", "]", "I"}));
        grammar.addDeriver(new Deriver("proc", new String[]{"L", "复合语句"}));
        grammar.addDeriver(new Deriver("L", new String[]{"声明语句", ";", "L"}));
        grammar.addDeriver(new Deriver("L", new String[]{}));
        grammar.addDeriver(new Deriver("声明语句", new String[]{"var", "I", "标识符表", "类型"}));
        grammar.addDeriver(new Deriver("标识符表", new String[]{",", "I", "标识符表"}));
        grammar.addDeriver(new Deriver("标识符表", new String[]{}));
        grammar.addDeriver(new Deriver("复合语句", new String[]{"循环", "复合语句"}));
        grammar.addDeriver(new Deriver("复合语句", new String[]{"条件", "复合语句"}));
        grammar.addDeriver(new Deriver("复合语句", new String[]{"顺序", "复合语句"}));
        grammar.addDeriver(new Deriver("复合语句", new String[]{}));
        grammar.addDeriver(new Deriver("顺序", new String[]{"I", "语句成分", ";"}));
        grammar.addDeriver(new Deriver("语句成分", new String[]{"赋值"}));
        grammar.addDeriver(new Deriver("语句成分", new String[]{"函数"}));
        grammar.addDeriver(new Deriver("顺序", new String[]{"return", "bool", ";"}));
        grammar.addDeriver(new Deriver("类型", new String[]{"I"}));
        grammar.addDeriver(new Deriver("类型", new String[]{"[", "const int", "]", "I"}));
        grammar.addDeriver(
            new Deriver("循环", new String[]{"while", "(", "bool", ")", "{", "复合语句", "}"}));
        grammar
            .addDeriver(new Deriver("条件", new String[]{"if", "(", "bool", ")", "{", "复合语句",
                "}", "条件其他"}));
        grammar.addDeriver(new Deriver("条件其他", new String[]{"else", "{", "复合语句", "}"}));
        grammar.addDeriver(new Deriver("条件其他", new String[]{}));
        grammar.addDeriver(new Deriver("值成分", new String[]{"函数"}));
        grammar.addDeriver(new Deriver("值成分", new String[]{"[", "非负整数", "]"}));
        grammar.addDeriver(new Deriver("值成分", new String[]{".", "I"}));
        grammar.addDeriver(new Deriver("值成分", new String[]{}));
        grammar.addDeriver(new Deriver("值", new String[]{"I", "值成分"}));
        grammar.addDeriver(new Deriver("值", new String[]{"常量"}));
        grammar.addDeriver(new Deriver("数组下标", new String[]{"[", "bool", "]"}));
        grammar.addDeriver(new Deriver("数组下标", new String[]{}));
        grammar.addDeriver(new Deriver("非负整数", new String[]{"const int"}));
        grammar.addDeriver(new Deriver("非负整数", new String[]{"I"}));
        grammar.addDeriver(new Deriver("赋值", new String[]{"数组下标", "=", "bool"}));
        grammar.addDeriver(new Deriver("常量", new String[]{"const int"}));
        grammar.addDeriver(new Deriver("常量", new String[]{"const double"}));
        grammar.addDeriver(new Deriver("常量", new String[]{"const char"}));
        grammar.addDeriver(new Deriver("函数", new String[]{"(", "值列表", ")"}));
        grammar.addDeriver(new Deriver("值列表", new String[]{"值", "值列表1"}));
        grammar.addDeriver(new Deriver("值列表", new String[]{}));
        grammar.addDeriver(new Deriver("值列表1", new String[]{",", "值", "值列表1"}));
        grammar.addDeriver(new Deriver("值列表1", new String[]{}));
        //表达式文法
        grammar.addDeriver(new Deriver("bool", new String[]{"join", "BOOL"}));
        grammar.addDeriver(new Deriver("BOOL", new String[]{"||", "join", "BOOL"}));
        grammar.addDeriver(new Deriver("BOOL", new String[]{}));
        grammar.addDeriver(new Deriver("join", new String[]{"equality", "JOIN"}));
        grammar.addDeriver(new Deriver("JOIN", new String[]{"&&", "equality", "JOIN"}));
        grammar.addDeriver(new Deriver("JOIN", new String[]{}));
        grammar.addDeriver(new Deriver("equality", new String[]{"rel", "EQUALITY"}));
        grammar.addDeriver(new Deriver("EQUALITY", new String[]{"==", "rel", "EQUALITY"}));
        grammar.addDeriver(new Deriver("EQUALITY", new String[]{"!=", "rel", "EQUALITY"}));
        grammar.addDeriver(new Deriver("EQUALITY", new String[]{}));
        grammar.addDeriver(new Deriver("rel", new String[]{"expr", "expr1"}));
        grammar.addDeriver(new Deriver("expr1", new String[]{"<=", "expr"}));
        grammar.addDeriver(new Deriver("expr1", new String[]{">=", "expr"}));
        grammar.addDeriver(new Deriver("expr1", new String[]{">", "expr"}));
        grammar.addDeriver(new Deriver("expr1", new String[]{"<", "expr"}));
        grammar.addDeriver(new Deriver("expr1", new String[]{}));
        grammar.addDeriver(new Deriver("expr", new String[]{"term", "EXPR"}));
        grammar.addDeriver(new Deriver("EXPR", new String[]{"+", "term", "EXPR"}));
        grammar.addDeriver(new Deriver("EXPR", new String[]{"-", "term", "EXPR"}));
        grammar.addDeriver(new Deriver("EXPR", new String[]{}));
        grammar.addDeriver(new Deriver("term", new String[]{"unary", "TERM"}));
        grammar.addDeriver(new Deriver("TERM", new String[]{"*", "unary", "TERM"}));
        grammar.addDeriver(new Deriver("TERM", new String[]{"/", "unary", "TERM"}));
        grammar.addDeriver(new Deriver("TERM", new String[]{}));
        grammar.addDeriver(new Deriver("unary", new String[]{"!", "unary"}));
        grammar.addDeriver(new Deriver("unary", new String[]{"-", "unary"}));
        grammar.addDeriver(new Deriver("unary", new String[]{"值"}));
        grammar.addDeriver(new Deriver("unary", new String[]{"(", "bool", ")"}));

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

    public boolean LL1Analyze() throws InvalidLabelException, LexicalErrorException{
        SEM.clear();
        QT.clear();
        analyseStack.clear();
        Token w;//从input取来的token
        Token x;//从栈里取来的token
        Token y;
        i=0;
        //analyseStack.push(new Token("#","#"));
        analyseStack.push(new Token(null,"P"));
        //input.add(new Token("#","#"));
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
        if (!w.getLabel().equals("#")) {
            //如果栈空了，符号串还没读完
            //错误
            return false;
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
            Class cls = Class.forName("Translator");
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
    private Token next() throws LexicalErrorException{
        Token next = lexer.getOneWord();
        input.add(next);
        i = input.size() - 1;
        return next;
    }

    /**
     * 获取上一个单词
     * @return 单词
     */
    private Token last() {
        if (i == 0) {
            return null;
        }
        return input.get(i - 1);
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
}
