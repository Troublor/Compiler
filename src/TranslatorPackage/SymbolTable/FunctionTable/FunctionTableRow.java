package TranslatorPackage.SymbolTable.FunctionTable;

import TranslatorPackage.SymbolTable.TypeTable.TypeTableRow;
import TranslatorPackage.SymbolTable.VariableTable.VariableTable;
import TranslatorPackage.SymbolTable.VariableTable.VariableTableRow;
import TranslatorPackage.TranslatorExceptions.SemanticException;

import java.util.ArrayList;
import java.util.List;

public class FunctionTableRow {
    private String func_name, return_type;
    //函数名和返回值

    private List<VariableTableRow> param_list = new ArrayList<>();
    //参数列表 key为参数名 value是参数在参数局部变量表里的VariableTableRow的引用

    private VariableTable param_n_inner_var_table;
    //参数&局部变量表 由VariableTableManager分配 这里保存引用供查找

    private int entry_qt_index;

    public FunctionTableRow(VariableTable param_table, String func_name, String return_type, int entry_qt_index) {
        param_n_inner_var_table = param_table;
        this.func_name = func_name;
        this.return_type = return_type;
        this.entry_qt_index = entry_qt_index;
    }

    public void addParam(String param_name, TypeTableRow type_info) throws SemanticException {
        VariableTableRow new_param =
                param_n_inner_var_table.addVariable(param_name, type_info.getName(), type_info.getLength());
        param_list.add(new_param);
    }

    public String getFuncName() {
        return func_name;
    }

    public String getReturnType() {
        return return_type;
    }

    public int getEntryQtIndex() {
        return entry_qt_index;
    }

    public List<VariableTableRow> getParamList() {
        return param_list;
    }
}

