package com.hitchhikerprod.dragonwars.data;

import com.hitchhikerprod.dragonwars.Chunk;
import com.hitchhikerprod.dragonwars.ChunkTable;
import com.hitchhikerprod.dragonwars.HuffmanDecoder;
import com.hitchhikerprod.dragonwars.Properties;
import com.hitchhikerprod.dragonwars.StringDecoder;

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
    private int strength; // ?
    private int dexterity;
    private int intelligence; // ?
    private int spirit; // ?
    private int baseHealth;
    private int avBonus;
    private int dvBonus; // ?
    private WeaponDamage varHealth;
    private int confidence;
    private int varGroupSize;
    private int speed;
    private int attacksPerRound;

    private int b05;
    private int b0d;
    private int b0f;
    private int b20;
    private boolean b23;
    private int b24;

    private List<String> flags;
    private List<CombatAction> actions;

    private int imageChunk;
    private PowerInt xpReward;

    private List<Byte> raw;

    public Monster(Chunk data, StringDecoder.LookupTable table) {
        this.data = data;
        this.lookupTable = table;
    }

    public Monster decode(int offset) {
        this.offset = offset;
        this.flags = new ArrayList<>();

        raw = data.getBytes(offset, 0x21);

        this.strength = data.getUnsignedByte(offset);

        this.dexterity = data.getUnsignedByte(offset + 0x01);

        this.intelligence = data.getUnsignedByte(offset + 0x02);

        this.spirit = data.getUnsignedByte(offset + 0x03);

        this.baseHealth = data.getUnsignedByte(offset + 0x04);

        this.b05 = data.getUnsignedByte(offset + 0x05);

        this.avBonus = data.getUnsignedByte(offset + 0x06);

        this.dvBonus = data.getUnsignedByte(offset + 0x07);

        this.varHealth = new WeaponDamage(data.getByte(offset + 0x08));

        final int b09 = data.getUnsignedByte(offset + 0x09);
        this.speed = (b09 >> 4) & 0x0f; // g[21]; how far can this group advance in 1 round
        //this.range = b09 & 0x0f // value ignored, storage for range (distance from party)

        final int b0a = data.getUnsignedByte(offset + 0x0a);
        // **------ : gender [22]
        // --*----- : unknown [23]
        // ---***** : group size [0a], but only if random
        this.gender = Gender.of(b0a >> 6).orElseThrow();
        this.b23 = (b0a & 0x20) > 0;
        this.varGroupSize = b0a & 0x1f;

        this.imageChunk = data.getUnsignedByte(offset + 0x0b);
        switch (imageChunk) {
            case 0x09, 0x0e, 0x13, 0x24, 0x38 -> flags.add("awards gold");
        }

        this.xpReward = new PowerInt(data.getByte(offset + 0x0c)).plus(1);

        this.b0d = data.getUnsignedByte(offset + 0x0d);

        final int b0e = data.getUnsignedByte(offset + 0x0e);
        if ((b0e & 0x80) > 0) this.flags.add("Flag [0e].0x80");
        if ((b0e & 0x40) > 0) this.flags.add("Flag [0e].0x40");
        if ((b0e & 0x20) > 0) this.flags.add("Flag [0e].0x20");
        if ((b0e & 0x10) > 0) this.flags.add("Flag [0e].0x10");
        if ((b0e & 0x08) > 0) this.flags.add("Undead");
        if ((b0e & 0x04) > 0) this.flags.add("Flag [0e].0x04");
        if ((b0e & 0x02) > 0) this.flags.add("Flag [0e].0x02");
        if ((b0e & 0x01) > 0) this.flags.add("Vowel");

        this.b0f = data.getUnsignedByte(offset + 0x0f);

        this.actions = new ArrayList<>();
        for (int o = 0x10; o < 0x1f; o += 3) {
            if (data.getUnsignedByte(offset + o) != 0xff) {
                actions.add(CombatAction.decode(data.getBytes(offset + o, 3)));
            }
        }

        final int b1f = data.getUnsignedByte(offset + 0x1f);
        // ***----- : attacks per round [26]
        // ---***** : morale
        this.attacksPerRound = b1f >> 5;
        this.confidence = b1f & 0x1f;

        final int b20 = data.getUnsignedByte(offset + 0x20);
        // ***----- : unknown [24]
        // ---*---- : can't be disarmed [25]
        // ----**** : unknown [20]
        this.b24 = b20 >> 5;
        if ((b20 & 0x80) > 0) this.flags.add("can't be disarmed");
        this.b20 = b20 & 0x0f;

        final StringDecoder sd = new StringDecoder(this.lookupTable, this.data);
        sd.decodeString(offset + 0x21);
        this.name = sd.getDecodedString();

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
        return longString();
    }

    public String shortString() {
        final StringBuilder response = new StringBuilder("MON");
        response.append(String.format(" %02x", this.strength));
        response.append(String.format(" %02x", this.dexterity));
        response.append(String.format(" %02x", this.intelligence));
        response.append(String.format(" %02x", this.spirit));
        response.append(String.format(" %02x", this.baseHealth));
        response.append(String.format(" %02x", this.b05));
        response.append(String.format(" %02x", this.avBonus));
        response.append(String.format(" %02x", this.dvBonus));
        response.append(String.format(" %02x", raw.get(0x08)));
        response.append(" 00"); // range
        response.append(String.format(" %02x", this.varGroupSize));
        response.append(String.format(" %02x", this.imageChunk));
        for (int i = 0x0c; i <= 0x1e; i++) {
            response.append(String.format(" %02x", raw.get(i)));
        }
        response.append(String.format(" %02x", this.confidence));
        response.append(String.format(" %02x", this.b20));
        response.append(String.format(" %02x", this.speed));
        response.append(String.format(" %02x", (raw.get(0x0a) >> 6) & 0x3));
        response.append(String.format(" %02x", this.b23 ? 1 : 0));
        response.append(String.format(" %02x", this.b24));
        response.append(String.format(" %02x", ((raw.get(0x20) & 0x80) > 0) ? 1 : 0));
        response.append(String.format(" %02x", this.attacksPerRound));
        response.append(" ").append(this.name);
        return response.toString();
    }

    public String longString() {
        List<String> tokens = new ArrayList<>();
        tokens.add(this.name + " (" + this.gender.getPronouns() + ") [#" + varGroupSize + "]");
        tokens.add(String.format("STR %02d DEX %02d INT %02d SPR %02d",
            this.strength, this.dexterity, this.intelligence, this.spirit));
        tokens.add("HD:" + varHealth + "+" + baseHealth + " (" +
            (baseHealth + varHealth.getNum()) + "-" +
            (baseHealth + (varHealth.getNum() * varHealth.getSides())) + ")");
        tokens.add(String.format("AV%+d DV%+d", this.avBonus, this.dvBonus));
        tokens.add("att:" + (this.attacksPerRound + 1));
        tokens.add("morale:" + this.confidence);
        tokens.add("spd:" + this.speed + "0'");
        tokens.add("XP:" + xpReward.toInteger());
        tokens.addAll(this.flags);

        final String imageName = Lists.MONSTER_IMAGES[imageChunk];
        tokens.add("image:" + imageName);

        if (b05 != 0) { tokens.add(String.format("[05]:0x%02d", b05)); }
        if (b0d != 0) { tokens.add(String.format("[0d]:0x%02d", b0d)); }
        if (b0f != 0) { tokens.add(String.format("[0f]:0x%02d", b0f)); }
        if (b20 != 0) { tokens.add(String.format("[20]:0x%02d", b20)); }
        if (b23)      { tokens.add("Flag [0a].0x20"); }
        if (b24 != 0) { tokens.add(String.format("[24]:0x%02d", b24)); }

        StringBuilder response = new StringBuilder();
        response.append(String.join(", ", tokens));
        for (CombatAction a : this.actions) {
            response.append("\n      ");
            response.append(a.toString());
        }
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

    public static class CombatAction {
        final Bravery bravery;
        final Sentiment sentiment;
        final int action;

        private CombatAction(int bravery, int sentiment, int action) {
            this.bravery = Bravery.of(bravery).orElseThrow();
            this.sentiment = Sentiment.of(sentiment).orElseThrow();
            this.action = action;
        }

        public String toString() {
            return String.format("%s/%s:%02x", this.bravery, this.sentiment, this.action);
        }

        public static CombatAction decode(List<Byte> bytes) {
            final int b0 = bytes.get(0);
            final int bravery = (b0 & 0xc0) >> 6;
            final int sentiment = (b0 & 0x30) >> 4;
            final int action = (b0 & 0x0f);

            final CombatAction newAction;
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

    public static class AttackAction extends CombatAction {
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

    public static class BlockAction extends CombatAction {
        public BlockAction(int bravery, int modifier) {
            super(bravery, modifier, 0x5);
        }

        public String toString() {
            return String.format("%s/%s:Block", this.bravery, this.sentiment);
        }
    }

    public static class CallForHelpAction extends CombatAction {
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

    public static class CastAction extends CombatAction {
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

    public static class DodgeAction extends CombatAction {
        public DodgeAction(int bravery, int modifier) {
            super(bravery, modifier, 0x6);
        }

        public String toString() {
            return String.format("%s/%s:Dodge", this.bravery, this.sentiment);
        }
    }

    public static class FleeAction extends CombatAction {
        final int luck;

        public FleeAction(int bravery, int modifier, int luck) {
            super(bravery, modifier, 0x7);
            this.luck = luck;
        }

        public String toString() {
            return String.format("%s/%s:Flee(%d%%)", this.bravery, this.sentiment, this.luck);
        }
    }

    public static class PassAction extends CombatAction {
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
