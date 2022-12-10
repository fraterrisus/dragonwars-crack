package com.hitchhikerprod.dragonwars.data;

import java.util.ArrayList;
import java.util.List;

public class SpellEvent extends SpecialEvent {
    final String spellName;

    public SpellEvent(int header, int eventId, int metaprogramIndex) {
        super(header, eventId, metaprogramIndex);
        this.spellName = Lists.SPELL_NAMES[header / 8][header % 8];
    }

    @Override
    public String toString() {
        final List<String> fields = new ArrayList<>();
        fields.add(spellName);
        fields.add(String.format("eventId=%02x", eventId & 0xff));
        fields.add(String.format("metaprogramIndex=%02x", 1 + (metaprogramIndex & 0xff)));
        return "Spell<" + String.join(", ", fields) + ">";
    }
}
