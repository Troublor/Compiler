package TranslatorPackage.SymbolTable;

import TranslatorPackage.SymbolTable.TypeTable.TypeTable;
import TranslatorPackage.SymbolTable.TypeTable.TypeTableRow;
import TranslatorPackage.SymbolTable.VariableTable.VariableTableRow;
import TranslatorPackage.SymbolTable.VariableTable.VariableTableSetManager;

public class SymbolTableManager {
    TypeTable typeTable = new TypeTable();
    VariableTableSetManager variableTableSetManager = new VariableTableSetManager(typeTable);


    //声明结构体类型 目前用不上 hhh'
    public String declareStructType(String type_name) throws SemanticExcption {
        return typeTable.declareType(type_name);
    }

    //定义结构体里的域
    public String defineFieldOnStructType(String type_name, String field_type, String field_name) throws SemanticExcption {
        return typeTable.addFieldOnType(type_name, field_type, field_name);
    }


    //定义变量 定义失败(重复 类型未定义)会抛异常
    public void defineVariable(String name_id, String type) throws SemanticExcption {
        variableTableSetManager.addVariable(name_id, type);
    }

    // 定义数组类型 返回数组类型的格式化的类型名字符串: array_类型名_长度
    public String defineArrayType(String arr_elem_type_name, int array_len) throws SemanticExcption {
        return typeTable.declareArrayType(arr_elem_type_name, array_len);
    }

    //访问变量的域  在生成求值的四元式需要用到
    //返回也是一个格式化的字符串: 所在符号表id.变量名.域名
    //为了四元式可读性 这个格式化串 在后面目标代码生成的时候再转为偏移量
    // 要是访问出错也是报异常
    public String accessVariableAndField(String name_id, String field_name) throws SemanticExcption {
        VariableTableRow result = variableTableSetManager.requestVariable(name_id);
        if (result == null)
            throw new SemanticExcption("variable: " + name_id + "has not define");
        TypeTableRow typeInfo = typeTable.getTypeInfo(lookupVariableType(name_id));
        typeInfo.getField(field_name);
        return result.getTable_id() + "." + result.getName_id() + "." + field_name;
    }


    //查询一个变量的类型 用于表达式求值时进行类型判断
    public String lookupVariableType(String name_id) {
        VariableTableRow result = variableTableSetManager.requestVariable(name_id);
        return result.getType();
    }


    //进入一个新的块 时需要创建一个新的变量表
    public void stepIntoNewBlock() {
        variableTableSetManager.addTable();
    }

    public void stepBackBlock() {
        variableTableSetManager.traceBackToParentBlock();
    }





    /*
            目标代码生成部分
     */


    public String lookUpVariableOffset(String QT_identifier) throws SemanticExcption {

        return "";

    }










}

