package bloomfilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

public  class  TempFileReader {
    public static Stream<String> readFile(File file) throws IOException {
       return Files.lines(file.toPath());
    };
}
