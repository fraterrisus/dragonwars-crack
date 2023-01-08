# Board Data

Map data is stored in two chunks. The primary chunk (index + `0x46`) stores data about the board, the map squares, special events, and the graphics used to draw the map. The secondary chunk (index + `0x1e`) stores data about items, monsters, and encounters.

| Index | Board |
| :---: | ----- |
|  0x00 | Dilmun world map |
|  0x01 | Purgatory |
|  0x02 | Slave Camp |
|  0x03 | Guard Bridge (Forlorn/Sun) |
|  0x04 | Salvation |
|  0x05 | Tars Ruins |
|  0x06 | Phoebus |
|  0x07 | Guard Bridge (Sun/Lansk) |
|  0x08 | Mud Toad |
|  0x09 | Byzanople |
|  0x0a | Smuggler's Cove |
|  0x0b | War Bridge (Lansk/Quag) |
|  0x0c | Scorpion Bridge |
|  0x0d | Bridge of Exiles |
|  0x0e | Necropolis |
|  0x0f | Dwarf Ruins |
|  0x10 | Dwarf Clan Hall |
|  0x11 | Freeport |
|  0x12 | Magan Underworld |
|  0x13 | Slave Mines |
|  0x14 | Lansk |
|  0x15 | Sunken Ruins |
|  0x16 | Sunken Ruins Underground |
|  0x17 | Mystic Wood |
|  0x18 | Snake Pit |
|  0x19 | Kingshome |
|  0x1a | Pilgrim Dock |
|  0x1b | Nisir |
|  0x1c | Old Dock |
|  0x1d | Siege Camp |
|  0x1e | Game Preserve |
|  0x1f | Magic College |
|  0x20 | Dragon Valley |
|  0x21 | Phoeban Dungeon |
|  0x22 | Lanac'toor's Lab |
|  0x23 | Byzanople Dungeon |
|  0x24 | Kingshome Dungeon |
|  0x25 | Slave Estate |
|  0x26 | Lansk Undercity |
|  0x27 | Tars Underground |

## Primary Map Data

Primary Map Data is stored in data chunk `0x46` (Dilmun) – `0x6d` (Tars Underground). When the game loads map data, it runs the Huffman decompression routine on it and writes it to chunk `0x10` (`DATA1:0x006644`), and it's saved there when you save your game.

Byte 0: map width (max X)

Byte 1: map height (max Y). This value is 1 larger than it should be; internally, there's a list of pointers to rows of map squares, but it has an "end" pointer that gets skipped over. (For some reason the list is built backwards, so the extra pointer is *first* instead of *last*, but the point is that there's an extra pointer in the list.)

Byte 2 contains flags for the map:

| Byte`[02]` | Meaning                                                      |
| :--------: | ------------------------------------------------------------ |
| `––1–––––` | *unknown* (Kingshome Dungeon)                                |
| `–––1––––` | Map allows the use of *D:Create Wall*                        |
| `––––1–––` | Map provides light for you                                   |
| `–––––1––` | Compass is displayed                                         |
| `––––––1–` | Map wraps around                                             |
| `–––––––1` | *unknown* (Kingshome Dungeon, Slave Mines after losing your Chains) |

Byte 3: chance of a random encounter. If 0, random encounters don't occur on this map. Otherwise, the chance of an encounter is "1 in `x`", so if this value is 1, you will get a random encounter on literally every step. Larger values mean a lower chance of an encounter.

The next set of bytes are IDs of chunks containing texture data, i.e., the stuff that actually gets drawn in the gameplay area. (Texture chunks start at `0x63`; these values are offsets from that.) Bytes are read until bit `0x80` is set, which marks the end of the list. As part of parsing map data, this array is stored at `0x57e4`.

We then read two simultaneous arrays into `0x54a6` and `0x54b5`. (Only the first byte is checked for bit `0x80` and has it stripped; the array at `0x54b5` is allowed to have values larger than that.) The first byte contains an index into the texture list. The second byte contains wall metadata that indicates things like whether the wall is passable or not (for example, because it's a secret door).

We then read three more byte arrays, into `0x54c5` (ceiling), `0x54c9` (floor), and `0x54cd` ("other" decoration, like rocks and statues). These are used as indices into the array of texture list. (Note that the only ceiling textures are chunk `0x6f`, a blue sky, and 'everything else', which is just a generic red-and-black check pattern that's hardcoded at `0x5515`.)

The next word is a pointer to the (packed) string containing the name of the board.

Beyond that, we have a list of pointers to rows full of map square data. The code builds the array backwards (from highest index to lowest) and includes an "extra" pointer at the front of the list that points to the first byte after the map squares.

The actual map data follows, 3B per square (see *Map Squares*, below). Rows are arranged such that the northernmost row (highest value of Y) is first. Within a row, squares run from west (lowest X) to east (highest X).

The first entry in the list of map row pointers actually points to the next data word *after* the map squares. This word is a pointer to the start of the board's metaprogram. 

The next word is a pointer to an array that's interpreted to determine if there are any special events in the board that occur in response to player actions. This list is terminated with `0xff`.

Each event has a:
- header byte: `0x80` means this is an item event and it has an item offset byte next
- item offset byte: index into the array of pointers in secondary map data
- event byte: this must match the Special Event ID on the map square
- metaprogram pointer offset byte: used (+1) to index the list of pointers at the start of Primary map data to determine where to start executing the metaprogram 


The board name is (likely) the next thing after that.

The metaprogram copies the saved map data out of chunk 10 and overwrites the uncompressed map data, which helps restore bit `000800`.

### Map Squares

Usually stored in three adjacent bytes, i.e. `0x1166–0x1168`.

|             Bits             | Meaning                                                      |
| :--------------------------: | ------------------------------------------------------------ |
| `****–––– –––––––– ––––––––` | Texture index for north wall                                 |
| `––––**** –––––––– ––––––––` | Texture index for west wall                                  |
| `–––––––– **–––––– ––––––––` | Texture index for roof                                       |
| `–––––––– ––**–––– ––––––––` | Texture index for floor                                      |
| `–––––––– ––––*––– ––––––––` | if 1, square has been stepped on<br />*used when drawing the automap* |
| `–––––––– –––––*** ––––––––` | Texture index for decoration (bush, tree, rock, puddle, etc.) |
| `–––––––– –––––––– ********` | Special Event ID                                             |

Wall texture indices point into the arrays at `0x54a7` (texture) and `0x54b6` (metadata; see below). Wall texture `0x00` is a special case that means there's no wall there (and therefore nothing to draw). Texture index `0x1` points to the *first* value in the arrays.

### Wall Metadata

Stored in the array at `0x54b6`, and often saved to `heap[26]`.

|    Bits    | Meaning                                                      |
| :--------: | ------------------------------------------------------------ |
| `00––––––` | Wall is passable (i.e. no wall)                              |
| `10––––––` | Wall is passable, but only with Kick (i.e. an open door or secret door) |
| `–1––––––` | Wall is solid / impassable (i.e. a locked door or regular wall) |
| `––1–––––` | *D:Soften Stone* does *not* work on this wall                |
| `–––1––––` | *unknown*                                                    |
| `––––****` | Wall specials (see below)                                    |

*Purgatory*: most walls are `d1` (solid, impassable, susceptible to Soften, and ???) but the walls of the Arena are `e1` (solid, impassable, immune to Soften, and not ???).

### Wall Specials

Some maps check the wall metadata for particular values at certain times. For example, in Purgatory, when you try to climb through the hole, you must be facing a wall with bit 0x01 set, or the attempt fails. (However *all* walls in Purgatory have that bit set, so it's mostly just checking that you aren't facing open air I guess.) Other maps check 0x01, 0x02, or 0x04.

## Secondary Map Data

Secondary map data starts with a list of four pointers (two bytes each). Section 1 is for **monster groups**, which includes things like the name of the monster and their stats. Section 2 is for **encounters**, which is basically a combination of one or more monster groups. These are described below in more detail.

Section 3 stores a set of strings that are used as the "**tag line**", the color text the game prints when the encounter starts. Data is in the five-bit packed [string](Strings.md) format, but (as with most of these things) starts with a list of pointers.

Section 4 is for **items**, referenced by the "special encounters" section of Primary Map Data and stored in the same format as party [equipment](Equipment.md). It, too, starts with a list of pointers.

### Monster Groups

Byte 0: If an encounter calls for randomly generated monster groups, the game picks a random number from 0 to this value and uses that group. For example, if there are 7 monster groups in the list and this byte is 4, then a random encounter will pick from groups 0-3.

Everything after that is a two-byte pointer to a Monster Group. Groups contain 33 bytes, which gets sliced and unpacked into a 41-byte (`0x00–0x28`) array when the encounter starts, plus a variable-length name string in five-bit packed format.

| Source | Bitfield | Destination | Use |
| :----: | :------: | :---------: | --- |
| `[00]` | `––––––––` | `[00]` | *Strength?* sometimes 0, never used |
| `[01]` | `********` | `[01]` | **Dexterity** |
| `[02]` | `––––––––` | `[02]` | *Intelligence?* sometimes 0, never used |
| `[03]` | `––––––––` | `[03]` | *Spirit?* sometimes 0, never used |
| `[04]` | `********` | `[04]` | Base **Health** (added to die roll at [08]) |
| `[05]` | `––––––––` | `[05]` | *Unknown* |
| `[06]` | `********` | `[06]` | **AV bonus** |
| `[07]` | `––––––––` | `[07]` | *DV bonus?* always zero |
| `[08]` | `***–––––` | `[08]` | **Health** dice (sides) |
| `[08]` | `–––*****` | `[08]` | **Health** dice (count) |
| `[09]` | `****––––` | `[21]` | **Speed** (how far monster can advance in 1 turn) |
| `[09]` | `––––****` | `[09]` | value ignored, holds current **Range** (distance from party) |
| `[0a]` | `**––––––` | `[22]` | **Gender** (used for pronouns) |
| `[0a]` | `––*–––––` | `[23]` | *Unknown*, but always zero |
| `[0a]` | `–––*****` | `[0a]` | **Group size** |
| `[0b]` | `********` | `[0b]` | Index of **monster image** chunk (x 2 + 0x8a) |
| `[0c]` | `********` | `[0c]` | **XP** reward (uses Purchase Price format) |
| `[0d]` | `––––––––` | `[0d]` | *Unknown* |
| `[0e]` | `********` | `[0e]` | Flags: (only `0x80` is ever set) |
|        | `––––*–––` | `[0e]` | - Monsters are **Undead** (susceptible to *S:Exorcism*) |
|        | `–––––––*` | `[0e]` | - **Article** (0:'a', 1:'an') |
| `[0f]` | `––––––––` | `[0f]` | *Unknown*, but always zero |
| `[10-1e]` |  | `[10-1e]` | Combat **actions** |
| `[1f]` | `***–––––` | `[26]` | (+1) Melee **attacks per round** |
| `[1f]` | `–––*****` | `[1f]` | **Confidence level** |
| `[20]` | `***–––––` | `[24]` | *Unknown* |
| `[20]` | `–––*––––` | `[25]` | if 0, monster is immune to **Disarm** |
| `[20]` | `––––****` | `[20]` | *Unknown*, but always zero |
|       |            | `[27]` | holds temporary group **AV** modifier (from buffs, etc.) |
|       |            | `[28]` | holds temporary group **DV** modifier |

Each individual monster's health is randomly generated: the sum of the base health at `[04]` and the health dice (in the same format as equipment damage dice) at `[08]`.

If an encounter specifies a random **group size**, the group size at `[0a]` is used as a max for the roll. Regardless, this value is overwritten with the actual group size when the encounter is generated.

Gold is given for killing monsters whose image chunk is 09 (helmet and spear), 0e (guards), 13 (bloody axe), 24 (pikemen), and 38. You get $1–$40 per kill.

#### Unknown Values

**Byte [05]**

| Value           | Monster                  |
| --------------- | ------------------------ |
| `0x01.00000001` | Namtar (Nisir, 2nd)      |
| `0x02.00000010` | Namtar (Nisir, 1st)      |
| `0x03.00000011` | Namtar (Nisir, 3rd)      |
| `0x19.00011001` | Stone Demon (Necropolis) |

**Byte [0d]**

| Value           | Monster                                                      |
| --------------- | ------------------------------------------------------------ |
| `0x02.00000010` | Big Dogs (*Purgatory*), Jail Keepers (*Purgatory*), Mystalvision (*Phoebus*) |
| `0x10.00100000` | Giant Spiders (*Purgatory*)                                  |
| `0x40.01000000` | Automoton (*Dwarf Clan Hall*), Giant Snakes (*Tars, Tars Dungeon, Necropolis, Underworld, Lanac'toor*), Guards (*Dilmun*), Midget Maniacs (*Lansk Undercity*), Rats! (*Guard Bridge #1*), Rock Spiders (*Purgatory*), Snakes (*Slave Mines*), Spiders (*Purgatory*) |
| `0x42.01000010` | Adventurers (Freeport, Lansk), Bears (Scorpion Bridge), Earth Man (Mystic Wood), Gorgons (Dwarf Clan Hall), Ogres (Game Preserve), Serpent Swimmers (Sunken Ruins Topside, Sunken Ruins Below), Stosstrupen (Salvation, Phoebus, Pilgrim Dock) |
| `0x50.01010000` | Wild Dogs (Purgatory)                                        |
| `0x60.01100000` | Ghouls (Necropolis, Underworld, Lanac'toor, Tars Underground), Wolves (Purgatory) |
| `0xa9.10101001` | Spirit Ward (Slave Camp)                                     |
| `0xaa.10101010` | Man with a fever (Slave Camp)                                |

**Byte [24]**

| Value  | Monster                                                      |
| ------ | ------------------------------------------------------------ |
| `0x00` | Beach Bums (15), Cave Snakes (27), Dirty Rats (Phoebus), Drunks (Purgatory), Elementals (1e), Evil Spirits (0c), Goblin Beach Bums (16), Goblins (12), Grim Guardians (Necropolis), Guard Goblins (Dilmun), Guardian Snake (Tars), Lagooners (17), Mages (14,20), Magic Ghouls (0e,22), Midget Maniacs (26), Murk Trees (Dilmun), Namtar (Underworld), Ominous Fellow (Phoebus), Rats! (Guard Bridge #1), Rock Man (17), Rock Spiders (Purgatory), Snakes (13), Spiders (Purgatory, 05, 0e, 12, 27), Spirit Ward (Slave Camp), Spitting Lizards (05,0e), Spitting Snakes (12,22,27), Stone Demons (0e), Water Spirits (17), Wild Hounds (Purgatory), Wood Spirits (1e), Wraiths (0e, 12, 22, 27), Yonderboys (Purgatory) |
| `0x01` | Big Dogs (Purgatory), Cave Wolves (27), Crazed Old Ladies (0b), Dwarves (10), Fangers (20), Frothy Swamp Dogs (08), Goblins (05,0e,12,22,27), Mad Dogs (06), Rock Spiders (01), Scorpion Lizards (0e), Scorpion Snakes (12), Shadow Spiders (05), Snap Turtles (08), Spiders (1e), Swamp Rats (08), Vampire Wolves (12), Warriors (04), Wild Dogs (01,0c,17), Wolves (01,05) |
| `0x02` | Adventurers (11,14,27), Automaton (10), Bandit Leaders (1e), Bandits (01,1e), Bloated Corpses (15,16), Born Losers (01), Bridge Guards (03), Bridge Wolves (0c), Buck Ironhead (1b), Bush Wizards (1e), Cannibals (01), Citizens (11,14), City Militamen (23), Civil Servants (14), Crazed Miltiamen (08), Cruel Slave Boss (13), Dire Wolves (17), Doomsayers (11), Dragon warriors (20), Dungeon Guards (21), Dungeon Patrolers (21), Enforcers (1b), Escaped Slaves (02), Fanatics (01), Gaze Demon (25), Ghouls (0e,12,22,27), Gladiators (01), Goblins (00,25), Gorgons (10), Guard Captains (21), Guards (00,04,07,09,0b,11,13,14,1a,1d), Hell Hounds (1e), Innocent Men (01), Jail Keepers (01), King's Guards (01), Kingshomer Captains (1d), Lichs (1b), Lizard Warriors (1b), Long John Ugly (0a), Loons (01), Loopy Citizens (08), Man with a fever (02), Mercenaries (1d), Muscular Clowns (26), Mystal's Boys (21), Mystalvision (06,1b,21), Namtar (12,1b), Namtar Guards (1b), Old Gladiators (02), Old Guys (17), Old Jack (1e), Old Jailor (21), Patrolmen (24), Peg Leg Peggy (0a), Pikemen (01,07,09,0b,1d,26), Pilgrims (04,1a), Pirates (0a), Prince Jordan (23), Princess Myrilla (09), Princess Myrolla (23), Robbers (01,08), Royal Guards (09,1d,23), Scurvy Seadogs (0a), Sea Rats (15), Sea Snakes (16), Sea Spiders (16), Serpent Swimmers (15,16), Serpent Warriors (0e,12,22), Skeletons (05,0e,12,22,27), Snake (25), Soldiers (01,06), Spiders (13), Stosstrupen (04,06,1a,24), Sullen Citizens (06), Swamp Turtles (08), Thieves (06,08), Throneroom Guards (23), Unholy Guards (1b), Unjustly Accused (01), Vicious Guards (24), Water Turtles (15), Wild Women (26), Wizard (27), Zombies (23) |
| `0x03` | Cave Bears (0c), Cockatrice (20), Giant Snakes (12), Guard Goblins (27), Humbaba (01), Hydras (09), Lizard Apes (05), Lizard Men (12,27), Philistine (1f), Rock Men (04), Scorpions (0c), Serpent Swimmers (0a), Stag (1e), Swamp Dogs (08), Underworld Beasts (05), Wild Dogs (17), Young Dragons (1b) |
| `0x04` | Bears (Scorpion Bridge), Earth Man (Mystic Wood), Giant Spiders (Purgatory), Lizard Men (Tars Dungeon), Murk Trees (War Bridge), Ogres (Game Preserve) |
| `0x05` | Dragon (Lansk Undercity), Giant Snakes (Tars, Tars Dungeon, Necropolis, Lanac'toor), Lagooners (Sunken Ruins Topside, Sunken Ruins Below) |
| `0x06` | Spit Snakes (Game Preserve)                                  |
| `0x07` | Dragon Brood Queen (Dragon Valley)                           |

### Encounters

The encounter list uses the same one-byte prefix followed by two-byte pointers. The prefix byte similarly specifies the maximum random encounter that is generated if the game is told to run encounter `0xff`.

Each encounter is composed of a one-byte prefix and then 1-4 three-byte group specifiers, so an encounter consumes 4-13 bytes.

| Byte | Bitfield | Meaning |
| :--: | :------: | ------- |
| `[00]` | `**––––––` | (+1) Number of groups that follow, saved as heap[28] |
| `[00]` | `––******` | Index into tagline array, saved as heap[5c] |

Read bytes [03-01], [06-04], [09-07], and [0c-0a] as little-endian numbers, that is, reverse the order of the bytes before interpreting. This is important because the data fields span the bytes. So instead of byte offsets, I'm just laying out the 24 bits in a row:

| Bitfield | Meaning |
| :------: | ------- |
| `*––––––– –––––––– ––––––––` | "Sign" bit for confidence level |
| `–*****–– –––––––– ––––––––` | Confidence Level modifier |
| `––––––** **–––––– ––––––––` | *Unknown* |
| `–––––––– ––****–– ––––––––` | (x10) Range |
| `–––––––– ––––––** ***–––––` | Group size |
| `–––––––– –––––––– –––*****` | Monster index |

An encounter group's Confidence Level is added to the monster's Confidence Level; see byte `[1f]` above. If bit `[0]` is 1, this value is negative.

If the encounter group's Range value is zero, roll a random number 1-9 and use that as the range (x10). Write the range to byte `[09]` of the unpacked monster group data.

If the encounter group's Size value is zero, roll a random number using the Max Group Size (byte `[0a]`) from the monster group. Write the result to byte `[0a]` of the unpacked monster group data.

The bottom five bits index the Monster Group array. If they're zero, roll a random number using the prefix byte from the Monster Group pointer list.
