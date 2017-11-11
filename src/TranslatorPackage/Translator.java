package TranslatorPackage;

import TranslatorPackage.SymbolTable.OptNotSupportError;
import TranslatorPackage.SymbolTable.SemanticException;
import TranslatorPackage.SymbolTable.SymbolTableManager;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.Stack;

public class Translator {
    Stack<String> semanticStack;
    ArrayList<QT> QTs;

    SymbolTableManager symbolTableManager = new SymbolTableManager();

    int i = 0;



    // todo: push 需要parser为它传递上一次匹配的符号
    public void push(String name) {
        semanticStack.push(name);
    }

    public void afterUnary() throws OptNotSupportError, SemanticException{
        String operand = semanticStack.pop();
        String opt = semanticStack.pop();
        String type = symbolTableManager.lookupVariableType(operand);
        String return_type = getUnaryReturnType(type, opt);
        // 生成临时量
        String tmp = "t" + i++;
        symbolTableManager.defineVariable(tmp, return_type);
        QTs.add(new QT(opt, operand, "_", tmp));
        semanticStack.push(tmp);
    }



    // 目前生成四元式时操作数不进行类型转换
    public void afterDual() throws SemanticException{
        String left_operand = semanticStack.pop();
        String opt = semanticStack.pop();
        String right_operand = semanticStack.pop();
        String return_type = getDualReturnType(opt, left_operand, right_operand);
        String tmp = "t" + i++;
        symbolTableManager.defineVariable(tmp, return_type);
        QTs.add(new QT(opt, left_operand, right_operand, tmp));
        semanticStack.push(tmp);
    }



    private String getDualReturnType(String opt, String left_type, String right_type) {
        HashMap<String, Integer> to_level = new HashMap<String, Integer>();
        to_level.put("char", 1); to_level.put("constant_char", 1);
        to_level.put("int", 2); to_level.put("constant_int", 2);
        to_level.put("double", 3); to_level.put("constant_double", 3);

        HashMap<Integer, String> to_type = new HashMap<Integer, String>();
        to_type.put(1, "char");
        to_type.put(2, "int");
        to_type.put(3, "double");

        // 返回类型不为 constant_xx
        if (to_level.get(left_type) > to_level.get(right_type)) return to_type.get(to_level.get(left_type));
        return to_type.get(to_level.get(right_type));
    }


    private String getUnaryReturnType(String unaryOpt, String type) throws OptNotSupportError{
        // 基础类型都是数值类型，支持取反和取负
        // todo: 对char进行这些操作没有意义
        if (isBasicType(type)) return type;
        throw new OptNotSupportError(type, unaryOpt);
    }



    // 还不清楚叫什么名
    private boolean isBasicType(String type) {
        return type.equals("constant_int") || type.equals("int") ||
                type.equals("constant_double") | type.equals("double") ||
                type.equals("const_char") || type.equals("char");
    }


}

