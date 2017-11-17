package ASMPackage;

import java.util.ArrayList;
import java.util.Arrays;

public class ASMSentence {

    /**
     * 操作助记符
     */
    private String operator;

    /**
     * 操作数序列
     */
    private ArrayList<String> operands = new ArrayList<>();

    /**
     * 汇编语句构造方法
     * @param operator 操作助记符
     * @param operands 操作数，可变参数列表
     */
    public ASMSentence(String operator, String... operands) {
        this.operator = operator;
        this.operands.addAll(Arrays.asList(operands));
    }

    public String getOperator() {
        return operator;
    }

    public ArrayList<String> getOperands() {
        return operands;
    }

    @Override
    public String toString() {
        switch (operands.size()) {
            case 0:
                return operator;
            case 1:
                return operator + " " + operands.get(0);
            case 2:
                return operator + " " + operands.get(0) + "," + operands.get(1);
            default:
                return operator + operands.toString();
        }
    }
}
