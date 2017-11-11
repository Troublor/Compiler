package TranslatorPackage;

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
}
