import ASMPackage.ASMGenerater;
import ASMPackage.ASMSentence;
import MiddleDataUtilly.QT;
import OptimizePackage.Optimizer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Compile {

    public static void main(String[] args) {
        Parser parser = new Parser();

        StringBuilder input = new StringBuilder("");
        String filename =
                (args.length == 0) ? "/home/scarecrow/IdeaProjects/Compiler/src/input.txt" : args[0];
        boolean debug = true;
        if (args.length == 1) {
            if (!args[0].startsWith("-")) {
                filename = args[0];
            } else {
                debug = args[0].equals("-d");
            }
        } else if (args.length == 2) {
            debug = args[0].equals("-d");
            filename = args[1];
        }
        File file = new File(filename);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                input.append(tempString).append("$");//$用来表示行尾
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        parser.setSourceCode(input.toString());

        try {
            parser.setDebug(debug);
            parser.LL1Analyze();
            Optimizer optimizer = new Optimizer(parser.getAllQTs());
            ArrayList<QT> qts = optimizer.optimize();
            if (debug) {
                System.out
                        .println("\n\n优化后的所有四元式:\n" + parser.getAllQTs().size() + " => " + qts.size());
                System.out.println(String.format("%-11s%-25s%-25s%-25s", "oprt:", "left_oprd:", "right_oprd:", "result_target:"));
                for (QT qt : qts) {
                    System.out.println(qt);
                }

                ASMGenerater asmGenerater = new ASMGenerater(qts, parser.getSymbolTableManager());
                List<ASMSentence> asmSentences = asmGenerater.generate();
                System.out.println("\n\n以下是生成的汇编源码: ");
                for (ASMSentence asm : asmSentences) {
                    System.out.println(asm);
                }
            }
            System.out.println("编译成功！");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


}
