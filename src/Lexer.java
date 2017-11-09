/**
 * Created by troub on 2017/9/12.
 */

import DFA.DFA;
import java.util.ArrayList;

/**
 * 词法分析器
 */
public class Lexer extends Lang{

    class LexerDFA extends DFA {

        /**
         * 相应的语言定义
         */
        //private Lang lang;


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
            String[] ss={"q1","q2","q3","q4","q5","q6","q7","q8","q9","q10","q11","q12","q13","q14","q15","q16","q17","q18","qe"};
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
            String[] ls={" ",":","=",".",",",";","+","-","(",")","*","/",">","<","\'","\"","[","]","{","}","#"};
            super.addLetters(ls);
            super.setStartState("q1");
            super.addEndState("qe");
            addTransform("q1"," ","q1");
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

            //判断界符
            addTransform("q1",new String[]{"(",")","*","+","-","=",";",",",".","<",">","{","}","[","]"},"q9");
            addDefaultTransform("q9","q1");

            //>=,<=,==
            addTransform("q1",new String[]{">","<","="},"q15");
            addTransform("q15","=","q16");
            addDefaultTransform("q16","q1");

            //字符串
            addTransform("q1","\"","q10");
            for (char c='a';c<='z';c++){
                addTransform("q10",new String(new char[]{c}),"q10");
            }
            for (char c='A';c<='Z';c++){
                addTransform("q10",new String(new char[]{c}),"q10");
            }
            for (char c='0';c<='9';c++){
                addTransform("q10",new String(new char[]{c}),"q10");
            }
            addTransform("q10","\"","q11");
            addDefaultTransform("q11","q1");

            //字符
            addTransform("q1","\'","q12");
            for (char c='a';c<='z';c++){
                addTransform("q12",new String(new char[]{c}),"q13");
            }
            for (char c='A';c<='Z';c++){
                addTransform("q12",new String(new char[]{c}),"q13");
            }
            for (char c='0';c<='9';c++){
                addTransform("q12",new String(new char[]{c}),"q13");
            }
            addTransform("q13","\'","q14");
            addDefaultTransform("q14","q1");

            //注释
            addTransform("q1","/","q17");
            //如果不是注释，只是/
            addDefaultTransform("q17","q1");
            addTransform("q17","/","q18");

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
        protected boolean process(String currState,String input){
            if (currState.equals("q2")){
                type=1;
                word=word.concat(input);
            }else if (currState.equals("q1")&&type==1){
                type=0;
                if (isKeyWord(word)){
//                    System.out.println(word+"  (keyWord, "+(keyWordTable.indexOf(word)+4)+")");
                    output.add(new Token(word, "<" + (keyWordTable.indexOf(word)+4) + ">"));
                }else {
                    int index=identifierTable.indexOf(word);
                    if (index<0){
                        identifierTable.add(word);
                        index=identifierTable.indexOf(word);
                    }
//                    System.out.println(word+"  (identifier, "+"00"+")");
                    output.add(new Token(word, "<00>"));
                }
                word="";
                return true;
            } else if (currState.equals("q3")) {
                type=2;
                num_n=10*num_n+val(input);
            }else if (currState.equals("q4")){
                num_t=1;
            }else if (currState.equals("q5")){
                num_n=10*num_n+val(input);
                num_m=num_m+1;
            }else if (currState.equals("q6")){
                num_t=1;
            }else if (currState.equals("q7")){
                if (input.equals("-")){
                    num_e=-1;
                }else {
                    num_e=1;
                }
            }else if (currState.equals("q8")){
                num_p=10*num_p+val(input);
            }
            else if (currState.equals("q1")&&type==2){
                type=0;
                double number=num_n*Math.pow(10,num_e*num_p-num_m);
                int index=constTable.indexOf(number);
                if (index<0){
                    constTable.add(number);
                    index=constTable.indexOf(number);
                }
//                System.out.println(number+"  (Const, "+"03"+")");
                output.add(new Token(Double.toString(number), "<03>"));
                num_n=0;
                num_p=0;
                num_m=0;
                num_t=0;
                num_e=1;
                return true;
            }else if (currState.equals("q9")||currState.equals("q15")||currState.equals("q16")){
                type=3;
                delimiter+=input;
            }else if (currState.equals("q1")&&type==3){
                type=0;
//                System.out.println(delimiter+"  (p, "+(delimiterTable.indexOf(delimiter)+10)+")");
                output.add(new Token(delimiter, "<" + (delimiterTable.indexOf(delimiter)+10) + ">"));
                delimiter="";
                return true;
            }else if (currState.equals("q10")){
                if (input.equals("\"")){
                    type=4;
                    return false;
                }else {
                    string=string.concat(input);
                }
            }else if (currState.equals("q1")&&type==4){
                type=0;
                stringTable.add(string);
//                System.out.println(string+"  (sT, "+"02"+")");
                output.add(new Token(string, "<02>"));
                string="";
                return true;
            }else if (currState.equals("q12")){
                type=5;
            }else if (currState.equals("q13")){
                character=input.charAt(0);
            }else if (currState.equals("q14")||type==5){
                characterTable.add(character);
//                System.out.println(character+"  (cT, "+"01"+")");
                output.add(new Token(Character.toString(character), "<01>"));
                type=0;
                character=0;
                return true;
            }else if (currState.equals("q18")){
                type=0;
//                System.out.println("//"+"  (p, "+(delimiterTable.indexOf("//")+10)+")");
                output.add(new Token("//", "<" + (delimiterTable.indexOf("//")+10) + ">"));
                delimiter="";
                annotation=true;
            }else if (currState.equals("q17")){
                type=3;
                delimiter=input;
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

    private ArrayList<Token> output;


    public Lexer(){
        super();
        output = new ArrayList<>();
        String[] ks={
                "int",
                "main",
                "void",
                "if",
                "else",
                "char",
                "return"
        };
        super.addKeyWords(ks);
        String[] ds={">=","<=","==","=",">","<","+","-","*","/","{","}",",",";","(",")","\"","\'","[","]","//"};
        super.addDelimiters(ds);

        //构造DFA
        dfa=new LexerDFA();
    }

    public boolean analyse(String s){

        if (!dfa.checkString(s,true)){
            if (dfa.annotation)
            {
                dfa.annotation=false;
                return true;
            }
            return false;
        }else {
            return true;
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
    private boolean isSpliter(char c){
        return c==' '||c=='\r'||c=='\t'||c=='\n';
    }

    /**
     * 判断是否是字幕
     * @param c 输入字符
     * @return boolean
     */
    private boolean isLetter(char c){
        return (c>='a'&&c<='z')||(c>='A'&&c<='Z');
    }


    public ArrayList<Token> getOutput() {
        return output;
    }
}
