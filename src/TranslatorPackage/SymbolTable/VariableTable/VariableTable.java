package TranslatorPackage.SymbolTable.VariableTable;

import java.util.HashMap;

public class VariableTable{
    private VariableTable parent;
    private HashMap<String,VariableTableRow> variables;
    private int startOffset,currVarOffset;
    private int table_id;

    public VariableTable(VariableTable parent,int id,int startOffset){
        variables = new HashMap<>();
        this.parent = parent;
        this.currVarOffset = this.startOffset = startOffset;
        this.table_id = id;
    }


    //对符号表添加变量 如果没有重复return true
    public boolean addVariable(String type,String name_id, int offset){
        if (variables.get(name_id)!= null)
            return false;
        variables.put(name_id,new VariableTableRow(name_id,type,startOffset));
        currVarOffset += offset;
        return true;
    }

    public VariableTableRow getVariable(String name_id)
    {
            return  variables.get(name_id);
    }

    public int getTableOffset(){
        return startOffset - currVarOffset;
    }


}
