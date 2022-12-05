# General Events

The metaprogram for each board often calls out to functions in other chunks. Some of these are common to cities and feature additional data, which is worth explaining.

## Taverns<a name="tavern">

*Opcode:* `58090000 longcall(09,0000)`

The next word is the return address, i.e. where you want the program to resume after the party leaves the tavern.

The next word is a pointer to the tavern's "tagline" string.

The following byte is the number of NPC volunteers in residence, followed by an array of pointers to the NPC data. (In practice there's never more than one NPC per tavern, but there's support in the code for more than one.)

The following byte is the number of rumor strings available from the tavern keeper, followed by an array of string pointers.

The tagline string is (usually, although not necessarily) next, then all of the rumor strings.

NPC data starts with the NPC ID number, then the character's name (unpacked, variable-length), then five bytes for the five primary attributes (STR, DEX, INT, SPR, HP).

After the attributes, the next two bytes are the NPC's level.

Beyond that there are a series of offset/value pairs that indicate [character](Character.md) bytes to be updated. (I'm not sure why you couldn't have put the level here instead.) The first byte of the pair is the character data offset (i.e. `0x32` for Sun Magic), and the second is the value (i.e. `01`). This is also how we load spells (bytes `3c` and up). The array of bytes ends with `ff`.

There is a similar second array which contains one-byte pointers into the [secondary map data's](Board Data.md) Items list, indicating the NPC's starting inventory. If bit `0x80` is set, the item is equipped by default.

Finally, after the second array, there's a string that is printed when the NPC joins your party. (There's no pointer for this; it's just 1B after the end of the inventory array.)

## Chests<a name="chest">

*Opcode:* `580b0c00 longcall(0b,000c)`

The next word is the return address.

If the following byte is `0xff`, skip it. Otherwise, read that byte and the one after it and run a bitsplit operation to get a heap index and bit. If that value is zero, ignore the chest and return immediately.

A tagline string (five-bit packed) follows.

After that is an array of two-byte values. The first byte is an index into the [secondary map data's](Board Data.md) Items list, indicating which items are in the chest, and the second byte is the count. If the count is `0x00`, there are an infinite number of that item in the chest (see the Purgatory Low Magic Shoppe). If the item index is `0x80`, it's a Dragon Stone (hardcoded into chunk `0x0b`). If the item index is `0xfe`, it's gold, and the other byte is the amount (in the Purchase Price format). If the item index is `0xff`, the array terminates.

## Shops<a name="shop">

*Opcode:* `580a0000 longcall(0a,0000)`

The next word is a pointer to the tagline string (five-bit packed).

After that is an array of shop categories, starting with a one-byte item count. Each item contains two pointers; the first points to the category's inventory list, and the second points to the category name (five-bit packed string). If there's only one category, there's no category name (and the second pointer doesn't exist).

The return pointer (place we jump back to after the longcall) is set after that array, so the next thing in the stream is an instruction (usually a `jmp` or `exit` or similar).

The strings follow, starting with the tagline and proceeding through the category names.

The inventory lists are also arrays starting with a one-byte item count. Each item is a single byte which is an index into the [secondary map data's](Board Data.md) Items list, indicating which items are for sale in that category.

# 0x01 Purgatory

## Board State

| Heap byte |    Bit     | Meaning                                                    |
| :-------: | :--------: | ---------------------------------------------------------- |
|  `[9c]`   | `–*––––––` | Start square intro message has been played                 |
|  `[9d]`   | `–––––*––` | Defeated the Humbaba                                       |
|  `[9d]`   | `––––––*–` | Have received reward from Clopin for defeating the Humbaba |
|  `[9f]`   | `–––––*––` | Sold yourself into slavery                                 |
|  `[a5]`   | `––*–––––` | Won citizenship by defeating the Gladiators in the Arena   |
|  `[b9]`   | `–*––––––` | Have been shown p3 at the statue of Irkalla                |
|  `[b9]`   | `––*–––––` | Have been shown p4 when entering the Arena                 |
|  `[b9]`   | `–––*––––` | Have been shown p10 when entering the Magic Shoppe         |
|  `[b9]`   | `––––*–––` | "Dive into the harbor" message has been played             |
|  `[b9]`   | `–––––*––` | Have been shown p67 at the slave market                    |

## Events

 (almost all of them run instruction `0x73` at some point)

1. `[0dff]` Walking around inside the city walls. Color text.
2. `[0e2e]` Same, southeast section near the hole. Color text.
3. `[0e5d]` (23,10) Before the teeth-kicking fight. If facing South, print the color text at `{13:0251}`.
4. `[0e97]` (20,13) Starting square. Gate-and-set `heap[03+99].0x40` to print the color text at `{13:0317}`.
5. `[0eb2]` (25,08) The hole in the wall. Color text. Gate-and-set `heap[00+b9].0x08` to show more color text.
6. `[0fca]` Swimming in the harbor but adjacent to the walls. If `heap[40].0x80`, return. Else deal 1 HP damage to each non-dead party member without *Swim*. Then print color text.
7. `[0e69]` Run fight #ff (random). Clear the square if you win.
8. `[113c]` (Not referenced from a square.) Clear CF and return.
9. `[1055]` (06,13) Statue of Irkalla. Gate-and-set `heap[00+b9].0x40` to display color text. **The sacrifice works on a Spirit check (vs 1d20).** If it works, set `char[92].0x80` on every party member.
10. `[10e3]` (07,12) Apsu Waters. Teleports you to board 0x12 (Magan Underworld) at (13,04). If you say 'N', you back up.
11. `[112d]` (18,26) Entering the Arena. Runs the "free gear" program if you have less than **3 x your party size items** in your inventory. Then erases the entry door `ds[0320]<-0x30` and replaces this event `ds[02bc]<-0x1d` with event #29.
12. `[1279]` West of the Arena. Display color text.
13. `[12a6]` South of the Arena. Display color text.
14. `[12df]` East of the Arena. Display color text.
15. `[1010]` Swimming in the harbor. Same as Event #6 but different color text.
16. `[1303]` (31,10) The Morgue. Display paragraph #5.
17. `[130b]` Near the Morgue. Display color text.
18. `[1350]` City guard fight within the walls (#03); lose and you're kicked back to the staring square. Clear the square if you win.
19. `[1366]` (07,07) Clopin Trouillefou's Court of Miracles. Gate on `heap[04+99].0x04` to get a $1000 reward (gate-and-set `heap[04+99].0x02`) for defeating the Humbaba. Otherwise run the intro bit. Describe yourself as a beggar and you're **dealt 1d8 damage**. Refuse and you're given the Humbaba quest.
20. `[137f]` (09,22) Statue of Namtar. Read paragraph #9. 
21. `[1387]` (25,27) The [tavern](#tavern). Ulrik is here.
22. `[1564]` (03,22) The [magic shoppe](#chest). Gate-and-set `heap[00+b9].0x10` to read paragraph #10. Stock includes infinite quantities of Low Magic scrolls (items `0x15-0x1a`).
23. `[15a2]` (12,30) The [black market](#shop).
24. `[15fc]` (11,26) The slave market. Gate-and-set `heap[00+b9].0x04` to read paragraph #67. If you sell yourself into slavery, read paragraph #58, set `heap[06+99].0x04`, then travel to the Slave Mines (13:07,08).
25. `[113e]` (19,29) Runs fight #02 (Gladiators). If you win, display color text and pick up the Citizenship Papers, then set `heap[0c+99].0x20`. Restore the entry door `ds[0320]<-0x20` and event #11 `ds[02bc]<-0x0b`. Teleport the party outside the Arena. If you lose, display color text and lose all your Gold.
26. `[0e78]` Run fight #06 (Bandits). Clear the square if you win.
27. `[0e8c]` (23,09) Run fight #01 (5 King's Guard and 6 Pikemen). Clear the square if you win.
28. `[110e]` (06,12) One square shy of the Apsu Waters. Gate on Town Lore 1 to get paragraph 94. Otherwise you see "odd waters".
29. `[1255]` Inside the Arena. Display color text.
30. `[0f75]` Called after you Swim/Climb through the wall. Test `heap[00+26].0x01`; return if zero. Display color text. **Everyone in the party makes a Strength check (vs 1d20) or takes 1d10 damage.** Then set character flag `0x40` and move "forward" one square.
31. `[1337]` Called after you Hide/Dex in the Morgue. Read paragraph #69. **Everyone in the party makes a Strength check (vs 1d20) or takes 1d10 damage.** Then set character flag `0x40` and travel to Dilmun (00:13,02).
32. `[100c]` Outside the city walls. Print the same color text as event #6.
33. `[136b]` (31,31) Run fight #0a (Humbaba). Clear the square and set `heap[04+99].0x04` if you win.
34. `[0e6e]` (20,03) Run fight #04 (Unjust, Innocent, Cannibals). Clear the square if you win.
35. `[0e7d]` (12,28) Run fight #07 (Jail Keeprs). Clear the square if you win.
36. `[0e82]` (26,27) Just outside the tavern. Run fight #08. Clear the square if you win.
37. `[0e87]` (09,21) Run fight #09 (Loons). Clear the square if you win.
38. `[0e73]` (05,13) and (05,14) Run fight #05 (Fanatics). Clear the square if you win.
39. `[0ea5]` (20,15) 2N of start square. Gate on Town Lore 1 to read paragraph #14.
40. `[1517]` (29,27) The town healer. Run routine at `{11:0000}`. **When you pay for healing, the game automatically pools your gold with the first PC.**
41. `[1653]` (23,02) Magic refresh pool. Non-dead characters have their Power restored to max.

## Specials

1. Event 5, use *Climb* or *Swim* -> `[0f75]`
2. Event 16, use *Dexterity* or *Hiding* -> `[1337]`

# 0x08 Mud Toad

## Board State

| Heap byte |    Bit     | Meaning               |
| :-------: | :--------: | --------------------- |
|  `[41]`   | `*–––––––` | Stone Arms retrieved  |
|  `[41]`   | `–*––––––` | Stone Torso retrieved |
|  `[41]`   | `––*–––––` | Stone Head retrieved  |
|  `[41]`   | `–––*––––` | Stone Hands retrieved |

# 0x12 Magan Underworld

## Board State

| Heap byte |    Bit     | Meaning                           |
| :-------: | :--------: | --------------------------------- |
|  `[a9]`   | `–*––––––` | Have taken the 5 AP Leap of Faith |

# 0x16 Sunken Ruins Underground

## Board State

| Heap byte |    Bit     | Meaning                                                  |
| :-------: | :--------: | -------------------------------------------------------- |
|  `[9c]`   | `–––––––*` | Clam has been brought to surface and replaced with Skull |
|  `[9d]`   | `*–––––––` |                                                          |
|  `[b9]`   | `*–––––––` | Intro message has been played                            |
|  `[b9]`   | `–*––––––` | Clam has been picked up                                  |
|  `[b9]`   | `––––––*–` | Chest has been unlocked                                  |