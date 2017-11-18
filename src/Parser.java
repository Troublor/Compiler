import DFA.LexicalErrorException;
import MiddleDataUtilly.QT;
import MiddleDataUtilly.Token;
import TranslatorPackage.MiddleLangTranslator;
import TranslatorPackage.SymbolTable.SymbolTableManager;

import TranslatorPackage.TranslatorExceptions.SemanticException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

/**
 * Created by troub on 2017/10/10.
 */

/**
 * 日期 : 11.11 22:30
 * author : lrj
 * 更新说明: 加上了变量定义 表达式求值 顺序 循环 判断
 * 1.修复了const_int字面值带带小数点
 * 在常量在push到translator的semanticStack时 进行了下处理
 * 2. const型token压到语义栈的时候换成以 const xxx_数字 的形式了 避免浮点数影响
 * 3.基本的四元式都OK了 就是赋值还没有
 * 4. translator类里的exception都用try/catch输出了 非常好debug
 *
 * 日期：11.16 0：20
 * //translator类中的异常全部抛出，又Parser类处理，加上行号信息
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

    // 语义分析
    private MiddleLangTranslator translator;

    /**
     * 是否debug模式
     */
    private boolean debug;

//    private Stack<Token> SEM;

//    private ArrayList<QT> QT;

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
        translator = new MiddleLangTranslator();
        debug = false;
//        SEM = new Stack<>();
//        QT = new ArrayList<QT>();

        grammar.addVN(new HashSet<>(Arrays.asList(
                "bool", "join", "BOOL", "equality", "JOIN", "EQUALITY", "rel", "expr",
                "expr1", "EXPR", "term", "TERM", "unary", "值", "数组寻址", "常量", "S", "P", "S", "F",
                "structure", "function", "L", "形参列表", "形参列表1", "类型", "proc", "形参",
                "形参类型", "类型标识符", "复合语句", "声明语句", "标识符表", "类型", "循环", "条件",
                "顺序", "语句成分", "赋值", "函数", "条件其他", "数组下标", "非负整数", "值列表", "RET_BOOL",
                "值列表1", "S_L", "寻址", "数组类型声明", "多维数组", "数组寻址_next", "值成分",
                "结构体寻址_next")));

        grammar.addVT(new HashSet<>(Arrays.asList(
                "||", "&&", "==", "!=", "<", ">", "<=", ">=", "+", "-", "*", "/", "=", "!", "I",
                "[", "]", "(", ")", "const int", "const double", "const char", "struct", "{", "}",
                "func", ";", ",", ".", "var", "return", "while", "if", "else")));

        grammar.setStartVN("P");

        //TODO 需要修改，以支持结构体多层访问与数组多层访问


        grammar.addDeriver(new Deriver("P", new String[]{"S", "F"}));
        grammar.addDeriver(new Deriver("S", new String[]{"structure", "S"}));
        grammar.addDeriver(new Deriver("S", new String[]{}));
        grammar.addDeriver(new Deriver("F", new String[]{"function", "F"}));
        grammar.addDeriver(new Deriver("F", new String[]{}));


        grammar.addDeriver(new Deriver("structure",
                new String[]{"struct", "I", "_AC_PUSH", "_AC_defineStruct", "{", "S_L", "}"}));


        grammar.addDeriver(new Deriver("S_L",
                new String[]{"var", "_AC_pushDefineFieldStart", "I", "_AC_PUSH", "标识符表",
                        "类型", "_AC_defineStashedField", ";", "S_L"}));

        grammar.addDeriver(new Deriver("S_L", new String[]{}));


        grammar.addDeriver(new Deriver("function",
                new String[]{"func", "I", "_AC_PUSH", "I", "_AC_PUSH", "(", "_AC_stepIntoBlock",
                        "_AC_preDefineFuncName", "_AC_pushFunctionDefineParamsStart",
                        "形参列表", ")", "_AC_defineStashedParams",
                        "{", "proc", "_AC_addVoidDefaultRet", "}", "_AC_stepOutBlock", "_AC_clearCurrDefineFunc"}));

        grammar.addDeriver(new Deriver("形参列表", new String[]{}));

        grammar.addDeriver(new Deriver("形参列表", new String[]{"形参", "形参列表1"}));
        grammar.addDeriver(new Deriver("形参列表1", new String[]{",", "形参", "形参列表1"}));
        grammar.addDeriver(new Deriver("形参列表1", new String[]{}));

        grammar.addDeriver(new Deriver("形参", new String[]{"I", "_AC_PUSH", "形参类型"}));

        grammar.addDeriver(new Deriver("形参类型", new String[]{"I", "_AC_PUSH",}));
        grammar.addDeriver(new Deriver("形参类型", new String[]{"[", "]", "I", "_AC_PUSH", "_AC_pushArrayTypeParamFlag"}));


        grammar.addDeriver(new Deriver("proc", new String[]{"L", "复合语句"}));
        grammar.addDeriver(new Deriver("L", new String[]{"声明语句", ";", "L"}));
        grammar.addDeriver(new Deriver("L", new String[]{}));

        //变量声明文法
        grammar.addDeriver(new Deriver("声明语句", new String[]{
                "var", "_AC_pushFlagDefineVariableStart", "I", "_AC_PUSH",
                "标识符表", "类型", "_AC_defineStashedVariables"}));
        grammar.addDeriver(new Deriver("标识符表", new String[]{",", "I", "_AC_PUSH", "标识符表"}));
        grammar.addDeriver(new Deriver("标识符表", new String[]{}));
        grammar.addDeriver(new Deriver("类型", new String[]{"I", "_AC_PUSH", "_AC_checkTypeExist"}));
        grammar.addDeriver(new Deriver("类型", new String[]{"数组类型声明"}));

        grammar.addDeriver(new Deriver("数组类型声明",
                new String[]{"_AC_pushFlagStartMltArrayDeclare", "[", "const int", "_AC_PUSH", "]",
                        "多维数组", "I", "_AC_PUSH", "_AC_defineArrayType"}));

        grammar.addDeriver(new Deriver("多维数组",
                new String[]{"[", "const int", "_AC_PUSH", "]", "多维数组"}));

        grammar.addDeriver(new Deriver("多维数组", new String[]{}));


        grammar.addDeriver(new Deriver("复合语句", new String[]{"循环", "复合语句"}));
        grammar.addDeriver(new Deriver("复合语句", new String[]{"条件", "复合语句"}));
        grammar.addDeriver(new Deriver("复合语句", new String[]{"顺序", "复合语句"}));
        grammar.addDeriver(new Deriver("复合语句", new String[]{}));


        grammar.addDeriver(new Deriver("顺序", new String[]{"I","_AC_PUSH" , "语句成分", ";"}));

        grammar.addDeriver(new Deriver("语句成分", new String[]{"赋值"}));
        grammar.addDeriver(new Deriver("语句成分", new String[]{"函数", "_AC_funcRetNoNeed"}));
        grammar.addDeriver(new Deriver("函数",
                new String[]{"_AC_receiveCallingFuncName", "(", "_AC_funcParamStartFlag", "值列表", ")"
                        , "_AC_startFuncCalling"}));

        //函数返回值
        grammar.addDeriver(new Deriver("顺序", new String[]{"return", "_AC_pushMayRetValFlag", "RET_BOOL"}));

        grammar.addDeriver(new Deriver("RET_BOOL",
                new String[]{"_AC_pushEmptyRetValFlag", "_AC_reciveReturnVal", ";"}));
        grammar.addDeriver(new Deriver("RET_BOOL", new String[]{"bool", "_AC_reciveReturnVal", ";"}));



        grammar.addDeriver(new Deriver("循环",
                new String[]{
                        "while", "_AC_addWhileStartQT", "(", "bool", ")", "_AC_checkWhileDo",
                        "{", "_AC_stepIntoBlock", "proc", "}", "_AC_stepOutBlock", "_AC_addWhileEndQT"}));
        grammar.addDeriver(new Deriver("条件",
                new String[]{
                        "if", "(", "bool", ")", "_AC_addIfStartQt",
                        "{", "_AC_stepIntoBlock", "proc", "}",
                        "_AC_stepOutBlock", "条件其他", "_AC_addIfElseEndQt"}));

        grammar.addDeriver(new Deriver("条件其他",
                new String[]{"else", "_AC_addElseStartQt",
                        "{", "_AC_stepIntoBlock", "proc", "}", "_AC_stepOutBlock"}));
        grammar.addDeriver(new Deriver("条件其他", new String[]{}));


        // 只返回单个常数和变量时
        grammar.addDeriver(new Deriver("值", new String[]{"I", "_AC_PUSH", "值成分"}));
        grammar.addDeriver(new Deriver("值", new String[]{"常量"}));

        grammar.addDeriver(new Deriver("值成分", new String[]{"函数"}));
        grammar.addDeriver(new Deriver("值成分", new String[]{"寻址"}));


        grammar.addDeriver(new Deriver("非负整数", new String[]{"const int", "_AC_PUSH"}));
        grammar.addDeriver(new Deriver("非负整数", new String[]{"I", "_AC_PUSH"}));


        grammar.addDeriver(new Deriver("赋值", new String[]{"寻址", "=", "bool", "_AC_afterAssign"}));
        grammar.addDeriver(new Deriver("寻址", new String[]{"[", "bool", "]", "数组寻址"}));

        grammar.addDeriver(new Deriver("数组寻址", new String[]{"_AC_referenceEnd"}));
        grammar.addDeriver(new Deriver("数组寻址", new String[]{"_AC_reference", "[", "bool", "]", "数组寻址"}));

        grammar.addDeriver(new Deriver("寻址", new String[]{".", "I", "_AC_PUSH", "_AC_afterStruct", "结构体寻址"}));
        grammar.addDeriver(new Deriver("结构体寻址", new String[]{".", "I", "_AC_PUSH", "_AC_afterStruct", "结构体寻址"}));
        grammar.addDeriver(new Deriver("结构体寻址", new String[]{}));



        grammar.addDeriver(new Deriver("寻址", new String[]{}));


        grammar.addDeriver(new Deriver("常量", new String[]{"const int", "_AC_PUSH"}));
        grammar.addDeriver(new Deriver("常量", new String[]{"const double", "_AC_PUSH"}));
        grammar.addDeriver(new Deriver("常量", new String[]{"const char", "_AC_PUSH"}));

        // 到达这一步函数 函数名已经传入  可以push进参数进行传参
        grammar.addDeriver(new Deriver("值列表", new String[]{"值", "值列表1"}));
        grammar.addDeriver(new Deriver("值列表", new String[]{}));
        grammar.addDeriver(new Deriver("值列表1", new String[]{",", "值", "值列表1"}));
        grammar.addDeriver(new Deriver("值列表1", new String[]{}));
        //表达式文法

        grammar.addDeriver(new Deriver("bool", new String[]{"join", "BOOL"}));
        grammar.addDeriver(new Deriver("BOOL", new String[]{"||", "_AC_PUSH", "join","_AC_afterDual", "BOOL"}));
        grammar.addDeriver(new Deriver("BOOL", new String[]{}));
        grammar.addDeriver(new Deriver("join", new String[]{"equality", "JOIN"}));
        grammar.addDeriver(new Deriver("JOIN", new String[]{"&&","_AC_PUSH", "equality","_AC_afterDual", "JOIN"}));
        grammar.addDeriver(new Deriver("JOIN", new String[]{}));
        grammar.addDeriver(new Deriver("equality", new String[]{"rel", "EQUALITY"}));
        grammar.addDeriver(new Deriver("EQUALITY", new String[]{"==", "_AC_PUSH", "rel","_AC_afterDual", "EQUALITY"}));
        grammar.addDeriver(new Deriver("EQUALITY", new String[]{"!=","_AC_PUSH", "rel","_AC_afterDual", "EQUALITY"}));
        grammar.addDeriver(new Deriver("EQUALITY", new String[]{}));
        grammar.addDeriver(new Deriver("rel", new String[]{"expr", "expr1"}));
        grammar.addDeriver(new Deriver("expr1", new String[]{"<=","_AC_PUSH", "expr", "_AC_afterDual"}));
        grammar.addDeriver(new Deriver("expr1", new String[]{">=","_AC_PUSH", "expr", "_AC_afterDual"}));
        grammar.addDeriver(new Deriver("expr1", new String[]{">","_AC_PUSH", "expr", "_AC_afterDual",}));
        grammar.addDeriver(new Deriver("expr1", new String[]{"<", "_AC_PUSH", "expr", "_AC_afterDual",}));
        grammar.addDeriver(new Deriver("expr1", new String[]{}));
        grammar.addDeriver(new Deriver("expr", new String[]{"term", "EXPR"}));
        grammar.addDeriver(new Deriver("EXPR", new String[]{"+", "_AC_PUSH", "term", "_AC_afterDual", "EXPR"}));
        grammar.addDeriver(new Deriver("EXPR", new String[]{"-","_AC_PUSH",  "term", "_AC_afterDual", "EXPR"}));
        grammar.addDeriver(new Deriver("EXPR", new String[]{}));
        grammar.addDeriver(new Deriver("term", new String[]{"unary", "TERM"}));
        grammar.addDeriver(new Deriver("TERM", new String[]{"*", "_AC_PUSH", "unary","_AC_afterDual",  "TERM"}));
        grammar.addDeriver(new Deriver("TERM", new String[]{"/", "_AC_PUSH", "unary", "_AC_afterDual", "TERM"}));
        grammar.addDeriver(new Deriver("TERM", new String[]{}));
        grammar.addDeriver(new Deriver("unary", new String[]{"!","_AC_PUSH",  "unary", "_AC_afterUnary"}));
        grammar.addDeriver(new Deriver("unary", new String[]{"-", "_AC_PUSH", "unary","_AC_afterUnary"}));
        grammar.addDeriver(new Deriver("unary", new String[]{"值"}));
        grammar.addDeriver(new Deriver("unary", new String[]{"(", "bool", ")"}));

        try {
            grammar.generateLL1AnalyzeTable();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        analyseTable = grammar.getLL1AnalyseTable();
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * 设置源代码串
     *
     * @param s 源代码串
     */
    public void setSourceCode(String s) {
        lexer.setSourceCode(s);
    }

    public void LL1Analyze() throws CompileException{
        analyseStack.clear();
        Token w;//从input取来的token
        Token x;//从栈里取来的token
        Token y;
        i=0;
        //analyseStack.push(new Token("#","#"));
        analyseStack.push(new Token(null,"P"));
        try {

            //input.add(new Token("#","#"));
            w = next();
            while (!analyseStack.isEmpty()) {
                x = analyseStack.pop();
                if (grammar.isVT(x.getLabel())) {
                    //如果栈顶是终结符
                    if (!w.getLabel().equals(x.getLabel())) {
                        //如果与input不匹配
                        throw new GrammarException(
                                "GrammarException: unexpected word \"" + w.getWord() + "\"");
                    }
                    //如果匹配则input取下一个
                    w = next();
                } else if (grammar.isVN(x.getLabel())) {
                    //如果栈顶是非终结符
                    //通过分析表获取产生式序号
                    Integer index = analyseTable.get(x.getLabel()).get(w.getLabel());
                    if (index == null) {
                        //如果没找到对应的产生式
                        throw new GrammarException(
                                "GrammarException: unexpected word \"" + w.getWord() + "\"");
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
                throw new GrammarException(
                        "GrammarException: redundant words after \"" + w.getWord() + "\"");
            }

        } catch (Exception e) {
            throw new CompileException("GrammarException: At Line " + lexer.getLine() + " - " + e);
        } catch (Throwable throwable) {
            throw new CompileException("GrammarException: At Line " + lexer.getLine() + " - Unknown error");
        }

        analyseStack.clear();
        if (debug) {
            translator.printAllQTs();
        }
    }

    /**
     * 调用动作函数
     * @param a 动作字符串
     * @throws InvalidLabelException 非法符号异常
     */
    private void action(String a) throws InvalidLabelException, SemanticException, Throwable{
        String[] split = a.split("_");
        if (split.length < 3 || !split[1].equals("AC")) {
            throw new InvalidLabelException("InvalidLabelException: " + a + " is not an action\n");
        }
        if (split[2].equals("PUSH")) {
            PUSH();
            return;
        }
        //反射调用方法
        try {
            Class cls = translator.getClass();
            Method method;
            switch (split.length - 3) {
                case 0:
                    method = cls.getDeclaredMethod(split[2]);
                    method.invoke(translator);
                    break;
                case 1:
                    method = cls.getDeclaredMethod(split[2], String.class);
                    method.invoke(translator, split[3]);
                    break;
                case 2:
                    method = cls.getDeclaredMethod(split[2], String.class, String.class);
                    method.invoke(translator, split[3], split[4]);
                    break;
                case 3:
                    method = cls.getDeclaredMethod(split[2], String.class, String.class,
                            String.class);
                    method.invoke(translator, split[3], split[4], split[5]);
                    break;
                case 4:
                    method = cls.getDeclaredMethod(split[2], String.class, String.class,
                            String.class, String.class);
                    method.invoke(translator, split[3], split[4], split[5], split[6]);
                    break;
                case 5:
                    method = cls.getDeclaredMethod(split[2], String.class, String.class,
                            String.class, String.class, String.class);
                    method.invoke(translator, split[3], split[4], split[5], split[6], split[7]);
                    break;
            }
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new InvalidLabelException("InvalidLabelException: " + a + " action failure\n");
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
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

    //一下都是动作函数的定义
    private void PUSH() {
        String label = this.last().getLabel(),
                word = this.last().getWord();
        String input_item;
        if (label.equals("const int") || label.equals("const double") || label.equals("const char")) {
            // produce 1 -> "const int.1"

            // 有的时候int是 xx.xx的 在这里直接转成没有小数点的
            if (label.equals("const int"))
                word = String.valueOf(Float.valueOf(word).intValue());
            input_item = label + "_" + word;
        } else input_item = word;
        translator.push(input_item);
    }

    /**
     * 获取最后生成的四元式
     * @return 所有的四元式
     */
    public ArrayList<QT> getAllQTs() {
        return translator.getQTs();
    }

    public SymbolTableManager getSymbolTableManager() {
        return translator.getSymbolTableManager();
    }
//    private void PUSH() {
//        SEM.push(this.last());
//    }
//
//    private void GEQ(String w) {
//        Token n1, n2;
//        n1 = SEM.pop();
//        n2 = SEM.pop();
//        QT qt=new QT(w, n2.getWord(), n1.getWord());
//        QT.add(qt);
//        SEM.push(new Token(qt.getResult(), "i"));
//    }
//
//
//    public void printQT(){
//        for (QT qt : QT) {
//            System.out.println(qt.toString());
//        }
//    }
}