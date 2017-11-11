package TranslatorPackage;

import OptimizePackage.QtException;

/**
 * Created by troub on 2017/10/23.
 */
public class QT {
    private String operator;
    private String operand_left;
    private String operand_right;
    private String result;

    QT(String o, String left, String right, String r) {
        operator = o;
        operand_left = left;
        operand_right = right;
        result = r;
    }

    public String getOperator() {
        return operator;
    }

    public String getOperand_left() {
        return operand_left;
    }

    public String getOperand_right() {
        return operand_right;
    }

    public String getResult() {
        return result;
    }

    public String toString(){
        return "(" + operator + ", " + operand_left + ", " + operand_right + ", " + result + ")";
    }

    /**
     * 判断是否是临时变量
     *
     * TODO
     * 根据临时变量符号表，写判断是否是临时变量的逻辑
     *
     * @param s 要判断的变量
     * @return boolean
     */
    public static boolean isTemporaryVariable(String s) {

        return false;
    }

    /**
     * 判断标号是不是常数
     *
     * @param label 标号
     * @return boolean
     */
    public static boolean isConstVariable(String label) throws QtException {
        String[] split = label.split(".");
        if (split[0].substring(0, 5).equals("const")) {
            if (split.length != 2) {
                throw new QtException("QtException: " + label + " is invalid const value");
            }
            return true;
        }
        return false;
    }
}
