# Board Events

The third byte of map square data includes a "special" identifier. Every time you step on a square the game checks the ID, and if it's non-zero, it runs a block of code (hereafter an "event") that corresponds to that ID. This happens when you "start a turn" on a square, no matter how you got there; it's not tied to movement. Multiple squares on a map can have the same special ID. 

Events can also be triggered by (U)sing an item, skill, or attribute (hereafter an "action"). Usually you have to Use the right thing on the right square (i.e. a square with a particular Special ID), but there are some catch-all Actions that run if you do an unexpected thing.

For instance, in the Slave Camp, the square (04,02) is tagged with Special ID 0x11. When you step on that square, the game runs Event 0x11 `[0969]`, which checks to see if you've noticed the Nature Axe yet. But if you're standing on that square and you Use *Forest Lore*, it runs Event 0x13 `[0975]` instead. That prints some color text and sets a flag that says "yup, you found the Nature Axe". Now, the next time you run Event 0x11 (which is immediately afterwards, because you're still standing on that square), the game shows you the cache that contains the Nature Axe and lets you take it.

There's also a default handler that runs every time you step on every square. This is technically Event 0x00 (although there's a pointer between Event 0x00 and 0x01, it's complicated), but I've listed it last because Markdown's Ordered List tag starts at 1 whether I want it to or not.

Everything seems to run instruction `73`, `mov h[3e] <- h[3f]`. Heap `[3e]` and `[3f]` seem to be something like "the last special event ID"; see `{cs:44bb}`.

I wonder if there's a difference between flags in the 90's and flags in the b0's. Like maybe one gets persisted when you leave the board, and one doesn't? Might need to revisit chunk `0x00`. Offsets from `b9` get reused across boards.

## 0x00 Dilmun

### Flags

- **Random encounters:** no.
- **Create Wall** is forbidden.
- The map **wraps**, which is pretty funny since you can't actually travel over the water.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                             |
| :------: | :-------: | :--------: | ----------------------------------- |
| `b9,04`  |  `[b9]`   | `––––*–––` | Have access to the cache on Forlorn |

### Events

<!-- vimscript: iN:`(li0xh"b4xxi0xh"c4x:let @b=printf('%02d',@b)
:let @c=printf('%02d',@c)
ic,b)` -->

1. `[1218]` (19,12) If anyone in the party has *Forest Lore* 1 and you're facing east, get some color text `{46:1228}`. 
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
35. `[1647]` (12,06) If `bitsplit(99,77)` is not set, run combat #1 (Guards). If you win, set `bitsplit(99,77)` and erase this event.
36. `[1634]` (05,10) If `bitsplit(99,7f)` is not set, run combat #2 (Goblin brothers). If you win, set `bitsplit(99,7f)` and erase this event.
37. `[165a]` (16,03) Gate-and-set `bitsplit(99,63)` to access the cache on Forlorn (`bitsplit(b9,04)`)
38. `[16a5]` (14,01) The refresh pool SE of the Slave Camp. Non-dead party members have their Health, Stun, and Power restored to full.
39. `[1624]` (08,18) Run combat #3 (Revenge Goblins). If you win, erase this event.
40. `[170b]` (20,26) If `bitsplit(99,78)` is not set, color text `{46:1717}` and travel to `(24,00,15)`. *(The bitsplit will be set when you arrive in the Kingshome Dungeon.)*
41. `[161f]` (Various on King's) Run combat #4 (Goblin hordes). If you win, erase this event.
42. `[161a]` (Various on Quag) Run combat #5 (Murk Trees). If you win, erase this event.
43. `[1776]` (22,13) and (19,23) Transportation Nexus. If anyone in the party has *Arcane Lore* 1, print color text `{46:1784}` and ask if the party wants to travel to `(17,05,07)`.
44. `[17d5]` Default handler. Gate-and-set `bitsplit(b9,00)` (remember, this gets reset every time you enter the board). If `bitsplit(99,7e)` is **not** set, make Phoebus appear by writing `{46:0b31},{46:0b34},{46:0bbe},{46:0b33}`. This adds four walls and enables event #7 so you can enter the city.

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

1. `[0dff]` Walking around inside the city walls. Color text.

2. `[0e2e]` Same, southeast section near the hole. Color text.

3. `[0e5d]` (23,10) Before the teeth-kicking fight. If facing South, print the color text at `{13:0251}`.

4. `[0e97]` (20,13) Starting square. Gate-and-set `bitsplit(99,19)` to print the color text at `{13:0317}`.

5. `[0eb2]` (25,08) The hole in the wall. Color text. Gate-and-set `bitsplit(b9,04)` to show more color text.

6. `[0fca]` Swimming in the harbor but adjacent to the walls. If `heap[40].0x80`, return. Else deal 1 HP damage to each non-dead party member without *Swim*. Then print color text.

7. `[0e69]` Run a random combat. Clear the square if you win.

8. `[113c]` (Not referenced from a square.) Clear CF and return.

9. `[1055]` (06,13) Statue of Irkalla. Gate-and-set `bitsplit(b9,1b)` to display color text. **The sacrifice works on a Spirit check (vs 1d20).** If it works, set `char[92].0x80` on every party member.

10. `[10e3]` (07,12) Apsu Waters. Travel to board 0x12 (Magan Underworld) at (13,04). If you say 'N', you back up.

11. `[112d]` (18,26) Entering the Arena. Runs the "free gear" program if you have less than **3 x your party size items** in your inventory. Then erases the entry door `ds[0320]<-0x30` and replaces this Event `ds[02bc]<-0x1d` with Event #29.

12. `[1279]` West of the Arena. Display color text.

13. `[12a6]` South of the Arena. Display color text.

14. `[12df]` East of the Arena. Display color text.

15. `[1010]` Swimming in the harbor. Same as Event #6 but different color text.

16. `[1303]` (31,10) The Morgue. Display paragraph #5.

17. `[130b]` Near the Morgue. Display color text.

18. `[1350]` City guard combat within the walls (#3); lose and you're kicked back to the staring square. Clear the square if you win.

19. `[1366]` (07,07) Clopin Trouillefou's Court of Miracles. If `bitsplit(99,25)` is not set, read paragraph #77 and either get beat up for 1d8 damage (paragraph #8) or the Humbaba quest. Otherwise, gate-and-set `bitsplit(99,26)` to get a $1000 reward for defeating the Humbaba. Otherwise, "thanks for coming".

20. `[137f]` (09,22) Statue of Namtar. Read paragraph #9. 

21. `[1387]` (25,27) The [tavern](#tavern). Ulrik is here.

22. `[1564]` (03,22) The [magic shoppe](#chest). Gate-and-set `bitsplit(b9,03)` to read paragraph #10. Stock includes infinite quantities of Low Magic scrolls (items `0x15-0x1a`).

23. `[15a2]` (12,30) The [black market](#shop).

24. `[15fc]` (11,26) The slave market. Gate-and-set `bitsplit(b9,05)` to read paragraph #67. If you sell yourself into slavery, read paragraph #58, set `bitsplit(99,35)`, then travel to the Slave Mines (13:07,08).

25. `[113e]` (19,29) Runs combat #2 (Gladiators). If you win, display color text and pick up the Citizenship Papers, then set `bitsplit(99,62)`. Restore the entry door `ds[0320]<-0x20` and Event #11 `ds[02bc]<-0x0b`. Teleport the party outside the Arena. If you lose, display color text and lose all your Gold.

26. `[0e78]` Run combat #6 (Bandits). Clear the square if you win.

27. `[0e8c]` (23,09) Run combat #1 (5 King's Guard and 6 Pikemen). Clear the square if you win.

28. `[110e]` (06,12) One square shy of the Apsu Waters. Gate on Town Lore 1 to get paragraph 94. Otherwise you see "odd waters".

29. `[1255]` Inside the Arena. Display color text.

30. `[0f75]` Called after you Swim/Climb through the wall. Test bit `0x01` of the wall metadata (which is true for every actual wall on this map); return if zero. Display color text. Everyone in the party makes a Strength check (vs 1d20) or takes 1d10 damage. Then set character flag `0x40` and move "forward" one square.

31. `[1337]` Called after you Hide/Dex in the Morgue. Read paragraph #69. Everyone in the party makes a Strength check (vs 1d20) or takes 1d10 damage. Then set character flag `0x40` and travel to Dilmun (00:13,02).

32. `[100c]` Outside the city walls. Print the same color text as Event #6.

33. `[136b]` (31,31) Run combat #10 (Humbaba). Clear the square and set `bitsplit(99,25)` if you win.

34. `[0e6e]` (20,03) Run combat #4 (Unjust, Innocent, Cannibals). Clear the square if you win.

35. `[0e7d]` (12,28) Run combat #7 (Jail Keeprs). Clear the square if you win.

36. `[0e82]` (26,27) Just outside the tavern. Run combat #08. Clear the square if you win.

37. `[0e87]` (09,21) Run combat #9 (Loons). Clear the square if you win.

38. `[0e73]` (05,13) and (05,14) Run combat #5 (Fanatics). Clear the square if you win.

39. `[0ea5]` (20,15) 2N of start square. Gate on Town Lore 1 to read paragraph #14.

40. `[1517]` (29,27) The town healer ($4/hp). Run routine at `{11:0000}`.

    > When you pay for healing, the game automatically pools your gold with the first PC. This shouldn't be a problem, unlike in some games where the max gold per PC is ridiculously low.

41. `[1653]` (23,02) Magic refresh pool. Non-dead characters have their Power restored to max.

42. `[1684]` Default handler. If party is outside the rectangle (0,0)-(33,33), ask to exit. Otherwise, check if we're outside (roof texture 0) or inside, and if inside, set the random encounter counter (`[0003]`) to `0x64` (1 in 100).
    - North: `(00:0f,05)` — *oops*, this really ought to be `(00:0d,05)`.
    - East or South: `(00:0d,02)`. Hilariously, this means you can exit to the east on dry land at (33,33) and wind up on the other side of the bay.
    - West: `(00:0c,04)`

### Actions

- Special #5, use *Climb* or *Swim* :arrow_right: Event #30
- Special #16, use *Dexterity* or *Hiding* :arrow_right: Event #31

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

1. `[0497]` "You see a campfire ahead."
2. `[04ac]` (10,11) The campfire. Read paragraph #63. Health, Stun, and Power are restored to full. Then the event is deleted from this square.
3. `[037c]` (02,05) "You hear someone singing."
4. `[03a9]` (10,06) Gate on `bitsplit(99,02)`. "You hear moans of pain."
5. `[0391]` (00,06) Display paragraph #68.
6. `[0399]` (00,16) Display paragraph #22.
7. `[03a1]` (04,13) Display paragraph #88.
8. `[03d2]` (08,07) The sick man. If `bitsplit(99,02)` is set, you can raid his [belongings](#chest) (`bitsplit(b9,02)`). Otherwise, there's a 1 in 20 chance the he attacks you (combat #1). If you kill him, set `bitsplit(99,02)`. If he doesn't attack you, you get color text.
9. `[047e]` You heal the sick man. Gate on `bitsplit(99,02)` to read paragraph #19. Set `bitsplit(99,02)` and `bitsplit(b9,02)`.
10. `[0940]` You used *Lockpick*. The only locked door is in the wizard's house facing the wall of his treasure room at (06,06); we know this because the wall metadata has a special (`heap[26].0x02` is set). Any skill level is sufficient to unlock the door, which overwrites the wall metadata to turn the impassable wall into a regular door.
11. `[04e5]` You appease the camp with *Bureaucracy* (set `bitsplit(99,01)` ).
12. `[0629]` You impress the wizard (set `bitsplit(99,03)`). Unlocks his treasure room door at `[020b]<-0x21`. Set `bitsplit(b9,04)`.
13. `[0589]` (07,05) If you barge into the wizard's house without showing off your magic skills (`bitsplit(99,03)`), he kicks you out.
14. `[0720]` (06,06) Outside the wizard's treasure room. Checks the door texture to see if the door is locked (`0x31`), and if so, tells you.
15. `[0739]` (06,07) The wizard's treasure. If you haven't either killed the guardian `(99,04)` or impressed the wizard `(99,03)`, you have to fight the Spirit Ward (encounter #2); if you win, set `bitsplit(99,04)` to mark your victory and `bitsplit(b9,04)` to get access to the treasure.
16. `[0377]` You stepped in water. (Splash)
17. `[0969]` (04,02) If `bitsplit(b9,01)`, get the Nature Axe.
18. `[0975]` You used *Forest Lore* in the right place. Gate-and-set `bitsplit(99,27)` to notice an axe stuck in a tree. Set `bitsplit(b9,01)`. Clear `heap[3d]` and `[3e]`.
19. `[05fd]` (07,04) The wizard peers at you from within his house, unless you did this already `(99,03)`
20. `[09a6]` (07,07) A [locked chest](#locked) behind the wizard's house (`bitsplit(99,28)`, difficulty 1).

    > This is the same bitsplit as the chest holding the Dwarven Hammer in the Ruins on King's Isle; if you unlock this chest, you lose that one (and vice versa).
21. `[09bd]` (10,02) The [tavern](#tavern). Louie is here.
22. `[0795]` Default handler. If the party is outside (0,0)-(17,16), prompt to exit. N:`(00:11,04)` ES:`(00:11,02)` W:`(00:10,03)`
    - If this is your first time here (`00,b9` is not set), set `00,b9`. If bitsplit `00,99` is set, you already killed everyone. Delete Events #1–12, 17, and 19, then exit. If bitsplit `01,99` is set, you're already friends, so exit. If any character has flag `0x40` (swam out of Purgatory), read paragraph #16; the camp is happy with you. Set `01,99` and exit. Otherwise, the residents are suspicious; exit (and await player action)
    - If you've been here before and either bitsplit `01,99` or `00,99` are set, exit. Otherwise, if you stay on the perimeter of the map, the residents wait for you to leave. If you step inside (1,1)-(13,15), the camp residents attack (encounter #0). If you kill them, read paragraph #18 (shame) and set `00,99`.


### Actions

- Use *Lockpick* :arrow_right: Event #10
- Use *Bureaucracy* :arrow_right: Event #11
- Special #8, use *Bandage* or cast *L:Lesser Heal, H:Healing, D:Greater Healing*, or *S:Heal* :arrow_right: Event #9
- Special #17, use *Forest Lore* :arrow_right: Event #18
- Special #19, use *Sun Magic, Druid Magic, Low Magic*, or *High Magic* :arrow_right: Event #12

## 0x03 Guard Bridge #1

### **Flags**

- **Random Encounters:** no, but see Event #6.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                                 |
| :------: | :-------: | :--------: | --------------------------------------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | Showed Citizenship Papers to the guards |
| `b9,01`  |  `[b9]`   | `–*––––––` | Bribed the guards                       |
| `b9,02`  |  `[b9]`   | `––*–––––` | Read paragraph #12 on entry             |
| `b9,04`  |  `[b9]`   | `––––*–––` | Have access to the arms cache           |

### Events

1. `[00e7]` You stepped in water.
2. `[00ec]` (04,02) If `bitsplit(b9,00)` is set, exit. Otherwise draw monster `0x0e`, then the guard demands to see your Citizenship Papers.
3. `[0123]` (04,02) You show your Citizenship Papers to the guard. If `bitsplit(b9,00)` is not set, the guard demands a bribe of $10 per party member. If you pay it, set `bitsplit(b9,00)` and `(b9,01)`.
4. `[023f]` (04,05) If `bitsplit(b9,00)` is set, exit. If `bitsplit(b9,01)` is set, run event #2 instead. Otherwise, the guard asks if you know where you're going; if you say yes, set `bitsplit(b9,01)` and run event #2, otherwise run away.
5. `[02ff]` (04,05) You show your Citizenship Papers to the guard. Set `bitsplit(b9,00)` and `(b9,01)`.
6. `[0383]` Crossing the bridge. If you've already dealt with the guards (`bitsplit(b9,00)` is set), read paragraph #13; there's a 1:10 chance of an encounter with a bunch of Rats (#01). Otherwise, you have to fight the guards (#00). If you win, set `bitsplit(b9,00)`. If you lose, you're kicked off the bridge, to (04,00) or (04,07) depending on which half of the bridge you're currently on.
7. `[0484]` (07,02) A [chest](#chest) (`bitsplit(b9,04)`). You only get one shot at it (`bitsplit(99,2c)`).
8. `[03c3]` (03,01) Color text `{49:03c9}` about Lanac'toor's statue. Erase this event.
9. `[0435]` Default handler. If the party is inside (0,0)-(7,7), gate-and-set `bitsplit(b9,02)` to read paragraph #12. Otherwise, prompt to exit; if the party is in the bottom half of the map (Y coordinate < 4) exit to `(00,12,06)`, else `(00,12,08)`.

### Actions

- Special #2, use Citizenship Papers > Event #3
- Special #4, use Citizenship Papers > Event #5

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

2. `[034b]` (13,11) Brandish the Sword of Freedom at the Universal God. Drop the Sword of Freedom you have, and pick up a version that casts *S:Inferno@10* but is otherwise identical. Everyone in the party gains 500 XP. Iterate over the party, gate-and-set `bitsplit(5c,03)` to gain 3 points on all four attributes.

3. `[046c]` (03,04) Stairs down to the Underworld `(12:19,19)`

4. `[047f]` (07,08) Run combat #05 (Guards and Stosstrupen). If you win, erase this event.

5. `[048a]` "An alarm sounds!" Reset event #4. 

   > Not sure how this gets triggered, since special #5 doesn't exist on the map...

6. `[049d]` You used *Lockpick*. Locked doors on this map have wall metadata bit 0x01 set, i.e. texture index 3. (This is true for the doors at (04,08) and (08,06).) Any number of ranks is sufficient to unlock the door (sets the upper nibble of the wall to 0x20).

7. `[04c4]` (05,07) The climber's puzzle. Color text `{04:04c5}`.

8. `[04e6]` (05,07) You used *Mountain Lore* or *Intelligence*, which sets `bitsplit(b9,01)`.

9. `[051c]` (05,07) You used *Climb*. If `bitsplit(b9,01)` is set, travel to (06,06). Otherwise, color text `{04:0523}`.

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

20. `[0668]` (01,09) [Locked chest](#locked) (`bitsplit(99,53)`, difficulty 5).

21. `[067d]` Default handler. If the party is outside (0,0)-(21,21), prompt to exit to`(00:19,20)`. Otherwise, gate-and-set `bitsplit(b9,00)` to read paragraph #98.

### Actions

- Use *Lockpick* > Event #6
- Special #1, use the Sword of Freedom > Event #2
- Special #7, use *Mountain Lore* or *INT* > Event #8
- Special #7, use *Climb* > Event #9
- Special #10, use the Golden Boots > Event #12

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
4. `[048b]` (15,15) Set `bitsplit(b9,01)`. If you're following tracks (`bitsplit(b9,00)`), clear that bit and color text `{4b:0499}`. If the slab hasn't been moved yet (`bitsplit(99,05)`= 0), color text `{4b:04b0}`. Otherwise, offer to travel to `(27:02,05)`.
5. `[04d8]` (15,15) You examined the slab with a skill. Set `heap[3e]` to 0x00 instead of `heap[3f]` (?!). If the slab hasn't been moved yet (`bitsplit(99,05)`= 0), color text `{4b:04e1}`.
6. `[0514]` (15,15) You used *Strength*. If the stone slab has already been moved (`bitsplit(99,05)`= 1), exit. Otherwise roll 1d40; if the value is below the PC's *Strength*, the slab moves (set `bitsplit(99,05)`) and exposes the stairs down.
7. `[05b1]` Inside the ruined city square. Color text `{4b:05b2}`.
8. `[05f0]` You asked your sage about the city ruins; read paragraph #23.
9. `[0666]` (04,08) "This way!!" says your Tracker. Set `bitsplit(b9,00)` and run event #10.
10. `[05f9]` On the path. If `bitsplit(b9,00)` is set, turn until you're facing North, then step forward.
11. `[05fe]` ... East
12. `[0604]` ... South
13. `[060a]` ... West
14. `[0637]` If you're already following the tracks (`bitsplit(b9,00)`), turn North. Otherwise, color text `{4b:063e}` says you should. 
15. `[0699]` You stepped in water.
16. `[0571]` (13,14) If you're following tracks (`bitsplit(b9,00)`= 1) or have already found the slab (`bitsplit(b9,01)`= 1) or have a Disarm Traps spell active (`heap[bf]`> 0), color text `{4b:059c}`. Otherwise you fall in the pit `{4b:0586}` and everyone takes 8 HP damage.
17. `[06e0]` (01,01) A bunch of [scrolls](#chest); you only get one shot at it (`bitsplit(b9,04)`).
18. `[0728]` (15,08) The snake's [treasure](#chest); you only get one shot at it (`bitsplit(b9,05)`).
19. `[06ca]` (02,02) If you've cleared this encounter (`bitsplit(99,29)`= 1), exit. Otherwise, run encounter #4 (random). If you win, set `bitsplit(99,29)` and `bitsplit(b9,04)` to grant access to the treasure.
20. `[0712]` (14,07) If you've killed the snake (`bitsplit(99,2a)`= 1), exit. Otherwise, run encounter #5 (the Guardian Snake). If you win, set `bitsplit(99,2a)` and `bitsplit(b9,05)` to grant access to the treasure.
21. `[0754]` Run a random encounter. If you win, erase this event.
22. `[0693]` Default handler. If the party is outside (0,0)-(16,16), prompt to exit to `(00:20,04)`.

### Actions

- Special #4, use *Cave Lore, Mountain Lore, Lockpick,* or *Tracker* > Event #5
- Special #4, use *STR* > Event #6
- Special #7, use *Town Lore* or *Arcane Lore* > Event #8
- Special #10-#14, use *Tracker* > Event #9

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

2. `[0422]` (08,03) Show monster image 0x23. Gate-and-set `bitsplit(b9,00)` to read paragraph #25.

3. `[0437]` (06,07) Color text `{4c:0438}`.

4. `[0475]` (06,09) Run encounter #7 (10 Stosstrupen). If you win, erase this event.

5. `[0494]` Outside Mystalvision's lair. Color text `{4c:0495}`.

6. `[04ad]` Outside the army grounds. Color text `{4c:04ae}`.

7. `[04d7]` (13,07) Color text `{4c:04d8}`.

8. `[0525]` (14,02) Gate-and-set `bitsplit(b9,01)` to read paragraph #28. Color text `{4c:05eb}`; you're joining the army. If you say yes, read paragraph #86 and travel to `(1d:03,07)`. Otherwise, color text `{4c:05a2}` and pay $50 per party member. If you decide to pay and have enough money, color text `{4c:0655}` and exit. Otherwise you've joined the army.

9. `[0731]` (05,11) and (11,10) Color text `{4c:0732}`.

10. `[076a]` (08,14) Meeting Mystalvision. If you've already killed Mystalvision in the Nisir (`bitsplit(99,70)` = 1), exit. Gate-and-set `bitsplit(99,7a)`. Show monster image 0x35 and read paragraph #66. Run encounter #3 (Stosstrupen and Mystalvision). Regardless of whether you win or not, color text `{4c:078f}`, blacken the screen (image 0xff), and travel to `(21:12,03)`.

11. `[0466]` (13,15) Run encounter #4 (Dirty Rats). If you win, erase this event.

12. `[046b]` (12,15) Run encounter #5 (Mad Dogs). If you win, erase this event.

13. `[0470]` (16,03) Run encounter #6 (Soldiers). If you win, erase this event.

14. `[047a]` (13,14) Run encounter #8 (Thieves). If you win, erase this event.

15. `[047f]` (16,13) Run encounter #9 (random). If you win, erase this event.

    > The max random encounter ID is 0, which means this should always be Guards?

16. `[0484]` (08,16) Run encounter #10 (Soldiers). If you win, erase this event.

17. `[0489]` (03,14) Run encounter #11 (Ominous Fellow). If you win, erase this event.

18. `[07f4]` (01,10) Locked [chest](#locked) (`bitsplit(99,2d)`, difficulty 2). Scrolls.

19. `[0807]` (01,15) Locked [chest](#locked) (`bitsplit(99,2e)`, difficulty 2). Tri-cross.

20. `[081a]` (11,15) Locked [chest](#locked) (`bitsplit(99,2f)`, difficulty 1). Magic Plate.

21. `[0461]` (02,15) Run encounter #12 (Guards, 1 Stosstrupen). If you win, erase this event.

22. `[0676]` (02,13), only reachable from below. Color text `{4c:067c}`, then move to (02,14).

23. `[0860]` (15,15) If you've been to the dungeon (`bitsplit(99,69)`= 1), gate and set `bitsplit(99,38)` for color text `{4c:0875}` from Berengaria. Run the [tavern](#tavern); Valar is here.

24. `[045c]` (Various) Run encounter #1 (Stosstrupen). If you win, erase this event.

25. `[0829]` (14,15) Color text `{4c:028a}` for the tavern.

26. ` [07d0]` Default handler. If the party is outside (1,1)-(16,16), prompt to exit. N:`(00:05,12)` E:`(00:06,11)` S:`(00:05,10)` W:`(00,04,12)`.

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

3. `[02d5]` (03,06) N approach to the bridge. If `bitsplit(b9,02)`= 0, color text `{4d:02e1}`. Refuse to obey their laws and you have to fight combat #1 (3 Guards). Agree, or kill the guards, and they let you pass (set `bistsplit(b9,02)`).

   > **Bug?:** This event never gets triggered. If you approach from the N, it's assumed that you've already "sworn fealty" to Lansk. If you're approaching from the S, `bitsplit(b9,02)` is set in the default handler as soon as Y=5. In theory if that check was >5 instead of >=5 it might run Event 3 before the default handler, and you'd actually get this encounter.

4. `[0135]` (03,03) S approach to the bridge. If you've already passed "Customs" (`bitsplit(b9,01)`= 1), exit. Otherwise, color text `{4d:0141}`. If you refuse inspection, run combat #0 (3 Pikemen) *unless* someone in your party has at least two ranks in *Merchant*. If you submit to inspection, they take the greater of $100 or 20% of your gold. If you refuse to pay up (or can't), run combat #0. Regardless of how you end this encounter (except for running away from combat), set `bitsplit(b9,01)`.

   > Of course, no one has two ranks in *Merchant*, because they took that skill out of the Level Up screen.

5. `[0371]` Inside the S building. If you've already killed both sets of sleeping guards (`bitsplit(99,7d)`= 1), exit. If the guards *didn't* wake up (`bitsplit(b9,05)`= 0) and you *didn't* just walk into a wall (`bitsplit(40,01)`= 1), the guards continue to slumber. Color text `{4d:0384}` and exit. Otherwise, run combat #2 (5 Guards). If you lose, color text `{4d:03f3}` and re-run combat #2. If you win, color text `{4d:03ba}` and run combat #3 ("not easy"). Again, if you lose, color text `{4d:03f3}` and re-run combat #3. If you win, set `bitsplit(99,7d)`.

6. `[040c]` (01,03) The chest inside the S building. Follow the same tree as Event #5, but when you exit there, you find the strong box in the corner. If `bitsplit(b9,03)`= 0, it's still locked; otherwise the [chest](#chest) is open.

   > Something about this chest is different. The lock isn't handled with the usual mechanism `longcall(0b:000f)` and uses a local flag `bitsplit(b9,03)` instead of a global one `bitsplit(99,??)`, which seems to be why you can raid this chest repeatedly.

7. `[0477]` Your lockpick tries to unlock the chest. If it's already open (`bitsplit(b9,03)`= 1), you wake up the guards (run Event #9). If you have *Lockpick* 3 or better, set `bitsplit(b9,03)`; otherwise, you wake up the guards.

8. `[052e]` (04,08) Inside the N building. If `bitsplit(99,7c)`= 1, exit. Gate-and-set `bitsplit(b9,04)` for color text `{4d:0543}` and the official throws you out. If you go in a second time, run encounter #0 (Guards, etc.). If you win, set `bitsplit(b9,08)` and `bitsplit(99,7c)`.

9. `[04e0]` You used something unexpected. Set `bitsplit(b9,05)` to wake up the guards.

10. `[0487]` You use the key to unlock the chest. If it's already open (`bitsplit(b9,03)`= 1), you wake up the guards (run Event #9). Otherwise, drop the key and set `bitsplit(b9,03)`.

11. `[04e7]` You pick a sleeping guard's pocket. If you've already killed both sets of sleeping guards (`bitsplit(99,7d)`= 1). If you already stole the key (`bitsplit(b9,06)`= 1), you wake up the guards (run Event #9). Otherwise, color text `{4d:04f6}`, pick up item #4 (the Key), and set `bitsplit(b9,06)`. (If you didn't have room to pick up the key, don't set the bit.)

12. `[05e1]` (05,06) Erase this event. If you don't have a "defeat traps" spell running (`heap[bf]`= 0), color text `{4d:0x05e8}` and you fall in a pit for 1d8 damage. Otherwise color text `{4d:0x05fb}`.

13. `[0614]` (04,06) Set `bitsplit(b9,07)` to activate the second Armory fight (see Event #14). Then run a [chest](#chest) `(bitsplit(b9,08)`) with several magic items, including the Axe of Kalah.

14. `[064c]` (05,07) If you're on your way *out* (`bitsplit(b9,07)`= 1), run combat #3 ("not easy"). If you win, erase this event.

15. `[065d]` Default handler. If the party is outside (0,0)-(7,8), prompt to exit. If you're in the lower half of the map (Y <= 4), exit to `(00:14,11)`, otherwise `(00:14,13)`.

    Otherwise, gate-and-set `bitsplit(b9,00)` for color text `{4d:06a6}`. If you're in the upper half of the map (Y >= 5), set `bitsplit(b9,02)`, which prevents you from ever needing to swear fealty to Lansk.

### Actions

- Special #5, use *Pickpocket* > Event #11 `[04e7]`
- Special #6, use *Lockpick* > Event #7 `[0477]`
- Special #6, use Key > Event #10 `[0487]`
- Special #5 or #6 > Event #9 `[04e0]`

## 0x08 Mud Toad

### Flags

- **Random Encounters:** yes (1 in 100)

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                         |
| :------: | :-------: | :--------: | ------------------------------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | Intro paragraph                 |
| `b9,01`  |  `[b9]`   | `–*––––––` |                                 |
| `b9,02`  |  `[b9]`   | `––*–––––` | Have access to the Golden Boots |
| `b9,03`  |  `[b9]`   | `–––*––––` |                                 |
| `b9,04`  |  `[b9]`   | `––––*–––` | Have found the militia's cache  |
| `b9,05`  |  `[b9]`   | `–––––*––` | Have met with Berengaria        |

### Events

1. `[03aa]` (13,06) The Souvenir [Shop](#shop). Read paragraph #30.

2. `[03cc]` (07,10) Lanac'toor's statue. Loop over the four part bitsplits and set `heap[41]` accordingly. If all parts are present, offer to travel to `(22:07,09)`. If no parts are present, read paragraph #20. Otherwise, display which parts are still missing.

3. `[0461]` You brought the Stone Hand (`ax := 0x03`) back to Lanac'toor's statue. If this part has been returned already ( `bitsplit(99,1a+ax)`= 1), exit. Otherwise, return the part, drop the Stone Hand, set `heap[88]=0` and `heap[41]=0`. Then loop over the four statue part bitsplits and set `heap[41]` accordingly. If there are still missing parts, color text `{4e:0490}` and exit. Otherwise, color text `{4e:04a9}` and earn 500 XP for repairing the statue. Change the decoration texture on this square from `0x04` (broken statue) to `0x05` (repaired statue).

4. `[0452]` ... the Stone Arms (`ax := 0x00`)

5. `[045c]` ... the Stone Head (`ax := 0x02`)

6. `[0457]` ... the Stone Trunk (`ax := 0x01`)

7. `[055d]` (14,10) Color text `{4e:055e}`

8. `[0591]` A climbable wall. Color text `{4e:0592}`

9. `[05b3]` You used *Climb*. If you're facing a wall with metadata `0x01` set (only happens on the climbable walls), you step forward 'through' the wall.

10. `[05dc]` (04,13) The Priests of the Mud Toad. If `bitsplit(b9,02)`= 1, you receive the Golden Boots. Otherwise, if `bitsplit(99,56)`= 1, exit. Otherwise, if you've sealed up the leak, read paragraph #113, set `bitsplit(99,56)` and `bitsplit(b9,02)`, and receive the Boots. Otherwise, read paragraph #17 and get the leak quest.

11. `[06af]` (05,12) The mud leak. Color text `{4e:06b0}`.

12. `[06cf]` (02,02) The city militia. Read paragraph #32, then run combat #7 (Crazed Militiamen). If you win, erase this event.

13. `[06e1]` (12,14) The [tavern](#tavern).

14. `[088a]` (06,02) The militia's cache. Gate-and-set `bitsplit(99,39)`, then run the [chest](#chest) (`bitsplit(b9,04)`).

15. `[08c5]` (10,04) A healer ($4/hp).

16. `[0834]` (13,14) If you tried to find Berengaria in Phoebus (`bitsplit(99,38)`= 1), he's here instead; clear `bitsplit(99,38)` and run a "[chest](#chest)" (`bitsplit(b9,05)`) with a bunch of scrolls.

17. `[0667]` You cast *D:Create Wall*. If you've already sealed the leak (`bitsplit(99,56)`= 1), exit. If there's already a wall in the right place (the wall texture > 0), exit. Otherwise, run the normal *Create Wall* handler to update the map state, then color text `{4e:0694}`.

18. `[0943]` Run a random combat, then erase this event if you win.

19. `[08f4]` Default handler. If the party is outside (0,0)-(16,16), prompt to exit. N:`(00:25,09)` E:`(00:26,08)` S:`(00:25,07)` W:`(00,24,08)`.

    Gate-and-set `bitsplit(b9,00)` to read paragraph #29. If you've already sealed the leak (`bitsplit(99,56)`= 1), put a wall there. If `bitsplit(99,1a-1d)` are all set, update the decoration texture on the statue to `0x05` (repaired statue).

    > Apparently this handler doesn't run when you're first dropped on the map and haven't taken an action yet.

### Actions

- Special #2, use Stone Hand > Event #3 `[0461]`
- Special #2, use Stone Arms > Event #4 `[0452]`
- Special #2, use Stone Head > Event #5 `[045c]`
- Special #2, use Stone Trunk > Event #6 `[0457]`
- Use *Climb* > Event #9 `[05b3]`
- Cast *D:Create Wall* > Event #17 `[0667]`

## 0x09 Byzanople

### Flags

## 0x0a Smuggler's Cove

### Flags

## 0x0b War Bridge

### Flags

## 0x0c Scorpion Bridge

### Flags

## 0x0d Bridge of Exiles

### Flags

## 0x0e Necropolis

### Flags

## 0x0f Dwarf Ruins

### Flags

- **Random Encounters:** no.

### Board State

| Bitsplit | Heap byte |    Bit     | Meaning                       |
| :------: | :-------: | :--------: | ----------------------------- |
| `b9,00`  |  `[b9]`   | `*–––––––` | If set, the tunnel is closed. |

### Events

1. `[00e9]` (02,04) Color text: if `bitsplit(99,0d)` is set, the stairs down are accessible `{55:0116}`. If not, the tunnel is blocked `{55:00f1}`.
1. `[0137]` (04,04) Color text: a proud statue `{55:01e3}`. If `bitsplit(99,0d)` is not set, "the statue has no eyes" `{55:0185}`
1. `[019e]` You use the Jade Eyes on the statue. Set `bitsplit(99,0d)` and write `[0061]<-0x10`, which opens the tunnel to access the stairs.
1. `[01e2]` (04,01) Color text `{55:01e3}`.
1. `[01fe]` (07,01) Color text `{55:01ff}`.
1. `[0232]` (02,06) Color text `{55:0233}`.
1. `[025a]` (05,04) Color text `{55:025b}`.
1. `[0285]` (00,04) Stairs down to `(10:15,08)`.
1. `[02d1]` (01,06) A [locked chest](#locked) (`bitsplit(99,28)`, difficulty 4) containing the Dwarf Hammer. *This is the same bitsplit as the chest in the Slave Camp, which unfortunately means you get to open one or the other but not both.*
1. `[0293]` Default handler. If the party is outside (0,0)-(7,7), prompt to exit. N:`(10,22)` E:`(11,21)` S:`(10,20)` W:`(09,21)` . Otherwise, if `bitsplit(b9,00)` is not set and `bitsplit(99,0d)` is set, write `[0061]<-0x10` to open the tunnel. (*... (b9,00) isn't set anywhere on this map, though.*)


### Actions

- Special #2, use Jade Eyes -> `[019e]`

## 0x10 Dwarf Clan Hall

> Random cool thing: The invisible wall is index 4, texture `0x7f` and metadata `0xe0=11100000`. Usually that texture is also an index into another list, but clearly not that one.

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
4. `[03e6]` (05,08) If the dwarves have been revived `(99,0e)`, color text `{56:03ee}` the smith is working. Otherwise color text `{56:040f}` the forge is empty.
5. `[03d8]` (00,08) Stairs down to `(12:10,21)`.
6. `[034a]` (01,08) Color text `{56:034b}`.
7. `[0446]` You show the Skull of Roba to the smith. If the dwarves have been revived `(99,0e)`, get color text `{56:044d}` and set `bitsplit(99,0f)` and `bitsplit(99,6b)`.
8. `[04b6]` (06,05) Color text `{56:04b7}`.
9. `[04f2]` (09,04) If the dwarves have *not* been revived `!(99,0e)` or you stole from them `(99,10)`, run combat #1 (Gorgon). If you win (or the bits were set the other way), erase this event.
10. `[0513]` (09,06) Color text `{56:0514}` warning you about the Gorgon fight, even if it isn't actually there.
11. `[0534]` (08,15) If the dwarves have *not* been revived `!(99,0e)` or you stole from them `(99,10)`, run combat #0 (Automata). If you win (or the bits were set the other way), erase this event.
12. `[0545]` (07,11) If the dwarves have been revived `(99,0e)`, erase this event. Otherwise, if you *just* stole from them `(b9,01)`, run combat #0 (Automata) and erase this event if you win. Otherwise, read paragraph #118.
13. `[0564]` (06,13) Gate-and-set `bitsplit(99,4c)` for the Bombs and Spiked Flail (`bitsplit(b9,03)`). If the dwarves have not been revived `(99,0e)`, you're stealing; set `bitsplit(99,10)`. Gate-and-set `bitsplit(b9,02)` for color text that indicates that you woke up the Automata. (also sets `bitsplit(b9,01)`).
14. `[0663]` (09,12) and room. If the dwarves have not been revived `!(99,0e)`, read paragraph #119. Otherwise, if you stole from them `(99,10)`, color text `{56:0678}` they don't like you. (*There's an Encounter all set where the dwarves attack you, but it never gets called. They even have a 2d4 breath weapon.*) Otherwise, color text `{56:069a}` they're rebuilding.
15. `[06c0]` You depetrify the dwarves (so long as you didn't do that already); set `bitsplit(99,0e)` and read paragraph #38. If you haven't stolen from them `!(99,10)`, **gain 500 XP** and color text `{56:06d8}` the dwarves give you access to their treasure. Otherwise color text `{56:0739}` they're pissed at you.
16. `[0761]` Do nothing.
17. `[05eb]` Gate-and-set `bitsplit(99,4d)` for the Dragon Horn (`bitsplit(b9,06)`). 
18. `[061d]` Gate-and-set `bitsplit(99,4e)` for the Crush Mace and Spell Staff  (`bitsplit(b9,07)`). 
19. `[0761]` Default handler.

### Actions

- Special #4, use the Skull of Roba :arrow_right: `[0446]`
- Special #14, cast *D:Soften Stone* :arrow_right: `[06c0]`

## 0x11 Freeport

### Flags

## 0x12 Magan Underworld

### Flags

### Board State

## 0x13 Slave Mines

## 0x14 Lansk

## 0x15 Sunken Ruins (Above)

## 0x16 Sunken Ruins (Below)

### Board State

| Heap byte |    Bit     | Meaning                       |
| :-------: | :--------: | ----------------------------- |
|  `[b9]`   | `*–––––––` | Intro message has been played |
|  `[b9]`   | `–*––––––` | Clam has been picked up       |
|  `[b9]`   | `––––––*–` | Chest has been unlocked       |

## 0x17 Mystic Wood

## 0x18 Snake Pit

## 0x19 Kingshome

## 0x1a Pilgrim Dock

## 0x1b Depths of Nisir

>  the "Dragon Wand" is new...!

## 0x1c Old Dock

## 0x1d Siege Camp

> `Mace: Axe, 1d12, -1AV, requires Strength 17, $70`

## 0x1e Game Preserve

## 0x1f Magic College

## 0x20 Dragon Valley

## 0x21 Phoeban Dungeon

## 0x22 Lanac'toor's Lab

## 0x23 Byzanople Dungeon

## 0x24 Kingshome Dungeon

### Events

1. `[04ed]` Default event. Set `bitsplit(99,78)` to disable the Kingshome ambush.

## 0x25 Slave Estate

## 0x26 Lansk Undercity

## 0x27 Tars Dungeon

# General Events

The metaprogram for each board often calls out to functions in other chunks. Some of these are common to cities and feature additional data, which is worth explaining.

## Taverns<a name="tavern">

*Opcode:* `58090000 longcall(09,0000)`

The next word is the return address, i.e. where you want the program to resume after the party leaves the tavern.

The next word is a pointer to the tavern's "tagline" string.

The following byte is the number of NPC volunteers in residence, followed by an array of pointers to the NPC data. (In practice there's never more than one NPC per tavern, but there's support in the code for more than one.)

The following byte is the number of rumor strings available from the tavern keeper, followed by an array of string pointers.

The tagline string is (usually, although not necessarily) next, then all of the rumor strings.

NPC data starts with the NPC ID number, then the character's name (unpacked, variable-length), then five bytes for the five primary attributes (STR, DEX, INT, SPR, HP). When you add an NPC to your party, `bitsplit(99,57+ID)` is set.

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

There are five destinations. You pass in the index of your current location so that one isn't show on the list of choices.

- Pirate's Cove `(0a:01,01)`
- Necropolis `(0e:07,14)`
- Freeport `(11:14,15)`
- Sunken Ruins `(00:39,14)`
- Rustic `(00:29,28)`

Your "parking space" is stored in `heap[bd]`.
