/**
 * Created by troub on 2017/10/23.
 */
public class QT {
    private String operator;
    private String number1;
    private String number2;
    private static int count = 0;
    private String result;

    QT(String o, String n1, String n2) {
        operator = o;
        number1 = n1;
        number2 = n2;
        result = "t" + Integer.toString(count);
        count++;
    }

    public String getResult() {
        return result;
    }

    public String toString(){
        return "(" + operator + ", " + number1 + ", " + number2 + ", " + result + ")";
    }
}
