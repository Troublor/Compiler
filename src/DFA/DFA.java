package DFA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

/**
 * Created by troub on 2017/9/8.
 */
public class DFA {

    /**
     * 压缩变换表
     */
    private TransformTable transformTable;

    /**
     * 状态集合
     */
    private ArrayList<String> states;

    /**
     * 字符集合
     */
    private ArrayList<String> letters;

    /**
     * 开始状态
     */
    private String startState;

    /**
     * 终止状态集合
     */
    private ArrayList<String> endStates;

    public boolean annotation;

    /**
     * 源代码
     */
    private String sourceCode;

    /**
     * 扫描字符串指针
     */
    public static int index = 0;

    /**
     * 记录行号
     */
    public static int line = 1;

    public DFA(){
        states=new ArrayList<>();
        letters=new ArrayList<>();
        endStates=new ArrayList<>();
        startState="";
        transformTable=new TransformTable();
    }

    /**
     * 构造方法
     */
    public DFA(ArrayList<String> s,ArrayList<String> l, String sS, ArrayList<String> eS){
        states=s;
        transformTable=new TransformTable();
        ListIterator iterator=states.listIterator();
        while (iterator.hasNext()){
            String state=(String)iterator.next();
            transformTable.addState(state);
        }
        letters=l;
        startState=sS;
        endStates=eS;
        annotation=false;
    }

    /**
     * 添加变换
     * @param state String 当前状态
     * @param input String 读入字符
     * @param destination String 下一个状态
     * @return boolean
     */
    public boolean addTransform(String state, String input, String destination){
        if (!states.contains(state)||!states.contains(destination)||!letters.contains(input)){
            return false;
        }
        return transformTable.addTransform(state,input,destination);
    }

    /**
     * 添加变换集合 重载
     * @param state String 当前状态
     * @param collection String[] 读入字符的集合
     * @param destination String 下一个状态
     * @return boolean
     */
    public boolean addTransform(String state, String[] collection, String destination){
        for (String input:collection){
            addTransform(state,input,destination);
        }
        return true;
    }

    /**
     * 为某一状态读入其他字符是转到默认的状态
     * @param state 当前状态
     * @param destination 目标状态
     * @return boolean
     */
    public boolean addDefaultTransform(String state,String destination){
        return transformTable.addDefaultTransform(state,destination);
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    /**
     * 检测字符串
     * 得到一个单词就停下
     * 读到末尾返回false
     * @param method 检测模式，true为检测同时处理
     * @return boolean
     */
    public boolean checkString(boolean method) throws LexicalErrorException{
        sourceCode=sourceCode.concat("##");
        String currState=startState;
        if (method){
            process(currState);
        }
        if (index >= sourceCode.length()) {
            throw new LexicalErrorException(
                "LexicalErrorException: not expected to end");
        }
        String input = sourceCode.substring(index, index + 1);
        if (input.equals("#")&&endStates.contains(currState)){
            return false;
        }
        currState=transformTable.transform(currState,input);
        if (currState.equals("no")){
            throw new LexicalErrorException(
                "LexicalErrorException: illegal word \"" + input + "\"");
        }
        if (method){
            if (process(currState,input)){
                index--;
            }
        }
        index++;
        if (index >= sourceCode.length()) {
            throw new LexicalErrorException(
                "LexicalErrorException: not expected to end");
        }
        if (input.equals("$")) {
            line++;
        }
        //状态转移回q1就停止运行
        while (!currState.equals(startState)) {
            input = sourceCode.substring(index, index + 1);
            if (input.equals("#")&&endStates.contains(currState)){
                return false;
            }
            currState=transformTable.transform(currState,input);
            if (currState.equals("no")){
                throw new LexicalErrorException(
                    "LexicalErrorException: illegal word \"" + input + "\"");
            }
            if (method){
                if (process(currState,input)){
                    index--;
                }
            }
            index++;
            if (index >= sourceCode.length()) {
                throw new LexicalErrorException(
                    "LexicalErrorException: not expected to end");
            }
            /*if (input.equals("$")) {
                line++;
            }*/
        }
        return true;
    }

    /**
     * 状态处理
     * 需要被重写
     */
    protected void process(String currState){}

    /**
     * 状态处理
     * 需要被重写
     * @return boolean true代表需要退格，false表示不需要退格
     */
    protected boolean process(String currState,String input) throws LexicalErrorException{return false;}

    /**
     * 添加状态
     * @param s 新的状态
     * @return boolean
     */
    public boolean addState(String s){
        states.add(s);
        return transformTable.addState(s);
    }

    /**
     * 添加状态集合（数组）
     * @param ss 状态集合（数组）
     * @return boolean
     */
    public boolean addStates(String[] ss){
        states.addAll(Arrays.asList(ss));
        for (String s:ss){
            if (!transformTable.addState(s)){
                return false;
            }
        }
        return true;
    }

    /**
     * 添加字符
     * @param l 要添加的字符
     * @return boolean
     */
    public boolean addLetter(String l){
        letters.add(l);
        return true;
    }

    /**
     * 添加字符集合（数组）
     * @param ls 字符集合（数组）
     * @return boolean
     */
    public boolean addLetters(String[] ls){
        letters.addAll(Arrays.asList(ls));
        return true;
    }

    /**
     * 设置开始状态
     * @param sS 开始状态
     * @return boolean
     */
    public boolean setStartState(String sS){
        if (states.contains(sS)){
            startState=sS;
            return true;
        }else {
            return false;
        }
    }

    /**
     * 添加终止状态
     * @param eS 要添加的终止状态
     * @return boolean
     */
    public boolean addEndState(String eS){
        endStates.add(eS);
        return true;
    }

    /**
     * 添加终止状态集合（数组）
     * @param eSs 终止状态集合（数组）
     * @return boolean
     */
    public boolean addEndStates(String[] eSs){
        letters.addAll(Arrays.asList(eSs));
        return true;
    }

    /**
     * 获取当前行号
     * @return 行号
     */
    public static int getLine() {
        return line;
    }

}
