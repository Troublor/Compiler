package ASMPackage;

import MiddleDataUtilly.QT;

import OptimizePackage.Optimizer;
import TranslatorPackage.SymbolTable.SymbolTableManager;
import TranslatorPackage.TranslatorExceptions.SemanticException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;



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
     * 生成函数相关的asm
     */
    private ASMFunctionGenerater asmFunctionGenerater;

    private String jmp_judge_val;

    /**
     * 生成的汇编语句序列
     */
    private ArrayList<ASMSentence> sentences;

    public ASMGenerater(ArrayList<QT> qts, SymbolTableManager symbolTableManager) {
        active_table = new HashMap<>();
        this.qts = qts;
        sentences = new ArrayList<>();
        this.symbolTableManager = symbolTableManager;
        asmFunctionGenerater = new ASMFunctionGenerater(symbolTableManager, this);
    }

    /**
     * 符号表
     */
    private SymbolTableManager symbolTableManager;


    /**
     * 根据qts初始化变量的活跃信息表
     */
    private void initializeActiveTable(List<QT> qts_block) {
        for (QT qt : qts_block) {
            if (qt.getOperator().equals("ref")) {
                //如果是引用ref跳过
                continue;
            }
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
                String active_state;
                //在这里特判一下
                if (QT.isTemporaryVariable(qt.getResult()) || qt.getResult().equals(jmp_judge_val))
                    active_state = "y";
                else
                    active_state = "n";
                active_table.put(qt.getResult(), active_state);
            }
        }
    }

    /**
     * 为每一个四元式的变量附加上活跃信息
     */
    private void addActiveInfomation(List<QT> qts_block) {
        QT qt;
        for (int i = qts_block.size() - 1; i >= 0; i--) {
            qt = qts_block.get(i);
            if (qt.getOperator().equals("ref")){
                continue;
            }
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
                    active_table.put(qt.getOperand_left(), Integer.toString(i));
                }
            }

            qts_block.remove(i);
            qts_block.add(i, new QT(qt.getOperator(), (sb_left != null) ? sb_left.toString() : null,
                    (sb_right != null) ? sb_right.toString() : null,
                    (sb_result != null) ? sb_result.toString() : null));
        }
    }


    public ArrayList<ASMSentence> generate() throws ASMException, SemanticException {
        ArrayList<ASMSentence> result = new ArrayList<>();
        ArrayList<QT> cache = new ArrayList<>();
        Stack<String> jumpStack = new Stack<>();
        System.out.println("下面输出各变量的相对偏移");
        symbolTableManager.printAllVariable();
        //NASM头部
        result.add(new ASMSentence("section", ".text"));
        result.add(new ASMSentence("global", "_start"));
        QT qt;
        for (int i = 0; i < qts.size(); i++) {
            qt = qts.get(i);

            // 函数调用四元式块
            if (qt.getOperator().equals("push_stk")) {
                //收集
                result.add(new ASMSentence("; func calling block start"));
                while (!qt.getOperator().equals("call")) {
                    cache.add(qt);
                    i++;
                    qt = qts.get(i);
                }
                cache.add(qt);

                //生成
                List<ASMSentence> res = asmFunctionGenerater.generateFunctionCalling(cache);
                cache.clear();
                result.addAll(res);

                result.add(new ASMSentence("; func calling block end"));
                continue;
            }



            //算式表达式运算四元式块
            if (Optimizer.isArithmeticOperator(qt.getOperator())
                    || qt.getOperator().equals("ref")) {

                cache.add(qt);
                i++;
                qt = qts.get(i);
                while (Optimizer.isArithmeticOperator(qt.getOperator())) {
                    cache.add(qt);
                    i++;
                    qt = qts.get(i);
                }

                //如果发现该运算块下部需要用到变量进行判断
                //默认将最后一条的变量都设为活跃
                if (qt.getOperator().equals("whl_do_ck") || qt.getOperator().equals("if_sta"))
                    jmp_judge_val = qt.getOperand_left();

                initializeActiveTable(cache);
                addActiveInfomation(cache);
                ASMArith asmArith = new ASMArith(cache, this, symbolTableManager);
                result.addAll(asmArith.produceASM());
                cache.clear();
            }

            //如果碰到控制四元式（非运算类的四元式）
            String judge_opd;
            switch (qt.getOperator()) {
                case "if_sta":
                    //与0比较判断真假
                    //查找变量offset并从内存中取到AX中
                    judge_opd = toASMOprd(qt.getOperand_left(), "esi");
                    result.add(new ASMSentence("; if start position"));
                    result.add(new ASMSentence("mov", "eax", judge_opd));
                    result.add(new ASMSentence("cmp", "eax", "0"));
                    result.add(new ASMSentence("jz", "IF" + Integer.toString(i)));
                    result.add(new ASMSentence("; if jump judge"));

                    //i 是四元式序列序号,不可能撞
                    jumpStack.push("IF" + Integer.toString(i));//等待回填
                    break;
                case "el_sta":
                    result.add(new ASMSentence("jmp", "ELSE" + Integer.toString(i)));
                    result.add(new ASMSentence(jumpStack.pop() + ":"));//回填Label
                    jumpStack.push("ELSE" + Integer.toString(i));//等待回填
                    break;
                case "ifel_end":
                    result.add(new ASMSentence(jumpStack.pop() + ":"));
                    break;
                case "whl_do_ck":
                    judge_opd = toASMOprd(qt.getOperand_left(), "esi");
                    result.add(new ASMSentence("mov", "eax", judge_opd));
                    result.add(new ASMSentence("cmp", "eax", "0"));
                    result.add(new ASMSentence("jz", "while_end" + Integer.toString(i)));
                    jumpStack.push("while_end" + Integer.toString(i) + ":");
                    break;
                case "whl_sta":
                    result.add(new ASMSentence("while_start" + Integer.toString(i) + ":"));
                    jumpStack.push("while_start" + Integer.toString(i));
                    break;
                case "whl_end":
                    String temp = jumpStack.pop();
                    result.add(new ASMSentence("jmp", jumpStack.pop()));
                    result.add(new ASMSentence(temp));
                    break;
                case "ret":
                    List<ASMSentence> res = asmFunctionGenerater.generateFunctionReturn(qt);
                    result.addAll(res);
                    break;
                case "func_label":
                    result.add(new ASMSentence(qt.getOperand_left() + ":"));
                    //函数定义的时候添加标号
                    break;
                default:
                    //其他情况不处理，回退一个，留给下次处理
                    i--;
            }


        }

        //程序入口
        result.add(new ASMSentence("_start:"));
        //将SS和DS变成相同
        result.add(new ASMSentence("mov", "eax", "ds"));
        result.add(new ASMSentence("add", "eax", "4"));
        result.add(new ASMSentence("mov", "ss", "eax"));
        //调用main函数
        ArrayList<QT> call_main = new ArrayList<>();
        call_main.add(new QT("push_stack", "main", "_", "_"));
        call_main.add(new QT("call", "main", "_", "_"));
        List<ASMSentence> res = asmFunctionGenerater.generateFunctionCalling(call_main);
        result.addAll(res);
        result.add(new ASMSentence("mov", "eax", "1"));
        result.add(new ASMSentence("int", "0x80"));

        //数据段
        result.add(new ASMSentence("section", ".data"));
        result.add(new ASMSentence("trash", "DD 0"));
        return result;
    }


    /**
     * 根据给定的偏移寄存器
     * 将四元式中的表示转换为汇编指令中的运算数
     *
     * @param qt_form_opd 四元式形式
     * @param register    偏移寄存器
     * @return 汇编代码中的操作数
     * @throws SemanticException
     */
    public String toASMOprd(String qt_form_opd, String register) {

        String ASM_form_opd;
        //如果是常数的话 用立即数传参
        if (qt_form_opd.startsWith("const")) {
            ASM_form_opd = qt_form_opd.split(" ")[1];
            String type = ASM_form_opd.split("_")[0];
            String val = ASM_form_opd.split("_")[1];
            if (type.equals("int")) {
                ASM_form_opd = val;
            } else if (type.equals("char"))
                ASM_form_opd = "'" + val + "'";
            else
                ASM_form_opd = "__float32__(" + val + ")";
        } else {
            //反之则是从上层函数(eax)向当前函数栈层(esi)传参
            ASM_form_opd = "dword " + toAddress(register, symbolTableManager.lookUpVariableOffset(qt_form_opd));
        }
        return ASM_form_opd;
    }

    public String toAddress(String register, int offset) {
        return "[" + register + "+" + offset + "]";
    }

}
