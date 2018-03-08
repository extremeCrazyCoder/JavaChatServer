import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


public class FileAccess {
    public static void append(String filePath, String data) throws IOException {
        PrintWriter w = new PrintWriter(
                new BufferedWriter(
                new FileWriter(filePath, true)));
        w.println(data);
        w.flush();
        w.close();
    }

    public static void write(String filePath, String[] data) throws IOException {
        PrintWriter w = new PrintWriter(
                new BufferedWriter(
                new FileWriter(filePath, false)));
        for(int i = 0; i < data.length; i++)
            w.println(data[i]);
        w.flush();
        w.close();
    }

    public static String[] read(String path) throws IOException {
        List<String> data = new ArrayList<String>();
        
        BufferedReader br = new BufferedReader(new FileReader(path));
        
        String line;
        while((line = br.readLine()) != null) {
            data.add(line);
        }
        
        br.close();
        
        String[] finalData = new String[data.size()];
        for(int i = 0; i < data.size(); i++)
            finalData[i] = data.get(i);
        
        return finalData;
    }

    public static void write(String filePath, String data) throws IOException {
        PrintWriter w = new PrintWriter(
                new BufferedWriter(
                new FileWriter(filePath, false)));
        w.println(data);
        w.flush();
        w.close();
    }

    public static void createDir(String path) {
        new File(path).mkdir();
    }

}
