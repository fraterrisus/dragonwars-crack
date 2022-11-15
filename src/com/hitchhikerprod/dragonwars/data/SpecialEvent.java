package com.hitchhikerprod.dragonwars.data;

import java.util.ArrayList;
import java.util.List;

public class SpecialEvent {
    final int header;
    final int eventId;
    final int metaprogramIndex;

    public SpecialEvent(int header, int eventId, int metaprogramIndex) {
        this.header = header;
        this.eventId = eventId;
        this.metaprogramIndex = metaprogramIndex;
    }

    public int getHeader() {
        return header;
    }

    public int getEventId() {
        return eventId;
    }

    public int getMetaprogramIndex() {
        return metaprogramIndex;
    }

    @Override
    public String toString() {
        final List<String> fields = new ArrayList<>();
        fields.add(String.format("header=%02x", header & 0xff));
        fields.add(String.format("eventId=%02x", eventId & 0xff));
        fields.add(String.format("metaprogramIndex=%02x", metaprogramIndex & 0xff));
        return "SpecialEvent<" + String.join(",", fields) + ">";
    }
}
