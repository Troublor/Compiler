package ASMPackage;

import MiddleDataUtilly.QT;
import TranslatorPackage.SymbolTable.SymbolTableManager;

import java.util.*;

public class ASMFuncCallingGenerater {
    private SymbolTableManager symbolTableManager;

    private Map<String, Integer> func_start_map = new HashMap<>();

    public ASMFuncCallingGenerater(SymbolTableManager symbolTableManager) {
        this.symbolTableManager = symbolTableManager;
    }


    public void addFuncStartLabel(int start_index, String func_name) {
        func_start_map.put(func_name, start_index);
    }

    public List<ASMSentence> startGenerate(List<QT> qts) {
        try {
            List<ASMSentence> asmSentences = new ArrayList<>();
            asmSentences.add(new ASMSentence("push", "sp"));
            asmSentences.add(new ASMSentence("push", "es"));
            asmSentences.add(new ASMSentence("push", "ip"));
            asmSentences.add(new ASMSentence("mov", "ax", "es"));
            asmSentences.add(new ASMSentence("mov", "es", "id"));
            String func_name = qts.get(0).getOperand_left();
            int length = symbolTableManager.getCallingFuncStackLength(func_name);
            //获取运行栈大小
            //构建栈
            asmSentences.add(new ASMSentence("add", "sp", String.valueOf(length)));
            //传参
            for (int i = 1; qts.get(i).getOperator().equals("pass_param"); i++) {
                String real_param_qtform = qts.get(i).getOperand_left();
                String form_param_qtform = qts.get(i).getResult();

                int form_param_offset = symbolTableManager.lookUpVariableOffset(form_param_qtform);
                String real_param_val;
                if (real_param_qtform.startsWith("const")) {
                    real_param_val = real_param_qtform.split(" ")[1];
                    String type = real_param_val.split("_")[0];
                    String val = real_param_val.split("_")[1];
                    if (type.equals("int")) {
                        real_param_val = val;
                    } else if (type.equals("char"))
                        real_param_val = "'" + val + "'";
                    else
                        real_param_val = "__float__" + val;
                } else {
                    real_param_val = "ax" + symbolTableManager.lookUpVariableOffset(real_param_qtform);
                }

                asmSentences.add(new ASMSentence("mov", "es+" + form_param_offset, real_param_val));
            }

            String func_start_lable = func_start_map.get(func_name).toString();
            asmSentences.add(new ASMSentence("jmp", func_start_lable));

            asmSentences.add(new ASMSentence("mov", "sp", "es"));
            asmSentences.add(new ASMSentence("pop", "ax"));
            String ret_val = symbolTableManager
                    .getCallingFuncVariableTableID(func_name) + ".#ret_val.value.double";
            int ret_val_offset = symbolTableManager.lookUpVariableOffset(ret_val);
            asmSentences.add(new ASMSentence("mov", "es+" + ret_val_offset, "bx"));
            asmSentences.add(new ASMSentence("pop", "es"));
            asmSentences.add(new ASMSentence("pop", "sp"));
            asmSentences.add(new ASMSentence("jmp", "ax"));


            return asmSentences;
        } catch (Exception ee) {
            ee.printStackTrace();
            System.exit(-1);
        }
        return null;
    }
}
