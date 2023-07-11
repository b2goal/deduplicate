package bloomfilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class TempFileWriter {

   public static int numberLines = 300_000_000;
    static int maxIndex = 300_000_000;
    static String fileName = "300m.txt";

    public static File getFile() {
        return new File(fileName);
    }

    public  void generateFile() throws IOException {
//        File file = File.("test", ".txt");
        File file = new File(fileName);
        file.createNewFile();
        Random rand = new Random();
        long start1 = System.nanoTime();
        PrintWriter pw = new PrintWriter(new FileWriter(file));
        for (int i = 0; i < numberLines; i++)
        {
//            pw.println(i+"|00000000000000000000000000000");
            pw.println(rand.nextInt(maxIndex)+"|00000000000000000000000000000");
        }
        pw.close();
        long time1 = System.nanoTime() - start1;
        System.out.printf("Took %.3f seconds to write  %d lines, file rate: %.1f lines/s%n",
                time1 / 1e9, numberLines >> 20, numberLines * 1000.0 / time1);

    }
}
