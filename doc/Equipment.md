# Equipment

Items are represented as a series of 11 bytes of data followed by an ASCII [string](Strings.md) (the item name). Items in a character's inventory always have their name padded out to 12 bytes; everything after the terminal byte is ignored. Items that appear elsewhere in the data file often don't have padding.

| Byte  | Bitfield  | Function |
| :---: | :-------: | -------- |
| `[00]` | `*–––––––` | If 1, item is currently **equipped** |
| `[00]` | `–*––––––` | If 1, the item **disappears** when it runs out of charges/uses |
| `[00]` | `––******` | Number of **charges**/uses left (0x3f = infinite) |
| `[01]` | `*–––––––` | If 1, item's AV modifier is _negative_ |
| `[01]` | `–*––––––` | If 1, item's AC modifier is _negative_ |
| `[01]` | `––******` | Required **attribute** or [skill](Skills.md) |
| `[02]` | `**––––––` | Supports **burst fire** (missile weapons only) |
| `[02]` | `––*–––––` | _Unknown_<br />*Items with 0x1: Arrow, Grey Arrow (Kingshome only), IBM PS/2, Magic Arrow, Silver Arrow (but not the White Arrow or the Guard Bridge Grey Arrow)* |
| `[02]` | `–––*****` | Required **value**; if 0, no requirement |
| `[03]` | `****––––` | **AV modifier** |
| `[03]` | `––––****` | **AC modifier** |
| `[04]` | `***–––––` | **Purchase price** exponent |
| `[04]` | `–––*****` | **Purchase price** base |
| `[05]` | `***–––––` | _Unknown_, but always zero |
| `[05]` | `–––*****` | **Item type** |
| `[06]` |           | **Magical effect** (first byte) |
| `[07]` |           | **Magical effect** (second byte) |
| `[08]` | `***–––––` | Damage die (primary) **sides** |
| `[08]` | `–––*****` | Damage die (primary) **count** |
| `[09]` |           | Damage die (secondary), if non-zero |
| `[0a]` | `****––––` | **Ammo type** (bow and ammo must match) |
| `[0a]` | `––––****` | Weapon **range** (x 10') |

There are plenty of items that have a -0 AC or AV "bonus"; that is, bit 8 or 9 is set to indicate a negative value even though the corresponding field in byte `[03]` is zero. I'm not sure why this is. Otherwise, there are very few items that *actually* have a negative AC modifier: the slave Chains, both Boomerangs, and the Heavy Sword.

## Burst Fire

The Tri-Cross supports "Burst" mode (fires three bolts); the Gatlin Bow supports "Burst" and "Full Auto" (empties the entire quiver). The Lance Sword and the Long Mace both have this bit set, which implies they support Full Auto somehow, but the code doesn't look at this byte for anything that doesn't call itself a missile weapon. (I'll be curious to see if I find another place where that value is checked.)

| Byte `[02]` | Weapons                            | Firing Modes                       |
| ----------- | ---------------------------------- | ---------------------------------- |
| `0b00`      | most weapons                       | Single                             |
| `0b01`      | Tri-Cross                          | Single, Burst (3)                  |
| `0b10`      | Gatlin Bow, Lance Sword, Long Mace | Single, Burst (3), Full Auto (all) |

## Purchase Price

Stored in a exponent (3b) and mantissa (5b) format; the value is M x 10^E. This doesn't give you perfect representation of every possible number, but it does well enough for these purpose. (Also, remember that these are purchase prices; sale prices are half of this.) Here are a few examples:

| Byte `[04]` | Exponent    | Mantissa       | Value                    |
| :---------: | ----------- | -------------- | ------------------------ |
|   `0x00`    |             |                | Item can't be sold       |
|   `0x08`    | `0b000` = 0 | `0b01000` = 8  | 8 x 10^0 = $8            |
|   `0x48`    | `0b010` = 2 | `0b01000` = 8  | 8 x 10^2 = $800          |
|   `0xff`    | `0b111` = 7 | `0b11111` = 31 | 31 x 10^7 = $310,000,000 |

## Item Types

The entire byte could be used, but every time the Item Type is checked in the code, it only looks at the bottom five bits (`0x1f`). It's possible that the upper three bits have another use, but (a) I haven't found it yet (b) every Item in the game has those three bits set to zero, so it's unlikely. Unsurprisingly, Item Types are in the same order as the weapon [skills](Skills.md) they relate to.

| Byte `[05]` | Item Type             | Conflicts with                                               |
| :---------: | --------------------- | ------------------------------------------------------------ |
|   `0x00`    | General Item          |                                                              |
|   `0x01`    | Shield                | Shield, Full Shield                                          |
|   `0x02`    | Full Shield           | Shield, Full Shield, Two-hander, Bow                         |
|   `0x03`    | Axe                   | all melee, all missile, Thrown weapon, Ammunition            |
|   `0x04`    | Flail                 | all melee, all missile, Thrown weapon, Ammunition            |
|   `0x05`    | Sword                 | all melee, all missile, Thrown weapon, Ammunition            |
|   `0x06`    | Two-hander            | Full Shield, all melee, all missile, Thrown weapon, Ammunition |
|   `0x07`    | Mace                  | all melee, all missile, Thrown weapon, Ammunition            |
|   `0x08`    | Bow                   | Full Shield, all melee, all missile, Thrown weapon, Ammunition |
|   `0x09`    | Crossbow              | all melee, all missile, Thrown weapon, Ammunition            |
|   `0x0a`    | Gun (?!)              | all melee, all missile, Thrown weapon, Ammunition            |
|   `0x0b`    | Thrown weapon         | all melee, all missile, Thrown weapon, Ammunition            |
|   `0x0c`    | Ammunition            | Ammunition                                                   |
|   `0x0d`    | Gloves                | Gloves, Mage Gloves                                          |
|   `0x0e`    | Mage Gloves           | Gloves, Mage Gloves                                          |
|   `0x0f`    | Ammo Clip (?!)        | Ammo Clip                                                    |
|   `0x10`    | Cloth armor           | all body armor                                               |
|   `0x11`    | Leather armor         | all body armor                                               |
|   `0x12`    | Cuir Bouilli armor    | all body armor                                               |
|   `0x13`    | Brigandine armor      | all body armor                                               |
|   `0x14`    | Scale armor           | all body armor                                               |
|   `0x15`    | Chain armor           | all body armor                                               |
|   `0x16`    | Plate and Chain armor | all body armor                                               |
|   `0x17`    | Full Plate armor      | all body armor                                               |
|   `0x18`    | Helmet                | Helmet                                                       |
|   `0x19`    | Scroll                | Scroll                                                       |
|   `0x1a`    | Pair of Boots         | Boots                                                        |

No, there aren't actually any Guns in *Dragon Wars*. And there's no game difference between the different types of body armor.

The matrix of item types that conflict with each other is stored at `{02:00e0}` in a bitfield format. Index that array with the item type above. Bytes `[00:01] ` are a bitfield of other item types that this type conflicts with; shift that value over by byte `[02]`. (There are only `0x1a` item types, so `[02]` will either be `00` or `02`.)

Example: Full Shields are item type 02. Its matrix value is `62 80 00`. Since byte `[00]=0` , don't shift it. That gives the bitmask `0110 1000 1000 0000 0000 0000 000– ––––` (again, there's only `0x1a` item types). Bits 1, 2, 4, and 8 are set, which means Full Shields conflict with Shields, Full Shields, Two-handers, and Bows.

Example: All body armor (type `0x10-0x17`) has the same matrix value, `ff 00 02`. Shift over two bytes for the bitmask `0000 0000 0000 0000 1111 1111 000– ––––`. So all body armor types conflict with the other body armor types.

The odd one to point out here is that Bows conflict with Ammunition, but Ammunition doesn't conflict with Bows. So to load a missile weapon, you have to pick the missile weapon first, then equip the relevant quiver.

## Magical Effects

Encoded across two bytes `[06-07]`. The first byte always means the same things:

|  Byte `[06]`   | Recharge? | Effect                                                       |
| :------------: | :-------: | ------------------------------------------------------------ |
| `0b 0000 0000` |    yes    | No magical effect                                            |
| `0b 00–– ––––` |    yes    | Item casts a [spell](Spells.md) when you **(U)se** it; spell ID is in lower six bits,<br />and spell power is in byte `[07]` |
| `0b 1000 0000` |    no     | No magical effect                                            |
| `0b 1000 0001` |    yes    | No magical effect                                            |
| `0b 1000 0010` |    yes    | No magical effect                                            |
| `0b 1000 0011` |    yes    | Item teaches you a spell; spell ID is in byte `[07]`         |
| `0b 1000 0100` |    no     | Item restores Power; amount restored is in byte `[07]`       |
| `0b 1––– ––––` |    no     | Other combinations are impossible / unexpected / programming errors<br />(See jump table at `{06:01d9}`) |

Note that you **can** add charges to spell scrolls with *S:Charger*. Just make sure you add two charges before you use it, or the scroll will disappear after the first use (see byte `[00].0x40`).

Learning a spell from a scroll is always successful, so long as you have at least 1 rank in the associated magic skill (see `{06:01f6}`. The check is skipped for Miscellaneous Magic, since there's no skill for that. However, the scrolls themselves also have skill requirements; the Miscellaneous Magic scrolls require *Low Magic 1* or you can't even use them to learn the spell.

If the item has no magical effect, byte `[07]` sometimes is used to hold a flag that indicates that the item has been modified. For example, the Battered Cup that you find in the Slave Mines sets this byte to `0x1` to indicate that you've filled it with water. Similarly, the Papers you're supposed to get stamped in Lansk set this byte when you do. (Interestingly, the Dead Body you collect during the end game does *not* work this way...)

## Damage Dice

Encoded with the number of sides in the high three bits:

|  Bits   | Sides |
| :-----: | :---: |
| `0b000` |  d4   |
| `0b001` |  d6   |
| `0b010` |  d8   |
| `0b011` |  d10  |
| `0b100` |  d12  |
| `0b101` |  d20  |
| `0b110` |  d30  |
| `0b111` | d100  |

... and the number of dice to be rolled in the bottom five, plus one (so `0b00000` = 1 die and `0b00111` = 8 dice). The code `{03:06e8}` allows five bits of dice (up to 32) to be rolled, but no weapon has more than 7. (Except for the Gatlin Bow on Full Auto...)

## Secondary Damage

Melee weapons with range (the Axe of Kalah, Long Mace, and Throw Mace) have two different damage ratings. Primary damage is rolled for attacks at 10', and secondary damage is rolled for anything longer than that.

Weirdly, the Druid Mace has a Secondary Damage die of 1d20, but no Primary Damage die. (Technically `0x00` = 1d4, but somehow the game knows not to do that?)

## Ammo Type

*Dragon Wars* only has two types of missile weapons: bows and crossbows. When you try to load (equip a unit of ammunition), the ammo item's Ammo Type field must match that of the weapon:

| Byte `[09].0xf0` | Type            |
| ---------------- | --------------- |
| `0x0`            | Bow / Arrow     |
| `0x1`            | Crossbow / Bolt |

Given the hints of the existence of Guns in this engine, and knowing what I know of *Wasteland*, I'm guessing this field gets a lot more interesting if you need more types of ammunition.

The weird thing is that the Fire Spear (a thrown weapon) also has its ammo type field set to `0x1`. You can't equip a Crossbow and the Fire Spear at the same time, nor can you equip the Fire Spear and a set of Bolts.

## Range

Most melee weapons are listed as having a 10' range, but a few outliers (Druid Mace, Runed Flail, Magic Axe, etc.) don't have any range at all. It doesn't stop you from using them in melee; when the game checks if your target group is in range for your weapon, it rounds up to a minimum of 10'.

Even more weirdly, the Fire Shield has a 40' range and deals 1d12 damage, but can't be used as a weapon as far as I can tell. And there's a pair of Gauntlets with a 10' range, although it doesn't seem to affect your unarmed damage at all. The Shovel from the Phoebus Dungeon also has a 10' range, although you can't use it as a weapon.
