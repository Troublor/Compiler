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

    QT(String o, String left, String right, String result) {
        operator = o;
        operand_left = left;
        operand_right = right;
        this.result = result;
    }

    public String toString(){
        return "(optr :" + operator + ",opnd_l: " + operand_left + " ,opnd_r: " + operand_right + ",res: " + result + ")";
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
