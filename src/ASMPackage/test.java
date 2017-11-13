package ASMPackage;

import MiddleDataUtilly.QT;

import java.util.ArrayList;

public class test {

    public static void main(String[] args) {
        ArrayList<QT> qts = new ArrayList<>();
        qts.add(new QT("+", "a.value", "b.value", "$t1.value"));
        qts.add(new QT("-", "c.value", "d.value", "$t2.value"));
        qts.add(new QT("*", "$t1.value", "$t2.value", "$t3.value"));
        qts.add(new QT("-", "a.value", "$t3.value", "$t4.value"));
        qts.add(new QT("/", "$t1.value", "const int_2", "$t5.value"));
        qts.add(new QT("+", "$t4.value", "$t5.value", "x.value"));

        ASMGenerater generater = new ASMGenerater(qts);
        generater.generate();
        System.out.println("");

    }

}
