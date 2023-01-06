# Board Events

The third byte of map square data includes a "special" identifier. Every time you step on a square the game checks the ID, and if it's non-zero, it runs a block of code (hereafter an "event") that corresponds to that ID. This happens when you "start a turn" on a square, no matter how you got there; it's not tied to movement. Multiple squares on a map can have the same special ID. 

Events can also be triggered by (U)sing an item, skill, or attribute (hereafter an "action"). Usually you have to Use the right thing on the right square (i.e. a square with a particular Special ID), but there are some catch-all Actions that run if you do an unexpected thing.

For instance, in the Slave Camp, the square (04,02) is tagged with Special ID 0x11. When you step on that square, the game runs Event 0x11 `[0969]`, which checks to see if you've noticed the Nature Axe yet. But if you're standing on that square and you Use *Forest Lore*, it runs Event 0x13 `[0975]` instead. That prints some color text and sets a flag that says "yup, you found the Nature Axe". Now, the next time you run Event 0x11 (which is immediately afterwards, because you're still standing on that square), the game shows you the cache that contains the Nature Axe and lets you take it.

There's also a default handler that runs every time you step on every square. This is technically Event 0x00 (although there's a pointer between Event 0x00 and 0x01, it's complicated), but I've listed it last because Markdown's Ordered List tag starts at 1 whether I want it to or not.

Everything seems to run instruction `73`, `mov h[3e] <- h[3f]`. Heap `[3e]` and `[3f]` seem to be something like "the last special event ID"; see `{cs:44bb}`. Occasionally one or both values are set to 0 instead.

I wonder if there's a difference between flags in the 90's and flags in the b0's. Like maybe one gets persisted when you leave the board, and one doesn't? Might need to revisit chunk `0x00`. Offsets from `b9` get reused across boards.

## Key

`[99,01]` is a **bitsplit**: it's an index to a bit, starting at the high bit (bit 7) of `heap[99]` and shifting `[01]` places to the right. In this example that refers to bit `0x40` of `[99]`. The phrase "gate-and-set" means to check the bitsplit to see if it's true, and if so, don't do anything else; if it's false, set it and then continue.

`(0a:05,07)` is a **map location**. The first number (in hex) is the board number. The second and third are an (X,Y) coordinate (in decimal) on that map. Coordinates on the current map are usually not printed in monotype, i.e. (05,07).

`{45:0412}` is a **memory location** within a data chunk (uncompressed, if necessary).

## 0x00 Dilmun

### Flags

- **Random encounters:** no.
- **Create Wall** is forbidden.
- The map **wraps**, which is pretty funny since you can't actually travel over the water.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                                                     |
| :------: | :-------: | :--------: | ----------------------------------------------------------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | Have determined if Phoebus still exists (once per entrance) |
| `b9,04`  |  `[b9]`   | `––––*–––` | Have access to the Forlorn cache                            |

### Events

<!-- vimscript: iN:`(li0xh"b4xxi0xh"c4x:let @b=printf('%02d',@b)
:let @c=printf('%02d',@c)
ic,b)` -->

1. `[1218]` (19,12) If anyone in the party has *Forest Lore 1* and you're facing E, get some color text `{46:1228}`. 
2. `[125c]` (West of the Bridge of Exiles.) Color text `{46:125d}`.
3. `[13f0]` (13,04) Enter Purgatory? `(01)` N:`(26,00)` E:`(00,16)` S:`(16,33)` W:`(33,03)`
4. `[13d1]` (11,03) Enter Slave Camp? `(02)` N:`(07,00)` E:`(00,04)` S:`(07,16)` W:`(13,04)`
5. `[13ac]` (12,07) Guard bridge ahead, approach? `(03)` N:`(04,00)` E:`(00,03)` S:`(04,07)` W:`(08,04)` (except there is no X=08...)
6. `[12b3]` (21,04) Enter ancient Runs? (Tars) NESW:`(05:00,08)`
7. `[1297]` (05,11) Enter Phoebus? NESW:`(06:08,01)`
8. `[1381]` (14,12) Heavily guarded bridge ahead, approach? `(07)` N:`(03,00)` E:`(00,04)` S:`(03,08)` W:`(07,04)`
9. `[1300]` (07,18) Bridge ahead, approach? `(0d)` N:`(03,00)` E:`(00,04)` S:`(03,07)` W:`(07,04)`
10. `[1360]` (18,12) Bridge ahead, approach? `(0b)` N:`(03,00)` E:`(00,03)` S:`(03,07)` W:`(07,03)`
11. `[12d3]` (25,08) Enter the City of the Yellow Mud Toad? `(08)`  N:`(07,01)` E:`(10,09)` S:`(07,10)` W:`(00,09)` 
12. `[1275]` (24,13) Enter Smuggler's Cove? `(0a)` NESW:`(07,03)`
13. `[133f]` (31,19) Bridge ahead, approach? `(0c)` N:`(03,00)` E:`(00,03)` S:`(03,07)` W:`(07,03)`  
14. `[1321]` (27,15) Enter ruined city? `(0e)` N:`(07,00)` N:`(00,15)` N:`(07,14)` N:`(16,07)` 
15. `[1607]` You stepped in the ocean.
16. `[140e]` (14,17) Approach Old Dock? `(1c)` NESW:`(07,03)`
17. `[142e]` (17,21) Approach Pilgrim's Dock? `(1a)` N:`(05,00)` E:`(00,06)` S:`(02,07)` W:`(07,03)` 
18. `[1451]` (25,27) Enter Royal Game Preserve? `(1e)` N:`(07,00)` E:`(00,09)` S:`(05,15)` W:`(15,06)` 
19. `[1476]` (02,19) Enter decaying city? `(18)` N:`(08,01)` E:`(02,07)` S:`(06,14)` W:`(0f,08)` 
20. `[1496]` (18,27) Enter Kingshome? `(19)` N:`(07,00)` E:`(00,02)` S:`(14,13)` W:`(14,07)` 
21. `[14b4]` (10,21) Ruins ahead, Approach? `(0f)` N:`(03,00)` E:`(00,03)` S:`(03,07)` W:`(07,01)` 
22. `[14d5]` (02,06) Magical forest ahead, Enter? `(17)` N:`(08,00)` E:`(02,07)` S:`(08,15)` W:`(15,09)` 
23. `[14fa]` (38,15) Enter sunken ruins? `(15)` NESW:`(00,00)`
24. `[1519]` (36,24) Enter strange building? `(1f)` NESW:`(03,00)`
25. `[153a]` (34,15) Enter Dragon Valley? `(20)` NESW:`(07,00)`
26. `[155b]` (19,19) Enter Nisir? `(04)` NESW:`(07,15)`
27. `[1576]` (16,14) Enter Lansk? `(14)` N:`(07,00)` ES:`(00,07)` W:`(15,07)` 
28. `[1591]` (07,27) Enter Byzanople? `(89)` NESW:`(06,00)` *The high bit means "run away if no"*
29. `[15af]` (43,23) Enter Freeport? `(11)` N:`(09,00)` N:`(00,13)` N:`(13,16)` N:`(13,16)` 
30. `[15cc]` (17,07) Enter Slave Estate? `(25)` NESW:`(07,00)`
31. `[15ec]` (07,26) Enter Camp? `(1d)` NEW:`(07,00)` S:`(09,14)`
32. `[160c]` (29,28) The [boat dock](#boat) at Rustic.
33. `[1613]` (39,14) The [boat dock](#boat) at Sunken Ruins.
34. `[1629]` (12,09) Run combat #0 (Goblins). If you win, erase this event.
35. `[1647]` (12,06) If `![99,77]`, run combat #1 (Guards). If you win, set `[99,77]` and erase this event.
36. `[1634]` (05,10) If `![99,7f]`, run combat #2 (Goblin brothers). If you win, set `[99,7f]` and erase this event.
37. `[165a]` (16,03) Gate-and-set `[99,63]` to access the cache on Forlorn `[b9,04]`
38. `[16a5]` (14,01) The refresh pool SE of the Slave Camp. Non-dead party members have their Health, Stun, and Power restored to full.
39. `[1624]` (08,18) Run combat #3 (Revenge Goblins). If you win, erase this event.
40. `[170b]` (20,26) If `![99,78]`, color text `{46:1717}` and travel to Kingshome Dungeon `(24:00,15)`.

    > `[99,78]` will be set when you arrive in the Kingshome Dungeon.
41. `[161f]` (Various on King's) Run combat #4 (Goblin hordes). If you win, erase this event.
42. `[161a]` (Various on Quag) Run combat #5 (Murk Trees). If you win, erase this event.
43. `[1776]` (22,13) and (19,23) Transportation Nexus. If anyone in the party has *Arcane Lore 1*, print color text `{46:1784}` and ask if the party wants to travel to the Mystic Wood `(17:05,07)`.
44. `[17d5]` Default handler. Gate-and-set `[b9,00]`. If Phoebus *hasn't* been destroyed `![99,7e]`, add it to the map by writing `{46:0b31},{46:0b34},{46:0bbe},{46:0b33}`. This adds four walls and enables event #7 so you can enter the city.

## 0x01 Purgatory

### Flags

- **Random encounters**: yes (1 in 100), but only when inside (roof texture is non-zero).

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                                                    |
| :------: | :-------: | :--------: | ---------------------------------------------------------- |
| `b9,01`  |  `[b9]`   | `–*––––––` | Have been shown p3 at the statue of Irkalla                |
| `b9,02`  |  `[b9]`   | `––*–––––` | Have been shown p4 when entering the Arena                 |
| `b9,03`  |  `[b9]`   | `–––*––––` | Have been shown p10 when entering the Magic Shoppe         |
| `b9,04`  |  `[b9]`   | `––––*–––` | "Dive into the harbor" message has been played             |
| `b9,05`  |  `[b9]`   | `–––––*––` | Have been shown p67 at the slave market                    |

### Events

1. `[0dff]` Walking around inside the city walls. Color text `{47:0e00}`

2. `[0e2e]` Same, southeast section near the hole. Color text `{47:0e2f}`

3. `[0e5d]` (23,10) Before the teeth-kicking fight. If facing S, color text `{13:0251}`.

4. `[0e97]` (20,13) Starting square. Gate-and-set `[99,19]` for color text `{13:0317}`.

5. `[0eb2]` (25,08) The hole in the wall. Color text. Gate-and-set `[b9,04]` for color text `{47:0eb8}`

6. `[0fca]` Swimming in the harbor but adjacent to the walls. If `[40,00]`, return. Else deal 1 HP damage to each non-dead party member without *Swim*. Then print color text `{47:0fd3}`.

7. `[0e69]` Run a random combat. Clear the square if you win.

8. `[113c]` (Not referenced from a square.) Clear CF and return.

9. `[1055]` (06,13) Statue of Irkalla. Gate-and-set `[b9,1b]` to display color text. **The sacrifice works on a Spirit check (vs 1d20).** If it works, set `char[92].0x80` on every party member.

10. `[10e3]` (07,12) Apsu Waters. Travel to board 0x12 (Magan Underworld) at (13,04). If you say 'N', you back up.

11. `[112d]` (18,26) Entering the Arena. Runs the "free gear" program if you have less than **3 x your party size items** in your inventory. Then erases the entry door `ds[0320]<-0x30` and replaces this Event `ds[02bc]<-0x1d` with Event #29.

12. `[1279]` Color text `{47:127a}`

13. `[12a6]` Color text `{47:12a7}`

14. `[12df]` Color text `{47:12e0}`

15. `[1010]` Swimming in the harbor. Same as Event #6 but different color text.

16. `[1303]` (31,10) The Morgue. Display paragraph #5.

17. `[130b]` Near the Morgue. Display color text.

18. `[1350]` City guard combat within the walls (#3); lose and you're kicked back to the staring square. Clear the square if you win.

19. `[1366]` (07,07) Clopin Trouillefou's Court of Miracles. If `![99,25]`, read paragraph #77 and either get beat up for 1d8 damage (paragraph #8) or the Humbaba quest. Otherwise, gate-and-set `[99,26]` to get a $1000 reward for defeating the Humbaba. Otherwise, "thanks for coming".

20. `[137f]` (09,22) Statue of Namtar. Read paragraph #9. 

21. `[1387]` (25,27) The [tavern](#tavern). Ulrik is here.

22. `[1564]` (03,22) The [magic shoppe](#chest). Gate-and-set `[b9,03]` to read paragraph #10. Stock includes infinite quantities of Low Magic scrolls.

23. `[15a2]` (12,30) The [black market](#shop).

24. `[15fc]` (11,26) The slave market. Gate-and-set `[b9,05]` to read paragraph #67. If you sell yourself into slavery, read paragraph #58, set `[99,35]`, then travel to the Slave Mines `(13:07,08)`.

25. `[113e]` (19,29) Runs combat #2 (Gladiators). If you win, display color text and pick up the Citizenship Papers, then set `[99,62]`. Restore the entry door `ds[0320]<-0x20` and Event #11 `ds[02bc]<-0x0b`. Teleport the party outside the Arena. If you lose, display color text and lose all your Gold.

26. `[0e78]` Run combat #6 (Bandits). Clear the square if you win.

27. `[0e8c]` (23,09) Run combat #1 (5 King's Guard and 6 Pikemen). Clear the square if you win.

28. `[110e]` (06,12) One square shy of the Apsu Waters. Gate on Town Lore 1 to get paragraph 94. Otherwise you see "odd waters".

29. `[1255]` Color text `{47:1256}`

30. `[0f75]` Called after you Swim/Climb through the wall. Test bit `0x01` of the wall metadata (which is true for every actual wall on this map); return if zero. Display color text. Everyone in the party makes a Strength check (vs 1d20) or takes 1d10 damage. Then set character flag `0x40` and move "forward" one square.

31. `[1337]` Called after you Hide/Dex in the Morgue. Read paragraph #69. Everyone in the party makes a Strength check (vs 1d20) or takes 1d10 damage. Then set character flag `0x40` and travel to Dilmun `(00:13,02)`.

32. `[100c]` Outside the city walls. Print the same color text as Event #6.

33. `[136b]` (31,31) Run combat #10 (Humbaba). If you win, set `[99,25]` and erase this event.

34. `[0e6e]` (20,03) Run combat #4 (Unjust, Innocent, Cannibals). If you win, erase this event.

35. `[0e7d]` (12,28) Run combat #7 (Jail Keeprs). If you win, erase this event.

36. `[0e82]` (26,27) Just outside the tavern. Run combat #8. If you win, erase this event.

37. `[0e87]` (09,21) Run combat #9 (Loons). If you win, erase this event.

38. `[0e73]` (05,13) and (05,14) Run combat #5 (Fanatics). If you win, erase this event.

39. `[0ea5]` (20,15) 2N of start square. Gate on Town Lore 1 to read paragraph #14.

40. `[1517]` (29,27) The town healer ($4/hp). Run routine at `{11:0000}`.

    > When you pay for healing, the game automatically pools your gold with the first PC. This shouldn't be a problem, unlike in some games where the max gold per PC is ridiculously low.

41. `[1653]` (23,02) Magic refresh pool. Color text `{47:1654}`. Non-dead characters have their Power restored to max.

42. `[1684]` Default handler. If party is outside the rectangle (0,0)-(33,33), ask to exit. Otherwise, check if we're outside (roof texture 0) or inside, and if inside, set the random encounter counter (`[0003]`) to `0x64` (1 in 100).
    - North: `(00:0f,05)` — *oops*, this really ought to be `(00:0d,05)`.
    - East or South: `(00:0d,02)`. Hilariously, this means you can exit to the east on dry land at (33,33) and wind up on the other side of the bay.
    - West: `(00:0c,04)`

### Actions

| Special | Skill, Item, or Spell | Event | Handler  |
| :-----: | :-------------------: | :---: | :------: |
|   #5    | *Climb, Swim*         |  #30  | `[0f75]` |
|   #16   | *DEX, Hiding*         |  #31  | `[1337]` |

## 0x02 Slave Camp

### Flags

- **Random encounters**: no

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                                            |
| :------: | :-------: | :--------: | -------------------------------------------------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | If 0, you haven't yet stepped into the camp proper |
| `b9,01`  |  `[b9]`   | `–*––––––` | Have noticed the axe in the tree                   |
| `b9,02`  |  `[b9]`   | `––*–––––` | Have access to the sick man's cache                |
| `b9,04`  |  `[b9]`   | `––––*–––` | Have access to the wizard's cache                  |

### Events

1. `[0497]` Color text `{48:0498}`.
2. `[04ac]` (10,11) The campfire. Read paragraph #63. Health, Stun, and Power are restored to full. Then erase this event.
3. `[037c]` (02,05) Color text `{48:037d}`.
4. `[03a9]` (10,06) If `![99,02]`, color text `{48:03b0}`.
5. `[0391]` (00,06) Read paragraph #68.
6. `[0399]` (00,16) Read paragraph #22.
7. `[03a1]` (04,13) Read paragraph #88.
8. `[03d2]` (08,07) The sick man. If `[99,02]`, you can raid his [belongings](#chest) `[b9,02]`. Otherwise, there's a 1 in 20 chance the he attacks you (combat #1). If you kill him, set `[99,02]`. If he doesn't attack you, color text `{48:03e6}`.
9. `[047e]` You heal the sick man. Gate on `[99,02]` to read paragraph #19. Set `[99,02]` and `[b9,02]`.
10. `[0940]` You used *Lockpick*. Always works on wall metadata special `0x02`.
11. `[04e5]` You appease the camp with *Bureaucracy*; set `[99,01]`.
12. `[0629]` (07,04) You impress the wizard; set `[99,03]`. Unlocks his treasure room door at `[020b]<-0x21`. Set `[b9,04]` to gain access to his treasure.
13. `[0589]` (07,05) If you barge into the wizard's house without showing off your magic skills `![99,03]`, he kicks you out.
14. `[0720]` (06,06) Outside the wizard's treasure room. Checks the door texture to see if the door is locked (`0x31`), and if so, tells you.
15. `[0739]` (06,07) The wizard's treasure. If you haven't either killed the guardian `![99,04]` or impressed the wizard `![99,03]`, you have to fight the Spirit Ward (encounter #2); if you win, set `[99,04]` to mark your victory and `[b9,04]` to get access to the treasure.
16. `[0377]` You stepped in water. (Splash)
17. `[0969]` (04,02) If `[b9,01]`, get the Nature Axe.
18. `[0975]` You used *Forest Lore* in the right place. Gate-and-set `[99,27]` to notice an axe stuck in a tree. Set `[b9,01]`.
19. `[05fd]` (07,04) The wizard peers at you from within his house, unless you did this already `[99,03]`
20. `[09a6]` (07,07) A [locked chest](#locked) behind the wizard's house (`[99,28]`, difficulty 1).

    > This is the same bitsplit as the chest holding the Dwarven Hammer in the Ruins on King's Isle; if you unlock this chest, you lose that one (and vice versa).
21. `[09bd]` (10,02) The [tavern](#tavern). Louie is here.
22. `[0795]` Default handler. If the party is outside (0,0)-(17,16), prompt to exit. N:`(00:11,04)` ES:`(00:11,02)` W:`(00:10,03)`
    - If this is your first time here `!(00,b9)`, set `(00,b9)`. If `(00,99)`, you already killed everyone; erase events #1–12, #17, and #19, then exit. If `(01,99)` is set, you're already friends, so exit. If any character has flag `0x40` (swam out of Purgatory), read paragraph #16; the camp is happy with you. Set `(01,99)` and exit. Otherwise, the residents are suspicious; exit (and await player action)
    - If you've been here before and either `(01,99)` or `(00,99)`, exit. Otherwise, if you stay on the perimeter of the map, the residents wait for you to leave. If you step inside (1,1)-(13,15), the camp residents attack (encounter #0). If you kill them, read paragraph #18 (shame) and set `00,99`.

### Actions

| Special |                    Skill, Item, or Spell                     | Event | Handler  |
| :-----: | :----------------------------------------------------------: | :---: | :------: |
|   #8    | *Bandage, L:Lesser Heal, H:Healing, D:Greater Healing, S:Heal* |  #9   | `[047e]` |
|    —    |                          *Lockpick*                          |  #10  | `[0940]` |
|    —    |                        *Bureaucracy*                         |  #11  | `[04e5]` |
|   #19   |       *Sun Magic, Druid Magic, Low Magic, High Magic*        |  #12  | `[0629]` |
|   #17   |                        *Forest Lore*                         |  #18  | `[0975]` |

## 0x03 Guard Bridge #1

### **Flags**

- **Random Encounters:** no, but see Event #6.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                                                      |
| :------: | :-------: | :--------: | ------------------------------------------------------------ |
| `b9,00`  |  `[b9]`   | `*–––––––` | Made the guards go away (showed Papers, bribed or killed them) |
| `b9,01`  |  `[b9]`   | `–*––––––` | Bribed the guards                                            |
| `b9,02`  |  `[b9]`   | `––*–––––` | Read paragraph #12 on entry                                  |
| `b9,04`  |  `[b9]`   | `––––*–––` | Have access to the arms cache                                |

### Events

1. `[00e7]` You stepped in water.
2. `[00ec]` (04,02) If `[b9,00]`, exit. Otherwise draw monster `0x0e`, then the guard demands to see your Citizenship Papers.
3. `[0123]` (04,02) You show your Citizenship Papers to the guard. If `![b9,00]`, the guard demands a bribe of $10 per party member. If you pay it, set `[b9,00]` and `[b9,01]`.
4. `[023f]` (04,05) If `[b9,00]`, exit. If `[b9,01]`, run event #2 instead. Otherwise, the guard asks if you know where you're going; if you say yes, set `[b9,01]` and run event #2, otherwise run away.
5. `[02ff]` (04,05) You show your Citizenship Papers to the guard. Set `[b9,00]` and `[b9,01]`.
6. `[0383]` Crossing the bridge. If you've already dealt with the guards `[b9,00]`, read paragraph #13; there's a 1:10 chance of an encounter with a bunch of Rats (#01). Otherwise, you have to fight the guards (#0). If you win, set `[b9,00]`. If you lose, you're kicked off the bridge, to `(04,00)` or `(04,07)` depending on which half of the bridge you're currently on.
7. `[0484]` (07,02) Gate-and-set `[99,2c]` to open a [chest](#chest) `[b9,04]`.
8. `[03c3]` (03,01) Color text `{49:03c9}` about Lanac'toor's statue. Erase this event.
9. `[0435]` Default handler. If the party is inside (0,0)-(7,7), gate-and-set `[b9,02]` to read paragraph #12. Otherwise, prompt to exit; if the party is in the bottom half of the map (Y coordinate < 4) exit to `(00:12,06)`, else `(00:12,08)`.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|   #2    |   Citizenship Papers   |  #3   | `[0123]` |
|   #4    |   Citizenship Papers   |  #5   | `[02ff]` |

## 0x04 Salvation

### Flags

- **Random Encounters:** yes (1 in 100)

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                                  |
| :------: | :-------: | :--------: | ---------------------------------------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | Read paragraph #98                       |
| `b9,01`  |  `[b9]`   | `–*––––––` | Deduced a path through the mountain pass |

### Events

1. `[0343]` (13,11) Statue of the Universal God. Read paragraph #97.

2. `[034b]` (13,11) Brandish the Sword of Freedom at the Universal God. Drop the Sword of Freedom you have, and pick up a version that casts *S:Inferno@10* but is otherwise identical. Everyone in the party gains 500 XP. Iterate over the party, gate-and-set `(5c,03)` to gain 3 points on all four attributes.

3. `[046c]` (03,04) Stairs down to the Underworld `(12:19,19)`

4. `[047f]` (07,08) Run combat #05 (Guards and Stosstrupen). If you win, erase this event.

5. `[048a]` "An alarm sounds!" Reset event #4. 

   > Not sure how this gets triggered, since special #5 doesn't exist on the map...

6. `[049d]` You used *Lockpick*. Locked doors on this map have wall metadata bit 0x01 set, i.e. texture index 3. (This is true for the doors at (04,08) and (08,06).) Any number of ranks is sufficient to unlock the door (sets the upper nibble of the wall to 0x20).

7. `[04c4]` (05,07) The climber's puzzle. Color text `{04:04c5}`.

8. `[04e6]` (05,07) You used *Mountain Lore* or *Intelligence*, which sets `[b9,01]`.

9. `[051c]` (05,07) You used *Climb*. If `[b9,01]`, travel to `(04:06,06)`. Otherwise, color text `{04:0523}`.

10. `[05c5]` (08,03) Paragraph #55.

11. `[0000]` *Oops?*

12. `[05cd]` (08,03) You used the Golden Boots. If you're facing East, hop across the chasm to (0a,03).

13. `[05f1]` (03,06) Paragraph #100.

14. `[05f9]` Inside the mountain. Color text `{04:05fa}`.

15. `[061c]` Color text `{04:061d}`. "No one uses the front door fool!"

16. `[0637]` Color text `{04:063d}`. Travel to Nisir (1b:15,17).

17. `[057c]` (10,06) Color text `{04:057d}`.

18. `[05a5] `Fall through the chasm; travel to the Underworld `(12:20,19)`.

19. `[047a]` (01,08) Run combat #06 (Stosstrupen + random). If you win, erase this event.

20. `[0668]` (01,09) [Locked chest](#locked) (`[99,53]`, difficulty 5).

21. `[067d]` Default handler. If the party is outside (0,0)-(21,21), prompt to exit to`(00:19,20)`. Otherwise, gate-and-set `[b9,00]` to read paragraph #98.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|    —    | *Lockpick*             |  #6   | `[049d]` |
|   #1    | Sword of Freedom       |  #2   | `[034b]` |
|   #7    | *INT, Mountain Lore*   |  #8   | `[04e6]` |
|   #7    | *Climb*                |  #9   | `[051c]` |
|   #10   | Golden Boots           |  #12  | `[05cd]` |

## 0x05 Tars Ruins

### Flags

- **Random Encounters:** yes (1 in 100)

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                                     |
| :------: | :-------: | :--------: | ------------------------------------------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | Actively following the tracks               |
| `b9,01`  |  `[b9]`   | `–*––––––` | Found the slab                              |
| `b9,04`  |  `[b9]`   | `––––*–––` | Have access to the scrolls in the SW corner |
| `b9,05`  |  `[b9]`   | `–––––*––` | Have access to the Fireshield chest         |

### Events

1. `[067e]` Near the water in the south part of the map. Color text `{4b:067f}`.
2. `[03d9]` (00,08) The entry square. Color text `{4b:03df}`, then erase this event.
3. `[0449]` (01,08) If anyone in the party has *Arcane Lore* 1, color text `{4b:0452}`, then erase this event.
4. `[048b]` (15,15) Set `[b9,01]`. If you're following tracks `[b9,00]`, clear that bit and color text `{4b:0499}`. If the slab hasn't been moved yet `![99,05]`, color text `{4b:04b0}`. Otherwise, offer to travel to `(27:02,05)`.
5. `[04d8]` (15,15) You examined the slab with a skill. Set `heap[3e]` to 0x00 instead of `heap[3f]` (?!). If the slab hasn't been moved yet `![99,05]`, color text `{4b:04e1}`.
6. `[0514]` (15,15) You used *Strength*. If the stone slab has already been moved `[99,05]`, exit. Otherwise roll 1d40; if the value is below the PC's *Strength*, the slab moves (set `[99,05]`) and exposes the stairs down.
7. `[05b1]` Inside the ruined city square. Color text `{4b:05b2}`.
8. `[05f0]` You asked your sage about the city ruins; read paragraph #23.
9. `[0666]` (04,08) "This way!!" says your Tracker. Set `[b9,00]` and run event #10.
10. `[05f9]` On the path. If `[b9,00]`, turn until you're facing North, then step forward.
11. `[05fe]` ... East
12. `[0604]` ... South
13. `[060a]` ... West
14. `[0637]` If you're already following the tracks `[b9,00]`, turn North. Otherwise, color text `{4b:063e}` says you should. 
15. `[0699]` You stepped in water.
16. `[0571]` (13,14) If you're following tracks `[b9,00]` or have already found the slab `[b9,01]` or have a Disarm Traps spell active (`heap[bf]`> 0), color text `{4b:059c}`. Otherwise you fall in the pit `{4b:0586}` and everyone takes 8 HP damage.
17. `[06e0]` (01,01) A bunch of [scrolls](#chest) (`[b9,04]`).
18. `[0728]` (15,08) The snake's [treasure](#chest) (`[b9,05]`).
19. `[06ca]` (02,02) If you've cleared this encounter `[99,29]`, exit. Otherwise, run encounter #4 (random). If you win, set `[99,29]` and `[b9,04]` to grant access to the treasure.
20. `[0712]` (14,07) If you've killed the snake `[99,2a]`, exit. Otherwise, run encounter #5 (the Guardian Snake). If you win, set `[99,2a]` and `[b9,05]` to grant access to the treasure.
21. `[0754]` Run a random encounter. If you win, erase this event.
22. `[0693]` Default handler. If the party is outside (0,0)-(16,16), prompt to exit to `(00:20,04)`.

### Actions

| Special | Skill, Item, or Spell    | Event | Handler  |
| :-----: | :----------------------: | :---: | :------: |
|   #4    | *Cave Lore, Mountain Lore, Lockpick, Tracker* | #5 | `[04d8]` |
|   #4    | *STR*                    |  #6   | `[0514]` |
|   #7    | *Town Lore, Arcane Lore* |  #8   | `[05f0]` |
| #10–#14 | *Tracker*                |  #9   | `[0666]` |

## 0x06 Phoebus

### Flags

- **Random encounters**: yes (1 in 100)

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                                |
| :------: | :-------: | :--------: | -------------------------------------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | Have met the handshake machine.        |
| `b9,01`  |  `[b9]`   | `–*––––––` | Have met Buck Ironhead (paragraph #28) |

### Events

1. `[0419]` (08,01) Read paragraph #26.

2. `[0422]` (08,03) Show monster image 0x23. Gate-and-set `[b9,00]` to read paragraph #25.

3. `[0437]` (06,07) Color text `{4c:0438}`.

4. `[0475]` (06,09) Run encounter #7 (10 Stosstrupen). If you win, erase this event.

5. `[0494]` Outside Mystalvision's lair. Color text `{4c:0495}`.

6. `[04ad]` Outside the army grounds. Color text `{4c:04ae}`.

7. `[04d7]` (13,07) Color text `{4c:04d8}`.

8. `[0525]` (14,02) Gate-and-set `[b9,01]` to read paragraph #28. Color text `{4c:05eb}`; you're joining the army. If you say yes, read paragraph #86 and travel to `(1d:03,07)`. Otherwise, color text `{4c:05a2}` and pay $50 per party member. If you decide to pay and have enough money, color text `{4c:0655}` and exit. Otherwise you've joined the army.

9. `[0731]` (05,11) and (11,10) Color text `{4c:0732}`.

10. `[076a]` (08,14) Meeting Mystalvision. If you've already killed Mystalvision in the Nisir `[99,70]`, exit. Gate-and-set `[99,7a]`. Show monster image 0x35 and read paragraph #66. Run encounter #3 (Stosstrupen and Mystalvision). Regardless of whether you win or not, color text `{4c:078f}`, blacken the screen (image 0xff), and travel to `(21:12,03)`.

11. `[0466]` (13,15) Run encounter #4 (Dirty Rats). If you win, erase this event.

12. `[046b]` (12,15) Run encounter #5 (Mad Dogs). If you win, erase this event.

13. `[0470]` (16,03) Run encounter #6 (Soldiers). If you win, erase this event.

14. `[047a]` (13,14) Run encounter #8 (Thieves). If you win, erase this event.

15. `[047f]` (16,13) Run encounter #9 (random). If you win, erase this event.

    > The max random encounter ID is 0, which means this should always be Guards?

16. `[0484]` (08,16) Run encounter #10 (Soldiers). If you win, erase this event.

17. `[0489]` (03,14) Run encounter #11 (Ominous Fellow). If you win, erase this event.

18. `[07f4]` (01,10) Locked [chest](#locked) (`[99,2d]`, difficulty 2). Scrolls.

19. `[0807]` (01,15) Locked [chest](#locked) (`[99,2e]`, difficulty 2). Tri-cross.

20. `[081a]` (11,15) Locked [chest](#locked) (`[99,2f]`, difficulty 1). Magic Plate.

21. `[0461]` (02,15) Run encounter #12 (Guards, 1 Stosstrupen). If you win, erase this event.

22. `[0676]` (02,13), only reachable from below. Color text `{4c:067c}`, then move to (02,14).

23. `[0860]` (15,15) If you've been to the dungeon `[99,69]`, gate and set `[99,38]` for color text `{4c:0875}` from Berengaria. Run the [tavern](#tavern); Valar is here.

24. `[045c]` (Various) Run encounter #1 (Stosstrupen). If you win, erase this event.

25. `[0829]` (14,15) Color text `{4c:028a}` for the tavern.

26. ` [07d0]` Default handler. If the party is outside (1,1)-(16,16), prompt to exit. N:`(00:05,12)` E:`(00:06,11)` S:`(00:05,10)` W:`(00:04,12)`.

    > The map dimensions are (00,00)-(17,17), but the the walls can't be Softened, so you can really only exit to the S.

## 0x07 Guard Bridge #2

### Flags

- **Random Encounters**: no

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                                                      |
| :------: | :-------: | :--------: | ------------------------------------------------------------ |
| `b9,00`  |  `[b9]`   | `*–––––––` | Have been shown the "bridge ahead" color text                |
| `b9,01`  |  `[b9]`   | `–*––––––` | Have passed the Customs Inspection                           |
| `b9,02`  |  `[b9]`   | `––*–––––` | Have touched the top half of the map or sworn fealty to Lansk |
| `b9,03`  |  `[b9]`   | `–––*––––` | The Barracks strong box has been unlocked                    |
| `b9,04`  |  `[b9]`   | `––––*–––` | Have been thrown out of the Armory by the bored official     |
| `b9,05`  |  `[b9]`   | `–––––*––` | The Barracks guards woke up because you did something dumb   |
| `b9,06`  |  `[b9]`   | `––––––*–` | Have stolen the Key from the sleeping Barracks guards        |
| `b9,07`  |  `[b9]`   | `–––––––*` | Have found the chest (which activates the second Armory fight) |
| `b9,08`  |  `[ba]`   | `*–––––––` | Have defeated the guards in the Armory (and can therefore access the chest) |

### Events

1. ` [0112]` On the bridge. Color text `{4d:0113}`

2. `[0130]` You stepped in water.

3. `[02d5]` (03,06) N approach to the bridge. If `![b9,02]`, color text `{4d:02e1}`. Refuse to obey their laws and you have to fight combat #1 (3 Guards). Agree, or kill the guards, and they let you pass (set `bistsplit[b9,02]`).

   > **Bug?:** This event never gets triggered. If you approach from the N, it's assumed that you've already "sworn fealty" to Lansk. If you're approaching from the S, `[b9,02]` is set in the default handler as soon as Y=5. In theory if that check was >5 instead of >=5 it might run Event 3 before the default handler, and you'd actually get this encounter.

4. `[0135]` (03,03) S approach to the bridge. If you've already passed "Customs" `[b9,01]`, exit. Otherwise, color text `{4d:0141}`. If you refuse inspection, run combat #0 (3 Pikemen) *unless* someone in your party has at least two ranks in *Merchant*. If you submit to inspection, they take the greater of $100 or 20% of your gold. If you refuse to pay up (or can't), run combat #0. Regardless of how you end this encounter (except for running away from combat), set `[b9,01]`.

   > Of course, no one has two ranks in *Merchant*, because they took that skill out of the Level Up screen.

5. `[0371]` Inside the S building. If you've already killed both sets of sleeping guards `[99,7d]`, exit. If the guards *didn't* wake up `![b9,05]` and you *didn't* just walk into a wall `!(40,01)`, the guards continue to slumber. Color text `{4d:0384}` and exit. Otherwise, run combat #2 (5 Guards). If you lose, color text `{4d:03f3}` and re-run combat #2. If you win, color text `{4d:03ba}` and run combat #3 ("not easy"). Again, if you lose, color text `{4d:03f3}` and re-run combat #3. If you win, set `[99,7d]`.

6. `[040c]` (01,03) The chest inside the S building. Follow the same tree as Event #5, but when you exit there, you find the strong box in the corner. If `![b9,03]`, it's still locked; otherwise the [chest](#chest) is open.

   > Something about this chest is different. The lock isn't handled with the usual mechanism `longcall(0b:000f)` and uses a local flag `[b9,03]` instead of a global one `(99,??)`, which seems to be why you can raid this chest repeatedly.

7. `[0477]` Your lockpick tries to unlock the chest. If it's already open `[b9,03]`, you wake up the guards (run Event #9). If you have *Lockpick* 3 or better, set `[b9,03]`; otherwise, you wake up the guards.

8. `[052e]` (04,08) Inside the N building. If `[99,7c]`, exit. Gate-and-set `[b9,04]` for color text `{4d:0543}` and the official throws you out. If you go in a second time, run encounter #0 (Guards, etc.). If you win, set `[b9,08]` and `[99,7c]`.

9. `[04e0]` You used something unexpected. Set `[b9,05]` to wake up the guards.

10. `[0487]` You use the key to unlock the chest. If it's already open `[b9,03]`, you wake up the guards (run Event #9). Otherwise, drop the key and set `[b9,03]`.

11. `[04e7]` You pick a sleeping guard's pocket. If you've already killed both sets of sleeping guards `[99,7d]`. If you already stole the key `[b9,06]`, you wake up the guards (run Event #9). Otherwise, color text `{4d:04f6}`, pick up item #4 (the Key), and set `[b9,06]`. (If you didn't have room to pick up the key, don't set the bit.)

12. `[05e1]` (05,06) Erase this event. If you don't have a "defeat traps" spell running (`heap[bf]`= 0), color text `{4d:0x05e8}` and you fall in a pit for 1d8 damage. Otherwise color text `{4d:0x05fb}`.

13. `[0614]` (04,06) Set `[b9,07]` to activate the second Armory fight (see Event #14). Then run a [chest](#chest) (`[b9,08]`) with several magic items, including the Axe of Kalah.

14. `[064c]` (05,07) If you're on your way *out* `[b9,07]`, run combat #3 ("not easy"). If you win, erase this event.

15. `[065d]` Default handler. If the party is outside (0,0)-(7,8), prompt to exit. If you're in the lower half of the map (Y <= 4), exit to `(00:14,11)`, otherwise `(00:14,13)`.

    Otherwise, gate-and-set `[b9,00]` for color text `{4d:06a6}`. If you're in the upper half of the map (Y >= 5), set `[b9,02]`, which prevents you from ever needing to swear fealty to Lansk.

### Actions

| Special | Skill, Item, or Spell | Event | Handler  |
| :-----: | :-------------------: | :---: | :------: |
|   #5    |     *Pickpocket*      |  #11  | `[04e7]` |
|   #6    |      *Lockpick*       |  #7   | `[0477]` |
|   #6    |          Key          |  #10  | `[0487]` |
| #5, #6  |           —           |  #9   | `[04e0]` |

## 0x08 Mud Toad

### Flags

- **Random Encounters:** yes (1 in 100)

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                         |
| :------: | :-------: | :--------: | ------------------------------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | Intro paragraph                 |
| `b9,02`  |  `[b9]`   | `––*–––––` | Have access to the Golden Boots |
| `b9,04`  |  `[b9]`   | `––––*–––` | Have found the militia's cache  |
| `b9,05`  |  `[b9]`   | `–––––*––` | Have met with Berengaria        |

### Events

1. `[03aa]` (13,06) The Souvenir [Shop](#shop). Read paragraph #30.

2. `[03cc]` (07,10) Lanac'toor's statue. Loop over the four part bitsplits and set `heap[41]` accordingly. If all parts are present, offer to travel to `(22:07,09)`. If no parts are present, read paragraph #20. Otherwise, display which parts are still missing.

3. `[0461]` You brought the Stone Hand (`ax:=0x03`) back to Lanac'toor's statue. If this part has been returned already `[99,1a+ax]`, exit. Otherwise, return the part, drop the Stone Hand, set `heap[88]=0` and `heap[41]=0`. Then loop over the four statue part bitsplits and set `heap[41]` accordingly. If there are still missing parts, color text `{4e:0490}` and exit. Otherwise, color text `{4e:04a9}` and earn 500 XP for repairing the statue. Change the decoration texture on this square from `0x04` (broken statue) to `0x05` (repaired statue).

4. `[0452]` ... the Stone Arms (`ax:=0x00`)

5. `[045c]` ... the Stone Head (`ax:=0x02`)

6. `[0457]` ... the Stone Trunk (`ax:=0x01`)

7. `[055d]` (14,10) Color text `{4e:055e}`

8. `[0591]` A climbable wall. Color text `{4e:0592}`

9. `[05b3]` You used *Climb*. If you're facing a wall with metadata `0x01` set (only happens on the climbable walls), you step forward 'through' the wall.

10. `[05dc]` (04,13) The Priests of the Mud Toad. If `[b9,02]`, you receive the Golden Boots. Otherwise, if `[99,56]`, exit. Otherwise, if you've sealed up the leak, read paragraph #113, set `[99,56]` and `[b9,02]` and receive the Boots. Otherwise, read paragraph #17 and get the leak quest.

11. `[06af]` (05,12) The mud leak. Color text `{4e:06b0}`.

12. `[06cf]` (02,02) The city militia. Read paragraph #32, then run combat #7 (Crazed Militiamen). If you win, erase this event.

13. `[06e1]` (12,14) The [tavern](#tavern).

14. `[088a]` (06,02) The militia's cache. Gate-and-set `[99,39]`, then run the [chest](#chest) (`[b9,04]`).

15. `[08c5]` (10,04) A healer ($4/hp).

16. `[0834]` (13,14) If you tried to find Berengaria in Phoebus `[99,38]`, he's here instead; clear `[99,38]` and run a "[chest](#chest)" (`[b9,05]`) with a bunch of scrolls.

17. `[0667]` You cast *D:Create Wall*. If you've already sealed the leak `[99,56]`, exit. If there's already a wall in the right place (the wall texture > 0), exit. Otherwise, run the normal *Create Wall* handler to update the map state, then color text `{4e:0694}`.

18. `[0943]` Run a random combat, then erase this event if you win.

19. `[08f4]` Default handler. If the party is outside (0,0)-(16,16), prompt to exit. N:`(00:25,09)` E:`(00:26,08)` S:`(00:25,07)` W:`(00:24,08)`.

    Gate-and-set `[b9,00]` to read paragraph #29. If you've already talked to the priests after sealing the leak `[99,56]`, put a wall there. If `[99,1a-1d]` are all set, update the decoration texture on the statue to `0x05` (repaired statue).

    > Apparently this handler doesn't run when you're first dropped on the map and haven't taken an action yet.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|   #2    |  Stone Hand |  #3  | `[0461]` |
|   #2    |  Stone Arms |  #4  | `[0452]` |
|   #2    |  Stone Head |  #5  | `[045c]` |
|   #2   |  Stone Trunk |  #6  | `[0457]` |
|    —    |  *Climb* |  #9  | `[05b3]` |
|    —    | *D:Create Wall* |  #17  | `[0667]` |

## 0x09 Byzanople

### Flags

- **Random Encounters:** no

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                                  |
| :------: | :-------: | :--------: | ---------------------------------------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | Initial board setup (erase events, etc.) |
| `b9,01`  |  `[b9]`   | `–*––––––` | Dealt with the giant boulder             |

### Events

1. `[0354]` (06,01) Entering from the Siege Camp; color text `{4f:0355}`.

2. `[038e]` (03,09) Outside the front door; color text `{4f:0394}`.

3. `[0465]` (01,01) If you've dealt with the giant boulder `[b9,01]`, offer travel to `(23:09,01)`. Otherwise, color text `{4f:047b}`.

4. `[04bf]` (09,01) You moved or *Softened* the giant boulder. If you already dealt with it `[b9,01]`, exit. Otherwise, if you have **STR 18** or cast *D:Soften Stone*, color text `{4f:04cd}` and set `[b9,01]`. Otherwise, color text `{4f:04fe}` and it doesn't work.

   > It tests `heap[86] >= 0x12`, so I presume that if you cast a spell instead, this byte is `0xff` or something.

5. `[0524]` The defenders rain arrows down upon you. Color text `{4f:0525}` and take 1 HP damage.

6. `[0550]` (05,02) Read paragraph #34.

7. `[0407]` (04,09) Inside the front door; color text `{4f:040d}`.

8. `[0892]` You tried to unlock something. Only walls with texture `0x01` can be unlocked, but it always works.

9. `[0558]` (07,03) Read paragraph #37.

10. `[0560]` (07,04) The sappers' tunnel; offer travel to `(23:07,03)`.

11. `[056e]` The inner courtyard. Color text `{4f:056f}`.

12. `[0591]` (02,01) If you haven't killed the defenders `![99,23]`, read paragraph #21.

13. `[059f]` (03,01) If you haven't killed the defenders `![99,23]`, you're offered the choice to Attack or Hail them (color text `{4f:05ab}`). Attack them and they fall easily; set `[99,23]`. Hail them to meet Princess Myrilla (paragraph #36) and travel to `(23:07,11)`.

14. `[066b]` You unlocked (**requires *Lockpick 2***, but only from the inside) or *Softened* the front door. Read paragraph #71; Buck Ironhead throws you in the Kingshome Dungeon `(24:00,15)`. Set `[99,21]` (and the Carry flag).

15. `[0688]` (06,09) Stairs up from the Resistance HQ. If `![99,24]`, `[99,21]`, or `[99,22]`, offer stairs down to `(23:06,09)`. Otherwise, read paragraph #110 and travel to the fight against Kingshome `(1d:02,05)`.

16. `[06c1]` (07,11) [Marik's Armory](#shop).

17. `[071c]` (08,11) [Bart's Weaponsmithing](#shop).

18. `[0772]` (07,10) Sign for Marik's `{4f:0776}`

19. `[0783]` (08,10) Sign for Bart's `{4f:0787}`

20. `[07b7]` (13,04) Fire Shield [chest](#locked) (`[99,46]`, difficulty 3)

21. `[08b9]` (09,11) Alchemist Boris, the healer ($4/hp)

22. `[0799]` (09,10) Sign for Boris's `{4f:079d}`

23. `[07c2]` (09,07) Offer travel to `(1d:02,05)` (see Event #15)

24. `[08de]` (09,02) If you haven't killed the Hydra yet `![99,74]`, do it now (combat #3).

25. `[08f1]` (07,09) If you haven't already beat this fight `![99,75]` and you *didn't* join up with Prince Jordan `![99,24]`, fight a random combat. If you win, set `[99,75]`.

26. `[0908]` (05,11) If you haven't already beat this fight `![99,76]` and you *didn't* join up with Prince Jordan `![99,24]`, fight a random combat. If you win, set `[99,76]`.

27. `[0801]` Default handler. If the party is outside (0,0)-(15,15), prompt to exit to `(00:07,26)`.

    Gate-and-set `[b9,00]`. If Kingshome has won `[99,21]`, erase events #1, #2, #5, #6, #7, #9, #11, #12, #13, #14, #16, #17, #21, #25, and #26. If Byzanople has won `[99,22]`, erase events #1, #2, #5, #6, #7, #9, #12, #13, #14, #25, and #26. If you've already met Prince Jordan and joined up with him `[99,24]`, exit. Otherwise, read paragraph #33.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|   #3    | *STR, D:Soften Stone*  |  #4   | `[04bf]` |
|   —     | *Lockpick*             |  #8   | `[0892]` |
| #2, #7  | *D:Soften Stone*       |  #14  | `[066b]` |
|   #7    | *Lockpick*             |  #14  | `[066b]` |

## 0x0a Smuggler's Cove

### Flags

- **Random Encounters:** no

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                                       |
| :------: | :-------: | :--------: | --------------------------------------------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | Read intro paragraph                          |
| `b9,01`  |  `[b9]`   | `–*––––––` | Successfully bribed the pirates               |
| `b9,03`  |  `[b9]`   | `–––*––––` | Have been thrown out of the Pirate's Den once |
| `b9,05`  |  `[b9]`   | `–––––*––` | Read Irkalla paragraph #3                     |

### Events

1. `[0552]` You stepped in the water.
1. `[04ba]` (06,05) A statue of Irkalla. Praying to her here is identical to doing so in Purgatory (see Event #9).
1. `[0000]` *Er...*
1. `[0557]` (06,01) Run combat #1 (Serpent Swimmer). If you win, erase this event.
1. `[00f2]` (03,03) If you've already killed `[99,06]` or bribed the pirates `[b9,01]`, exit. Otherwise, color text `{50:0104}` and `{50:017c}`, get thrown out, and set `[b9,03]`.
1. `[01d2]` (04,03) You're speaking the pirates' language. If `[99,06]`, you already bribed them `[b9,01]`, or you aren't facing the door, exit. Otherwise, color text `{50:01e6}` prompts for a bribe. If you offer less than $50, color text `{50:0239}`. If you don't have enough money to pay your bribe, color text `{50:025f}`. Otherwise, read paragraph #41, set `[b9,01]`, and step inside.
1. `[027e]` (04,03) If you've already tried to force your way in `[b9,03]` but haven't either killed `![99,06]` or bribed the pirates `![b9,01]`, color text `{50:0291}`.
1. `[02b5]` (01,02) If you already killed the pirates `[99,06]`, exit. Otherwise, color text `{50:02c1}` and `{50:031f}` and run combat #0 (Pirate crew). If you lose, you jump right back into combat until they *kill* you. If you win, set `[99,06]`, read paragraph #24, and open a [locked chest](#locked) (`[99,3a]`, difficulty 2).
1. `[03ee]` (02,01) If you already killed the pirates `[99,06]`, exit. Otherwise, color text `{50:03fa}`, paragraph #43, and color text `{50:0453}` as Ugly maroons you on the isle of the Necropolis. Travel to `(0e:07,13)`.
1. `[04b4]` (01,01) [Boat dock](#boat).
1. `[0562]` Default handler. If the party is outside (0,0)-(7,7), prompt to exit to `(00:25,13)`. Otherwise, gate-and-set `[b9,00]` to read paragraph #39.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|   #7    | *Bureaucracy, Hiding, Lockpick, Merchant, Pickpocket* | #6 | `[01d2]` |

## 0x0b War Bridge

### Flags

- **Random Encounters:** no

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                                  |
| :------: | :-------: | :--------: | ---------------------------------------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | Smelled the curious odor                 |
| `b9,01`  |  `[b9]`   | `–*––––––` | Killed the ladies of the Visitors Bureau |
| `b9,02`  |  `[b9]`   | `––*–––––` | Dealt the bridge guards                  |

### Events

1. `[00ee]` (06,06) Exit if `[b9,01]`. There's a 1 in 5 chance that the ladies of the Visitors Information Bureau attack you (combat #1); if you win, set `[b9,01]`. If they don't attack you, read paragraph #47 and display monster `0x11` (Wild Women).
1. `[0113]` (06,04) Color text `{51:0114}`.
1. `[0135]` (02,03),(04,03) Guards demand your pass. Display monster `0x24` (Pikemen), then color text `{51:013f}`.
1. `[016c]` (03,03) If `![b9,02]`, run combat #0 (Guards and Pikemen). If you win, set `[b9,02]`.
1. `[017f]`(02,03),(04,03) You show your pass. Set `[b9,02]`.
1. `[00e9]` You stepped in the water.
1. `[01ff]` (07,03) Run combat #2 (Murk Tree). If you win, erase this event.
1. `[0196]` Default handler. If the party is outside (0,0)-(7,7), offer to exit to `(00:17,12)` if you're on the west side of the bridge (X<4) or `(00:19,12)` otherwise. If the party is inside that range and on the west side of the bridge (X<4), gate-and-set `[b9,00]` for color text `{51:01e6}`.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|   #3    | Governor's Pass        |  #5   | `[017f]` |

## 0x0c Scorpion Bridge

### Flags

- **Random Encounters:** no

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                                       |
| :------: | :-------: | :--------: | --------------------------------------------- |
| `b9,00`  |  `[b9]`   | `*–––––––` |                                               |
| `b9,01`  |  `[b9]`   | `–*––––––` | Appeased or killed the Scorpions              |
| `b9,02`  |  `[b9]`   | `––*–––––` | The Scorpions block you from using the bridge |
| `b9,04`  |  `[b9]`   | `––––*–––` | Have access to the chest at (04,06)           |

### Events

1. `[00ed]` You stepped in the water.

1. `[00f2]` (02,03),(02,04) If you've already showed the Totem `[b9,01]`, killed the Scorpions `[99,87]`, or you aren't facing the bridge, exit. Otherwise, set `[b9,02]`, display monster `0x27` (scorpion guards), and color text `{52:010c}`.

1. `[0162]` You display Enkidu's Totem. Exit if you killed the Scorpions `[99,87]`. Otherwise, color text `{52:0169}`, set `[b9,01]` and `![b9,02]`.

1. `[01c5]` (03,03),(03,04) Exit if the Scorpions *aren't* angry `![b9,02]` or you killed them already `[99,87]`. Otherwise, run combat #0 (Scorpions). If you win, set `[99,87]`, `[b9,01]`, and `![b9,02]`.

1. `[01e4]` (04,03),(04,04) Run combat #1 (Evil Spirits). If you win, erase this event.

1. `[01e9]` (04,02),(04,05) Run combat #2 (Bears). If you win, erase this event.

1. `[01ee]` (04,01) Run combat #3 (Dogs). If you win, erase this event.

1. `[01f3]` (05,03),(05,04) Run combat #4 (Wolves). If you win, erase this event.

1. `[01fe]` (04,06) If `![99,47]`, run combat #4 (Wolves). If you win, set `[99,47]` and open a [chest](#chest) (`[b9,04]`).

1. `[0225]` Default handler. If the party is outside (0,0)-(7,7), offer to exit to `(00:30,19)` if you're on the west side of the bridge (X<4) or `(00:32,19)` otherwise.

   Gate-and-set `[b9,00]`. If you're on the west side of the bridge (X<4), get paragraph #48, otherwise color text `{52:0275}`.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|   #2    | Enkidu Totem           |  #3   | `[0162]` |

## 0x0d Bridge of Exiles

### Flags

- **Random Encounters:** no

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                                                |
| :------: | :-------: | :--------: | ------------------------------------------------------ |
| `b9,00`  |  `[b9]`   | `*–––––––` | Intro text                                             |
| `b9,01`  |  `[b9]`   | `–*––––––` | You touched the west side of the bridge (doors erased) |

### Events

1. `[00e1]` You stepped in water.
2. `[020b]` (02,04) If you're facing the door, color text `{53:0218}`.
3. `[0279]` (01,04) If `![b9,01]`, read paragraph #50, erase both doors, set `[b9,01]`, and erase this event.
4. `[00e6]` Default handler. If the party is outside (0,0)-(7,7), offer to exit to `(00:06,18)` if you're on the west side of the bridge (X<4) or `(00:08,18)` otherwise.

   Gate-and-set `[b9,00]`. If you're on the east side of the bridge (X>=4), color text `{53:013b}`. Otherwise, color text `{53:01d0}`, erase both doors, and set `[b9,01]`.

## 0x0e Necropolis

### Flags

- **Random Encounters:** yes (1 in 100)

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                          |
| :------: | :-------: | :--------: | -------------------------------- |
| `b9,04`  |  `[b9]`   | `––––*–––` | Have access to the guarded chest |
| `b9,06`  |  `[b9]`   | `––––––*–` | Have access to Nergal's treasure |

### Events

1. `[039c]` (07,14) [Boat dock](#dock).
1. `[03a3]` You stepped in the water.
1. `[03a8]` Run a random combat. If you win, erase this event.
1. `[03c0]` (01,12) Gate-and-set `[99,08]` for access to the [chest](#chest) (`[b9,04]`) with the Stone Trunk.
1. `[03ff]` (02,11),(02,13) Run combat #12 (Grim Guardians). If you win, erase this event.
1. `[040a]` If you haven't burned out the spiders `![99,09]`, run combat #13 (Spiders). If you win, replace this with Event #7.
1. `[0421]` If you haven't burned out the spiders `![99,09]`, color text `{54:0429}`.
1. `[0445]` You burn out the spiders. Color text `{54:0446}` and set `[99,09]`.
1. `[0464]` (05,07) Meet Nergal. If you haven't already fought his goons `![99,0a]`, display monster `0x2b`, read paragraph #114, and run combat #14. If you win, set `[99,0a]` and Nergal demands tribute (paragraph #115).

   If you have fought his goons (either before or just now) but haven't fed him yet `![99,0b]`, display monster `0x2b` and color text `{54:0498}`, then exit while he waits for you to feed him.

   If you have fought his goons `[99,0a]` and fed him `[99,0b]`, Nergal [leaves behind](#chest) (`[b9,06]`) the silver key and some important scrolls.
1. `[04e0]` You give the Mushrooms to Nergal. Color text `{54:04e1}`, then pick a party member to have his head bitten off (paragraph #93). Color text `{54:052d}`,`{54:055a}`,`{54:0581}`, `{54:05d9}`. Set `[99,0b]` and `[b9,06]`
1. `[061e]` (15,10) Color text `{54:061f}` (portal of power)
1. `[065f]` (15,10) You use Arcane Lore; color text `{54:0660}` (portals are random)
1. `[06a7]` (15,11) If the boat is here (`heap[bd]=0x1`), the teleporter takes you back to the dock `(07,14)`. Otherwise it picks a random location:

   - Dilmun, 1N of Purgatory `(00:13,05)`
   - Dilmun, 1S of the Slave Camp `(00:11,02)`
   - The Forlorn/Sun guard bridge `(03:02,02)`
   - The entrance to Mud Toad `(08:07,01)`
   - The statue of Irkalla at Smuggler's Cove `(0a:06,05)`
   - The E edge of the Bridge of Exiles `(0d:07,04)`
   - The Necropolis boat dock `(0e:07,14)`

1. `[03b2]` (00,07) Offer stairs down to the Well of Souls `(12:27,14)`.
1. `[03fa]` (07,06) Run combat #11 (Stone Demon). If you win, erase this event.
1. `[06f5]` Default handler. If the party is outside (0,0)-(16,16), prompt to exit. NE:`(00:28,15)` S:`(00:27,14)` W:`(00:26,15)`.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|   #7    | *L:Mage Fire, H:Fire Light, H:Elvar's Fire, D:Fire Blast,<br />S:Fire Storm, S:Inferno, S:Column of Fire* | #8 | `[0445]` |
|   #9    | Mushrooms              |  #10  | `[04e0]` |
|   #11   | *Arcane Lore*          |  #12  | `[065f]` |

## 0x0f Dwarf Ruins

### Flags

- **Random Encounters:** no.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                       |
| :------: | :-------: | :--------: | ----------------------------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | If set, the tunnel is closed. |

### Events

1. `[00e9]` (02,04) Color text: if `[99,0d]`, the stairs down are accessible `{55:0116}`. If not, the tunnel is blocked `{55:00f1}`.
1. `[0137]` (04,04) Color text: a proud statue `{55:01e3}`. If `![99,0d]`, "the statue has no eyes" `{55:0185}`
1. `[019e]` You use the Jade Eyes on the statue. Set `[99,0d]` and write `[0061]<-0x10`, which opens the tunnel to access the stairs.
1. `[01e2]` (04,01) Color text `{55:01e3}`.
1. `[01fe]` (07,01) Color text `{55:01ff}`.
1. `[0232]` (02,06) Color text `{55:0233}`.
1. `[025a]` (05,04) Color text `{55:025b}`.
1. `[0285]` (00,04) Stairs down to `(10:15,08)`.
1. `[02d1]` (01,06) A [locked chest](#locked) (`[99,28]`, difficulty 4) containing the Dwarf Hammer.

   > This is the same bitsplit as the chest in the Slave Camp, which unfortunately means you get to open one or the other but not both.
1. `[0293]` Default handler. If the party is outside (0,0)-(7,7), prompt to exit. N:`(00:10,22)` E:`(00:11,21)` S:`(00:10,20)` W:`(00:09,21)` .

   If `![b9,00]` and `[99,0d]` is set, write `[0061]<-0x10` to open the tunnel.

   > ... [b9,00] isn't set anywhere on this map, though.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|   #2    | Jade Eyes              |  #3   | `[019e]` |

## 0x10 Dwarf Clan Hall

> Random cool thing: The invisible wall is index 4, texture `0x7f` and metadata `0xe0=11100000`. Usually that texture is also an index into another list, but clearly not that one. Maybe `0x7f` is literal, though?

### Flags

- **Random encounters:** no.
- You **need a light** in order to see.
- You **need a compass** to get your bearings.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                                                    |
| :------: | :-------: | :--------: | ---------------------------------------------------------- |
| `b9,01`  |  `[b9]`   | `–*––––––` | The automata guarding the treasure are about to attack you |
| `b9,02`  |  `[b9]`   | `––*–––––` | "Tortured metal lurches into motion" has been played       |
| `b9,03`  |  `[b9]`   | `–––*––––` | Have access to the Bomb cache (once only)                  |
| `b9,06`  |  `[b9]`   | `––––––*–` | Have access to the Dragon Horn cache (once only)           |
| `b9,07`  |  `[b9]`   | `–––––––*` | Have access to the Mace / Staff / Potion cache (once only) |

### Events

1. `[033c]` (14,08) Stairs up to `(0f:00,04)`.
2. `[038a]` (09,08) Color text `{56:038b}` about the crystal wall.
3. `[03be]` Near the forge. Color text `{56:03bf}`.
4. `[03e6]` (05,08) If the dwarves have been revived `[99,0e]`, color text `{56:03ee}` the smith is working. Otherwise color text `{56:040f}` the forge is empty.
5. `[03d8]` (00,08) Stairs down to `(12:10,21)`.
6. `[034a]` (01,08) Color text `{56:034b}`.
7. `[0446]` You show the Skull of Roba to the smith. If the dwarves have been revived `[99,0e]`, get color text `{56:044d}` and set `[99,0f]` and `[99,6b]`.
8. `[04b6]` (06,05) Color text `{56:04b7}`.
9. `[04f2]` (09,04) If the dwarves have *not* been revived `![99,0e]` or you stole from them `[99,10]`, run combat #1 (Gorgon). If you win (or the bits were set the other way), erase this event.
10. `[0513]` (09,06) Color text `{56:0514}` warning you about the Gorgon fight, even if it isn't actually there.
11. `[0534]` (08,15) If the dwarves have *not* been revived `![99,0e]` or you stole from them `[99,10]`, run combat #0 (Automata). If you win (or the bits were set the other way), erase this event.
12. `[0545]` (07,11) If the dwarves have been revived `[99,0e]`, erase this event. Otherwise, if you *just* stole from them `[b9,01]`, run combat #0 (Automata) and erase this event if you win. Otherwise, read paragraph #118.
13. `[0564]` (06,13) Gate-and-set `[99,4c]` for the Bombs and Spiked Flail chest (`[b9,03]`). If the dwarves have not been revived `[99,0e]`, you're stealing; set `[99,10]`. Gate-and-set `[b9,02]` for color text that indicates that you woke up the Automata. (also sets `[b9,01]`).
14. `[0663]` (09,12) and room. If the dwarves have not been revived `![99,0e]`, read paragraph #119. Otherwise, if you stole from them `[99,10]`, color text `{56:0678}` they don't like you. (*There's an Encounter all set where the dwarves attack you, but it never gets called. They even have a 2d4 breath weapon.*) Otherwise, color text `{56:069a}` they're rebuilding.
15. `[06c0]` You depetrify the dwarves (so long as you didn't do that already); set `[99,0e]` and read paragraph #38. If you haven't stolen from them `![99,10]`, **gain 500 XP** and color text `{56:06d8}` the dwarves give you access to their treasure. Otherwise color text `{56:0739}` they're pissed at you.
16. `[0761]` Do nothing.
17. `[05eb]` Gate-and-set `[99,4d]` for the Dragon Horn (`[b9,06]`). 
18. `[061d]` Gate-and-set `[99,4e]` for the Crush Mace and Spell Staff (`[b9,07]`). 
19. `[0761]` Default handler; do nothing.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|   #4    | Skull of Roba          |  #7   | `[0446]` |
|   #14   | *D:Soften Stone*       |  #15  | `[06c0]` |

## 0x11 Freeport

### Flags

- **Random Encounters:** yes (1 in 100)

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                                               |
| :------: | :-------: | :--------: | ----------------------------------------------------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | Set upon entering the town and checking for the Sword |
| `b9,01`  |  `[b9]`   | `–*––––––` | Freeport has been deserted                            |

### Events

1. `[0386]` (14,15) [Boat dock](#boat).
1. `[038d]` You stepped in the water.
1. `[03b2]` (10,03) Color text `{57:03b6}` "Tars City Council"
1. `[03c6]` (11,03) Read paragraph #56.
1. `[03ce]` (02,12) Color text `{57:03d2}` "Freeport City Council"
1. `[03fa]` (01,12) Read paragraph #57.
1. `[0392]` Color text `{57:0393}` (harbor bridge).
1. `[06ec]` (03,04) Exit if the Sword of Freedom has already been revealed `[99,07]`. Read paragraph 0x1b, color text `{57:0700}` with the world's most annoying apostrophe typo. Pick a player to take the sword. If you don't hit ESC to cancel, color text `{57:073f}`,`{57:079e}`,`{57:07b1}`,`{57:07d5}`,`{57:0819}`,`{57:0831}`, and finally `{57:084e}` and `{57:08e1}`. The player you picked is killed (0 Stn, 0 HP, flag 0x1). Set `[99,07]`.

   > You know, you *have* a "personal pronoun" function. You aren't *obligated* to print the PC's name over and over again.
1. `[08f8]` (03,04) Gate-and-set `[99,07]`; color text `{57:08ff}` and `{57:096e}` to reveal the illusionary sword.
1. `[09a1]` (02,05) If the Sword has been revealed `[99,07]`, gate-and-set `[b9,01]`. Color text `{57:09b7}`; Freeport is a ghost town. Erase events #4, #6, and #19-24.

   > Weird that this only takes place if you happen to step on the NW corner of the island after revealing the fake sword.
1. `[0a77]` (14,08) If you haven't beaten them already `![99,4b]`, run combat #5 (Order of the Sword). If you win, set `[99,4b]` and there's a [locked chest](#locked) (`[99,4a]`, difficulty 4) with the Stone Hands.
1. `[0455]` (03,15) Color text `{57:0459}` "The Temple Faith"
1. `[0402]` (05,15) Color text `{57:0406}` "Ryan's Armor"
1. `[0412]` (05,09) Color text `{57:0416}` "Freeport Arms"
1. `[0423]` (13,07) Color text `{57:0427}` "The Brew's Brothers"
1. `[0438]` (03,02) Color text `{57:043c}` "Bewitching Potions and Exilers"
1. `[0468]` (07,02) Color text `{57:046c}` "Magic Inc."
1. `[0476]` (13,08) Color text `{57:047a}` "The Order of the Sword"
1. `[048d]` (07,01) High Magic [shop](#shop)
1. `[04e1]` (04,09) Weapon [shop](#shop)
1. `[053f]` (05,14) Armor [shop](#shop)
1. `[058b]` (03,01) Dragon Stone [shop](#shop)
1. `[05e4]` (14,07) [The Brew's Brothers](#tavern). Halifax is here.
1. `[0a3e]` (02,07) If the Sword hasn't been revealed `![99,07]`, read paragraph #52.
1. `[0a4c]` If you're facing S, hop across the bay `{57:0a5b}` .
1. `[0a51]` ... N
1. `[06d8]` (03,16) Temple ($4/hp).
1. `[08f6]` Do nothing.
1. `[08f6]` Do nothing.
1. `[0a9a]` Default handler. If the party is outside (0,0)-(15,16), prompt to exit. NE:`(00:43,24)` S:`(00:43,22)` W:`(00:42,23)` . Gate-and-set `[b9,00]` to read paragraph #51 and run Event #10 (is Freeport a ghost town?)

   > This is almost certainly a logic bug. If you enter the city, it immediately sets `[b9,00]`. Then when you touch the Sword, it sets `[b9,01]`. The next time you take a step, the default handler *should* clear out the city and print the "ghost town" message, but it doesn't, because `[b9,00]` was already set.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
| #10, #16, #28 | Golden Boots     |  #26  | `[0a51]` |
| #8, #24, #29  | Golden Boots     |  #25  | `[0a4c]` |
|   #8    | *H:Reveal Glamour, H:Sense Traps, S:Disarm Trap* | #9 | `[08f8]` |

## 0x12 Magan Underworld

### Flags

- **Random encounters:** yes (1 in 50)
- You **need a light** in order to see.
- The map **wraps**.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                               |
| :------: | :-------: | :--------: | ------------------------------------- |
| `b9,01`  |  `[b9]`   | `–*––––––` | Paid the fairies' toll                |
| `b9,02`  |  `[b9]`   | `––*–––––` | Have access to the Water Potion chest |
| `b9,04`  |  `[b9]`   | `––––*–––` | Defeated Namtar the second time       |

### Events

1. `[0c65]` You stepped in the water.
1. `[0c6a]` (13,04) Stairs up to Purgatory `(01:07,12)`, but only if no one in the party has the Dead Body of Namtar
1. `[0c84]` (19,19) Stairs up to Salvation `(03:03,04)`, but only if...
1. `[0c90]` (19,04) Stairs up to the Tars Dungeon `(27:00,05)`, but only if...
1. `[0c9c]` (10,21) Stairs up to the Dwarven forge `(10:00,08)`, but only if...
1. `[0ca8]` (27,14) Stairs up to the Necropolis `(0e:00,07)`, but only if...
1. `[0cb4]` (16,14) Stairs up to Lansk Undercity `(26:14,05)`, but only if...
1. `[0cc0]` (02,06) Stairs up to the Mystic Wood (`17:04,15)`, but only if...
1. `[1147]` (20,20) Color text `{58:1148}`. You can't cast *D:Scare* from here.
1. `[1166]` (20,19) and surrounding.
   - If you've already beaten Namtar twice `[b9,04]` and don't have the Dead Body in your inventory, open a [chest](#chest) (`[99,86]`) with the Dead Body.
   - If you haven't `![b9,04]` and you *do* have the Dead Body, drop it, set `[b9,04]`, set `heap[bd]` to prevent Running, and run combat #12. If you lose, do it again. If you win, set `[99,86]` and open the chest.
   - Color text `{58:11b9}` 

1. `[135c]` (14,16) The Lansk Balcony; do nothing.
1. `[0ccc]` (14,14) Color text `{58:0ccd}`.
1. `[0cfd]` (20,22) If you *Scared* them off `[99,55]` or have already paid their toll `[b9,01]`, the fairies watch you pass `{58:0d83}`. Otherwise, they demand "the price of life". Agree, and your Health and Stun are both set to 1; set `[b9,01]` and color text `{58:0d60}`. Refuse, and they don't let you pass `{58:0da5}`.
1. `[1147]` (20,21),(20,23) Color text `{58:1148}`.
1. `[0dce]` You cast *D:Scare* while next to the Fairies. Gate-and-set `[99,55]` to **permanently scare them off** `{58:0dd8}`.
1. `[0e04]` (26,16) If anyone in your party has prayed to Irkalla (character flag `0x80`), you are welcome `{58:0e0f}` to her realm. Otherwise you're kicked out `{58:0e35}`.
1. `[0e7d]` (12,29) Color text `{58:0e83}`. If you enter the cave, read paragraph #127.
1. `[0ebd]` (03,11) You find the Well of Souls `{58:0ebe}`.
1. `[0ed6]` You used *Arcane Lore* at the Well. Color text `{58:0eda}` and you're asked which character's body to throw into the well. It doesn't work if they aren't dead `{58:0f33}`. Otherwise, read paragraph #128; the character is no longer dead (`0x01`) but they are stunned (`0x80`) and given 1 HP.
1. `[0f5e]` You step in flames `{58:0f5f}`. Take 1 HP damage.
1. `[0fbb]` (02,17) You hop to the Isle. Face E, step forward twice, and color text `{58:0fc7}`.
1. `[0fc0]` (04,17) You hop off the Isle. Face W...
1. `[0f82]` (02,17) Color text `{58:0f83}`.
1. `[0f9a]` (04,17) Color text `{58:0f9b}`.
1. `[0fe4]` (05,17) If you haven't freed Irkalla yet `![99,6a]`, display monster `0x2f` (Irkalla) and read paragraph #137. Otherwise, run a [chest](#chest) (`[b9,02]`) with the Water Breathing potion.
1. `[1018]` Gate-and-set `[99,6a]` to free Irkalla from her chains with Nergal's key. Drop the Silver Key, color text `{58:1025}` and `{58:10c1}`, set `[b9,02]`, and **gain 500 XP**.
1. `[10ff]` (05,18) Run a [chest](#chest) (`[99,6b]`) with paragraph #138 and the Sword of Freedom. Color text `{58:111b}`.
1. `[125d]` (31,02) If `![99,73]`, run combat `0x1b` (**which doesn't exist?**). If you win, set `[99,73]` and open a [locked chest](#locked) (`[99,72]`, **difficulty 3**) with the Rusty Axe.
1. `[135e]` You throw Namtar's body into The Pit. Run the "You Win" slideshow.
1. `[1280]` (11,24) Run a [locked chest](#locked) (`[99,7b]`) with The Slicer and some Dragon Stones.
1. `[128d]` (28,18) Gate-and-set `[99,81]` and take the Leap of Faith `{58:129c}`,`{58:12cf}`,`{58:1307}`. All party members are granted +5 AP.
1. `[11e5]` (09,12) First fight with Namtar.
   - If you've already beaten Namtar once `[99,85]`, run a [chest](#chest) (`[99,83]`) with the Dead Body.
   - If you haven't `![99,85]`...
     - If you have the Dead Body in your inventory, drop it and set `[99,84]`. Now you don't.
     - If you don't have the Dead Body but `[99,84]`, run combat #11. If you lose, run away. If you win, set `[99,83]` and `[99,85]`. Now you've beaten Namtar, so run the chest.

1. `[1334]` (09,14) Magic refresh pool. Color text `{58:1335}`. Non-dead characters have their Power restored to max.
1. `[135c]` Default handler; do nothing.

### Actions

| Special | Skill, Item, or Spell | Event | Handler  |
| :-----: | :-------------------: | :---: | :------: |
|   #14   |       *D:Scare*       |  #15  | `[0dce]` |
|   #18   |     *Arcane Lore*     |  #19  | `[0ed6]` |
|   #23   |     Golden Boots      |  #21  | `[0fbb]` |
|   #24   |     Golden Boots      |  #22  | `[0fc0]` |
|   #25   |      Silver Key       |  #26  | `[1018]` |
|   #10   |       Dead Body       |  #29  | `[135e]` |

## 0x13 Slave Mines

### Flags

- **Random encounters:** no
- The map **wraps**.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                                  |
| :------: | :-------: | :--------: | ---------------------------------------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | Ran the "sold into slavery" routine once |
| `b9,01`  |  `[b9]`   | `–*––––––` | Party has been chained                   |
| `b9,02`  |  `[b9]`   | `––*–––––` | Have access to the Tin Cup               |
| `b9,03`  |  `[b9]`   | `–––*––––` | Have access to the Rocks                 |
| `b9,04`  |  `[b9]`   | `––––*–––` | Have access to the Pick Handle           |
| `b9,05`  |  `[b9]`   | `–––––*––` | Have access to the Shoelaces             |

### Events

1. `[0343]` (05,11) Stairs up to Slave Estate `(25:05,12)`

1. `[0352]` (08,08) If you're still a slave `[b9,01]`, guards sneer at you `{59:035a}`. Otherwise, run combat #0 (Guards). If you win, erase this event.

1. `[0395]` (13,04) The garbage pile `{59:0396}` is north of here.

1. `[03c2]` (13,02) and surroundings. This room stinks `{59:03c3}`.

1. `[0716]` (13,05) A garbage pile. If you didn't have any inventory when you came in, color text `{59:071f}`. Otherwise, your discarded inventory is here `{59:0742}`, but you can't pick it up if you're still chained (status `0x2`). The rest of this routine is basically the same as "get loot from chest" except there can be a larger number of items to pick up.

1. `[03dd]` (04,05) Read paragraph #60.

1. `[03e5]` (02,15) A ["chest"](#chest) (`[b9,02]`) with a tin cup.

1. `[040d]` (12,08) A [pile](#chest) (`[b9,03]`) of loose rocks.

1. `[0439]` (07,01) A pile of [debris](#chest) (`[b9,04]`) with a pick handle.

1. `[0462]` (12,12) A feeble little spring `{59:0463}`

1. `[049e]` (03,06) If the old man is dead `[99,36]`, you find his [shoelaces](#chest) (`[b9,05]`). Otherwise, the dying old man begs for water `{59:04a5}`. 

1. `[04d5]` (03,06) You use the Cup on the old man. If he's dead `[99,36]`, exit. If the Cup is empty (item flag `0x0`), he scolds you and asks for water `{59:04e3}`. If the Cup is full (item flag `0x1`), read paragraph #61. He dies `[99,36]` and wills you the laces from his boots `[b9,05]`.

1. `[0483]` (12,12) You fill the Cup `{59:0488}`. Set item flag `0x1` .

1. `[0509]` You use one of the hammer parts. If you have all three parts (Rocks, Laces, Handle), drop them and pick up the Crude Hammer. Otherwise, you're still missing something `{59:0558}`.

1. `[058a]` Gate-and-set `![b9,01]` to break your chains `{59:059d}`. Set board flag `0x1` (**purpose unknown**). Drop the Hammer and all the Chains, then clear status bit `0x2` from everyone in the party.

1. `[05ca]` (04,12) If you haven't defeated the guards yet `![99,33]`, they're blocking the exit `{59:05d1}`.

1. `[0600]` (05,12) If you're still wearing chains `[b9,01]`, the guards `{59,0627}` drag you back into the mines (05,06). Otherwise, run combat #3 (Guards and Slave Boss). If you win, read paragraph #62, set `[99,33]`, and erase this event.

1. `[066a]` (04,11) A [locked chest](#locked) (`[99,37]`, difficulty 1)

1. `[089d]` Run combat #0 (Guards). If you win, erase this event.

1. `[08a2]` (03,00) Run combat #1 (Snakes). If you win, erase this event.

1. `[08a7]` (02,03) Run combat #2 (Spiders). If you win, erase this event.

1. `[067d]` Default handler. Gate-and-set `[b9,00]`. Then set `[b9,02]`,`[b9,03]`, and `[b9,04]` to enable access to the Hammer parts. If you somehow got here *without* selling yourself into slavery in Purgatory `![99,35]`, exit.

   Set `![99,35]` and `[b9,01]`. Take the party's inventory, unequip it, and move it all to a data area `{59:08b2}`. Give everyone a set of Chains and set status `0x2` (chained). Throw away the party's gold.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|   #11   | Battered Cup           |  #12  | `[04d5]` |
|   #10   | Battered Cup           |  #13  | `[0483]` |
|    —    | Laces, Handle, Rocks   |  #14  | `[0509]` |
|    —    | Crude Hammer           |  #15  | `[058a]` |

## 0x14 Lansk

### Flags

- **Random encounters:** no

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning |
| :------: | :-------: | :--------: | ------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | Intro paragraph |
| `b9,01`  |  `[b9]`   | `–*––––––` | Stairs to the Undercity are open |

### Events

1. `[0b0b]` You stepped in the water.
1. `[03df]` (04,03) Sign for the Governor's Office `{5a:03e3}`
1. `[03f2]` (05,03) Sign for the Visitor's Registration Dept. `{5a:03f6}`
1. `[0410]` (11,05) Sign for the Visitor's Information Bureau `{5a:0414}`
1. `[042b]` (13,13) Sign for the Quarter Master's Office `{5a:042f}`
1. `[0443]` (06,14) Sign for the Dept. of Lubrication `{5a:0447}`
1. `[045b]` (02,11) Sign for the Office of the Bureau of Departments `{5a:045f}`
1. `[0491]` (07,06) Read paragraph #35.
1. `[0499]` (02,08) Vibration beneath your feet `{5a:049a}`
1. `[04da]` (05,04) Registration. `{5a:04e3}`,`{5a:0561}`,`{5a:05f1}`.
1. `[0664]` You try to bribe an official, but they warn you off `{5a:0665}` because this isn't the Department of Lubrication.
1. `[06e1]` (12,13) Quartermaster. Mog leaves his estate to you `{5a:06ea}`. This doesn't have any effect on the game.
1. `[0796]` (03,11) Bureau of Departments; it's empty `{5a:0797}`
1. `[07cd]` (04,04) Governor's Office. He gives you a set of papers `{5a:07d6}`, item #0.
1. `[0875]` (11,04) Information Bureau. He asks for your papers `{5a:087e}`.
1. `[08e3]` You try to use *Bureaucracy*, but the official turns into an Australian `{5a:08e4}`.
1. `[0933]` You show him your papers. If they haven't been stamped (item flag `0x0`), he throws you out `{5a:09eb}`. If they have `0x1`, he exchanges it `(5a:0952}` for a Governor's Pass, item #1.
1. `[098c]` (06,13) Lubrication. He asks for your papers `{5a:0991}`.
1. `[09b7]` You show him your papers and he stamps them `{5a:09b8}`. Set item flag `0x1`.
1. `[09d7]` You use *Bureaucracy*. The official requires a bribe `{5a:09d8}` of $500 or more. Give too little and `{5a:0a34}`. Don't have enough to bribe him, `{5a:0ad2}`. Otherwise, he opens the stairs down to the Undercity `{5a:0a70}`,`{5a:0abe}`; set `[b9,01]`.
1. `[0af7]` (05,08) If `[b9,01]`, offer stairs down to the Undercity `(26:05,08)`.
1. `[0b44]` (03,07) A [locked chest](#locked) (`[99,3b]`, difficulty 2) with the Druid's Mace.
1. `[0b4f]` Run a random combat. If you win, erase this event.
1. `[0b10]` Default handler. If the party is outside (0,0)-(16,17), prompt to exit. NE:`(00:17,14)` S:`(00:16,13)` W:`(00:15,14)`.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
| #10, #12, #14 |  *Bureaucracy*  |  #11  | `[0664]` |
| #15 |  *Bureaucracy*  |  #16  | `[08e3]` | 
| #15 |  Governor's Pass  |  #17  | `[0933]` |
| #18 |  Governor's Pass  |  #19  | `[09b7]` |
| #18 |  *Bureaucracy*  |  #20  | `[09d7]` |

## 0x15 Sunken Ruins (Above)

### Flags

- **Random encounters:** no

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning |
| :------: | :-------: | :--------: | ------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | Intro text |
| `b9,04`  |  `[b9]`   | `––––*–––` | Chest access |

### Events

1. `[0159]` Run random combat. If you win, erase this event.
2. `[011a]` You pick a lock. Only works on wall texture `0x01`. Requires *Lockpick 4*.
3. `[0162]` Color text `{5b:0163}`.
4. `[0197]` You try to swim, but don't get very far `{5b:0198}`, `{5b:01f0}`.
5. `[0220]` You use the Water Breathing potion. Travel to `(16:05,03)`.
6. `[0257]` Gate-and-set `[99,48]` for access to a [chest](#chest) `[b9,04]`.
7. `[0299]` Default handler. If the party is outside (0,0)-(8,8), prompt to exit. NW:`(00:37,15)` SE:`(00:38,14)`

    Gate-and-set `[b9,00]` for color text `{5b:02cb}`, `{5b:033d}`.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
| #3 |  *Swim* or *Climb*  |  #4  | `[0197]` |
| #3 |  Water Potion  |  #5  | `[0220]` |
| — |  *Lockpick*  |  #2  | `[011a]` |

## 0x16 Sunken Ruins (Below)

### Flags

- **Random encounters:** no.
- You **need a light** in order to see.
- You **need a compass** to get your bearings.
- The map **wraps**.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning |
| :------: | :-------: | :--------: | ------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | Intro text |
| `b9,01`  |  `[b9]`   | `–*––––––` | Acquired the Clam |
| `b9,03`  |  `[b9]`   | `–*––––––` | |
| `b9,04`  |  `[b9]`   | `–*––––––` | Chest access |

### Events (chunk 0x5c)

1. `[00ed]` *Entrance square: intro text, clam-to-skull, stairs up*
1. `[01f6]` *Take the clam*
1. `[0303]` *Chest (diff 3)*
1. `[034e]` *Murky locker*
1. `[0314]` *Lockpick (diff 2)*
1. `[03ae]` Run a random combat. If you win, erase this event.
1. `[00e4]` Spinner trap; assign random(4) to `heap[03]` (facing).
1. `[03b7]` *Default handler.*

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|   #4    | *Lockpick*             |  #5   | `[0314]` |

## 0x17 Mystic Wood

### Flags

- **Random Encounters:** yes (1 in 25)

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning |
| :------: | :-------: | :--------: | ------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | |
| `b9,01`  |  `[b9]`   | `–*––––––` | |
| `b9,02`  |  `[b9]`   | `––*–––––` | |
| `b9,03`  |  `[b9]`   | `–––*––––` | |
| `b9,04`  |  `[b9]`   | `––––*–––` | |
| `b9,05`  |  `[b9]`   | `–––––*––` | |
| `b9,06`  |  `[b9]`   | `––––––*–` | |

### Events (chunk 0x5d)

1. `[038a]`
1. `[038f]`
1. `[03bd]`
1. `[0463]`
1. `[0538]`
1. `[053d]`
1. `[0543]`
1. `[0549]`
1. `[0576]`
1. `[0501]`
1. `[0594]`
1. `[05af]`
1. `[05e4]`
1. `[064c]` You used Arcane Lore on the island shrine. Read paragraph #72.
1. `[0655]`
1. `[07be]`
1. `[07c6]`
1. `[0829]` *Wrestling Enkidu; you win if 15 + (0-8) is lower than your STR*
1. `[043b]`
1. `[0aab]`
1. `[0acc]`
1. `[0718]`
1. `[06ed]`
1. `[06f2]`
1. `[0af0]`
1. `[0b09]`
1. `[0b1a]`
1. `[074c]`
1. `[0b3f]`
1. `[0b4b]`
1. `[0af9]`
1. `[0afe]`
1. `[0ba6]` Default handler.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
| #5, #9 |  *Tracker*  |  #11  | `[0594]` |
| #13 |  any item  |  #15  | `[0655]` |
| #13 |  *Arcane Lore*  |  #14  | `[064c]` |
| #17 |  *SPR* or cast *D:Beast Call*  |  #18  | `[0829]` |
| #3 |  Soul Bowl  |  #19  | `[043b]` |
| #20 |  *Climb*  |  #21  | `[0acc]` |
| #12 |  Golden Boots  |  0x17  | `[06ed]` |
| #22 |  Golden Boots  |  0x18  | `[06f2]` |
| #12, #22 |  *Swim*  |  0x1c  | `[074c]` |
| #29 |  *Swim*  |  0x1e  | `[0b4b]` |

## 0x18 Snake Pit

### Flags

- **Random Encounters:** no

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning |
| :------: | :-------: | :--------: | ------- |
| `b9,00`  |  `[b9]`   | `*–––––––` |         |

### Events (chunk 0x5e)

1. `[0343]` You stepped in the water.
1. `[0348]` Color text `{5e:0349}`
1. `[035a]` Color text `{5e:035b}`
1. `[037a]` Color text `{5e:037a}`
1. `[039d]` Color text `{5e:039d}`
1. `[03f6]` Color text `{5e:03f7}`
1. `[0420]` Read paragraph #81.
1. `[0428]` Show the Jade Eyes to the sad dwarf? Color text `{5e:0429}` urges you to "replace them in the statue".
1. `[045f]` 
1. `[0471]`
1. `[0532]`
1. `[0565]`
1. `[05c5]`
1. `[0679]`
1. `[069d]`
1. `[06b9]`
1. `[06f3]`
1. `[07f1]`
1. `[073b]` The old man teaches you *D:Beast Call*.
1. `[06e0]`
1. `[07ba]` Default handler.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
| #7 |  item #9  |  #8  | `[0428]` |
| #10 |  Signet Ring  |  #12  | `[0565]` |
| #17 |  Branches, *Druid Magic*  |  #19  | `[073b]` |

> Well, this is a poser... there *is no* item #9. It's clearly supposed to be the Jade Eyes, given paragraph #81 and the result is for the dwarf to encourage you to return them to the statue to reawaken the other dwarves. But if you bring the Eyes here and try to Use them on the Dwarf, it doesn't work.

## 0x19 Kingshome

### Flags

- **Random Encounters:** no.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning |
| :------: | :-------: | :--------: | ------- |
| `b9,00`  |  `[b9]`   | `*–––––––` |         |

### Events (chunk 0x5f)

1. `[079c]`
1. `[0392]`
1. `[0382]`
1. `[0335]`
1. `[03f6]`
1. `[03fe]`
1. `[0453]`
1. `[0516]`
1. `[061b]`
1. `[06b9]`
1. `[06f9]`
1. `[07c9]`
1. `[078a]`
1. `[05db]`
1. `[0414]`
1. `[07a1]` Default handler.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
| #2 |  Signet Ring  |  #3  | `[0382]` |
| —  |  *Lockpick*  |  #12  | `[07c9]` |

## 0x1a Pilgrim Dock

### Flags

- **Random Encounters:** no.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning |
| :------: | :-------: | :--------: | ------- |
| `b9,00`  |  `[b9]`   | `*–––––––` |         |

### Events

1. `[0112]` You stepped in the water.
1. `[0117]`
1. `[0142]`
1. `[01e3]` Color text `{60:01e4}`.
1. `[01f7]` Read paragraph #83.
1. `[01ff]`
1. `[0243]` Read paragraph #84.
1. `[024b]` Color text `{60:024c}`.
1. `[026b]` Color text `{60:0271}`. Travel to `(04:07,15)`.
1. `[02a2]` Run combat #2. If you win, erase this event.
1. `[02e7]` Lockpick. Texture 0x1 requires Lockpick 3. Texture 0x3 always works.
1. `[02ad]` Default handler.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|   #0    | *Lockpick*             |  #11  | `[02e7]` |

## 0x1b Depths of Nisir

>  the "Dragon Wand" is new...!

### Flags

- **Random Encounters:** yes (1 in 100)
- You **need a light** in order to see.
- You **need a compass** to get your bearings.
- The map **wraps**.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning |
| :------: | :-------: | :--------: | ------- |
| `b9,00`  |  `[b9]`   | `*–––––––` |         |

### Events (chunk 0x61)

1. `[19d3]` *Lockpick. Always works on wall texture 0x01.*
1. `[0c70]`
1. `[0c9d]`
1. `[0cf6]`
1. `[0cf1]`
1. `[0d23]`
1. `[0d94]`
1. `[0db3]`
1. `[0db3]`
1. `[0e29]` The Air Element[al] ferries you across the gap. Face E and move forward three times.
1. `[0e2e]` The Air Element[al] ferries you across the gap. Face W and move forward three times.
1. `[0e58]`
1. `[0ea1]`
1. `[0f00]`
1. `[0f01]`
1. `[0f1c]` *Set `[b9,01]` to disable the invisible wall maze*
1. `[0f92]`
1. `[0fbc]`
1. `[10a5]`
1. `[1100]`
1. `[1135]`
1. `[1156]`
1. `[11e8]`
1. `[121a]`
1. `[1215]`
1. `[1225]`
1. `[125e]`
1. `[1265]`
1. `[1257]`
1. `[1249]`
1. `[1250]`
1. `[123b]`
1. `[1242]`
1. `[1275]`
1. `[1313]`
1. `[146d]`
1. `[148b]`
1. `[14b9]`
1. `[17b4]`
1. `[14ab]`
1. `[19fa]` Default handler.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|    —    | *Lockpick*             |  #1   | `[19d3]` |
|   #8    | *H:Air Summon*         |  #10  | `[0e29]` |
|   #9    | *H:Air Summon*         |  #11  | `[0e2e]` |
|    —    | *H:Reveal Glamour*     |  #16  | `[0f1c]` |
| #36, #37 | Dragon Gem            |  #39  | `[17b4]` |

## 0x1c Old Dock

### Flags

- **Random Encounters:** no.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning |
| :------: | :-------: | :--------: | ------- |
| `b9,00`  |  `[b9]`   | `*–––––––` |         |

### Events (chunk 0x62)

1. `[00f5]`
1. `[00fa]`
1. `[01e7]`
1. `[0203]`
1. `[021d]`
1. `[0267]`
1. `[023e]`
1. `[02b1]`
1. `[02c4]`
1. `[0301]`
1. `[033e]`
1. `[0392]` *Move the statue; requires STR 24*.
1. `[0318]` Default handler.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
| #5 | Lansk Ticket |  #9  | `[02c4]` |
| #6 | Pilgrim Garb |  #10  | `[0301]` |
| #11 |  *STR*  |  #12  | `[0392]` |

## 0x1d Siege Camp

> `Mace: Axe, 1d12, -1AV, requires Strength 17, $70`

### Flags

- **Random Encounters:** no.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning |
| :------: | :-------: | :--------: | ------- |
| `b9,00`  |  `[b9]`   | `*–––––––` |         |

### Events (chunk 0x63)

1. `[0335]`
1. `[0379]`
1. `[0387]`
1. `[040d]`
1. `[0451]`
1. `[0507]`
1. `[0415]`
1. `[0556]`
1. `[05c9]`
1. `[08ef]` You climb over the rocks. Color text `{63:08f0}`, then travel to `(03,07)`.
1. `[0916]`
1. `[0954]`
1. `[04c8]`
1. `[095f]`
1. `[0968]` Default handler.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|   #9    | *DEX, Climb*           |  #10  | `[08ef]` |

## 0x1e Game Preserve

### Flags

- **Random Encounters:** yes (1 in 33)

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning |
| :------: | :-------: | :--------: | ------- |
| `b9,00`  |  `[b9]`   | `*–––––––` |         |

### Events (chunk 0x64)

1. `[0337]`
1. `[033c]`
1. `[035c]`
1. `[03b8]`
1. `[03e3]`
1. `[041f]`
1. `[07a7]`
1. `[0353]`
1. `[049a]`
1. `[057c]`
1. `[0543]`
1. `[0827]`
1. `[05b3]`
1. `[05d8]`
1. `[0646]`
1. `[0678]` Default handler.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
| #3 |  *Hiding* or *Tracker*  |  #6  | `[041f]` |
| — |  *Bureaucracy, Forest Lore, Tracker* | #7  | `[07a7]` |
| — |  Signet Ring  |  #12  | `[0827]` |
| — |  *Forest Lore*  |  #4  | `[03b8]` |
| — |  *Tracker*  |  #5  | `[03e3]` |
| #9 |  any item  |  #10  | `[057c]` |
| #9 |  *STR*  |  #11  | `[0543]` |

## 0x1f Magic College

### Flags

- **Random Encounters:** no

### Events

1. `[0109]` (01,00) Color text `{65:010a}` (tracks)
1. `[01a5]` (01,00) If you're facing N, color text `{65:01ab}`, then `{65:01bd}`, then `{65:01fb}`. The front door appears.
1. `[0120]` (01,00) Color text `{65:0120}` (this is the Magic College)
1. `[016b]` (01,00) Color text `{65:016c}` (can't quite see the door)
1. `[026f]` (Room 1) The front door closes behind you. Color text `{65:0279}`, then `{65:02d3}` (wall of fire).
1. `[0324]` (Room 1) Color text `{65:0325}` (froze the wall). Travel to `(02,04)`.
1. `[034b]` (Room 2) Read paragraph #141 (second wall of fire)
1. `[037a]` (Room 2) Color text `{65:037b}` (burn through the wall of ice). Travel to `(03,05)`.
1. `[0352]` (Room 2) Color text `{65:0353}` (actually a wall of ice)
1. `[03a3]` (Room 3) Read paragraph #142 (gargoyle)
1. `[03aa]` (03,04) Color text `{65:03b0}` (the gargoyle petrifies you), then `{65:0404}`. You're dumped outside `(04,06)`.
1. `[043d]` (Room 3) Color text `{65:043e}` (invisible). Travel to `(03,02)`.
1. `[046b]` (Room 4) Read paragraph #143 (philistine)
1. `[0472]` (04,01) Set `heap[8a]=0xff` to prevent magic use. Run combat #0 (Philistine), then clear `heap[8a]`. If you lose, color text `{65:04e9}` and you're dumped outside `(04,06)`. If you win, color text `{65:0486}` and travel to `(05,01)`.

   > Note that at this point, the walls around (06,02) are invisible and allow you to walk through them (metadata 22), and the door is visible. Also, I think this is the only place that `h[8a]` is ever used to create an anti-magic field.
1. `[051b]` (Room 5) Read paragraph #144 (granite block)
1. `[0522]` (06,02) Color text `{65:0528}` (smashed to bits), then `{65:0574}`. You're dumped outside `(04,06)`.
1. `[05aa]` (Room 5) Color text `{65:05ab}` (destroyed the block). Travel to `(06,03)`, as below.
1. `[0590]` (Room 5) Color text `{65:0591}` (disarmed the trap). Travel to `(06,03)`. Hide the door S of you: set `[009f]=0x13` (N wall texture at 06,02, was `0x23`)
1. `[05e4]` (06,03) Read paragraph #145 (that was novel)
1. `[05f5]` (05,03) Color text `{65:0600}` (Utnapishtim is near).
1. `[05ec]` (06,04) Color text `{65:0600}` (Utnapishtim is near).
1. `[0618]` (05,04) Display monster image `0x35` (Utnapishtim). Fake a combat start screen `{65:062a}` and prompt for Fight/Quickfight/Run. Run works as usual. Fight/Quickfight result in color text `{65:067f}` and you're dumped outside `(04,06)`.
1. `[0695]` (05,06) Read paragraph #146 (you win). Prompt `{65:06a2}` to pick a reward. Color text `{65:06ce}` if you take the Soul Bowl. Then open an unlocked [chest](#locked). Then you're dumped outside `(04,06)`.
1. `[0690]` (06,05) Hide the door S of you: set `[006f]=0x10`. (N wall texture at 06,04, was `0x20`)

   > Wall texture 1 has metadata `0xf0` (impassable); texture 2 has metadata `0x90` (no wall). Because this wall is closed behind you, you have to leave the map entirely and come back in to reset this door if you want to repeat the Exam (say, because you picked the wrong item, or you just really wanted a second Soul Bowl).
1. `[0745]` Default handler. If the party is outside (0,0)-(7,7), prompt to exit. NE:`(00:37,24)` S:`(00:36,23)` W:`(00:35,24)`.

### Actions

| Special |                    Skill, Item, or Spell                     | Event | Handler  |
| :-----: | :----------------------------------------------------------: | :---: | :------: |
|   #1    |                      *H:Reveal Glamour*                      |  #4   | `[016b]` |
|   #1    |                        *Arcane Lore*                         |  #3   | `[0120]` |
|   #1    |                          Spectacles                          |  #2   | `[01a5]` |
|   #5    |                  *H:Ice Chill, H:Big Chill*                  |  #6   | `[0324]` |
|   #7    |                Spectacles, *H:Reveal Glamour*                |  #9   | `[0352]` |
|   #7    | *L:Mage Fire, H:Fire Light, H:Elvar's Fire, D:Fire Blast,<br />S:Fire Storm, S:Inferno, S:Column of Fire* |  #8   | `[037a]` |
|   #10   |                       *H:Cloak Arcane*                       |  #12  | `[043d]` |
|   #15   |                       *D:Soften Stone*                       |  #17  | `[05aa]` |
|   #15   |                       *S:Disarm Trap*                        |  #18  | `[0590]` |

## 0x20 Dragon Valley

### Flags

- **Random Encounters:** yes (1 in 33)

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning |
| :------: | :-------: | :--------: | ------- |
| `b9,01`  |  `[b9]`   | `–*––––––` |         |
| `b9,04`  |  `[b9]`   | `––––*–––` |         |
| `b9,06`  |  `[b9]`   | `––––––*–` |         |

### Events (chunk 0x66)

1. `[0339]`
1. `[033e]`
1. `[033f]`
1. `[0376]`
1. `[039e]`
1. `[03ec]`
1. `[03fd]`
1. `[0430]`
1. `[0463]`
1. `[0486]`
1. `[0481]`
1. `[0491]`
1. `[04bd]`
1. `[04e8]`
1. `[061e]`
1. `[062d]`
1. `[0643]`
1. `[047c]`
1. `[0516]` Default handler.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|   #13   | Dragon Gem             |  #14  | `[04e8]` |

## 0x21 Phoeban Dungeon

### Flags

- **Random Encounters:** no

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning |
| :------: | :-------: | :--------: | ------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | |
| `b9,01`  |  `[b9]`   | `–*––––––` | |
| `b9,02`  |  `[b9]`   | `––*–––––` | |
| `b9,03`  |  `[b9]`   | `–––*––––` | |
| `b9,04`  |  `[b9]`   | `––––*–––` | |
| `b9,05`  |  `[b9]`   | `–––––*––` | |
| `b9,06`  |  `[b9]`   | `––––––*–` | |
| `b9,07`  |  `[b9]`   | `–––––––*` |         |

### Events (chunk 0x67)

1. `[0383]`
1. `[09f4]`
1. `[09f9]`
1. `[0455]`
1. `[0a09]`
1. `[09fe]`
1. `[0391]`
1. `[03ac]`
1. `[03d2]`
1. `[03fb]`
1. `[0aa0]`
1. `[0447]`
1. `[04b4]`
1. `[0509]`
1. `[0530]`
1. `[0554]`
1. `[0571]`
1. `[05a1]`
1. `[05b5]`
1. `[05ed]`
1. `[06ad]`
1. `[0702]`
1. `[0710]`
1. `[075e]`
1. `[08e6]`
1. `[0a95]` Default handler.

### Actions

| Special |                    Skill, Item, or Spell                     | Event | Handler  |
| :-----: | :----------------------------------------------------------: | :---: | :------: |
|    —    |                          *Lockpick*                          |  #11  | `[0aa0]` |
|   #12   |                           *Hiding*                           |  #13  | `[04b4]` |
|    —    |                           *Climb*                            |  #15  | `[0530]` |
|    —    |                            Shovel                            |  #16  | `[0554]` |
|   #16   | *Bandage, L:Lesser Heal, H:Healing, D:Greater Healing, S:Heal* |  #23  | `[0710]` |

## 0x22 Lanac'toor's Lab

### Flags

- **Random Encounters:** yes (1 in 25)
- You **need a light** in order to see.
- You **need a compass** to get your bearings.
- The map **wraps**.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning |
| :------: | :-------: | :--------: | ------- |
| `b9,00`  |  `[b9]`   | `*–––––––` |         |

### Events (chunk 0x68)

1. `[032f]` Read paragraph 0x6b.
1. `[0337]` Color text `{68:0338}`.
1. `[036c]` Offer stairs up to `(08:07,10)`.
1. `[037a]` Offer stairs down to `(12:25,08)`.
1. `[0388]` *Can be used to find dangerous walls.*
1. `[0437]` *Walls with texture special 0x01 cause water to rush you. 1d6 damage*
1. `[04f8]` If the Underworld has not yet been sealed off `![b9,01]`, run a random combat. If you win, erase this event.
1. `[04a5]` *If used on a wall with texture special 0x02, it seals up the Underworld.*
1. `[0508]` *the Spectacles*
1. `[0541]` *some High Magic scrolls*
1. `[058f]` *a chest*
1. `[059e]` Default handler.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
| — |  *Cave Lore*  |  #5  | `[0388]` |
| — |  *D:Soften Stone*  |  #6  | `[0437]` |
| — |  *D:Create Wall*  |  #8  | `[04a5]` |

## 0x23 Byzanople Dungeon

### Flags

- **Random Encounters:** no.
- You **need a light** in order to see.
- You **need a compass** to get your bearings.
- The map **wraps**.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning |
| :------: | :-------: | :--------: | ------- |
| `b9,00`  |  `[b9]`   | `*–––––––` |         |

### Events (chunk 0x69)

1. `[030f]`
1. `[031d]`
1. `[037a]`
1. `[0385]`
1. `[041a]`
1. `[0422]`
1. `[043d]`
1. `[044b]`
1. `[047b]`
1. `[0504]`
1. `[0536]`
1. `[0544]`
1. `[0579]`
1. `[05b6]`
1. `[036f]`
1. `[080a]`
1. `[03da]`
1. `[09ed]`
1. `[0a73]` Default handler.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|   —     | *Lockpick*             |  #2   | `[031d]` |
|   #8    | *STR*                  |  #9   | `[047b]` |
|   #12   | *STR*                  |  #14  | `[0579]` |

## 0x24 Kingshome Dungeon

### Flags

- **Random Encounters:** yes (1 in 100)
- Flag `0x20` is set.
- **Create Wall** is forbidden.
- You **need a light** in order to see.
- You **need a compass** to get your bearings.
- Flag `0x01` is set.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning |
| :------: | :-------: | :--------: | ------- |
| `b9,00`  |  `[b9]`   | `*–––––––` |         |

### Events (chunk 0x6a)

1. `[0436]`
1. `[0466]`
1. `[04f0]`
1. `[03f2]`
1. `[040f]`
1. `[0396]`
1. `[03d2]`
1. `[0444]`
1. `[045b]`
1. `[048d]`
1. `[04b0]`
1. `[0456]`
1. `[04ed]` Default event. Set `[99,78]` to disable the Kingshome ambush.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|    —    | *Lockpick*             |  #2   | `[0466]` |

## 0x25 Slave Estate

### Flags

- **Random Encounters:** no

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning |
| :------: | :-------: | :--------: | ------- |
| `b9,00`  |  `[b9]`   | `*–––––––` |         |

### Events (chunk 0x6b)

1. `[0362]`
1. `[037f]`
1. `[03a3]`
1. `[03c5]`
1. `[0419]`
1. `[035d]`
1. `[047a]`
1. `[04c5]`
1. `[04cd]`
1. `[0511]`
1. `[054b]`
1. `[0598]`
1. `[05a0]`
1. `[05e3]`
1. `[05eb]`
1. `[0676]`
1. `[067b]`
1. `[0681]`
1. `[0687]`
1. `[06b4]`
1. `[06db]`
1. `[06fc]`
1. `[0753]`
1. `[0745]`
1. `[0782]`
1. `[0621]`
1. `[0669]`
1. `[08f0]` You use the elixer [sic] on a statue, but it doesn't work `{6b:08f1}`.
1. `[092c]`
1. `[0931]`
1. `[0936]`
1. `[093b]`
1. `[0853]` Default handler.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
| #16–20 |  *Tracker*  |  #21  | `[06db]` |
| #24 |  Mirror  |  #25  | `[0782]` |
| #15 |  *STR*  |  #26  | `[0621]` |
| #1–3 |  Elixir  |  #28  | `[08f0]` |

> I don't think the Elixir (item #1, sic) is actually findable...

## 0x26 Lansk Undercity

### Flags

- **Random Encounters:** yes (1 in 33)

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning |
| :------: | :-------: | :--------: | ------- |
| `b9,00`  |  `[b9]`   | `*–––––––` |         |

### Events (chunk 0x6c)

1. `[03c4]` If `[b9,02]`, you find a locked [chest](#locked) (`[99,79]`, difficulty 3). Read paragraph 0x7a.
1. `[0416]`
1. `[041e]`
1. `[0426]`
1. `[042e]`
1. `[0458]`
1. `[0466]`
1. `[048e]`
1. `[049c]`
1. `[0662]`
1. `[04d1]`
1. `[0513]`
1. `[0556]`
1. `[0570]`
1. `[0881]` *Lockpick; always works, on any wall with texture special 0x1*.
1. `[05b6]`
1. `[0608]`
1. `[06ca]`
1. `[070a]`
1. `[079f]`
1. `[07e8]`
1. `[0768]`
1. `[03bf]`
1. `[072f]`
1. `[0820]`
1. `[06e9]`
1. `[07b9]`
1. `[0781]`
1. `[08b9]`
1. `[03df]` Use Strength (any) to move the statue. Gate-and-set `[b9,02]` for color text `{6c:03e9}`.
1. `[08a8]` Default handler.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
| #11 | Kings Ticket |  #12  | `[0513]` |
| #13 | Ankh, *L:Lesser Heal, H:Healing, H:Group Heal,<br />D:Greater Healing, S:Sun Light, S:Heal, S:Major Healing*  |  #14  | `[0570]` |
| — |  *Lockpick*  |  #15  | `[0881]` |
| #1 |  *STR*  |  #30  | `[03df]` |

## 0x27 Tars Dungeon

### Flags

- **Random Encounters:** yes (1 in 100)
- You **need a light** in order to see.
- You **need a compass** to get your bearings.
- The map **wraps**.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                                 |
| :------: | :-------: | :--------: | --------------------------------------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | Have run the default handler            |
| `b9,01`  |  `[b9]`   | `–*––––––` | Have access to the healing potion chest |
| `b9,04`  |  `[b9]`   | `––––*–––` | Have access to the Stone Arms chest     |

### Events

1. `[0209]` You used *Tracker*; if you're facing a wall with special 0x02, you reveal a secret door.
1. `[01ce]` Run a random combat. If you win, erase this event.
1. `[017b]` If `![99,88]`, set `[99,88]` and `[b9,04]`. Then run a [chest](#chest) `[b9,04]` with the Stone Arms.
1. `[014c]` (04,07) A [chest](#chest) `[b9,01]` with a Healing Potion.
1. `[0106]` (00,05) Color text `{6d:0107}`.
1. `[00f8]` (02,05) Offer stairs up to Tars `(05:15,15)`.
1. `[012d]` (00,05) You used *Climb*. Travel to the Underworld `(12,19,04)`.
1. `[0408]` (04,02) Color text `{6d:0409}` (what a shame)
1. `[0239]` (04,02) You used *Arcane Lore*. Color text `{6d:023a},{6d:0262},{6d:02c3}` from the bridge of the Enterprise. No, that's not a typo.

   > "The scanner reports a vessel approaching at warp 19!!"
   
     "Spock?", Kirk says, "Intentions?" Spock scans the intruder. "Unable to tell, it seems to be doing evasive manouvers as if it is under attack."
   
     Kirk looks over to the transport tube and sees a party just standing there completely bewildered. "Who are you?" are the last words said as the strange people vanish. "Illogical" says Spock.

1. `[01ef]` (00,06), (05,07), and (07,06) Color text `{6d:01f0}` (clues a secret door)
1. `[0341]` (05,02) Color text `{6d:0342}` (you see yourself down the infinite corridor)
1. `[0364]` (03,06) Color text `{6d:0365}` (old ceiling)
1. `[0387]` (06,05) Color text `{6d:0388}` (stinky)
1. `[03a3]` (01,03) Color text `{6d:03a4}`. Spinner trap; assign random(4) to `heap[03]` (facing).
1. `[03cf]` (02,07) Color text `{6d:03d0}` (rotting body)
1. `[01bb]` (04,00) A [locked chest](#locked) (`[99,2b]`, difficulty 1) with some scrolls and cash.
1. `[03fd]` (03,01) Run combat #3 (Adventurers). If you win, erase this event.
1. `[01d7]` Default handler. Test and set `[b9,00]`, then set `[b9,01]`.

### Actions

| Special | Skill, Item, or Spell  | Event | Handler  |
| :-----: | :--------------------: | :---: | :------: |
|    —    | *Tracker*              |  #1   | `[0209]` |
| #5 |  *Climb*  |  #7  | `[012d]` |
| #8 |  *Arcane Lore*  |  #9  | `[0239]` |

# General Events

The metaprogram for each board often calls out to functions in other chunks. Some of these are common to cities and feature additional data, which is worth explaining.

## Taverns<a name="tavern">

*Opcode:* `58090000 longcall(09,0000)`

The next word is the return address, i.e. where you want the program to resume after the party leaves the tavern.

The next word is a pointer to the tavern's "tagline" string.

The following byte is the number of NPC volunteers in residence, followed by an array of pointers to the NPC data. (In practice there's never more than one NPC per tavern, but there's support in the code for more than one.)

The following byte is the number of rumor strings available from the tavern keeper, followed by an array of string pointers.

The tagline string is (usually, although not necessarily) next, then all of the rumor strings.

NPC data starts with the NPC ID number, then the character's name (unpacked, variable-length), then five bytes for the five primary attributes (STR, DEX, INT, SPR, HP). When you add an NPC to your party, `[99,57+ID]` is set.

After the attributes, the next two bytes are the NPC's level.

Beyond that there are a series of offset/value pairs that indicate [character](Character.md) bytes to be updated. (I'm not sure why you couldn't have put the level here instead.) The first byte of the pair is the character data offset (i.e. `0x32` for Sun Magic), and the second is the value (i.e. `01`). This is also how we load spells (bytes `3c` and up). The array of bytes ends with `ff`.

There is a similar second array which contains one-byte pointers into the [secondary map data's](Board Data.md) Items list, indicating the NPC's starting inventory. If bit `0x80` is set, the item is equipped by default.

Finally, after the second array, there's a string that is printed when the NPC joins your party. (There's no pointer for this; it's just 1B after the end of the inventory array.)

## Chests<a name="chest">

*Opcode:* `580b0c00 longcall(0b,000c)`

The next word is the return address.

If the following byte is `0xff`, skip it. Otherwise, read that byte and the one after it and run a bitsplit operation to get a heap index and bit. If that bit is zero, ignore the chest and return immediately. (You'll often find the bit is set just before this routine is called to clear it.) The bit gets cleared, although possibly not until you've taken everything out of the chest.

A tagline string (five-bit packed) follows.

After that is an array of two-byte values. The first byte is an index into the [secondary map data's](Board Data.md) Items list, indicating which items are in the chest, and the second byte is the count. If the count is `0x00`, there are an infinite number of that item in the chest (see the Purgatory Low Magic Shoppe). If the item index is `0x80`, it's a Dragon Stone (hardcoded into chunk `0x0b`). If the item index is `0xfe`, it's gold, and the other byte is the amount (in the Purchase Price format). If the item index is `0xff`, the array terminates.

## Locked Chests<a name="locked">

*Opcode:* `580a0f00 longcall(0a,000f)`

Very similar to unlocked chests above, but not exactly. The first word is still the return address.

The next bit is a bitsplit against `heap[99]`, but if it's `0xff`, skip the check. The bitsplit check determines if the chest has been opened (1) or is still locked (0).

If the chest is locked, the following bit is the lock's difficulty level. (If it's `0xff`, nevermind, the chest is actually unlocked.)  If your *Lockpick* skill is less than the difficulty level, you can't open it. If it's equal, you have a 50% chance of opening the lock. That goes up to 60%, 70%, 75%, 80%, 85%, 90%, and 95% if your skill level is higher than the lock's difficulty. It tops out at 95%, so there's always a chance you might fail, but trying again is free. When (if) you unlock the chest, it sets the heap flag to mark the chest "opened" and sets the lock's difficulty level to 0.

If the chest has been opened but the difficulty level is greater than zero, the chest is invisible to you.

There is no tagline, but there is an item array similar to regular chests.

## Shops<a name="shop">

*Opcode:* `580a0000 longcall(0a,0000)`

The next word is a pointer to the tagline string (five-bit packed).

After that is an array of shop categories, starting with a one-byte item count. Each item contains two pointers; the first points to the category's inventory list, and the second points to the category name (five-bit packed string). If there's only one category, there's no category name (and the second pointer doesn't exist).

The return pointer (place we jump back to after the longcall) is set after that array, so the next thing in the stream is an instruction (usually a `jmp` or `exit` or similar).

The strings follow, starting with the tagline and proceeding through the category names.

The inventory lists are also arrays starting with a one-byte item count. Each item is a single byte which is an index into the [secondary map data's](Board Data.md) Items list, indicating which items are for sale in that category.

## Sailing the Boat<a name="boat">

There are five destinations. You pass in the index of your current location so that one isn't shown on the list of choices.

Your "parking space" is stored in `heap[bd]`. If you approach a dock that isn't the same as where you left the boat, you can't use the dock. (This is how Ugly drops you at the Necropolis; the parking space is still 0, the Necropolis dock, index 1, doesn't work.)

- Pirate's Cove `(0a:01,01)`
- Necropolis `(0e:07,14)`
- Freeport `(11:14,15)`
- Sunken Ruins `(00:39,14)`
- Rustic `(00:29,28)`

