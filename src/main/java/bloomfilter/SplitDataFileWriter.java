package bloomfilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class SplitDataFileWriter {
    private int numberClusters ;
    private String tempFolderName ;

    private final String suffixFileName = "cluster.txt";
    private Map<String, PrintWriter> fileWriterMap = new HashMap<>();

    private  File folder ;
    private boolean close;

    public SplitDataFileWriter(int numberClusters) {
        System.out.printf("SplitDataFileWriter with %d clusters!", numberClusters);
        this.numberClusters = numberClusters;
        this.tempFolderName = UUID.randomUUID().toString();
        folder = new File(tempFolderName);
        folder.mkdir();
    }

    public void write(String value) throws IOException {
        int index = Math.abs(value.hashCode() % numberClusters);
        PrintWriter writer =  fileWriterMap.computeIfAbsent(String.valueOf(index), k -> {
            try {
                return new PrintWriter(new FileWriter(new File(tempFolderName,index+ suffixFileName)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        writer.println(value);
    }

    public List<File> getFiles()
    {
        List<File> rs = new ArrayList<>();
        if(close)
        {
            String[]entries = folder.list();
            for(String s: entries){
                File currentFile = new File(folder.getPath(),s);
                rs.add(currentFile);
            }
        }
        return  rs;
    }
    
    

    public void close()
    {
        this.close = true;
        fileWriterMap.values().forEach(
                fileWriter -> {
                        fileWriter.close();
                }
        );
    }

    public void clear()
    {
        String[]entries = folder.list();
        for(String s: entries){
            File currentFile = new File(folder.getPath(),s);
            currentFile.delete();
        }
        folder.delete();
        System.out.printf("delete  %d files !", entries.length);
        System.out.printf("delete  %s folder !", this.tempFolderName);
    }
}
