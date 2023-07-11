package org.example;

import bloomfilter.SplitDataFileWriter;
import bloomfilter.TempFileReader;
import bloomfilter.TempFileWriter;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException {
        usingHashTableBloomFilter();
        usingHashTable();
//        usingHashSet();
//        createTempFile();

        }

    private static void usingHashTable() throws IOException {
        System.out.println("Using Hash Table !!");
        long start1 = System.nanoTime();
        final int onePercent = TempFileWriter.numberLines /100;
        SplitDataFileWriter phase1Writer = new SplitDataFileWriter(TempFileWriter.numberLines /1_000_000);
        System.out.println("read file 1st...");
        AtomicInteger countPhase1 = new AtomicInteger();
        AtomicInteger percentPh1 = new AtomicInteger();
        TempFileReader.readFile(TempFileWriter.getFile()).forEach((line) -> {
            countPhase1.getAndIncrement();
            if (countPhase1.get() == onePercent)
            {
                percentPh1.getAndIncrement();
                countPhase1.set(0);
                System.out.println(percentPh1 + "% ....");
            }
            try {
                phase1Writer.write(line);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
        phase1Writer.close();
        System.out.println("read file 1st complete!");
        System.out.println("read file 2nd...");



        AtomicReference<Integer> count = new AtomicReference<>(new Integer(0));
        AtomicInteger percentPh2 = new AtomicInteger();
        AtomicInteger countPhase2 = new AtomicInteger();


        phase1Writer.getFiles().forEach(
                file -> {
                    Set<String> cache = new HashSet<>();
                    try {
                        System.out.println("Read file: " + file.getName()+ " ....");
                        TempFileReader.readFile(file).forEach(line -> cache.add(line));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    count.updateAndGet(v -> v + cache.size());
                }
        );

        phase1Writer.clear();

        System.out.println("unique: " + count.toString());

        long time1 = System.nanoTime() - start1;
        System.out.printf("Took %.3f seconds!",
                time1 / 1e9);
    }

    private static void createTempFile() throws IOException {
        TempFileWriter pw = new TempFileWriter();
        pw.generateFile();
    }

    private static void usingHashSet() throws IOException {
        System.out.println("Using Memory !!");
        long start1 = System.nanoTime();
        Set<String> cache =  new HashSet<>();

        TempFileReader.readFile(TempFileWriter.getFile()).forEach((line) -> {
            cache.add(line);
        });

        System.out.println("unique: " + cache.size());

        long time1 = System.nanoTime() - start1;
        System.out.printf("Took %.3f seconds!",
                time1 / 1e9);
    }

    public static void usingHashTableBloomFilter() throws IOException {
        System.out.println("Using Hash Table & Bloom Filter !!");
        long start1 = System.nanoTime();

        final int onePercent = TempFileWriter.numberLines /100;
        SplitDataFileWriter phase1Writer = new SplitDataFileWriter(1);
        BloomFilter<String> filter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8),TempFileWriter.numberLines,0.001);
        System.out.println("read file 1st...");
        AtomicInteger countPhase1 = new AtomicInteger();
        AtomicInteger percentPh1 = new AtomicInteger();
        TempFileReader.readFile(TempFileWriter.getFile()).forEach((line) -> {
            countPhase1.getAndIncrement();
            if (countPhase1.get() == onePercent)
            {
                percentPh1.getAndIncrement();
                countPhase1.set(0);
                System.out.println(percentPh1 + "% ....");
            }
            if (!filter.put(line))
            {
                try {
                    phase1Writer.write(line);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        phase1Writer.close();
        System.out.println("read file 1st complete!");
        System.out.println("read file 2nd...");

        final  BloomFilter<String> phase2Filter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8),TempFileWriter.numberLines,0.001);
        TempFileReader.readFile(phase1Writer.getFiles().get(0)).forEach((line)->phase2Filter.put(line));
        phase1Writer.clear();

        SplitDataFileWriter writer = new SplitDataFileWriter(TempFileWriter.numberLines /1_000_000);
        AtomicReference<Integer> count = new AtomicReference<>(new Integer(0));
        AtomicInteger percentPh2 = new AtomicInteger();
        AtomicInteger countPhase2 = new AtomicInteger();
        TempFileReader.readFile(TempFileWriter.getFile()).forEach((line) -> {
            countPhase2.getAndIncrement();
            if (countPhase2.get() == onePercent)
            {
                percentPh2.getAndIncrement();
                countPhase2.set(0);
                System.out.println(percentPh2 + "% ....");
            }
            try {
                if (phase2Filter.mightContain(line)) {
                    writer.write(line);
                }
                else {
                    count.updateAndGet(v -> v + 1);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        writer.close();

        writer.getFiles().forEach(
                file -> {
                    Set<String> cache = new HashSet<>();
                    try {
                        System.out.println("Read file: " + file.getName()+ " ....");
                        TempFileReader.readFile(file).forEach(line -> cache.add(line));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    count.updateAndGet(v -> v + cache.size());
                }
        );

        writer.clear();

        System.out.println("unique: " + count.toString());

        long time1 = System.nanoTime() - start1;
        System.out.printf("Took %.3f seconds!",
                time1 / 1e9);
    }
    }