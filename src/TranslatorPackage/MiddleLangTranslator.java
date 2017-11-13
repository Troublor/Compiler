package TranslatorPackage;


import MiddleDataUtilly.QT;
import TranslatorPackage.SymbolTable.VariableTable.VariableTable;
import TranslatorPackage.SymbolTable.VariableTable.VariableTableRow;
import TranslatorPackage.TranslatorExceptions.OptNotSupportError;

import TranslatorPackage.TranslatorExceptions.SemanticException;
import TranslatorPackage.SymbolTable.SymbolTableManager;
import TranslatorPackage.TranslatorExceptions.TypeError;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Stack;


/**
 * 更新说明 : 因为是通过反射的invoke 调用方法
 * 出了exception没法正常输出错误信息
 * 为了便于debug
 * 故在这一层都加了try /catch 进行错误信息和 栈活动的打印
 */

public class MiddleLangTranslator {
    private Stack<String> semanticStack = new Stack<String>();
    private ArrayList<QT> QTs = new ArrayList<QT>();
    private SymbolTableManager symbolTableManager = new SymbolTableManager();


    public void push(String name) {
        semanticStack.push(name);
    }


    // semantic stack's content
    // for constant: "const int.5"-> "const int.5.value"
    // for opt: "*" -> push "*"
    // for id: "a" -> "a"
    // for array a: "a[3]" -> "a.3"
    // todo: for struct type "a.b" -> "a.b"

    // contrast with QT's content

    // QT's content:
    // for constant : 5 -> "const int.5.value"
    // for id : a -> "tableid.a.value"
    // for temp, same as id: tmp -> "-1.tmp.value"
    // for array, a[3] -> "tableid.a.3.value"
    // todo: struct support a.b -> "tableid.a.b.value"
    // todo: array support
    public void afterUnary() {
        try {
            String operand = semanticStack.pop();
            String opt = semanticStack.pop();
            String type = symbolTableManager.lookupVariableType(operand);
            String return_type = getUnaryReturnType(opt, lookUpType(operand));
            // 生成临时量

            String tmp = symbolTableManager.addTempVariable(return_type);
            QTs.add(new QT(opt, toRepresent(operand), "_",
                    toRepresent(tmp)));
            semanticStack.push(tmp);
        } catch (Exception ee) {
            printErrLog(ee);
            System.exit(-1);
        }
    }


    // 目前生成四元式时操作数不进行类型转换
    // todo: array suport
    public void afterDual() {
        try {
            String left_operand = semanticStack.pop();
            String opt = semanticStack.pop();
            String right_operand = semanticStack.pop();
            String return_type = getDualReturnType(opt, lookUpType(left_operand), lookUpType(right_operand));
            String tmp = symbolTableManager.addTempVariable(return_type);
            QTs.add(new QT(opt, toRepresent(left_operand), toRepresent(right_operand), toRepresent(tmp)));
            semanticStack.push(tmp);
        } catch (Exception ee) {
            printErrLog(ee);
            System.exit(-1);
        }
    }

    public void afterArray() {
        try {
            String index_item = semanticStack.pop();
            String id_item = semanticStack.pop();
            // check id is array type
            String index_type = lookUpType(index_item);
            if (isConstant(index_type)) {
                index_item = index_item.split("_")[1];
            }
            if (index_item.contains(".")) {
                // 对 a[a[10]]这种情况，本来会生成a.a.10, 现在先生成 t = a.10,  然后生成a.t，防止多层嵌套
                String tmp = symbolTableManager.addTempVariable(index_type);
                QTs.add(new QT("=", index_item, "_", tmp));
                index_item = tmp;
            }
            // todo:如果是下标是变量的话 例子：b[a[3]]  b[a]
            else if (!isNumeric(index_type)) throw new TypeError(index_item, index_type, "numeric");
            semanticStack.push(id_item + "." + index_item);

        } catch (Exception ee) {
            printErrLog(ee);
            System.exit(-1);
        }
    }

    public void afterIndexOpt() {

    }

    public void afterStruct() {
        try {
            String field_item = semanticStack.pop();
            String id_item = semanticStack.pop();
            String struct_type = lookUpType(id_item);
            // assert struct has field named field_item
            symbolTableManager.lookupStructFieldType(struct_type, field_item);
            semanticStack.push(id_item + "." + field_item);

        } catch (Exception ee) {
            printErrLog(ee);
            System.exit(-1);
        }
    }

    public void afterAssign() {
        try {
            String right = semanticStack.pop();
            String left = semanticStack.pop();

            if (!isNumeric(lookUpType(right)) || !isNumeric(lookUpType(left)))
                throw new SemanticException(lookUpType(left) + " can not assign to " + lookUpType(right));
            QTs.add(new QT("=", toRepresent(right), "_", toRepresent(left)));
        } catch (Exception ee) {
            printErrLog(ee);
            System.exit(-1);
        }
    }

    // todo: array support
    private String getDualReturnType(String opt, String left_type, String right_type) {
        HashMap<String, Integer> to_level = new HashMap<String, Integer>();
        to_level.put("char", 1);
        to_level.put("const char", 1);
        to_level.put("int", 2);
        to_level.put("const int", 2);
        to_level.put("double", 3);
        to_level.put("const double", 3);

        HashMap<Integer, String> to_type = new HashMap<Integer, String>();
        to_type.put(1, "char");
        to_type.put(2, "int");
        to_type.put(3, "double");

        // 返回类型不为 const_xx
        if (to_level.get(left_type) > to_level.get(right_type))
            return to_type.get(to_level.get(left_type));

        return to_type.get(to_level.get(right_type));
    }


    private String getUnaryReturnType(String unaryOpt, String type) {
        try {
            // 基础类型都是数值类型，支持所有操作符
            if (isBasicType(type))
                return type;
            else
                throw new OptNotSupportError(type, unaryOpt);

        } catch (Exception ee) {
            printErrLog(ee);
            System.exit(-1);
        }
        System.out.println(" getUnaryReturnType 返回了个null");
        return null;
        //执行不到这
    }


    private boolean isBasicType(String type) {
        return type.equals("int") ||
                type.equals("double") || type.equals("char");
    }

    private boolean isConstant(String type) {
        return type.equals("const int") ||
                type.equals("const double") ||
                type.equals("const char");
    }


    private boolean isNumeric(String type) {
        return isBasicType(type) || isConstant(type);
    }


    // for constant: "const int_1" -> "const int"
    // for id: "a" -> a's type
    // for array a: "a.3" -> type of a's element
    //               "a" -> "array_xx_length"
    //                 "a.x" x is numeric
    // for struct array: "a.3" -> Point
    //                  "a.3.X" -> int
    // for struct contain array: "a.X.3" -> int
    private String lookUpType(String item) {
        try {
            String may_const = item.split("_")[0];
            // for constant
            if (isConstant(may_const)) return may_const;
            // not constant

            String[] parts = item.split("\\.");

            // . 是正则的通配符  得用 \\. 进行转义

            String id = item;
            if (parts.length != 0) {
                id = parts[0];
            }
            String type = symbolTableManager.lookupVariableType(id);
            int i = 1;
            while (i < parts.length) {
                // array index
                if (type.startsWith("array")) {
                    if (isInteger(parts[i]) || symbolTableManager.lookupVariableType(parts[i]).equals("int")) {
                        type = type.split("_")[1];
                    } else throw new SemanticException("index should be int variable or const int");
                }
                // struct field
                else {
                    type = symbolTableManager.lookupStructFieldType(type, parts[i]);
                }
                i++;
            }
            return type;
        } catch (Exception ee) {
            printErrLog(ee);
            System.exit(-1);
        }
        System.out.println("方法 lookUpType 因为错误返回了个null");
        return null;
        //执行不到这

    }

    private boolean isInteger(String s) {
        for (int i = 0; i < s.length(); ++i) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }


    // item: "a.3" "a.b" "const int.4"
    // "const int.5"-> "const int.5.value"
    // for id : a -> "tableid.a.value"
    // for temp, same as id: tmp -> "-1.tmp.value"
    // for array, a[3] -> "tableid.a.3.value"
    private String toRepresent(String item) {
        try {
            if (isConstant(lookUpType(item))) return item;
            // if is not constant
            // ( a is int id)  a -> tableid.a.int
            // (a is int array) a.3 -> tableid.a.3.int
            if (isBasicType(lookUpType(item))) {
                // is not int, char, double variable, is array element or struct like a.0
                if (item.split("\\.").length != 1) {
                    String[] parts = item.split("\\.");
                    assert item.split("\\.").length == 2;
                    return symbolTableManager.accessVariableAndField(parts[0], parts[1]);
                }
                return symbolTableManager.accessVariableAndField(item, "value");
            }
            String[] parts = item.split(".");
            String name = parts[0];
            int i = 1;
            while (i < parts.length) {
                name = symbolTableManager.accessVariableAndField(name, parts[i++]);
            }
            return name;
        } catch (Exception ee) {
            printErrLog(ee);
            System.exit(-1);
        }
        System.out.println("方法: toRepresent 因为错误返回了个null");
        return null;
        //实际执行不到这

    }

    // ========================定义函数=======================
    private String curr_define_func_name, curr_define_func_ret_type;
    //              要定义的函数名         该函数的返回值
    private boolean is_curr_func_has_ret;
    //              这个函数是否需要返回值 void?


    //压栈顺序 函数返回值类型 , 函数名
    public void preDefineFuncName() throws SemanticException {
        try {
            String func_name = semanticStack.pop();
            String func_type = semanticStack.pop();
            symbolTableManager.definefunction(func_name, func_type, QTs.size());
            curr_define_func_name = func_name;
            if (func_type.equals("void"))
                is_curr_func_has_ret = true;
            curr_define_func_ret_type = func_type;
        } catch (Exception ee) {
            printErrLog(ee);
        }
    }

    public void pushFunctionDefineParamsStart() {
        semanticStack.push("flag_func_define_start");
    }


    public void pushArrayTypeParam() {
        semanticStack.push("flag_array_type_param");
    }

    /**
     * pop 顺序 param_name, (array_type_flag ,) param_type_name
     */
    public void defineStashedParams() {
        try {
            String param_name, param_type;
            while (!semanticStack.peek().equals("flag_func_define_start")) {
                semanticStack.pop();
                param_name = semanticStack.pop();
                if (semanticStack.peek().equals("flag_array_type_param")) {
                    semanticStack.pop();
                    param_type = "array_" + semanticStack.pop() + ".";
                } else {
                    param_type = semanticStack.pop();
                }
                symbolTableManager.addParamOnfunc(curr_define_func_name, param_name, param_type);
            }
            semanticStack.pop();
        } catch (Exception ee) {
            printErrLog(ee);
        }
    }

    public void clearCurrDefineFunc() {
        try {
            if (!is_curr_func_has_ret) {
                String err_log = String.format("func: %s doesn't give valid return value, expect return a %s "
                        , curr_define_func_name, curr_define_func_ret_type);
                throw new SemanticException(err_log);
            } else {
                curr_define_func_name = null;
                curr_define_func_ret_type = null;
            }
        } catch (Exception ee) {
            printErrLog(ee);
        }
    }


    public void pushMayRetValFlag() {
        semanticStack.push("flag_may_ret_val");
    }

    public void pushEmptyRetValFlag() {
        if (semanticStack.peek().equals("flag_may_ret_val")) {
            semanticStack.pop();
            semanticStack.push("flag_empty_ret_val");
        }

    }


    // todo 要修改
    public void reciveReturnVal() {
        try {
            if (semanticStack.peek().equals("flag_empty_ret_val")) {
                if (curr_define_func_ret_type.equals("void")) {
                    semanticStack.pop();
                }
            } else {
                //如果返回值是一个算式 可以通过最后一条四元式获取返回值
                QT last_qt = QTs.get(QTs.size() - 1);
                String ret_val = last_qt.getResult();
                is_curr_func_has_ret = true;
                QTs.add(new QT("ret", ret_val, "_", "_"));
            }
        } catch (Exception ee) {
            printErrLog(ee);
        }
    }
    //  ====================调用函数=============================


    /*
       I(函数名) ,_AC_push ,_AC_缓存函数名 pop
      ( 参数表 -->  每个参数 _ac_push  )  _AC_取函数信息 ,检查参数列表
       构造传参四元式 (跨表赋值)  call 函数入口标号 四元式
     */

    private String calling_func_name;

    public void receiveCallingFuncName() {
        try {
            calling_func_name = semanticStack.pop();
            symbolTableManager.checkFuncName(calling_func_name);
        } catch (Exception ee) {
            printErrLog(ee);
        }
    }

    public void funcParamStartFlag() {
        semanticStack.push("flag_start_trans_params");
    }

    public void startFuncCalling() {
        try {
            List<String> param_type_list = new ArrayList<>();
            List<VariableTableRow> real_vars = new ArrayList<>();
            while (!semanticStack.peek().equals("flag_start_trans_params")) {
                String curr_real_param = semanticStack.pop();
                //实参
                param_type_list.add("");
                if (symbolTableManager.isBasicType("")) {
                    symbolTableManager.accessVariableAndField("", "value");
                } else {
                }
            }
            semanticStack.pop();
            List<String> params = symbolTableManager.checkFuncParams(calling_func_name, param_type_list);
        } catch (Exception ee) {
            printErrLog(ee);
        }

    }
    //  =================定义变量=================================

    public void pushFlagDefineVariableStart() {
        semanticStack.push("flag_defineVarStart");
    }

    public void checkTypeExist() {
        try {
            String varType = semanticStack.peek();
            symbolTableManager.lookupType(varType);
        } catch (Exception ee) {
            printErrLog(ee);
        }
    }


    public void defineArrayType() {
        try {
            checkTypeExist();
            String array_elem_type = semanticStack.pop();
            String[] array_size_raw_format = semanticStack.pop().split("_");
            if (!array_size_raw_format[0].equals("const int")) {
                throw new SemanticException("must use const int to define array length");
            }
            //reformat

            int array_length = Integer.valueOf(array_size_raw_format[1]);
            semanticStack.push(symbolTableManager.defineArrayType(array_elem_type, array_length));
        } catch (Exception ee) {
            printErrLog(ee);
        }

    }

    public void defineStashedVariables() {
        try {
            String varType = semanticStack.pop();
            while (!semanticStack.peek().equals("flag_defineVarStart")) {
                String toDefineVariableNameId = semanticStack.pop();
                symbolTableManager.defineVariable(toDefineVariableNameId, varType);
            }
            semanticStack.pop();
        } catch (SemanticException ee) {
            printErrLog(ee);

        }

    }


    public void addWhileStartQT() {
        QTs.add(new QT("whl_sta", "_", "_", "_"));
    }

    public void addWhileEndQT() {
        QTs.add(new QT("whl_end", "_", "_", "_"));
    }

    public void checkWhileDo() {
        try {
            String judge_condition_val = semanticStack.pop();
            judge_condition_val = symbolTableManager.accessVariableAndField(judge_condition_val, "value");
            QTs.add(new QT("whl_do_ck", judge_condition_val, "_", "_"));
        } catch (Exception ee) {
            printErrLog(ee);
        }
    }

    public void addIfStartQt() {
        QTs.add(new QT("if_sta", "_", "_", "_"));
    }

    public void addElseStartQt() {
        QTs.add(new QT("el_sta", "_", "_", "_"));
    }

    public void addIfElseEndQt() {
        QTs.add(new QT("ifel_end", "_", "_", "_"));
    }

    public void stepOutBlock() {
        symbolTableManager.stepBackBlock();
    }

    public void stepIntoBlock() {
        symbolTableManager.stepIntoNewBlock();
    }

    private void printErrLog(Exception ee) {
        ee.printStackTrace();
        System.out.println("exception occurred: " + ee);
        System.exit(-1);
    }


    public void printAllQTs() {

        System.out.println("\n\n当前所有的四元式:");
        System.out.println(String.format("%-11s%-25s%-25s%-25s", "oprt:", "left_oprd:", "right_oprd:", "result_target:"));
        for (QT qt : QTs) {
            System.out.println(qt);
        }
    }

    public ArrayList<QT> getQTs() {
        return QTs;
    }

    // pop 目前只是为了解决函数定义返回值一直在语义栈里的问题
    public void POP() {
        semanticStack.pop();
    }

}
