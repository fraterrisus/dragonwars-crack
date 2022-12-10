package com.hitchhikerprod.dragonwars.data;

import java.util.ArrayList;
import java.util.List;

public class SkillEvent extends SpecialEvent {
    final String skillName;

    public SkillEvent(int header, int eventId, int metaprogramIndex) {
        super(header, eventId, metaprogramIndex);
        this.skillName = Lists.REQUIREMENTS[header - 0x8c];
    }

    @Override
    public String toString() {
        final List<String> fields = new ArrayList<>();
        fields.add(skillName);
        fields.add(String.format("eventId=%02x", eventId & 0xff));
        fields.add(String.format("metaprogramIndex=%02x", 1 + (metaprogramIndex & 0xff)));
        return "Skill<" + String.join(", ", fields) + ">";
    }
}
