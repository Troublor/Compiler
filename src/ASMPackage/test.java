package ASMPackage;

import MiddleDataUtilly.QT;

import java.util.ArrayList;

public class test {

    public static void main(String[] args) {
        ArrayList<QT> qts = new ArrayList<>();
        qts.add(new QT("+", "a.int", "b.int", "$t1.int"));
        qts.add(new QT("-", "c.int", "d.int", "$t2.int"));
        qts.add(new QT("*", "$t1.int", "$t2.int", "$t3.int"));
        qts.add(new QT("-", "a.int", "$t3.int", "$t4.int"));
        qts.add(new QT("/", "$t1.int", "const int_2", "$t5.int"));
        qts.add(new QT("+", "$t4.int", "$t5.int", "x.int"));

        // ASMGenerater generater = new ASMGenerater(qts);
        //generater.generate();

//        try {
//            ASMArith arith = new ASMArith(qts,generater);
//            ArrayList<ASMSentence> asms = arith.getResult();
//            for (ASMSentence asm : asms) {
//                System.out.println(asm);
//            }
//        }
//        catch  (ASMException e) {
//            e.printStackTrace();
//        }
    }

}
