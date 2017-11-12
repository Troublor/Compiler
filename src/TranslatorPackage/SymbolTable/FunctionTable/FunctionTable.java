package TranslatorPackage.SymbolTable.FunctionTable;

import TranslatorPackage.SymbolTable.TypeTable.FieldTableRow;
import TranslatorPackage.TranslatorExceptions.SemanticException;

import java.util.HashMap;
import java.util.Map;

public class FunctionTable {

    private Map<String, FunctionTableRow> functions_map = new HashMap<>();
    //函数表

    public FunctionTableRow getFuntionInfo(String func_name) throws SemanticException {
        FunctionTableRow res = functions_map.get(func_name);
        if (res == null)
            throw new SemanticException("no such function: " + func_name);
        return res;
    }
}
