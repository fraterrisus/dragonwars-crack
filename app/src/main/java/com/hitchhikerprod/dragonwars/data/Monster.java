package com.hitchhikerprod.dragonwars.data;

import com.hitchhikerprod.dragonwars.Chunk;
import com.hitchhikerprod.dragonwars.ChunkTable;
import com.hitchhikerprod.dragonwars.HuffmanDecoder;
import com.hitchhikerprod.dragonwars.Properties;
import com.hitchhikerprod.dragonwars.StringDecoder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Monster {
    private final Chunk data;
    private final StringDecoder.LookupTable lookupTable;
    private int offset;

    private String name;
    private Gender gender;
    private int dexterity;
    private int baseHealth;
    private WeaponDamage varHealth;
    private int confidence;
    private int varGroupSize;
    private int speed;

    private List<String> flags;
    private List<Action> actions;

    private int unknown0b;
    private PowerInt xpReward;

    private List<Byte> raw;

    public Monster(Chunk data, StringDecoder.LookupTable table) {
        this.data = data;
        this.lookupTable = table;
    }

    public Monster decode(int offset) {
        this.offset = offset;

        raw = data.getBytes(offset, 0x21);

        final int b00 = data.getUnsignedByte(offset);
        this.dexterity = data.getUnsignedByte(offset + 0x01);
        final int b02 = data.getUnsignedByte(offset + 0x02);
        final int b03 = data.getUnsignedByte(offset + 0x03);
        this.baseHealth = data.getUnsignedByte(offset + 0x04);
        final int b05 = data.getUnsignedByte(offset + 0x05);
        final int b06 = data.getUnsignedByte(offset + 0x06);
        final int b07 = data.getUnsignedByte(offset + 0x07);
        this.varHealth = new WeaponDamage(data.getByte(offset + 0x08));
        final int b09 = data.getUnsignedByte(offset + 0x09);
        this.speed = (b09 >> 4) & 0x0f; // g[21]; how far can this group advance in 1 round
        //this.range = b09 & 0x0f // value ignored, storage for range (distance from party)
        final int b0a = data.getUnsignedByte(offset + 0x0a);
        this.gender = Gender.of((b0a & 0xc0) >> 6).orElseThrow();
        this.unknown0b = data.getUnsignedByte(offset + 0x0b);
        this.xpReward = new PowerInt(data.getByte(offset + 0x0c)).plus(1);
        final int b0d = data.getUnsignedByte(offset + 0x0d);
        final int b0e = data.getUnsignedByte(offset + 0x0e);
        this.flags = new ArrayList<>();
        if ((b0e & 0x08) > 0) this.flags.add("Undead");
        if ((b0e & 0x01) > 0) this.flags.add("Vowel");
        final int b0f = data.getUnsignedByte(offset + 0x0f);

        this.actions = new ArrayList<>();
        for (int o = 0x10; o < 0x1f; o += 3) {
            if (data.getUnsignedByte(offset + o) != 0xff) {
                actions.add(Action.decode(data.getBytes(offset + o, 3)));
            }
        }

        final int b1f = data.getUnsignedByte(offset + 0x1f);
        this.confidence = b1f & 0x1f;
        final int b20 = data.getUnsignedByte(offset + 0x20);

        final StringDecoder sd = new StringDecoder(this.lookupTable, this.data);
        sd.decodeString(offset + 0x21);
        this.name = sd.getDecodedString();


        final int b22 = (b0a >> 6) & 0x03;
        final int b23 = (b0a >> 5) & 0x01;
        // overwritten by Encounter, unless that number is zero, in which case this is the random max group size
        this.varGroupSize = b0a & 0x1f;

        final int b24 = (b20 >> 5) & 0x07;
        final boolean b25 = ((b20 >> 4) & 0x01) > 0;

        final int b26 = (b1f >> 5) & 0x07;

        // final int b27 = 0x0; // storage for temp AV modifier
        // final int b28 = 0x0; // storage for temp DV modifier

        return this;
    }

    public int getOffset() {
        return offset;
    }

    public void addConfidence(int a) {
        this.confidence += a;
    }

    @Override
    public String toString() {
        List<String> tokens = new ArrayList<>();
        tokens.add(this.name);
        tokens.add(this.gender.getPronouns());
        tokens.add("max:" + varGroupSize);
        tokens.add("HD:" + varHealth + "+" + baseHealth + " (" +
            (baseHealth + varHealth.getNum()) + "-" +
            (baseHealth + (varHealth.getNum() * varHealth.getSides())) + ")");
        tokens.add("DEX:" + this.dexterity);
        tokens.add("spd:" + this.speed + "0'");
        tokens.add("XP:" + xpReward.toInteger());
        tokens.addAll(this.flags);

        StringBuilder response = new StringBuilder();
        response.append(String.join(", ", tokens));
        for (Action a : this.actions) {
            response.append("\n      ");
            response.append(a.toString());
        }
        response.append("\n      ");
        response.append(raw.stream().map(x -> String.format("%02x", x)).collect(Collectors.joining(" ")));
        return response.toString();
    }

    /* 0x0b:
     *   case 0x03, 0x09, 0x24, 0x13, 0x38 -> add 1 to heap[6c] and 1+random(0x28) to heap[6a]
     */

    public enum Bravery {
        // Bravery is calculated from the difference between a monster's base bravery and the average party level.
        // If the base bravery is negative, or the party's average is 4+ higher than the monster's base, HALP.
        // Improve one level for each 4 points (party 0-4 higher, monster 1-5 higher, monster 6+ higher).
        // If no actions with this bravery level match, try one better. There should always be a GOOD/ALWAYS.
        HALP(3),
        EDGY(2),
        OKAY(1),
        GOOD(0);

        private final int index;

        Bravery(int index) {
            this.index = index;
        }

        public static Optional<Bravery> of(int index) {
            return Arrays.stream(Bravery.values()).filter(x -> x.index == index).findFirst();
        }
    }

    public enum Sentiment {
        ALWAYS(0),   // always matches
        DAMAGED(1),  // if monster has been damaged
        CLOSE(2),    // if party is within 10'
        ATTACKED(3); // if monster has been attacked (but not necessarily hit)

        private final int index;

        Sentiment(int index) {
            this.index = index;
        }

        public static Optional<Sentiment> of(int index) {
            return Arrays.stream(Sentiment.values()).filter(x -> x.index == index).findFirst();
        }
    }

    public static class Action {
        final Bravery bravery;
        final Sentiment sentiment;
        final int action;

        private Action(int bravery, int sentiment, int action) {
            this.bravery = Bravery.of(bravery).orElseThrow();
            this.sentiment = Sentiment.of(sentiment).orElseThrow();
            this.action = action;
        }

        public String toString() {
            return String.format("%s/%s:%02x", this.bravery, this.sentiment, this.action);
        }

        public static Action decode(List<Byte> bytes) {
            final int b0 = bytes.get(0);
            final int bravery = (b0 & 0xc0) >> 6;
            final int sentiment = (b0 & 0x30) >> 4;
            final int action = (b0 & 0x0f);

            final Action newAction;
            switch (action) {
                case 0, 1, 2, 3, 4 -> {
                    final WeaponDamage damageDie = new WeaponDamage(bytes.get(1));
                    final int range = (bytes.get(2) & 0xf0) >> 4;
                    newAction = new AttackAction(bravery, sentiment, action, damageDie, (range == 0) ? 1 : range);
                }
                case 5 -> newAction = new BlockAction(bravery, sentiment);
                case 6 -> newAction = new DodgeAction(bravery, sentiment);
                case 7 -> newAction = new FleeAction(bravery, sentiment, bytes.get(1));
                case 8 -> {
                    final boolean selfTarget = (bytes.get(1) & 0x80) > 0;
                    final int spellId = bytes.get(1) & 0x7f;
                    final int cost = bytes.get(2);
                    newAction = new CastAction(bravery, sentiment, action, selfTarget, spellId, cost);
                }
                case 9 -> {
                    final WeaponDamage damageDie = new WeaponDamage(bytes.get(1));
                    final int range = (bytes.get(2) & 0xf0) >> 4;
                    newAction = new BreathAction(bravery, sentiment, damageDie, (range == 0) ? 15 : range);
                }
                case 0xa -> newAction = new CallForHelpAction(bravery, sentiment, bytes.get(1), bytes.get(2));
                default -> newAction = new PassAction(bravery, sentiment, action);
            }
            return newAction;
        }
    }

    public static class AttackAction extends Action {
        final WeaponDamage damage;
        final int range;

        public AttackAction(int bravery, int modifier, int action, WeaponDamage damage, int range) {
            super(bravery, modifier, action);
            this.damage = damage;
            this.range = (range == 0) ? 1 : range;
        }

        public String toString() {
            final String dmgMod;
            switch (action) {
                case 1 -> dmgMod = " piercing";
                case 2 -> dmgMod = " stun"; // no health damage
                case 3 -> dmgMod = " / 4";
                case 4 -> dmgMod = " / 2 health"; // no stun damage
                default -> dmgMod = ""; // damage is normally 100% Stun and 50% Health
            }
            return String.format("%s/%s:Attack(%s%s, %d0')", this.bravery, this.sentiment,
                this.damage, dmgMod, this.range);
        }
    }

    public static class BreathAction extends AttackAction {
        public BreathAction(int bravery, int modifier, WeaponDamage damage, int range) {
            super(bravery, modifier, 0x9, damage, (range == 0) ? 16 : 0);
        }

        public String toString() {
            return String.format("%s/%s:Breath(%s, %d0')", this.bravery, this.sentiment, this.damage, this.range);
        }
    }

    public static class BlockAction extends Action {
        public BlockAction(int bravery, int modifier) {
            super(bravery, modifier, 0x5);
        }

        public String toString() {
            return String.format("%s/%s:Block", this.bravery, this.sentiment);
        }
    }

    public static class CallForHelpAction extends Action {
        final int luck;
        int numAllies;

        public CallForHelpAction(int bravery, int modifier, int luck, int allies) {
            super(bravery, modifier, 0x0a);
            this.luck = luck;
            this.numAllies = allies;
        }

        public String toString() {
            return String.format("%s/%s:Call(%d%%, %d)", this.bravery, this.sentiment, this.luck, this.numAllies);
        }
    }

    public static class CastAction extends Action {
        final boolean selfTarget;
        final int spellId;
        final int power;

        public CastAction(int bravery, int modifier, int action, boolean selfTarget, int spellId, int power) {
            super(bravery, modifier, action);
            this.selfTarget = selfTarget;
            this.spellId = spellId;
            this.power = power;
        }

        public String toString() {
            final String spellName = Lists.SPELL_NAMES[spellId / 8][spellId % 8];
            return String.format("%s/%s:Cast(%s, pow:%d%s)", this.bravery, this.sentiment, spellName,
                this.power, (this.selfTarget) ? "" : ", target");
        }
    }

    public static class DodgeAction extends Action {
        public DodgeAction(int bravery, int modifier) {
            super(bravery, modifier, 0x6);
        }

        public String toString() {
            return String.format("%s/%s:Dodge", this.bravery, this.sentiment);
        }
    }

    public static class FleeAction extends Action {
        final int luck;

        public FleeAction(int bravery, int modifier, int luck) {
            super(bravery, modifier, 0x7);
            this.luck = luck;
        }

        public String toString() {
            return String.format("%s/%s:Flee(%d%%)", this.bravery, this.sentiment, this.luck);
        }
    }

    public static class PassAction extends Action {
        public PassAction(int bravery, int modifier, int action) {
            super(bravery, modifier, action);
        }

        public String toString() {
            return String.format("%s/%s:Pass", this.bravery, this.sentiment);
        }
    }

    public static void main(String[] args) {
        final int chunkId;
        final int baseAddress;
        try {
            chunkId = Integer.parseInt(args[0].substring(2), 16);
            baseAddress = Integer.parseInt(args[1].substring(2), 16);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Insufficient arguments");
        }

        final String basePath = Properties.getInstance().basePath();

        try (
            final RandomAccessFile exec = new RandomAccessFile(basePath + "DRAGON.COM", "r");
            final RandomAccessFile data1 = new RandomAccessFile(basePath + "DATA1", "r");
            final RandomAccessFile data2 = new RandomAccessFile(basePath + "DATA2", "r");
        ) {
            final StringDecoder.LookupTable table = new StringDecoder.LookupTable(exec);
            final ChunkTable chunkTable = new ChunkTable(data1, data2);
            Chunk chunk = chunkTable.getChunk(chunkId);
            if (chunkId >= 0x1e) {
                final HuffmanDecoder mapDecoder = new HuffmanDecoder(chunk);
                final List<Byte> decodedMapData = mapDecoder.decode();
                chunk = new Chunk(decodedMapData);
            }
            final Monster monster = new Monster(chunk, table);
            monster.decode(baseAddress);
            System.out.println(monster);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
