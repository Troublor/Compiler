package OptimizePackage;

import TranslatorPackage.QT;
import java.util.ArrayList;

public class test {

    public static void main(String[] args) {
        System.out.println((char)40);

        ArrayList<QT> qts = new ArrayList<>();
        qts.add(new QT("=", "const int_5", null, "B.value"));
        qts.add(new QT("*", "const int_2", "const double_3.14", "$t1.value"));
        qts.add(new QT("+", "R.value", "r.value", "$t2.value"));
        qts.add(new QT("*", "$t1.value", "$t2.value", "$t3.value"));
        qts.add(new QT("=", "$t3.value", null, "A.value"));
        qts.add(new QT("*", "const int_2", "const double_3.14", "$t4.value"));
        qts.add(new QT("+", "R.value", "r.value", "$t5.value"));
        qts.add(new QT("*", "$t4.value", "$t5.value", "$t6.value"));
        qts.add(new QT("-", "R.value", "r.value", "$t7.value"));
        qts.add(new QT("/", "$t6.value", "$t7.value", "$t8.value"));
        qts.add(new QT("=", "$t8.value", null, "B.value"));

        DAG dag = new DAG();

        try {
            ArrayList<QT> result = dag.optimize(qts);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
}
