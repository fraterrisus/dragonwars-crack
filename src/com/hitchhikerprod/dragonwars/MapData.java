package com.hitchhikerprod.dragonwars;

import com.hitchhikerprod.dragonwars.data.Encounter;
import com.hitchhikerprod.dragonwars.data.Item;
import com.hitchhikerprod.dragonwars.data.ItemEvent;
import com.hitchhikerprod.dragonwars.data.Monster;
import com.hitchhikerprod.dragonwars.data.SkillEvent;
import com.hitchhikerprod.dragonwars.data.SpecialEvent;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.hitchhikerprod.dragonwars.Main.basePath;

// Largely based on fcn.55bb
public class MapData {
    private static final Color BUSH = new Color(87, 202, 87);
    private static final Color CAMPFIRE = new Color(200, 158, 18);
    private static final Color ENCOUNTER = new Color(0x70, 0x90, 0xff);
    private static final Color FENCE = new Color(0x80, 0xd3, 0xff);
    private static final Color FLOOR = new Color(0xee, 0xee, 0xee);
    private static final Color GRID = new Color(0x80, 0xd3, 0xff);
    private static final Color ROCK = Color.LIGHT_GRAY;
    private static final Color STATUE = Color.LIGHT_GRAY;
    private static final Color TREE = new Color(87, 202, 87);
    private static final Color WALL = Color.DARK_GRAY;
    private static final Color WATER = new Color(0x80, 0xa0, 0xff);

    private static final int GRID_SCALE = 4;
    private static final int GRID_SIZE = GRID_SCALE * 10;

    private final int mapId;
    // private final RandomAccessFile executable;
    private final ChunkTable chunkTable;
    private final HintTable hintTable;
    private final Chunk primaryData;
    private final Chunk secondaryData;
    private final StringDecoder.LookupTable stringDecoderLookupTable;

    private int chunkPointer;
    private int xMax; // heap[21]
    private int yMax; // heap[22]
    private int flags; // heap[23]
    private int randomEncounters; // heap[24]

    private List<Integer> primaryPointers;
    // private List<Integer> secondaryPointers;
    // private List<Integer> itemPointers;

    private int titleStringPtr;
    private int metaprogramStartPtr;
    private int specialEventsPtr;
    private int itemListPtr;
    private int monsterDataPtr;
    private int encountersPtr;
    private int tagLinesPtr;

    private String titleString;

    private final List<Byte> wallTextures54a7 = new ArrayList<>();
    // 54b5: translates map square bits [0:3]
    private final List<Byte> wallMetadata54b6 = new ArrayList<>();
    // 54c5: texture array? maxlen 4, values are indexes into 5677
    private final List<Byte> roofTextures54c5 = new ArrayList<>();
    private final List<Byte> floorTextures54c9 = new ArrayList<>();
    private final List<Byte> otherTextures54cd = new ArrayList<>();
    // 5677: list of texture chunks (+0x6e); see 0x5786()
    // 57c4: list of texture? segments
    private final List<Byte> textureChunks5677 = new ArrayList<>();
    // 5734: list of pointers to square data rows
    private final List<Integer> rowPointers57e4 = new ArrayList<>();
    private final List<SpecialEvent> specialEvents = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();
    private final List<Encounter> encounters = new ArrayList<>();
    private final List<Monster> monsters = new ArrayList<>();

    public MapData(RandomAccessFile executable, ChunkTable chunkTable, int mapId) {
        this(executable, chunkTable, mapId, new HintTable());
    }

    public MapData(RandomAccessFile executable, ChunkTable chunkTable, int mapId, HintTable hintTable) {
        this.mapId = mapId;
        // this.executable = executable;
        this.chunkTable = chunkTable;
        this.primaryData = decompressChunk(mapId + 0x46);
        this.secondaryData = decompressChunk(mapId + 0x1e);
        this.stringDecoderLookupTable = new StringDecoder.LookupTable(executable);
        this.hintTable = hintTable;
    }


    public void parse(int offset) throws IOException {
        chunkPointer = offset;
        this.xMax = primaryData.getByte(chunkPointer);
        // for some reason this is one larger than it should be; see below
        this.yMax = primaryData.getByte(chunkPointer + 1);
        this.flags = primaryData.getUnsignedByte(chunkPointer + 2);
        this.randomEncounters = primaryData.getUnsignedByte(chunkPointer + 3);
        chunkPointer += 4;

        byteReader(textureChunks5677::add);

        byte f = 0;
        while ((f & 0x80) == 0) {
            f = primaryData.getByte(chunkPointer);
            wallTextures54a7.add((byte)(f & 0x7f));
            wallMetadata54b6.add(primaryData.getByte(chunkPointer+1));
            chunkPointer += 2;
        }

        byteReader((b) -> roofTextures54c5.add((byte)(b & 0x7f)));
        byteReader((b) -> floorTextures54c9.add((byte)(b & 0x7f)));
        byteReader((b) -> otherTextures54cd.add((byte)(b & 0x7f)));

        titleStringPtr = primaryData.getWord(chunkPointer);
        chunkPointer += 2;

        final StringDecoder sd = new StringDecoder(stringDecoderLookupTable, primaryData);
        sd.decodeString(titleStringPtr);
        titleString = sd.getDecodedString();
        // System.out.printf("Title string: %04x - %04x\n", titleStringAdr, sd.getPointer());

        int ptr = chunkPointer - offset;
        int xInc = xMax * 3;
        for (int y = 0; y <= yMax; y++) {
            rowPointers57e4.add(ptr);
            ptr += xInc;
        }
        // 55bb:parseMapData() builds this array backwards, from yMax down to 0, so it can include an "end" pointer(?)
        Collections.reverse(rowPointers57e4);

        primaryPointers = discoverPointers(primaryData, rowPointers57e4.get(0));
        metaprogramStartPtr = primaryPointers.get(0);
        specialEventsPtr = primaryPointers.get(1);

        monsterDataPtr = secondaryData.getWord(0);
        encountersPtr = secondaryData.getWord(2);
        tagLinesPtr = secondaryData.getWord(4);
        itemListPtr = secondaryData.getWord(6);

        parseEncounters();
        parseItems();
        parseSpecialEvents();
    }

    public int getSquare(int x, int y) {
        if (rowPointers57e4.isEmpty()) { throw new RuntimeException("parse() hasn't been called"); }

        // The list of row pointers has one-too-many, and the "extra" is at the START
        // So 52b8:fetchMapSquare() starts at 0x57e6 i.e. [0x5734+2] i.e. it skips the extra pointer
        final int offset = rowPointers57e4.get(y + 1) + (3 * x);
        return (primaryData.getUnsignedByte(offset) << 16) |
            (primaryData.getUnsignedByte(offset+1) << 8) |
            (primaryData.getUnsignedByte(offset+2));
    }

    public void draw() {
        int imageWidth = GRID_SIZE * (xMax + 2);
        int imageHeight = GRID_SIZE * (yMax + 2);
        final BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        final Graphics2D gfx = image.createGraphics();
        gfx.setFont(new Font("Liberation Sans", Font.PLAIN, 3 * GRID_SCALE));

        gfx.setColor(Color.WHITE);
        gfx.fill(new Rectangle(0, 0, imageWidth, imageHeight));

        gfx.setColor(GRID);
        drawGrid(image);

        for (int y = yMax; y > 0; y--) { // skip the end pointer
            for (int x = 0; x < xMax; x++) {
                drawSquare(gfx, x, y-1);
            }
        }

        if ((flags & 0x02) > 0) { // map wraps around
            int base;
            for (int y = yMax; y > 0; y--) {
                base = rowPointers57e4.get(y);
                drawEastEdge(gfx, base, y-1);
            }
            base = rowPointers57e4.get(yMax);
            for (int x = 0; x < xMax; x++) {
                drawNorthEdge(gfx, base + (3*x), x);
            }
        }

        try {
            final String filename = String.format("map-%02x.png", mapId);
            ImageIO.write(image, "png", new File(filename));
            System.out.println("Wrote " + filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void drawGrid(BufferedImage image) {
        final int grid_blue = GRID.getRGB();
        final int base = GRID_SIZE + (GRID_SCALE / 2);

        for (int y = GRID_SIZE; y <= (GRID_SIZE * (yMax + 1)); y += GRID_SIZE) {
            for (int x = base; x < GRID_SIZE * (xMax + 1); x += 2 * GRID_SCALE) {
                for (int i = 0; i < GRID_SCALE; i++) {
                    image.setRGB(x+i, y, grid_blue);
                }
            }
        }

        for (int x = GRID_SIZE; x <= (GRID_SIZE * (xMax + 1)); x += GRID_SIZE) {
            for (int y = base; y < GRID_SIZE * (yMax + 1); y += 2 * GRID_SCALE) {
                for (int i = 0; i < GRID_SCALE; i++) {
                    image.setRGB(x, y+i, grid_blue);
                }
            }
        }
    }

    private void drawEastEdge(Graphics2D gfx, int pointer, int y) {
        // grab square data from x=0
        final int byte0 = primaryData.getUnsignedByte(pointer);
        final int wallEastTexture = byte0 & 0x0f;

        int x = xMax;
        final int yRel = yMax - y - 1;
        final Point2D.Double x0y0 = new Point2D.Double(GRID_SIZE * (x + 1), GRID_SIZE * (yRel + 1));
        final Point2D.Double x0y1 = new Point2D.Double(GRID_SIZE * (x + 1), GRID_SIZE * (yRel + 2));

        final Line2D eastWall = new Line2D.Double(x0y0, x0y1);
        final Rectangle2D eastDoor = new Rectangle2D.Double(x0y0.getX() - GRID_SCALE, x0y0.getY() + (3 * GRID_SCALE), 2 * GRID_SCALE, 4 * GRID_SCALE);
        drawWall(gfx, wallEastTexture, eastWall, eastDoor);
    }

    private void drawNorthEdge(Graphics2D gfx, int pointer, int x) {
        final int byte0 = primaryData.getUnsignedByte(pointer);
        final int wallSouthTexture = (byte0 >> 4) & 0x0f;

        int yRel = yMax;
        final Point2D.Double x0y0 = new Point2D.Double(GRID_SIZE * (x + 1), GRID_SIZE * (yRel + 1));
        final Point2D.Double x1y0 = new Point2D.Double(GRID_SIZE * (x + 2), GRID_SIZE * (yRel + 1));

        final Line2D southWall = new Line2D.Double(x0y0, x1y0);
        final Rectangle2D southDoor = new Rectangle2D.Double(
            x0y0.getX() + (3 * GRID_SCALE), x0y0.getY() - GRID_SCALE, 4 * GRID_SCALE, 2 * GRID_SCALE);
        drawWall(gfx, wallSouthTexture, southWall, southDoor);
    }

    private void drawSquare(Graphics2D gfx, int x, int y) {
        final int squareData = getSquare(x, y);

        final int wallNorthTexture = (squareData >> 20) & 0xf;
        final int wallWestTexture = (squareData >> 16) & 0xf;
        // final int roofTexture = (squareData >> 14) & 0x3;
        final int floorTexture = (squareData >> 12) & 0x3;
        // final boolean steppedOn = (squareData & 0x008000) > 0;
        final int specialTexture = (squareData >> 8) & 0x7;
        final int eventId = squareData & 0xff;

        final int yRel = yMax - y - 1;

        final Point2D.Double x0y0 = new Point2D.Double(GRID_SIZE * (x + 1), GRID_SIZE * (yRel + 1));
        final Point2D.Double x0y1 = new Point2D.Double(GRID_SIZE * (x + 1), GRID_SIZE * (yRel + 2));
        final Point2D.Double x1y0 = new Point2D.Double(GRID_SIZE * (x + 2), GRID_SIZE * (yRel + 1));
        //final Point2D.Double x1y1 = new Point2D.Double(GRID_SIZE * (x + 2), GRID_SIZE * (yRel + 2));

        final Rectangle2D floor = new Rectangle2D.Double(x0y0.getX() + 1, x0y0.getY() + 1, GRID_SIZE-1, GRID_SIZE-1);
        drawFloor(gfx, floorTexture, floor);
        drawSpecial(gfx, specialTexture, floor);
        if (eventId != 0) {
            gfx.setColor(ENCOUNTER);
            gfx.drawString(String.format("%X", eventId), (int) floor.getX()+2, (int) floor.getMaxY()-2);
        }

        final Line2D northWall = new Line2D.Double(x0y0, x1y0);
        final Rectangle2D northDoor = new Rectangle2D.Double(
            x0y0.getX() + (3 * GRID_SCALE), x0y0.getY() - GRID_SCALE, 4 * GRID_SCALE, 2 * GRID_SCALE);
        drawWall(gfx, wallNorthTexture, northWall, northDoor);

        final Line2D westWall = new Line2D.Double(x0y0, x0y1);
        final Rectangle2D westDoor = new Rectangle2D.Double(x0y0.getX() - GRID_SCALE, x0y0.getY() + (3 * GRID_SCALE), 2 * GRID_SCALE, 4 * GRID_SCALE);
        drawWall(gfx, wallWestTexture, westWall, westDoor);
    }

    private void drawFloor(Graphics2D gfx, int floorIndex, Rectangle2D floor) {
        final int textureChunk = floorTextures54c9.get(floorIndex);
        switch (textureChunk) {
            case 0x00, 0x01, 0x02, 0x07, 0x0e -> gfx.setColor(Color.WHITE);
            // Some of these are more like "abyss" than "water", but it varies by map :(
            case 0x03, 0x04, 0x05, 0x06 -> gfx.setColor(WATER);
            default -> gfx.setColor(FLOOR);
        }
        gfx.fill(floor);
//        gfx.setColor(Color.RED);
//        gfx.drawString(String.valueOf(textureChunk), (int) floor.getX(), (int) floor.getMaxY());
    }

    private void drawSpecial(Graphics2D gfx, int specialIndex, Rectangle2D floor) {
        final Ellipse2D circle = new Ellipse2D.Double(floor.getX() + (GRID_SIZE/4.0), floor.getY() + (GRID_SIZE/4.0),
            (GRID_SIZE/2.0), (GRID_SIZE/2.0));

        if (specialIndex > 0) {
            final int textureId = otherTextures54cd.get(specialIndex - 1);
            final int chunkId = textureChunks5677.get(textureId) & 0x7f;
//            gfx.setColor(Color.RED);
//            gfx.drawString(String.format("%02x", chunkId), (int) floor.getX(), (int) floor.getMaxY());
            gfx.setColor(Color.WHITE);
            switch (chunkId) {
                case 0x03 -> gfx.setColor(TREE);
                case 0x04, 0x09 -> gfx.setColor(ROCK);
                case 0x0a -> gfx.setColor(BUSH);
                case 0x0b -> gfx.setColor(CAMPFIRE);
                case 0x06 -> gfx.setColor(WATER);
                // case 0x12 -> gfx.setColor(HOUSE);
                case 0x13 -> gfx.setColor(STATUE);
            }
            if (gfx.getColor() != Color.WHITE) { gfx.fill(circle); }
        }
    }

    private void drawWall(Graphics2D gfx, int wallIndex, Line2D wall, Rectangle2D door) {
        if (wallIndex > 0) {
            final int textureId = wallTextures54a7.get(wallIndex - 1);
            final int metadata = wallMetadata54b6.get(wallIndex - 1);
            if (textureId == 0x7f) {
                gfx.setColor(WALL);
                gfx.draw(wall);
            } else {
                final int chunkId = textureChunks5677.get(textureId) & 0x7f;
                if (chunkId == 0x0c) {
                    gfx.setColor(FENCE);
                } else {
                    gfx.setColor(WALL);
                }
                gfx.draw(wall);

                if (((metadata & 0x40) == 0) && gfx.getColor().equals(WALL)) {
                    drawDoor(gfx, chunkId, door);
                } else if (visibleDoors.contains(chunkId)) {
                    // there's gotta be a better way to detect a locked door; where can you (U)se Lockpick?
                    gfx.setColor(Color.DARK_GRAY);
                    gfx.fill(door);
                    gfx.setColor(Color.RED);
                    gfx.drawString(String.format("%02x", metadata & 0xff), (int) door.getX(), (int) door.getY() - 2);
                }
            }
        }
    }

    private static final List<Integer> visibleDoors = List.of(0x05, 0x10);

    private static void drawDoor(Graphics2D gfx, int chunkId, Rectangle2D door) {
        if (visibleDoors.contains(chunkId)) {
            gfx.setColor(Color.WHITE);
        } else {
            gfx.setColor(Color.LIGHT_GRAY);
        }
        gfx.fill(door);
        gfx.setColor(Color.DARK_GRAY);
        gfx.draw(door);
    }

    public void display() {
        final String binString = Integer.toBinaryString(flags);
        final String foo = "0".repeat(8 - binString.length()) + binString;
        System.out.printf("\"%s\" (%dx%d)  meta:%8s\n", titleString, xMax, yMax, foo);
        System.out.println("  Flags: " + displayMapFlags());
        if (this.randomEncounters > 0) {
            System.out.println("  Random encounters: 1 in " + randomEncounters);
        }

        System.out.print("\n0x5677 Texture chunks:");
        for (byte b : textureChunks5677) { System.out.printf(" %02x", (b & 0x7f) + 0x6e); }
        System.out.print("\n0x54a6 Wall textures : --");
        for (byte b : wallTextures54a7) { System.out.printf(" %02x", b); }
        System.out.print("\n0x54b5 Wall metadata : --");
        for (byte b : wallMetadata54b6) { System.out.printf(" %02x", b); }
        System.out.print("\n0x54c5 Roof textures :");
        for (byte b : roofTextures54c5) { System.out.printf(" %02x", b); }
        System.out.print("\n0x54c9 Floor textures:");
        for (byte b : floorTextures54c9) { System.out.printf(" %02x", b); }
        System.out.print("\n0x54cd Decor textures:");
        for (byte b : otherTextures54cd) { System.out.printf(" %02x", b); }
        System.out.println();

        System.out.println();
        System.out.printf("Primary Map Data (chunk %02x):\n", mapId + 0x46);
        System.out.printf("  Title string: [%04x]\n", titleStringPtr);
        System.out.print("  Pointers:");
        for (int p : primaryPointers) {
            System.out.printf(" [%04x]", p);
        }
        System.out.println();
        System.out.printf("  Metaprogram start: ptr[00] -> [%04x]\n", metaprogramStartPtr);
        System.out.printf("  Special events:    ptr[01] -> [%04x]\n", specialEventsPtr);
        for (SpecialEvent a : specialEvents) {
            System.out.printf("    %s -> [%04x]\n", a, primaryPointers.get(a.getMetaprogramIndex() + 1));
        }

        System.out.print("\nMap squares:\n");
        for (int y = yMax-1; y >= 0; y--) {
            int base = rowPointers57e4.get(y+1);
            System.out.printf(" [%04x]  y=%02d ", base, y);
            for (int x = 0; x < xMax; x++) {
                System.out.printf(" %06x", getSquare(x, y));
            }
            System.out.println();
        }
        System.out.printf(" [%04x]       ", rowPointers57e4.get(0));
        for (int x = 0; x < xMax; x++) { System.out.printf("  x=%02d ", x); }
        System.out.println();

/*
        final int startOfMetaprogram = primaryPointers.stream().min(Integer::compareTo).orElse(0);
        primaryData.display(startOfMetaprogram);
        System.out.println();
*/
        System.out.println("\nDisassembly:");
        final List<Integer> metaprogramPointers = new ArrayList<>();
        final MetaprogramDecompiler decompiler =
            new MetaprogramDecompiler(primaryData, stringDecoderLookupTable, hintTable.getHints(mapId + 0x46));
        metaprogramPointers.add(primaryPointers.get(0));
        metaprogramPointers.addAll(primaryPointers.subList(2,primaryPointers.size()));
        metaprogramPointers.sort(Integer::compareTo);
        int mpi = 0;
        while (mpi < metaprogramPointers.size()) {
            final Integer thisPointer = metaprogramPointers.get(mpi);
            if ((mpi > 0) && (thisPointer.equals(metaprogramPointers.get(mpi-1)))) { continue; }
            Integer nextPointer = thisPointer;
            while (nextPointer.equals(thisPointer)) {
                if (mpi == metaprogramPointers.size() - 1) {
                    nextPointer = primaryData.getSize();
                } else {
                    nextPointer = metaprogramPointers.get(mpi + 1);
                }
                mpi++;
            }
            if ((thisPointer < specialEventsPtr) & (specialEventsPtr < nextPointer)) {
                nextPointer = specialEventsPtr;
            }
            try {
                System.out.printf("  0x%04x:\n", thisPointer);
                decompiler.disasm(thisPointer, nextPointer, false);
            } catch (MetaprogramDecompiler.DecompilationException ignored) {
            } catch (ArrayIndexOutOfBoundsException ignored) {
                System.out.println();
            }
        }
        System.out.println();

        System.out.println();
        System.out.printf("Secondary Data (chunk %02x):\n", mapId + 0x1e);

        System.out.printf("  Monsters: ptr[00]->[%04x]", monsterDataPtr);
        System.out.printf("    Random max: %02x\n", secondaryData.getUnsignedByte(monsterDataPtr));
        for (int i = 0; i < monsters.size(); i++) {
            System.out.printf("    [%02x] %s\n", i, monsters.get(i));
        }

        System.out.println();
        System.out.printf("  Encounters: ptr[01]->[%04x], ptr[02]->[%04x]\n", encountersPtr, tagLinesPtr);
        for (int i = 0; i < encounters.size(); i++) {
            final Encounter enc = encounters.get(i);
            System.out.printf("    [%02x:%04x]%s", i, enc.getOffset(), enc);
        }

        System.out.println();
        System.out.printf("  Items: ptr[03]->[%04x]\n", itemListPtr);

        for (int i = 0; i < items.size(); i++) {
            final Item item = items.get(i);
            System.out.printf("    [%02x:%04x]%s\n", i, item.getOffset(), item);
        }

        System.out.println();
        System.out.println("Texture Chunks:");
        for (byte id : textureChunks5677) {
            final int chunkId = (id & 0x7f) + 0x6e;
            final Chunk moreData = decompressChunk(chunkId);
            if (chunkId == 0x6f) {
                System.out.printf(" *0x6f* [0000] [0000] [%04x]", moreData.getWord(0x04));
            } else {
                System.out.printf("  0x%02x:", chunkId);
                for (int p : discoverPointers(moreData, 0)) {
                    System.out.printf(" [%04x]", p);
                }
            }
            System.out.println();
        }

/*
        System.out.println();
        System.out.println("Packed strings in primary data:");
        final StringDecoder sd1 = new StringDecoder(stringDecoderLookupTable, primaryData);
        try {
            for (int ptr = 0; ptr < primaryData.getSize(); ptr++) {
                sd1.decodeString(ptr);
                final String s = sd1.getDecodedString();
                if (s.isEmpty()) { continue; }
                System.out.printf("  [%04x-%04x] %s\n", ptr, sd1.getPointer()-1, sd1.getDecodedString());
                //ptr = sd1.getPointer()-1;
            }
        } catch (IndexOutOfBoundsException ignored) {}
*/
/*
        System.out.println();
        System.out.println("Packed strings in secondary data:");
        final StringDecoder sd2 = new StringDecoder(stringDecoderLookupTable, secondaryData);
        try {
            for (int ptr = 0; ptr < secondaryData.getSize(); ptr++) {
                sd2.decodeString(ptr);
                final String s = sd2.getDecodedString();
                if (s.isEmpty()) { continue; }
                System.out.printf("  [%04x-%04x] %s\n", ptr, sd2.getPointer()-1, sd2.getDecodedString());
                //ptr = sd2.getPointer()-1;
            }
        } catch (IndexOutOfBoundsException ignored) {}

        System.out.println();
        System.out.println("Data strings in secondary data:");
        try {
            for (int ptr = 0; ptr < secondaryData.getSize(); ptr++) {
                final String s = new DataString(secondaryData, ptr, 13).toString();
                if (s.isEmpty()) { continue; }
                System.out.printf("  [%04x] %s\n", ptr, s);
            }
        } catch (IndexOutOfBoundsException ignored) {}
*/
    }

    private List<Integer> discoverPointers(Chunk chunk, int basePtr) {
        final List<Integer> pointers = new ArrayList<>();
        int thisPtr = basePtr;
        int firstPtr = chunk.getWord(basePtr);
        while (thisPtr < firstPtr) {
            int nextPtr = chunk.getWord(thisPtr);
            // if (nextPtr > chunk.getSize()) { return pointers; }
            pointers.add(nextPtr);
            if ((nextPtr != 0) && (nextPtr < firstPtr)) { firstPtr = nextPtr; }
            thisPtr += 2;
        }
        return pointers;
    }

    private String displayMapFlags() {
        List<String> flagStrings = new ArrayList<>();
        // if ((flags & 0x80) > 0) flagStrings.add("0x80");
        // if ((flags & 0x40) > 0) flagStrings.add("0x40");
        if ((flags & 0x20) > 0) flagStrings.add("0x20"); // Kingshome Dungeon
        if ((flags & 0x10) == 0) flagStrings.add("forbids Create Wall");
        if ((flags & 0x08) == 0) flagStrings.add("needs light");
        if ((flags & 0x04) == 0) flagStrings.add("needs compass");
        if ((flags & 0x02) > 0) flagStrings.add("wraps");
        if ((flags & 0x01) > 0) flagStrings.add("0x01"); // Kingshome Dungeon
        return String.join(", ", flagStrings);
    }

    private void byteReader(Consumer<Byte> consumer) {
        byte f = 0;
        while ((f & 0x80) == 0) {
            f = primaryData.getByte(chunkPointer);
            chunkPointer++;
            consumer.accept(f);
        }
    }

    private Chunk decompressChunk(int mapId) {
        final Chunk mapChunk = this.chunkTable.getChunk(mapId);
        final HuffmanDecoder mapDecoder = new HuffmanDecoder(mapChunk);
        final List<Byte> decodedMapData = mapDecoder.decode();
        return new Chunk(decodedMapData);
    }

    private void parseSpecialEvents() {
        if (specialEventsPtr == 0) return;

        int pointer = specialEventsPtr;
        while (true) {
            final int header = primaryData.getUnsignedByte(pointer);
            if (header == 0xff) {
                return;
            } else if (header == 0x80) {
                specialEvents.add(new ItemEvent(
                    primaryData.getUnsignedByte(pointer + 1),
                    primaryData.getUnsignedByte(pointer + 2),
                    primaryData.getUnsignedByte(pointer + 3)));
                pointer += 4;
            } else if (header >= 0x8c && header <= 0xba) {
                specialEvents.add(new SkillEvent(header,
                    primaryData.getUnsignedByte(pointer + 1),
                    primaryData.getUnsignedByte(pointer + 2)));
                pointer += 3;
            } else {
                specialEvents.add(new SpecialEvent(header,
                    primaryData.getUnsignedByte(pointer + 1),
                    primaryData.getUnsignedByte(pointer + 2)));
                pointer += 3;
            }
        }
    }

    private void parseItems() {
        if (itemListPtr == 0) return;

        for (int offset : discoverPointers(secondaryData, itemListPtr)) {
            this.items.add(new Item(secondaryData).decode(offset));
        }
    }

    private void parseEncounters() {
        if ((monsterDataPtr == 0) ||
            (monsterDataPtr == tagLinesPtr) ||
            (monsterDataPtr == encountersPtr)) { return; }

        for (int offset : discoverPointers(secondaryData, monsterDataPtr + 1)) {
            monsters.add(new Monster(secondaryData, stringDecoderLookupTable).decode(offset));
        }

        final List<String> taglines = new ArrayList<>();
        final StringDecoder sd = new StringDecoder(this.stringDecoderLookupTable, secondaryData);
        for (int offset : discoverPointers(secondaryData, tagLinesPtr)) {
            sd.decodeString(offset);
            taglines.add(sd.getDecodedString());
        }

        for (int offset : discoverPointers(secondaryData, encountersPtr + 1)) {
            final Encounter enc = new Encounter(secondaryData).decode(offset);
            enc.setTagline(taglines.get(enc.getTaglineIndex()));
            this.encounters.add(enc);
        }
    }

    public static void main(String[] args) {
        final int boardIndex;
        try {
            boardIndex = Integer.parseInt(args[0].substring(2), 16);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Insufficient arguments");
        }

        try (
            final RandomAccessFile hints = new RandomAccessFile(basePath + "hints", "r");
            final RandomAccessFile exec = new RandomAccessFile(basePath + "DRAGON.COM", "r");
            final RandomAccessFile data1 = new RandomAccessFile(basePath + "DATA1", "r");
            final RandomAccessFile data2 = new RandomAccessFile(basePath + "DATA2", "r");
        ) {
            final ChunkTable chunkTable = new ChunkTable(data1, data2);
            final HintTable hintTable = new HintTable(hints);
            final MapData mapData = new MapData(exec, chunkTable, boardIndex, hintTable);
            mapData.parse(0);
            mapData.display();
            mapData.draw();

/* Sunken Ruins (0x16)
            for (int offset : List.of(0x024e, 0x025d, 0x0274, 0x0286, 0x029d, 0x02b4, 0x02cb)) {
                final Item inv = mapData.decodeItemFromSecondaryData(offset - 11);
                System.out.println(inv);
            }
*/
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
