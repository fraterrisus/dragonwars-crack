package com.hitchhikerprod.dragonwars.data;

public class Lists {
    static final String[] SKILL_NAMES = { "Arcane Lore", "Cave Lore", "Forest Lore", "Mountain Lore",
            "Town Lore", "Bandage", "Climb", "Fistfighting", "Hiding", "Lockpick", "Pickpocket", "Swim", "Tracker",
            "Bureaucracy", "Druid Magic", "High Magic", "Low Magic", "Merchant" /* ?! */, "Sun Magic", "Axes", "Flails",
            "Maces", "Swords", "Two-handers", "Bows", "Crossbows", "Thrown Weapons" };

    static final String[][] SPELL_NAMES = {
            { "L:Mage Fire", "L:Disarm", "L:Charm", "L:Luck", "L:Lesser Heal",
                    "L:Mage Light", "H:Fire Light", "H:Elvar's Fire" },
            { "H:Poog's Vortex", "H:Ice Chill", "H:Big Chill", "H:Dazzle",
                    "H:Mystic Might", "H:Reveal Glamour", "H:Sala's Swift", "H:Vorn's Guard" },
            { "H:Cowardice", "H:Healing", "H:Group Heal", "H:Cloak Arcane",
                    "H:Sense Traps", "H:Air Summon", "H:Earth Summon", "H:Water Summon" },
            { "H:Fire Summon", "D:Death Curse", "D:Fire Blast", "D:Insect Plague",
                    "D:Whirl Wind", "D:Scare", "D:Brambles", "D:Greater Healing" },
            { "D:Cure All", "D:Create Wall", "D:Soften Stone", "D:Invoke Spirit",
                    "D:Beast Call", "D:Wood Spirit", "S:Sun Stroke", "S:Exorcism" },
            { "S:Rage of Mithras", "S:Wrath of Mithras", "S:Fire Storm", "S:Inferno",
                    "S:Holy Aim", "S:Battle Power", "S:Column of Fire", "S:Mithras' Bless" },
            { "S:Light Flash", "S:Armor of Light", "S:Sun Light", "S:Heal",
                    "S:Major Healing", "S:Disarm Trap", "S:Guidance", "S:Radiance" },
            { "S:Summon Salamander", "S:Charger", "M:Zak's Speed", "M:Kill Ray",
                    "M:Prison", "0x04", "0x02", "0x01" }
            // S:Light Flash and M:Prison are in the manual, but never found in the game.
    };

    static public final String[] REQUIREMENTS = {
        "Strength", "Strength", "Dexterity", "Dexterity",
        "Intelligence", "Intelligence", "Spirit", "Spirit",
        "-", "-", "-", "-",
        "-", "-", "-", "-",
        "-", "-", "-", "-",
        "Arcane Lore", "Cave Lore", "Forest Lore", "Mountain Lore",
        "Town Lore", "Bandage", "Climb", "Fistfighting",
        "Hiding", "Lockpick", "Pickpocket", "Swim",
        "Tracker", "Bureaucracy", "Druid Magic", "High Magic",
        "Low Magic", "Merchant", "Sun Magic", "Axes",
        "Flails", "Maces", "Swords", "Two-handers",
        "Bows", "Crossbows", "Thrown Weapons", "-",
        "-", "-", "-", "-",
        "-", "-", "-", "-",
        "-", "-", "-", "-",
        "-", "-", "-", "0xFF"
    };

    static final String[] ITEM_TYPES = {
        "General Item", "Shield", "Full Shield", "Axe",
        "Flail", "Sword", "Two-handed sword", "Mace",
        "Bow", "Crossbow", "Gun" /* ?!?! */, "Thrown weapon",
        "Ammunition", "Gloves", "Mage Gloves", "Ammo Clip",
        "Cloth Armor", "Leather Armor", "Cuir Bouilli Armor", "Brigandine Armor",
        "Scale Armor", "Chain Armor", "Plate and Chain Armor", "Full Plate Armor",
        "Helmet", "Scroll", "Boots", "-",
        "-", "-", "-", "-"
    };

    static final String[] WEAPON_TYPES = {
        "Axe", "Flail", "Sword", "Two-handed sword", "Mace", "Thrown weapon", "Ammunition"
    };
}
