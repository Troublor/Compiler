/**
 * Created by troub on 2017/9/12.
 */

import DFA.*;
import MiddleDataUtilly.Token;

import java.util.ArrayList;

/**
 * 词法分析器
 */
public class Lexer extends Lang{

    class LexerDFA extends DFA {
        /**
         * 用于存储刚刚读完的是什么类型
         */
        private int type;

        /**
         * 用于存储转换出来的数字
         */
        private ArrayList<Double> constTable;
        private int num_n;
        private int num_p;
        private int num_m;
        private int num_t;
        private int num_e;

        //private ArrayList<String> keyWordTable;
        private ArrayList<String> identifierTable;
        private String word;

        private String delimiter;
        //private ArrayList<String> delimiterTable;

        private ArrayList<Character> characterTable;
        char character;

        private ArrayList<String> stringTable;
        private String string;

        //初始化
        {
            num_n=0;
            num_p=0;
            num_m=0;
            num_t=0;
            num_e=1;
            type=0;
            constTable=new ArrayList<>();
            word="";
            identifierTable=new ArrayList<>();
            delimiter="";
            characterTable=new ArrayList<>();
            stringTable=new ArrayList<>();
            string="";
            character=0;
        }

        /**
         * 构造方法
         */
        public LexerDFA(){
            super();
            //lang=l;
            //keyWordTable=lang.getKeyWordTable();
            //delimiterTable=lang.getDelimiterTable();
            String[] ss = {"q1", "q2", "q3", "q4", "q5", "q6", "q7", "q8", "q9", "q10", "q11",
                    "q12", "q13", "q14", "q15", "q16", "q17", "q18", "q19", "q20", "q21", "q22", "qe"};
            super.addStates(ss);
            for (char c='0';c<='9';c++){
                super.addLetter(new String(new char[]{c}));
            }
            for (char c='a';c<='z';c++){
                super.addLetter(new String(new char[]{c}));
            }
            for (char c='A';c<='Z';c++){
                super.addLetter(new String(new char[]{c}));
            }
            //$用来标识行尾
            String[] ls={" ",":","=",".",",",";","+","-","(",")","*","/",">","<","\'","\"","[","]","{","}","#","|", "&", "$", "!"};
            super.addLetters(ls);
            super.setStartState("q1");
            super.addEndState("qe");
            addTransform("q1"," ","q1");
            addTransform("q1", "$", "q1");
            addTransform("q1", "\r", "q1");
            addTransform("q1", "\n", "q1");
            addTransform("q1","#","qe");
            for (char c='a';c<='z';c++){
                addTransform("q1",new String(new char[]{c}),"q2");
                addTransform("q2",new String(new char[]{c}),"q2");
            }
            for (char c='A';c<='Z';c++){
                addTransform("q1",new String(new char[]{c}),"q2");
                addTransform("q2",new String(new char[]{c}),"q2");
            }
            for (char c='0';c<='9';c++){
                addTransform("q2",new String(new char[]{c}),"q2");
                addTransform("q1",new String(new char[]{c}),"q3");
                addTransform("q3",new String(new char[]{c}),"q3");
                addTransform("q4",new String(new char[]{c}),"q5");
                addTransform("q5",new String(new char[]{c}),"q5");
                addTransform("q6",new String(new char[]{c}),"q8");
                addTransform("q8",new String(new char[]{c}),"q8");
                addTransform("q7",new String(new char[]{c}),"q8");
            }
            addTransform("q6",new String[]{"+","-"},"q7");
            addTransform("q5","e","q6");
            addTransform("q3","e","q6");
            addTransform("q3",".","q4");
            addDefaultTransform("q5","q1");
            addDefaultTransform("q2","q1");
            addDefaultTransform("q3","q1");
            addDefaultTransform("q8","q1");

            //判断界符（非连用界符）
            addTransform(
                "q1",
                new String[]{"(", ")", "*", ";", ",", ".", "%", "{", "}", "[", "]"},
                "q9"
            );
            addDefaultTransform("q9","q1");

            //二连用字符
            addTransform(
                "q1",
                new String[]{">", "<", "=", "+", "-", "!", "&", "|"},
                "q10"
            );
            addTransform(
                "q10",
                new String[]{">", "<", "=", "+", "-", "!", "&", "|"},
                "q11"
            );
            addDefaultTransform("q10", "q1");
            addDefaultTransform("q11", "q1");

            //字符串
            addTransform("q1", "\"", "q12");
            addTransform("q12", "\"", "q13");
            addDefaultTransform("q12", "q12");
            addDefaultTransform("q13","q1");

            //字符
            addTransform("q1","\'","q14");
            addTransform("q15","\'","q16");
            addDefaultTransform("q14", "q15");
            addDefaultTransform("q16","q1");

            //注释
            addTransform("q1","/","q17");
            //如果不是注释，只是/
            addDefaultTransform("q17","q1");
            addTransform("q17","/","q18");
            addDefaultTransform("q18", "q18");
            addTransform("q18", "$", "q19");
            addDefaultTransform("q19", "q1");
            addTransform("q17", "*", "q20");
            addDefaultTransform("q20", "q20");
            addTransform("q20", "*", "q21");
            addDefaultTransform("q21", "q20");
            addTransform("q21", "/", "q22");
            addDefaultTransform("q22", "q1");

            //终止字符
            addTransform("q1","#","qe");
        }

        /**
         * constTable访问器
         * @return double
         */
        public ArrayList<Double> getConstTable(){
            return constTable;
        }


        /**
         * 重写父类的process函数，实现在每个状态对字符串进行处理
         * @param currState 当前状态
         * @param input 转到当前状态时，读入的字符
         * @return boolean true代表需要退格，false表示不需要退格
         */
        @Override
        protected boolean process(String currState,String input) throws LexicalErrorException {
            if (currState.equals("q2")){
                //进入标识符分支
                type = Token.WordType.IDENTITY;
                word=word.concat(input);
            } else if (currState.equals("q1") && type == Token.WordType.IDENTITY) {
                //标识符分支结束
                if (isKeyWord(word)) {
                    //如果是关键字
                    output = new Token(word, word);
                } else {
                    output = new Token(word, "I");
                }
                //辅助参数归零
                word = "";
                type = Token.WordType.NONE;
                return true;
            } else if (currState.equals("q3")) {
                //进入数字分支
                type = Token.WordType.NUMBER;
                num_n = 10 * num_n + val(input);
            } else if (currState.equals("q4")) {
                num_t = 1;
            } else if (currState.equals("q5")) {
                num_n = 10 * num_n + val(input);
                num_m = num_m + 1;
            } else if (currState.equals("q6")) {
                num_t = 1;
            } else if (currState.equals("q7")) {
                if (input.equals("-")) {
                    num_e = -1;
                } else {
                    num_e = 1;
                }
            } else if (currState.equals("q8")) {
                num_p = 10 * num_p + val(input);
            } else if (currState.equals("q1") && type == Token.WordType.NUMBER) {
                //数字分支结束
                double number = num_n * Math.pow(10, num_e * num_p - num_m);
                double eps = 1e-10;  // 精度范围
                if (num_t == 0) {
                    //如果是整数
                    output = new Token(Double.toString(number), "const int");
                } else {
                    output = new Token(Double.toString(number), "const double");
                }
                //辅助参数归零
                type = Token.WordType.NONE;
                num_n = 0;
                num_p = 0;
                num_m = 0;
                num_t = 0;
                num_e = 1;
                return true;
            } else if (currState.equals("q9") || currState.equals("q10") || currState
                .equals("q11")) {
                //进入界符分支
                type = Token.WordType.DELIMITER;
                delimiter += input;
            } else if (currState.equals("q1") && type == Token.WordType.DELIMITER) {
                //界符分支结束
                if (!isDelimiter(delimiter)) {
                    throw new LexicalErrorException(
                        "DFA.LexicalErrorException: " + delimiter + " is not a delimiter\n");
                }
                output = new Token(delimiter, delimiter);
                //辅助参数归零
                type = Token.WordType.NONE;
                delimiter = "";
                return true;
            } else if (currState.equals("q12")) {
                //进入字符串分支
                if (input.equals("\"")) {
                    type = Token.WordType.STRING;
                } else {
                    string = string.concat(input);
                }
            } else if (currState.equals("q1") && type == Token.WordType.STRING) {
                //字符串分支结束
                output= new Token(string, "字符串常量");
                //辅助参数归零
                type = Token.WordType.NONE;
                string = "";
                return true;
            } else if (currState.equals("q14")) {
                //进入字符分支
                type = Token.WordType.CHARACTER;
            } else if (currState.equals("q15")) {
                character = input.charAt(0);
            } else if (currState.equals("q1") && type == Token.WordType.CHARACTER) {
                //字符分支结束
                output = new Token(Character.toString(character), "const char");
                //辅助参数归零
                type = Token.WordType.NONE;
                character = 0;
                return true;
            } else if (currState.equals("q17")) {
                //进入注释分支，但是还不能确定是不是注释
                type = Token.WordType.DELIMITER;
                delimiter += input;
            } else if (currState.equals("q18") || currState.equals("q20")) {
                //进入注释分支
                delimiter = "";
                type = Token.WordType.ANNOTATION;
                annotation = true;
            } else if (currState.equals("q1") && type == Token.WordType.ANNOTATION) {
                //注释分支结束
                //辅助参数归零
                type = Token.WordType.NONE;
            }
            return false;
        }

        /**
         * 将一位字符转换成一位数字
         * @param n String 一位字符
         * @return int
         */
        private int val(String n){
            return n.charAt(0)-'0';
        }
    }

    private LexerDFA dfa;

    private Token output;

    public Lexer(){
        super();
        //构造DFA
        dfa=new LexerDFA();
        LexerDFA.index=0;
        LexerDFA.line=1;
    }

    /**
     * 获取一个单词
     * 调用一次DFA
     * 读到末尾了就返回#的Token
     *
     * @return 单词
     * @throws LexicalErrorException 词法错误异常
     */
    public Token getOneWord() throws LexicalErrorException {
        output = null;
        if (!dfa.checkString(true)){
            //如果读完了，到末尾了
            return new Token("", "#");
        }else {
            if (output == null) {
                //如果当前读啥也没读到，可能有空格，注释
                //读下一个
                return getOneWord();
            }
            return output;
        }
    }

    /**
     * 单词拆分
     * @param s 输入文本
     * @return boolean 是否接受
     */
    @Deprecated
    public boolean split(String s){
        int i=0;
        while (i<s.length()){
            //跳过分隔符
            while (isSpliter(s.charAt(i))){
                i++;
            }

            //标识符/关键字 分支
            if (isLetter(s.charAt(i))){
                i++;
            }
        }
        return true;
    }

    /**
     * 判断是否是单词分隔符
     * @param c 输入字符
     * @return boolean
     */
    @Deprecated
    private boolean isSpliter(char c){
        return c==' '||c=='\r'||c=='\t'||c=='\n';
    }

    /**
     * 判断是否是字幕
     * @param c 输入字符
     * @return boolean
     */
    @Deprecated
    private boolean isLetter(char c){
        return (c>='a'&&c<='z')||(c>='A'&&c<='Z');
    }


    public void setSourceCode(String s) {
        dfa.setSourceCode(s);
    }

    /**
     * 获取当前行号
     * @return 行号
     */
    public int getLine() {
        return DFA.getLine();
    }
}
