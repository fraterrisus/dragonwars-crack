# Game State

## Board State Bits

`heap[99] = 0x3cb2` in the `DATA1` file.

| Bitsplit | Heap byte |    Bit     |                Board                 | Meaning                                                      |
| :------: | :-------: | :--------: | :----------------------------------: | ------------------------------------------------------------ |
| `99,00`  |  `[99]`   | `*–––––––` |              Slave Camp              | Have slaughtered the camp residents                          |
| `99,01`  |  `[99]`   | `–*––––––` |              Slave Camp              | Have appeased the camp                                       |
| `99,02`  |  `[99]`   | `––*–––––` |              Slave Camp              | Have dealt with the sick man                                 |
| `99,03`  |  `[99]`   | `–––*––––` |              Slave Camp              | Have impressed the wizard                                    |
| `99,04`  |  `[99]`   | `––––*–––` |              Slave Camp              | Have killed the guardian spirit                              |
| `99,05`  |  `[99]`   | `–––––*––` |              Tars Ruins              | Have moved the stone slab                                    |
| `99,06`  |  `[99]`   | `––––––*–` |           Smuggler's Cove            |                                                              |
| `99,07`  |  `[99]`   | `–––––––*` |               Freeport               | Have revealed the fake Sword of Freedom (one way or the other) |
| `99,08`  |  `[9a]`   | `*–––––––` |              Necropolis              |                                                              |
| `99,09`  |  `[9a]`   | `–*––––––` |              Necropolis              |                                                              |
| `99,0a`  |  `[9a]`   | `––*–––––` |              Necropolis              |                                                              |
| `99,0b`  |  `[9a]`   | `–––*––––` |              Necropolis              |                                                              |
| `99,0c`  |  `[9a]`   | `––––*–––` |                  —                   |                                                              |
| `99,0d`  |  `[9a]`   | `–––––*––` |             Dwarf Ruins              | Used Jade Eyes to open the Dwarf Clan Hall                   |
| `99,0e`  |  `[9a]`   | `––––––*–` |           Dwarf Clan Hall            | Depetrified the dwarves                                      |
| `99,0f`  |  `[9a]`   | `–––––––*` |           Dwarf Clan Hall            | [Gave the Skull to the dwarven smith]                        |
| `99,10`  |  `[9b]`   | `*–––––––` |           Dwarf Clan Hall            | Stole from the dwarves                                       |
| `99,11`  |  `[9b]`   | `–*––––––` |                                      |                                                              |
| `99,12`  |  `[9b]`   | `––*–––––` |                                      |                                                              |
| `99,13`  |  `[9b]`   | `–––*––––` |                                      |                                                              |
| `99,14`  |  `[9b]`   | `––––*–––` |                                      |                                                              |
| `99,15`  |  `[9b]`   | `–––––*––` |                                      |                                                              |
| `99,16`  |  `[9b]`   | `––––––*–` |                                      |                                                              |
| `99,17`  |  `[9b]`   | `–––––––*` |                                      |                                                              |
| `99,18`  |  `[9c]`   | `*–––––––` |                                      |                                                              |
| `99,19`  |  `[9c]`   | `–*––––––` |              Purgatory               | Start square intro message has been played                   |
| `99,1a`  |  `[9c]`   | `––*–––––` |               Mud Toad               | Stone Arms have been returned                                |
| `99,1b`  |  `[9c]`   | `–––*––––` |               Mud Toad               | Stone Trunk has been returned                                |
| `99,1c`  |  `[9c]`   | `––––*–––` |               Mud Toad               | Stone Head has been returned                                 |
| `99,1d`  |  `[9c]`   | `–––––*––` |               Mud Toad               | Stone Hand has been returned                                 |
| `99,1e`  |  `[9c]`   | `––––––*–` |                                      |                                                              |
| `99,1f`  |  `[9c]`   | `–––––––*` |            Sunken Dungeon            | Clam has been brought to surface and replaced with Skull     |
| `99,20`  |  `[9d]`   | `*–––––––` |            Sunken Dungeon            |                                                              |
| `99,21`  |  `[9d]`   | `–*––––––` |              Byzanople               | Kingshome wins the siege of Byzanople                        |
| `99,22`  |  `[9d]`   | `––*–––––` |      Byzanople,<br />Siege Camp      | Byzanople wins the siege of Byzanople                        |
| `99,23`  |  `[9d]`   | `–––*––––` |              Byzanople               | Killed the Byzanople observation party                       |
| `99,24`  |  `[9d]`   | `––––*–––` |          Byzanople Dungeon           | Joined forces with Prince Jordan                             |
| `99,25`  |  `[9d]`   | `–––––*––` |              Purgatory               | Defeated the Humbaba                                         |
| `99,26`  |  `[9d]`   | `––––––*–` |              Purgatory               | Have received reward from Clopin for defeating the Humbaba   |
| `99,27`  |  `[9d]`   | `–––––––*` |              Slave Camp              | Have access to the Nature Axe cache                          |
| `99,28`  |  `[9e]`   | `*–––––––` |     Slave Camp,<br />Dwarf Ruins     | Have unlocked *either* the chest in the trees (Camp) or the Dwarf Hammer chest (Ruins) |
| `99,29`  |  `[9e]`   | `–*––––––` |                                      |                                                              |
| `99,2a`  |  `[9e]`   | `––*–––––` |              Tars Ruins              | Killed the Guardian Serpent                                  |
| `99,2b`  |  `[9e]`   | `–––*––––` |             Tars Dungeon             | Unlocked the scrolls chest                                   |
| `99,2c`  |  `[9e]`   | `––––*–––` |           Guard Bridge #1            | Have access to the cache                                     |
| `99,2d`  |  `[9e]`   | `–––––*––` |               Phoebus                | Unlocked the chest at (01,10)                                |
| `99,2e`  |  `[9e]`   | `––––––*–` |               Phoebus                | Unlocked the chest at (01,15)                                |
| `99,2f`  |  `[9e]`   | `–––––––*` |               Phoebus                | Unlocked the chest at (11,15)                                |
| `99,30`  |  `[9f]`   | `*–––––––` |                                      |                                                              |
| `99,31`  |  `[9f]`   | `–*––––––` |             Slave Estate             |                                                              |
| `99,32`  |  `[9f]`   | `––*–––––` |             Slave Estate             |                                                              |
| `99,33`  |  `[9f]`   | `–––*––––` |                                      |                                                              |
| `99,34`  |  `[9f]`   | `––––*–––` |                                      |                                                              |
| `99,35`  |  `[9f]`   | `–––––*––` |              Purgatory               | Sold yourself into slavery                                   |
| `99,36`  |  `[9f]`   | `––––––*–` |                                      |                                                              |
| `99,37`  |  `[9f]`   | `–––––––*` |                                      |                                                              |
| `99,38`  |  `[a0]`   | `*–––––––` |          Phoebus, Mud Toad           | Attempted to meet Berengaria at the Icarus<br />(If 0, you don't get to meet him in Mud Toad) |
| `99,39`  |  `[a0]`   | `–*––––––` |               Mud Toad               | Have access to the militia's cache                           |
| `99,3a`  |  `[a0]`   | `––*–––––` |                                      |                                                              |
| `99,3b`  |  `[a0]`   | `–––*––––` |                                      |                                                              |
| `99,3c`  |  `[a0]`   | `––––*–––` |                                      |                                                              |
| `99,3d`  |  `[a0]`   | `–––––*––` |                                      |                                                              |
| `99,3e`  |  `[a0]`   | `––––––*–` |                                      |                                                              |
| `99,3f`  |  `[a0]`   | `–––––––*` |                                      |                                                              |
| `99,40`  |  `[a1]`   | `*–––––––` |                                      |                                                              |
| `99,41`  |  `[a1]`   | `–*––––––` |                                      |                                                              |
| `99,42`  |  `[a1]`   | `––*–––––` |                                      |                                                              |
| `99,43`  |  `[a1]`   | `–––*––––` |                                      |                                                              |
| `99,44`  |  `[a1]`   | `––––*–––` |                                      |                                                              |
| `99,45`  |  `[a1]`   | `–––––*––` |                                      |                                                              |
| `99,46`  |  `[a1]`   | `––––––*–` |              Byzanople               | Have unlocked the Fire Shield chest                          |
| `99,47`  |  `[a1]`   | `–––––––*` |                                      |                                                              |
| `99,48`  |  `[a2]`   | `*–––––––` |             Sunken Ruins             | Have access to the chest                                     |
| `99,49`  |  `[a2]`   | `–*––––––` |                                      |                                                              |
| `99,4a`  |  `[a2]`   | `––*–––––` |                                      |                                                              |
| `99,4b`  |  `[a2]`   | `–––*––––` |                                      |                                                              |
| `99,4c`  |  `[a2]`   | `––––*–––` |                                      |                                                              |
| `99,4d`  |  `[a2]`   | `–––––*––` |                                      |                                                              |
| `99,4e`  |  `[a2]`   | `––––––*–` |                                      |                                                              |
| `99,4f`  |  `[a2]`   | `–––––––*` |                                      |                                                              |
| `99,50`  |  `[a3]`   | `*–––––––` |                                      |                                                              |
| `99,51`  |  `[a3]`   | `–*––––––` |                                      |                                                              |
| `99,52`  |  `[a3]`   | `––*–––––` |           Lanac'toor's Lab           |                                                              |
| `99,53`  |  `[a3]`   | `–––*––––` |                Nisir                 | Have unlocked the weapons chest                              |
| `99,54`  |  `[a3]`   | `––––*–––` |            Game Preserve             |                                                              |
| `99,55`  |  `[a3]`   | `–––––*––` |           Magan Underworld           | Scared off the fairies of death                              |
| `99,56`  |  `[a3]`   | `––––––*–` |               Mud Toad               | Sealed the leak, received the Golden Boots                   |
| `99,57`  |  `[a3]`   | `–––––––*` |              Purgatory               | NPC ID 0 (Ulrik) is in the party                             |
| `99,58`  |  `[a4]`   | `*–––––––` |                                      | NPC ID 1 (—) is in the party                                 |
| `99,59`  |  `[a4]`   | `–*––––––` |              Slave Camp              | NPC ID 2 (Louie) is in the party                             |
| `99,5a`  |  `[a4]`   | `––*–––––` |               Phoebus                | NPC ID 3 (Valar) is in the party                             |
| `99,5b`  |  `[a4]`   | `–––*––––` |               Freeport               | NPC ID 4 (Halifax) is in the party                           |
| `99,5c`  |  `[a4]`   | `––––*–––` |                                      | NPC ID 5 (—) is in the party                                 |
| `99,5d`  |  `[a4]`   | `–––––*––` |                                      | NPC ID 6 (—) is in the party                                 |
| `99,5e`  |  `[a4]`   | `––––––*–` |                                      | NPC ID 7 (—) is in the party                                 |
| `99,5f`  |  `[a4]`   | `–––––––*` |                                      | NPC ID 8 (—) is in the party                                 |
| `99,60`  |  `[a5]`   | `*–––––––` |                                      |                                                              |
| `99,61`  |  `[a5]`   | `–*––––––` |                                      |                                                              |
| `99,62`  |  `[a5]`   | `––*–––––` |              Purgatory               | Won citizenship by defeating the Gladiators in the Arena     |
| `99,63`  |  `[a5]`   | `–––*––––` |                Dilmun                | Found the cache on Forlorn                                   |
| `99,64`  |  `[a5]`   | `––––*–––` |                                      |                                                              |
| `99,65`  |  `[a5]`   | `–––––*––` |                                      |                                                              |
| `99,66`  |  `[a5]`   | `––––––*–` |                                      |                                                              |
| `99,67`  |  `[a5]`   | `–––––––*` |                                      |                                                              |
| `99,68`  |  `[a6]`   | `*–––––––` |                                      |                                                              |
| `99,69`  |  `[a6]`   | `–*––––––` |           Phoebus Dungeon            | Been captured by Mystalvision and thrown in the dungeon      |
| `99,6a`  |  `[a6]`   | `––*–––––` |                                      |                                                              |
| `99,6b`  |  `[a6]`   | `–––*––––` |           Dwarf Clan Hall            | [Gave the Skull to the dwarven smith]                        |
| `99,6c`  |  `[a6]`   | `––––*–––` |                                      |                                                              |
| `99,6d`  |  `[a6]`   | `–––––*––` |                                      |                                                              |
| `99,6e`  |  `[a6]`   | `––––––*–` |                                      |                                                              |
| `99,6f`  |  `[a6]`   | `–––––––*` |                                      |                                                              |
| `99,70`  |  `[a7]`   | `*–––––––` | Nisir, Phoebus,<br />Phoebus Dungeon | Killed Mystalvision (for real)                               |
| `99,71`  |  `[a7]`   | `–*––––––` |                Nisir                 | Killed Buck Ironhead                                         |
| `99,72`  |  `[a7]`   | `––*–––––` |                                      |                                                              |
| `99,73`  |  `[a7]`   | `–––*––––` |                                      |                                                              |
| `99,74`  |  `[a7]`   | `––––*–––` |              Byzanople               | Killed the Hydra                                             |
| `99,75`  |  `[a7]`   | `–––––*––` |              Byzanople               | Killed the defenders at the top of the stairs                |
| `99,76`  |  `[a7]`   | `––––––*–` |              Byzanople               | Killed the defenders in the outer city ring                  |
| `99,77`  |  `[a7]`   | `–––––––*` |                Dilmun                | Defeated the Guards on Forlorn, before the first bridge      |
| `99,78`  |  `[a8]`   | `*–––––––` |        Dilmun,<br />Kingshome        | Kingshome ambush has been triggered                          |
| `99,79`  |  `[a8]`   | `–*––––––` |           Lansk Undercity            | Have unlocked to the chest under the statue                  |
| `99,7a`  |  `[a8]`   | `––*–––––` |               Phoebus                | Have encountered Mystalvision                                |
| `99,7b`  |  `[a8]`   | `–––*––––` |                                      |                                                              |
| `99,7c`  |  `[a8]`   | `––––*–––` |           Guard Bridge #2            | Have defeated the guards in the Armory                       |
| `99,7d`  |  `[a8]`   | `–––––*––` |           Guard Bridge #2            | Have defeated both sets of guards in the Barracks            |
| `99,7e`  |  `[a8]`   | `––––––*–` |       Phoeban Dungeon, Dilmun        | Phoebus has been destroyed                                   |
| `99,7f`  |  `[a8]`   | `–––––––*` |                Dilmun                | Defeated the Goblins outside Phoebus                         |
| `99,80`  |  `[a9]`   | `*–––––––` |           Phoeban Dungeon            |                                                              |
| `99,81`  |  `[a9]`   | `–*––––––` |           Magan Underworld           | Have taken the 5 AP Leap of Faith                            |
| `99,82`  |  `[a9]`   | `––*–––––` |             Pilgrim Dock             |                                                              |
| `99,83`  |  `[a9]`   | `–––*––––` |           Magan Underworld           |                                                              |
| `99,84`  |  `[a9]`   | `––––*–––` |           Magan Underworld           |                                                              |
| `99,85`  |  `[a9]`   | `–––––*––` |           Magan Underworld           |                                                              |
| `99,86`  |  `[a9]`   | `––––––*–` |           Magan Underworld           |                                                              |
| `99,87`  |  `[a9]`   | `–––––––*` |           Scorpion Bridge            |                                                              |
| `99,88`  |  `[aa]`   | `*–––––––` |           Tars Underground           | Have opened the Stone Arms cache                             |

## Marching Order

Heap bytes `[0a-10]` represent slots 1–7 in the marching order; the value of the byte indexes the character in the data file, but it's doubled to make the math easier, because each character occupies two "blocks" of `0x100` bytes.

| Index  | Character data pointer |
| :----: | :--------------------: |
| `0x00` |        `0x2e19`        |
| `0x02` |        `0x3019`        |
| `0x04` |        `0x3219`        |
| `0x06` |        `0x3419`        |
| `0x08` |        `0x3619`        |
| `0x0a` |        `0x3819`        |
| `0x0c` |        `0x3a19`        |

## NPCs in party

Spread between bit `heap[a3].0x01` and all bits of `heap[a4]`. This value is generated by reading the NPC IDs from each character's data blob; it's saved in the game state, but the saved value is ignored.

These bits correspond to the NPC Identifier from the [Character Data](Characters.md) which is why several of the bitfields aren't ever filled in.

|     Bits     |   NPC   |
| :----------: | :-----: |
| `1 ––––––––` |  Ulrik  |
| `– –1––––––` |  Louie  |
| `– ––1–––––` |  Valar  |
| `– –––1––––` | Halifax |
