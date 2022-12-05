package com.hitchhikerprod.dragonwars.data;

import java.util.ArrayList;
import java.util.List;

public class ItemEvent extends SpecialEvent {
    final int itemIndex;

    public ItemEvent(int itemIndex, int eventId, int metaprogramIndex) {
        super(0x80, eventId, metaprogramIndex);
        this.itemIndex = itemIndex;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    @Override
    public String toString() {
        final List<String> fields = new ArrayList<>();
        fields.add(String.format("itemIndex=%02x", itemIndex & 0xff));
        fields.add(String.format("eventId=%02x", eventId & 0xff));
        fields.add(String.format("metaprogramIndex=%02x", 1 + (metaprogramIndex & 0xff)));
        return "ItemEvent<" + String.join(",", fields) + ">";
    }
}
