package TranslatorPackage.SymbolTable.VariableTable;

import TranslatorPackage.SymbolTable.TypeTable.TypeTable;

import java.util.Map;

public class VariableTableSetManager {
    private VariableTable root,currActiveTable;
    private TypeTable typeTable;
    private int offsetTotal;

    private int idTotal;
    private Map<Integer,VariableTable>  idVariableTableMap;

    public VariableTableSetManager(){
        typeTable = new TypeTable();
        offsetTotal = 0;
        idTotal = 0;
        root = new VariableTable(null,idTotal,offsetTotal);
        currActiveTable = root;
        idVariableTableMap.put(idTotal,currActiveTable);
        idTotal ++;
    }


    public boolean requestVariable(String name_id){
        //todo 查符号表？
        return true;
    }

    public void addTable(){
        offsetTotal += currActiveTable.getTableOffset();
        currActiveTable = new VariableTable(currActiveTable,idTotal,offsetTotal);
        idVariableTableMap.put(idTotal,currActiveTable);
        idTotal ++;
    }





}
