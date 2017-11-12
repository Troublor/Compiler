package TranslatorPackage.SymbolTable.FunctionTable;

import TranslatorPackage.SymbolTable.VariableTable.VariableTable;
import TranslatorPackage.SymbolTable.VariableTable.VariableTableRow;

import java.util.HashMap;
import java.util.Map;

public class FunctionTableRow {
    private String func_name, return_type;
    //函数名和返回值

    private Map<String, VariableTableRow> param_map = new HashMap<>();
    //参数列表 key为参数名 value是参数在参数局部变量表里的VariableTableRow的引用

    private VariableTable param_n_inner_var_table;
    //参数&局部变量表 由VariableTableManager分配 这里保存引用供查找

    public FunctionTableRow(VariableTable param_table) {
        param_n_inner_var_table = param_table;
    }


}
