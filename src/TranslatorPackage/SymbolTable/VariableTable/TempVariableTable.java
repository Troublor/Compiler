package TranslatorPackage.SymbolTable.VariableTable;

import TranslatorPackage.SymbolTable.SemanticException;
import TranslatorPackage.SymbolTable.SymbolTableManager;

public class TempVariableTable extends VariableTable {
    private int tmpCount;

    public TempVariableTable() {
        super(null, SymbolTableManager.TEMP_VAR_TABLE_ID, 0);
        //startOffset编译时无效 后来再语义分析完之后根据变量表的偏移
        // 直接续在它的后面

        tmpCount = 0;
    }


    //对原来的接口重新封装了 只需提供类型就够了
    public TempVariableTableRow addTempVariable(String type_name) throws SemanticException {
        return (TempVariableTableRow)
                super
                        .addVariable(type_name, "$t" + tmpCount, getTableOffset(), SymbolTableManager.TEMP_VAR_TABLE_ID);
    }

    public void setStartOffset(int offset) {
        super.setStartOffset(offset);
    }

}
