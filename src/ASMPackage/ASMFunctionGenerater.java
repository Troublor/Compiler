package ASMPackage;

import MiddleDataUtilly.QT;
import TranslatorPackage.SymbolTable.SymbolTableManager;
import TranslatorPackage.TranslatorExceptions.SemanticException;

import java.util.*;

public class ASMFunctionGenerater {
    private SymbolTableManager symbolTableManager;


    private ASMGenerater asmGenerater;

    public ASMFunctionGenerater(SymbolTableManager symbolTableManager, ASMGenerater asmGenerater) {
        this.symbolTableManager = symbolTableManager;
        this.asmGenerater = asmGenerater;
    }


    /**
     * 产生函数调用时的汇编代码
     *
     * @param qts 调用时的四元式序列
     * @return 生成出来的汇编代码序列
     */
    public List<ASMSentence> generateFunctionCalling(List<QT> qts) {
        try {

            List<ASMSentence> asmSentences = new ArrayList<>();
            asmSentences.add(new ASMSentence("push", "esp"));
            asmSentences.add(new ASMSentence("push", "esi"));
            asmSentences.add(new ASMSentence("push", "$+4"));
            asmSentences.add(new ASMSentence("mov", "eax", "esi"));
            //暂存上层函数esi的值
            asmSentences.add(new ASMSentence("mov", "esi", "esp"));
            // 移动到新创建函数的运行栈区

            String func_name = qts.get(0).getOperand_left();
            int length = symbolTableManager.getCallingFuncStackLength(func_name);
            //获取运行栈大小

            //构建栈
            asmSentences.add(new ASMSentence("add", "esp", String.valueOf(length)));
            //传参

            for (int i = 1; qts.get(i).getOperator().equals("pass_param"); i++) {
                String real_param_qtform = qts.get(i).getOperand_left();
                String form_param_qtform = qts.get(i).getResult();


                String real_param_opd = asmGenerater.toASMOprd(real_param_qtform, "eax");

                int form_param_offset = symbolTableManager.lookUpVariableOffset(form_param_qtform);
                String form_param_opd = asmGenerater.toAddress("esi", form_param_offset);
                asmSentences.add(new ASMSentence("mov", "edx", real_param_opd));
                asmSentences.add(new ASMSentence("mov", form_param_opd, "edx"));
            }

            //在装载函数时 以函数名作为标号
            asmSentences.add(new ASMSentence("jmp", func_name));
            //跳转至函数执行位置


            String ret_var = symbolTableManager
                    .getCallingFuncVariableTableID(func_name) + ".#ret_val.double";
            int ret_val_offset = symbolTableManager.lookUpVariableOffset(ret_var);
            asmSentences.add(new ASMSentence(
                    "mov", asmGenerater.toAddress("esi", ret_val_offset), "bx"));
            //设定返回值



            return asmSentences;
        } catch (Exception ee) {
            ee.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    public List<ASMSentence> generateFunctionReturn(QT qt) throws SemanticException {
        List<ASMSentence> asmSentences = new ArrayList<>();
        //返回时的操作

        //保存函数返回值 如果有的话
        if (qt.getOperand_left() != null) {
            String ret_val = asmGenerater.toASMOprd(qt.getOperand_left(), "esi");
            asmSentences.add(new ASMSentence("mov", "ebx", ret_val));
        }

        asmSentences.add(new ASMSentence("mov", "esp", "esi"));
        //函数执行完毕 恢复栈的位置

        asmSentences.add(new ASMSentence("pop", "eax"));
        //取出上层函数指令运行到的地址

        asmSentences.add(new ASMSentence("pop", "esi"));
        asmSentences.add(new ASMSentence("pop", "esp"));
        //还原上层函数现场 esi esp标记的运行栈区

        asmSentences.add(new ASMSentence("jmp", "eax"));
        //跳转到上层函数的调用函数时的执行位置  继续执行原函数的内容

        return asmSentences;
    }



}
