package com.hitchhikerprod.dragonwars;

import com.hitchhikerprod.dragonwars.data.Hint;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.hitchhikerprod.dragonwars.Main.basePath;

public class HintTable {
    private final Map<Integer, Map<Integer, Hint>> hintMap;

    public HintTable() {
        hintMap = new HashMap<>();
    }

    public HintTable(RandomAccessFile hintFile) {
        try {
            hintMap = new HashMap<>();
            final BufferedReader reader = new BufferedReader(new FileReader(hintFile.getFD()));
            parse(hintMap, reader.lines());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HintTable(List<String> hints) {
        hintMap = new HashMap<>();
        parse(hintMap, hints.stream());
    }

    public Map<Integer,Hint> getHints(int chunkId) {
        if (hintMap.containsKey(chunkId))
            return hintMap.get(chunkId);
        else
            return new HashMap<>();
    }

    private static final Pattern hintPattern =
        Pattern.compile("\\{(?<chunk>[0-9a-f]+):(?<adr>[0-9a-f]+)}.(?<type>\\w+)(?<args>\\(.*\\))?.*$");

    public static void parse(Map<Integer, Map<Integer, Hint>> hintMap, Stream<String> lines) {
        lines.forEach(line -> {
            final Matcher matcher = hintPattern.matcher(line);
            if (! matcher.matches()) {
                throw new RuntimeException("Parse error: " + line);
            }
            final int chunk = Integer.parseInt(matcher.group("chunk"), 16);
            final int address = Integer.parseInt(matcher.group("adr"), 16);
            final String hintType = matcher.group("type").toUpperCase();
            final Hint hint = Hint.valueOf(hintType);
            final String args = matcher.group("args");
            if (args != null) {
                final int count = Integer.parseInt(args);
                hint.setCount(count);
            }
            hintMap.computeIfAbsent(chunk, (c) -> new HashMap<>());
            hintMap.get(chunk).put(address, hint);
            System.out.print(".");
        });
    }

    public static void main(String[] args) {
/*
        List<String> test = new ArrayList<>();
        test.add("{47:041e}.item");
        final HintTable table = new HintTable(test);
*/
        try (final RandomAccessFile hints = new RandomAccessFile(basePath + "hints", "r")) {
            final HintTable table = new HintTable(hints);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
