package MiddleDataUtilly;

import OptimizePackage.QtException;

/**
 * Created by troub on 2017/10/23.
 */
public class QT {
    private String operator;
    private String operand_left;
    private String operand_right;
    private String result;

    public QT(String o, String left, String right, String result) {
        operator = o;
        operand_left = left;
        operand_right = right;
        this.result = result;
    }

    public String toString(){
        String str = String.format("%-11s%-25s%-25s%-25s", operator, operand_left, operand_right, result);
        return str;
    }

    public String getOperator() {
        return operator;
    }

    public String getOperand_left() {
        return operand_left;
    }

    public void setOperand_left(String operand_left) {
        this.operand_left = operand_left;
    }

    public String getOperand_right() {
        return operand_right;
    }

    public void setOperand_right(String operand_right) {
        this.operand_right = operand_right;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    /**
     * 判断是否是临时变量
     *
     * @param s 要判断的变量
     * @return boolean
     */
    public static boolean isTemporaryVariable(String s) {
        return s.split("\\.")[1].charAt(0) == '$';
    }

    /**
     * 判断标号是不是常数
     *
     * @param label 标号
     * @return boolean
     */
    public static boolean isConstVariable(String label) {
        String[] split = label.split(" ");
        return split.length == 2 && split[0].equals("const");
    }
}
