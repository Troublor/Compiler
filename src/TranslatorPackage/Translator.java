package TranslatorPackage;

import TranslatorPackage.SymbolTable.SemanticException;
import TranslatorPackage.SymbolTable.SymbolTableManager;

import java.util.ArrayList;
import java.util.Stack;

public class Translator {
    Stack<String> semanticStack;

    SymbolTableManager symbolTableManager = new SymbolTableManager();

    ArrayList<QT> QTs = new ArrayList<QT>();

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

}