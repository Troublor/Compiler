package ASMPackage;

import MiddleDataUtilly.QT;

import java.util.ArrayList;

public class test {

    public static void main(String[] args) {
        ArrayList<QT> qts = new ArrayList<>();
        qts.add(new QT("<", "const char_a", "b.int", "$t1.int"));
        qts.add(new QT("&&", "const int_4", "const int_7", "c.int"));
        qts.add(new QT("-", "c.int", "const double_3.14", "$t2.double"));
        qts.add(new QT("*", "const char_a", "$t2.double", "$t3.double"));
        qts.add(new QT("-", "a.int", "$t3.double", "$t4.double"));
        qts.add(new QT("/", "$t1.int", "const int_2", "$t5.int"));
        qts.add(new QT("=", "const int_4", null, "c.int"));
        qts.add(new QT("+", "$t4.double", "$t5.int", "x.double"));
        qts.add(new QT("<", "x.double", "c.int", "$t6.int"));

        //ASMGenerater generater = new ASMGenerater(qts);
        //generater.generate();

        try {
            ASMArith arith = new ASMArith(qts);
            ArrayList<ASMSentence> asms = arith.produceASM();
            for (ASMSentence asm : asms) {
                System.out.println(asm);
            }
        }
        catch  (ASMException e) {
            e.printStackTrace();
        }
    }

}
