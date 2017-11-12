

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class test {

    public static void main(String[] args) {
        Parser parser = new Parser();

        StringBuilder input = new StringBuilder("");
        File file = new File("/Users/gexinjie/IdeaProjects/Compiler/src/input.txt");
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
            boolean r = parser.LL1Analyze();
            System.out.println(r);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


}
