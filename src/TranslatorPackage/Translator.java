package TranslatorPackage;


import TranslatorPackage.SymbolTable.OptNotSupportError;

import TranslatorPackage.SymbolTable.SemanticException;
import TranslatorPackage.SymbolTable.SymbolTableManager;
import TranslatorPackage.SymbolTable.TypeError;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Stack;


/**
 * 更新说明 : 因为是通过反射的invoke 调用方法
 * 出了exception没法正常输出错误信息
 * 为了便于debug
 * 故在这一层都加了try /catch 进行错误信息和 栈活动的打印
 */

public class Translator {
    Stack<String> semanticStack = new Stack<String>();
    ArrayList<QT> QTs = new ArrayList<QT>();
    SymbolTableManager symbolTableManager = new SymbolTableManager();


    // todo: push 需要parser为它传递上一次匹配的符号
    public void push(String name) {
        semanticStack.push(name);
    }


    // semantic stack's content
    // for constant: "const int.5"-> "const int.5.value"
    // for opt: "*" -> push "*"
    // for id: "a" -> "a"
    // for array a: "a[3]" -> "a.3"
    // todo: for struct type "a.b" -> "a.b"

    // todo : const xxx 生成四元式就不要加.value了
    // TODO : 赋值语句还没有四元式生成出来
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

    public void afterAssign()  throws SemanticException{
        String right = semanticStack.pop();
        String left = semanticStack.pop();

        if (!isNumeric(lookUpType(right)) || !isNumeric(lookUpType(left)))
            throw new SemanticException(lookUpType(left) + " can not assign to " + lookUpType(right));
        QTs.add(new QT("=", toRepresent(right), "_", toRepresent(left)));
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
            // todo item.split(".") 分开不开，怀疑 . 是正则的通配符
            String id  = item;
            if (parts.length !=  0) {
                id = parts[0];
            }
            String type = symbolTableManager.lookupVariableType(id);
            int i = 1;
            while (i < parts.length) {
                // array index
                if (type.startsWith("array") && (isInteger(parts[i]) ||
                        symbolTableManager.lookupVariableType(parts[i]).equals("int"))) {
                    type = type.split("_")[1];
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
                    String [] parts = item.split("\\.");
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


    public void pushFlagDefineVariableStart() {
        semanticStack.push("flag_defineVarStart");
    }

    public void pushToDefineVariable(String variable_id) {
        semanticStack.push(variable_id);
    }

    public void checkTypeExist() {
        try {
            String varType = semanticStack.peek();
            symbolTableManager.lookupType(varType);
        } catch (Exception ee) {
            printErrLog(ee);
            System.exit(-1);
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
        QTs.add(new QT("while_start", "", "", ""));
    }

    public void addWhileEndQT() {
        QTs.add(new QT("while_end", "", "", ""));
    }

    public void addIfStartQt() {
        QTs.add(new QT("if_start", "", "", ""));
    }

    public void addElseStartQt() {
        QTs.add(new QT("else_start", "", "", ""));
    }

    public void addIfElseEndQt() {
        QTs.add(new QT("if_else_end", "", "", ""));
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
    }


    public void printAllQTs() {

        System.out.println("\n\n当前所有的四元式:");
        for (QT qt : QTs) {
            System.out.println(qt);
        }
    }


    // pop 目前只是为了解决函数定义返回值一直在语义栈里的问题
    public void POP() {
        semanticStack.pop();
    }

}
