package ASMPackage;

import MiddleDataUtilly.QT;

import OptimizePackage.Optimizer;
import TranslatorPackage.SymbolTable.SymbolTableManager;
import TranslatorPackage.TranslatorExceptions.SemanticException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import jdk.nashorn.internal.runtime.arrays.ArrayLikeIterator;
import jdk.nashorn.internal.runtime.arrays.IntOrLongElements;

public class ASMGenerater {

    /**
     * 变量活跃信息表
     */
    private HashMap<String, String> active_table;

    /**
     * 四元式序列
     */
    private ArrayList<QT> qts;

    /**
     * 生成的汇编语句序列
     */
    private ArrayList<ASMSentence> sentences;

    public ASMGenerater(ArrayList<QT> qts, SymbolTableManager symbolTableManager) {
        active_table = new HashMap<>();
        this.qts = qts;
        sentences = new ArrayList<>();
        this.symbolTableManager = symbolTableManager;
    }

    /**
     * 符号表
     */
    private SymbolTableManager symbolTableManager;


    /**
     * 根据qts初始化变量的活跃信息表
     */
    private void initializeActiveTable() {
        for (QT qt : qts) {
            if (qt.getOperand_left() != null && !QT.isConstVariable(qt.getOperand_left())) {
                active_table
                    .put(qt.getOperand_left(),
                        QT.isTemporaryVariable(qt.getOperand_left()) ? "n" : "y");
            }
            if (qt.getOperand_right() != null && !QT.isConstVariable(qt.getOperand_right())) {
                active_table
                    .put(qt.getOperand_right(),
                        QT.isTemporaryVariable(qt.getOperand_right()) ? "n" : "y");
            }
            if (qt.getResult() != null && !QT.isConstVariable(qt.getResult())) {
                active_table
                    .put(qt.getResult(), QT.isTemporaryVariable(qt.getResult()) ? "n" : "y");
            }
        }
    }

    /**
     * 为每一个四元式的变量附加上活跃信息
     */
    private void addActiveInfomation() {
        QT qt;
        for (int i = qts.size() - 1; i >= 0; i--) {
            qt = qts.get(i);
            StringBuilder sb_result = null;
            if (qt.getResult() != null) {
                sb_result = new StringBuilder(qt.getResult());
                sb_result.append("->").append(active_table.get(qt.getResult()));
                active_table.put(qt.getResult(), "n");
            }
            StringBuilder sb_left = null;
            if (qt.getOperand_left() != null) {
                sb_left = new StringBuilder(qt.getOperand_left());
                if (!QT.isConstVariable(qt.getOperand_left())) {
                    sb_left.append("->").append(active_table.get(qt.getOperand_left()));
                    active_table.put(qt.getOperand_left(), Integer.toString(i));
                }
            }
            StringBuilder sb_right = null;
            if (qt.getOperand_right() != null) {
                sb_right = new StringBuilder(qt.getOperand_right());
                if (!QT.isConstVariable(qt.getOperand_right())) {
                    sb_right.append("->").append(active_table.get(qt.getOperand_right()));
                    active_table.put(qt.getOperand_right(), Integer.toString(i));
                }
            }

            qts.remove(i);
            qts.add(i, new QT(qt.getOperator(), (sb_left != null) ? sb_left.toString() : null,
                (sb_right != null) ? sb_right.toString() : null,
                (sb_result != null) ? sb_result.toString() : null));
        }
    }

    /**
     * 生成汇编代码序列
     *
     * @return 汇编代码序列 ArrayList
     */
    public ArrayList<ASMSentence> generate() throws ASMException, SemanticException{
        ArrayList<ASMSentence> result = new ArrayList<>();
        ArrayList<QT> cache = new ArrayList<>();
        Stack<String> jumpStack = new Stack<>();
        QT qt;
        for (int i = 0; i < qts.size(); i++) {
            qt = qts.get(i);
            if (Optimizer.isArithmeticOperator(qt.getOperator())) {
                cache.add(qt);
                continue;
            }
            if (cache.size() > 0) {
                result.addAll(new ASMArith(cache).getResult());
                cache.clear();
                continue;
            }

            //如果碰到控制四元式（非运算类的四元式）
            if (qt.getOperator().equals("if_sta")) {
                //与0比较判断真假
                //查找变量offset并从内存中取到AX中
                //TODO 修改8086指令到x86
                result.add(new ASMSentence("MOV", "AX",
                    "ES:" + symbolTableManager.lookUpVariableOffset(qt.getOperand_left())));
                result.add(new ASMSentence("CMP", "AX", "0"));
                result.add(new ASMSentence("JZ", "IF" + Integer.toString(i)));
                jumpStack.push("IF" + Integer.toString(i));//等待回填
            } else if (qt.getOperator().equals("el_sta")) {
                result.add(new ASMSentence("JMP", "ELSE" + Integer.toString(i)));
                result.add(new ASMSentence(jumpStack.pop() + ":"));//回填Label
                jumpStack.push("ELSE" + Integer.toString(i));//等待回填
            } else if (qt.getOperator().equals("ifel_end")) {
                result.add(new ASMSentence(jumpStack.pop() + ":"));
            } else if (qt.getOperator().equals("whl_sta")) {
                result.add(new ASMSentence("WHILE" + Integer.toString(i)));
                jumpStack.push("WHILE" + Integer.toString(i));
            } else if (qt.getOperator().equals("whl_do_ck")) {
                result.add(new ASMSentence("MOV", "AX",
                    "ES:" + symbolTableManager.lookUpVariableOffset(qt.getOperand_left())));
                result.add(new ASMSentence("CMP", "AX", "0"));
                result.add(new ASMSentence("JZ", "WHILE" + Integer.toString(i)));
                jumpStack.push("WHILE" + Integer.toString(i));
            } else if (qt.getOperator().equals("whl_end")) {
                String temp = jumpStack.pop();
                result.add(new ASMSentence("JMP", jumpStack.pop()));
                result.add(new ASMSentence(temp + ":"));
            }

            //TODO 下面写函数调用






        }
        return result;
    }
}
