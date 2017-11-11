package TranslatorPackage;



import TranslatorPackage.SymbolTable.OptNotSupportError;

import TranslatorPackage.SymbolTable.SemanticException;
import TranslatorPackage.SymbolTable.SymbolTableManager;
import TranslatorPackage.SymbolTable.TypeError;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.ArrayList;
import java.util.Stack;

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

    // contrast with QT's content

    // QT's content:
    // for constant : 5 -> "const int.5.value"
    // for id : a -> "tableid.a.value"
    // for temp, same as id: tmp -> "-1.tmp.value"
    // for array, a[3] -> "tableid.a.3.value"
    // todo: struct support a.b -> "tableid.a.b.value"
    // todo: array support
    public void afterUnary() throws OptNotSupportError, SemanticException, TypeError{
        String operand = semanticStack.pop();
        String opt = semanticStack.pop();
        String type = symbolTableManager.lookupVariableType(operand);
        String return_type = getUnaryReturnType(opt, lookUpType(operand));
        // 生成临时量

        String tmp = symbolTableManager.addTempVariable(return_type);
        QTs.add(new QT(opt,toRepresent(operand), "_",
                toRepresent(tmp)));
        semanticStack.push(tmp);
    }



    // 目前生成四元式时操作数不进行类型转换
    // todo: array suport
    public void afterDual() throws SemanticException, TypeError{
        String left_operand = semanticStack.pop();
        String opt = semanticStack.pop();
        String right_operand = semanticStack.pop();
        String return_type = getDualReturnType(opt, lookUpType(left_operand), lookUpType(right_operand));
        String tmp = symbolTableManager.addTempVariable(return_type);
        QTs.add(new QT(opt, left_operand + ".value", right_operand + ".value", tmp));
        semanticStack.push(tmp);
    }

    public void afterArray() throws SemanticException, TypeError{
        String index_item = semanticStack.pop();
        String id_item = semanticStack.pop();
        // check id is array type
        String index_type = lookUpType(index_item);
        if (!isNumeric(index_type)) throw new TypeError(index_item, index_type, "numeric");
        semanticStack.push(id_item + "." + index_item);
    }

    public void afterStruct() throws TypeError, SemanticException{
        String field_item = semanticStack.pop();
        String id_item = semanticStack.pop();
        String struct_type = lookUpType(id_item);
        // assert struct has field named field_item
        symbolTableManager.lookupStructFieldType(struct_type, field_item);
        semanticStack.push(id_item + "." + field_item);
    }


    // todo: array support
    private String getDualReturnType(String opt, String left_type, String right_type) {
        HashMap<String, Integer> to_level = new HashMap<String, Integer>();
        to_level.put("char", 1); to_level.put("const char", 1);
        to_level.put("int", 2); to_level.put("const int", 2);
        to_level.put("double", 3); to_level.put("const double", 3);

        HashMap<Integer, String> to_type = new HashMap<Integer, String>();
        to_type.put(1, "char");
        to_type.put(2, "int");
        to_type.put(3, "double");

        // 返回类型不为 const_xx
        if (to_level.get(left_type) > to_level.get(right_type)) return to_type.get(to_level.get(left_type));
        return to_type.get(to_level.get(right_type));
    }


    private String getUnaryReturnType(String unaryOpt, String type) throws OptNotSupportError{
        // 基础类型都是数值类型，支持所有操作符
        if (isBasicType(type)) return type;
        throw new OptNotSupportError(type, unaryOpt);
    }



    private boolean isBasicType(String type) {
        return type.equals("int") ||
               type.equals("double") || type.equals("char");
    }

    private boolean isConstant(String type) {
        return type.equals("const int") ||
                type.equals("const double") ||
                type.equals("const char") ;
    }


    private boolean isNumeric(String type) {
        return isBasicType(type) || isConstant(type);
    }


    // for constant: "const int.1" -> "const int"
    // for id: "a" -> a's type
    // for array a: "a.3" -> type of a's element
    // for struct array: "a.3" -> Point
    //                  "a.3.X" -> int
    //                                  // "a.X.3" -> int
    private String lookUpType(String item) throws SemanticException , TypeError{
        String may_const = item.split(".")[0];
        // for constant
        if (isConstant(may_const)) return may_const;
        // not constant

        String []parts = item.split(".");
        String id = parts[0];
        String type = symbolTableManager.lookupVariableType(id);
        if (isBasicType(type) && parts.length == 1) return type;
//        else if (isBasicType(type)) throw new TypeError(id, type, "array");
        int i = 1;
        while (i < parts.length) {
            // array index
            if (isInteger(parts[i])) {
                if (type.startsWith("array")) type = type.split("_")[1];
                else throw new TypeError(id, type, "array");
            }
            // struct field
            else {
                type = symbolTableManager.lookupStructFieldType(parts[i]);
            }
        }
        return type;
    }

    private boolean isInteger(String s) {
        for (int i = 0; i < s.length(); ++i) {
            if (!Character.isDigit(s.charAt(i)))  return false;
        }
        return true;
    }


    // item: "a.3" "a.b" "const int.4"
    // "const int.5"-> "const int.5.value"
    // for id : a -> "tableid.a.value"
    // for temp, same as id: tmp -> "-1.tmp.value"
    // for array, a[3] -> "tableid.a.3.value"
    private String toRepresent(String item) throws SemanticException, TypeError {
        if (isConstant(lookUpType(item))) return item + ".value";
        // if is not constant
        // ( a is int id)  a -> tableid.a.int
        // (a is int array) a.3 -> tableid.a.3.int
        if (isBasicType(lookUpType(item))) return symbolTableManager.accessVariableAndField(item, "value");
        String []parts = item.split(".");
        String name = parts[0];
        int i = 1;
        while (i < parts.length) {
            name = symbolTableManager.accessVariableAndField(name, parts[i++]);
        }
        return name;
    }


    public void pushFlagDefineVariableStart() {
        semanticStack.push("flag_defineVarStart");
    }

    public void pushToDefineVariable(String variable_id) {
        semanticStack.push(variable_id);
    }

    public void chechTypeExist() throws SemanticException {
        String varType = semanticStack.peek();
        symbolTableManager.lookupType(varType);
    }

    public void defineArrayType() throws SemanticException {
        chechTypeExist();
        String array_elem_type = semanticStack.pop();
        String[] array_size_raw_format = semanticStack.pop().split(".");
        if (!array_size_raw_format[0].equals("const_int")) {
            throw new SemanticException("must use const int to define array length");
        }
        symbolTableManager.defineArrayType(array_elem_type, Integer.valueOf(array_size_raw_format[1]));
    }

    public void defineStashedVariables() throws SemanticException {
        String varType = semanticStack.pop();
        while (semanticStack.peek().equals("flag_defineVarStart")) {
            String toDefineVariableNameId = semanticStack.pop();
            symbolTableManager.defineVariable(toDefineVariableNameId, varType);
        }
    }

    public void addWhileStartQT() {
        symbolTableManager.stepIntoNewBlock();
        QTs.add(new QT("while_start", "", "", ""));
    }

    public void addWhileEndQT() {
        symbolTableManager.stepBackBlock();
        QTs.add(new QT("while_end", "", "", ""));
    }

    public void addIfStartQt() {
        symbolTableManager.stepBackBlock();
        QTs.add(new QT("if_start", "", "", ""));
    }

    public void StepOutBlock() {
        symbolTableManager.stepBackBlock();
    }

    public void addelseStartQt() {
        symbolTableManager.stepBackBlock();
        QTs.add(new QT("else_start", "", "", ""));
    }

    public void addIfElseEndQt() {
        symbolTableManager.stepBackBlock();
        QTs.add(new QT("if_else_end", "", "", ""));
    }



}
