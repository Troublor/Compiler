package TranslatorPackage;


/**
 *
 * Created by troub on 2017/10/23.
 */
public class QT {
    private String operator;
    private String operand_left;
    private String operand_right;
    private String result;



    // result 的设置放在QT外，因为result 的类型是由两个操作数来决定的，暂时决定交给动作函数来做,
    QT(String o, String left, String right, String result) {
        operator = o;
        operand_left = left;
        operand_right = right;
        this.result = result;
    }

    public String toString(){
        return "(optr :" + operator + ",opnd_l: " + operand_left + " ,opnd_r: " + operand_right + ",res: " + result + ")";
    }
}
