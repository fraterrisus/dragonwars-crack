# Game State

## Board State Bits

`heap[99] = 0x3cb2` in the `DATA1` file.

| Bitsplit | Heap byte |    Bit     |                   Board                   | Meaning                                                      |
| :------: | :-------: | :--------: | :---------------------------------------: | ------------------------------------------------------------ |
| `99,00`  |  `[99]`   | `*–––––––` |                Slave Camp                 | Slaughtered the camp residents                               |
| `99,01`  |  `[99]`   | `–*––––––` |                Slave Camp                 | Appeased the camp                                            |
| `99,02`  |  `[99]`   | `––*–––––` |                Slave Camp                 | Dealt with the sick man                                      |
| `99,03`  |  `[99]`   | `–––*––––` |                Slave Camp                 | Impressed the wizard                                         |
| `99,04`  |  `[99]`   | `––––*–––` |                Slave Camp                 | Killed the guardian spirit                                   |
| `99,05`  |  `[99]`   | `–––––*––` |                Tars Ruins                 | Moved the stone slab                                         |
| `99,06`  |  `[99]`   | `––––––*–` |              Smuggler's Cove              | Killed the pirate crew                                       |
| `99,07`  |  `[99]`   | `–––––––*` |                 Freeport                  | Revealed the fake Sword of Freedom (one way or the other)    |
| `99,08`  |  `[9a]`   | `*–––––––` |                Necropolis                 | Found the chest in the Guardian room                         |
| `99,09`  |  `[9a]`   | `–*––––––` |                Necropolis                 | Burned out the Spiders' nest                                 |
| `99,0a`  |  `[9a]`   | `––*–––––` |                Necropolis                 | Beat Nergal's guards                                         |
| `99,0b`  |  `[9a]`   | `–––*––––` |                Necropolis                 | Fed Nergal Mushrooms                                         |
| `99,0c`  |  `[9a]`   | `––––*–––` |                     —                     | *unused*                                                     |
| `99,0d`  |  `[9a]`   | `–––––*––` |                Dwarf Ruins                | Used Jade Eyes to open the Dwarf Clan Hall                   |
| `99,0e`  |  `[9a]`   | `––––––*–` |              Dwarf Clan Hall              | Depetrified the dwarves                                      |
| `99,0f`  |  `[9a]`   | `–––––––*` |              Dwarf Clan Hall              | [Gave the Skull to the dwarven smith]                        |
| `99,10`  |  `[9b]`   | `*–––––––` |              Dwarf Clan Hall              | Stole from the dwarves                                       |
| `99,11`  |  `[9b]`   | `–*––––––` |               Game Preserve               | Killed the stag                                              |
| `99,12`  |  `[9b]`   | `––*–––––` |               Game Preserve               | Jack has vanished (or was killed)                            |
| `99,13`  |  `[9b]`   | `–––*––––` |                 Snake Pit                 | Found the Stone Head                                         |
| `99,14`  |  `[9b]`   | `––––*–––` |                 Snake Pit                 | Found Drake's other throne                                   |
| `99,15`  |  `[9b]`   | `–––––*––` |                 Kingshome                 | Met Namtar                                                   |
| `99,16`  |  `[9b]`   | `––––––*–` |             Kingshome Dungeon             | Found Drake's real throne                                    |
| `99,17`  |  `[9b]`   | `–––––––*` |                Mystic Wood                | Wrestled Enkidu?                                             |
| `99,18`  |  `[9c]`   | `*–––––––` |                Mystic Wood                | Revived Zaton's spirit                                       |
| `99,19`  |  `[9c]`   | `–*––––––` |                 Purgatory                 | Start square intro message has been played                   |
| `99,1a`  |  `[9c]`   | `––*–––––` |                 Mud Toad                  | Stone Arms have been returned                                |
| `99,1b`  |  `[9c]`   | `–––*––––` |                 Mud Toad                  | Stone Trunk has been returned                                |
| `99,1c`  |  `[9c]`   | `––––*–––` |                 Mud Toad                  | Stone Head has been returned                                 |
| `99,1d`  |  `[9c]`   | `–––––*––` |                 Mud Toad                  | Stone Hand has been returned                                 |
| `99,1e`  |  `[9c]`   | `––––––*–` |                Lanac'toor                 | Found the Spectacles                                         |
| `99,1f`  |  `[9c]`   | `–––––––*` |              Sunken Dungeon               | Clam has been brought to surface and replaced with Skull     |
| `99,20`  |  `[9d]`   | `*–––––––` |              Sunken Dungeon               | Defeated the locker guards                                   |
| `99,21`  |  `[9d]`   | `–*––––––` |        Byzanople,<br />Siege Camp         | Kingshome won the siege of Byzanople                         |
| `99,22`  |  `[9d]`   | `––*–––––` |        Byzanople,<br />Siege Camp         | Byzanople won the siege of Byzanople                         |
| `99,23`  |  `[9d]`   | `–––*––––` |                 Byzanople                 | Killed the Byzanople observation party (and Princess Myrilla) |
| `99,24`  |  `[9d]`   | `––––*–––` |    Byzanople Dungeon,<br />Siege Camp     | Joined forces with Prince Jordan                             |
| `99,25`  |  `[9d]`   | `–––––*––` |                 Purgatory                 | Defeated the Humbaba                                         |
| `99,26`  |  `[9d]`   | `––––––*–` |                 Purgatory                 | Received reward from Clopin for defeating the Humbaba        |
| `99,27`  |  `[9d]`   | `–––––––*` |                Slave Camp                 | Found the Nature Axe                                         |
| `99,28`  |  `[9e]`   | `*–––––––` |       Slave Camp,<br />Dwarf Ruins        | Unlocked *either* the chest in the trees (Camp) or the Dwarf Hammer chest (Ruins) |
| `99,29`  |  `[9e]`   | `–*––––––` |                Tars Ruins                 | Killed the scroll guards                                     |
| `99,2a`  |  `[9e]`   | `––*–––––` |                Tars Ruins                 | Killed the Guardian Serpent                                  |
| `99,2b`  |  `[9e]`   | `–––*––––` |               Tars Dungeon                | Unlocked the scrolls chest                                   |
| `99,2c`  |  `[9e]`   | `––––*–––` |              Guard Bridge #1              | Found the cache                                              |
| `99,2d`  |  `[9e]`   | `–––––*––` |                  Phoebus                  | Unlocked the chest at (01,10)                                |
| `99,2e`  |  `[9e]`   | `––––––*–` |                  Phoebus                  | Unlocked the chest at (01,15)                                |
| `99,2f`  |  `[9e]`   | `–––––––*` |                  Phoebus                  | Unlocked the chest at (11,15)                                |
| `99,30`  |  `[9f]`   | `*–––––––` |               Slave Estate                | Found the chest at (10,11)                                   |
| `99,31`  |  `[9f]`   | `–*––––––` |               Slave Estate                | Found the chest at (05,08)                                   |
| `99,32`  |  `[9f]`   | `––*–––––` |               Slave Estate                | Unlocked the chest at (03,08)                                |
| `99,33`  |  `[9f]`   | `–––*––––` |         Slave Mines, Slave Estate         | Defeated the guards to escape the mines<br />*-or-* Found the ugly statue of Mog |
| `99,34`  |  `[9f]`   | `––––*–––` |               Slave Estate                | Killed the Gaze Demon                                        |
| `99,35`  |  `[9f]`   | `–––––*––` |          Purgatory, Slave Mines           | Sold yourself into slavery                                   |
| `99,36`  |  `[9f]`   | `––––––*–` |                Slave Mines                | Gave water to the dying old man                              |
| `99,37`  |  `[9f]`   | `–––––––*` |                Slave Mines                | Unlocked the hidden chest                                    |
| `99,38`  |  `[a0]`   | `*–––––––` |             Phoebus, Mud Toad             | Attempted to meet Berengaria at the Icarus<br />(If 0, you don't get to meet him in Mud Toad) |
| `99,39`  |  `[a0]`   | `–*––––––` |                 Mud Toad                  | Have access to the militia's cache                           |
| `99,3a`  |  `[a0]`   | `––*–––––` |              Smuggler's Cove              | Unlocked the pirates' treasure chest                         |
| `99,3b`  |  `[a0]`   | `–––*––––` |                   Lansk                   | Unlocked the chest                                           |
| `99,3c`  |  `[a0]`   | `––––*–––` |             Kingshome Dungeon             | Found the Armory                                             |
| `99,3d`  |  `[a0]`   | `–––––*––` |                 Kingshome                 | Found the weapons cache                                      |
| `99,3e`  |  `[a0]`   | `––––––*–` |                 Snake Pit                 | Unlocked the hidden chest                                    |
| `99,3f`  |  `[a0]`   | `–––––––*` |                Mystic Wood                | Unlocked the chest in the SW corner                          |
| `99,40`  |  `[a1]`   | `*–––––––` |                Mystic Wood                | Unlocked the chest along the shoreline                       |
| `99,41`  |  `[a1]`   | `–*––––––` |                Mystic Wood                | Found The Ring                                               |
| `99,42`  |  `[a1]`   | `––*–––––` |                Mystic Wood                | Found the Beast Horn                                         |
| `99,43`  |  `[a1]`   | `–––*––––` |                Mystic Wood                | Defeated the Water Spirits guarding the chest                |
| `99,44`  |  `[a1]`   | `––––*–––` |                Mystic Wood                | Defeated the Lagooners guarding the pond                     |
| `99,45`  |  `[a1]`   | `–––––*––` |                 Old Dock                  | Moved the statue                                             |
| `99,46`  |  `[a1]`   | `––––––*–` |                 Byzanople                 | Unlocked the Fire Shield chest                               |
| `99,47`  |  `[a1]`   | `–––––––*` |              Scorpion Bridge              | Have access to the chest                                     |
| `99,48`  |  `[a2]`   | `*–––––––` |               Sunken Ruins                | Have access to the chest                                     |
| `99,49`  |  `[a2]`   | `–*––––––` |              Sunken Dungeon               | Unlocked the hidden chest                                    |
| `99,4a`  |  `[a2]`   | `––*–––––` |                 Freeport                  | Unlocked the Order's chest                                   |
| `99,4b`  |  `[a2]`   | `–––*––––` |                 Freeport                  | Defeated the Order of the Sword                              |
| `99,4c`  |  `[a2]`   | `––––*–––` |              Dwarf Clan Hall              | Found the Spiked Flail                                       |
| `99,4d`  |  `[a2]`   | `–––––*––` |              Dwarf Clan Hall              | Found the Dragon Horn                                        |
| `99,4e`  |  `[a2]`   | `––––––*–` |              Dwarf Clan Hall              | Found the Crush Mace                                         |
| `99,4f`  |  `[a2]`   | `–––––––*` |               Dragon Valley               | Unlocked the chest in the NE corner                          |
| `99,50`  |  `[a3]`   | `*–––––––` |               Dragon Valley               | Beat the encounter guarding the SW chest                     |
| `99,51`  |  `[a3]`   | `–*––––––` |                Lanac'toor                 | Have access to the scrolls chest                             |
| `99,52`  |  `[a3]`   | `––*–––––` |                Lanac'toor                 | Unlocked the Healing Potion chest                            |
| `99,53`  |  `[a3]`   | `–––*––––` |                   Nisir                   | Unlocked the weapons chest                                   |
| `99,54`  |  `[a3]`   | `––––*–––` |               Game Preserve               | Killed the bandits                                           |
| `99,55`  |  `[a3]`   | `–––––*––` |             Magan Underworld              | Scared off the malign fairies                                |
| `99,56`  |  `[a3]`   | `––––––*–` |                 Mud Toad                  | Sealed the leak, received the Golden Boots                   |
| `99,57`  |  `[a3]`   | `–––––––*` |                 Purgatory                 | NPC ID 0 (Ulrik) is in the party                             |
| `99,58`  |  `[a4]`   | `*–––––––` |                     —                     | NPC ID 1 (—) is in the party                                 |
| `99,59`  |  `[a4]`   | `–*––––––` |                Slave Camp                 | NPC ID 2 (Louie) is in the party                             |
| `99,5a`  |  `[a4]`   | `––*–––––` |                  Phoebus                  | NPC ID 3 (Valar) is in the party                             |
| `99,5b`  |  `[a4]`   | `–––*––––` |                 Freeport                  | NPC ID 4 (Halifax) is in the party                           |
| `99,5c`  |  `[a4]`   | `––––*–––` |                     —                     | NPC ID 5 (—) is in the party                                 |
| `99,5d`  |  `[a4]`   | `–––––*––` |                     —                     | NPC ID 6 (—) is in the party                                 |
| `99,5e`  |  `[a4]`   | `––––––*–` |                     —                     | NPC ID 7 (—) is in the party                                 |
| `99,5f`  |  `[a4]`   | `–––––––*` |                     —                     | NPC ID 8 (—) is in the party                                 |
| `99,60`  |  `[a5]`   | `*–––––––` |                     —                     | *unused*                                                     |
| `99,61`  |  `[a5]`   | `–*––––––` |               Slave Estate                | Killed the Gaze Demon with a mirror                          |
| `99,62`  |  `[a5]`   | `––*–––––` |                 Purgatory                 | Won citizenship by defeating the Gladiators in the Arena     |
| `99,63`  |  `[a5]`   | `–––*––––` |                  Dilmun                   | Found the cache on Forlorn                                   |
| `99,64`  |  `[a5]`   | `––––*–––` |                Siege Camp                 | Found the soldier's cache                                    |
| `99,65`  |  `[a5]`   | `–––––*––` |                Siege Camp                 | Unlocked the other chest                                     |
| `99,66`  |  `[a5]`   | `––––––*–` |              Phoeban Dungeon              | Have access to the good treasure                             |
| `99,67`  |  `[a5]`   | `–––––––*` |              Phoeban Dungeon              | Have access to the bad treasure                              |
| `99,68`  |  `[a6]`   | `*–––––––` |              Phoeban Dungeon              | The Druid told you the password (and died)                   |
| `99,69`  |  `[a6]`   | `–*––––––` |              Phoeban Dungeon              | Been captured by Mystalvision and thrown in the dungeon      |
| `99,6a`  |  `[a6]`   | `––*–––––` |             Magan Underworld              | Freed Irkalla from the Isle of Woe                           |
| `99,6b`  |  `[a6]`   | `–––*––––` |  Dwarf Clan Hall,<br />Magan Underworld   | The Sword of Freedom has been forged                         |
| `99,6c`  |  `[a6]`   | `––––*–––` |             Byzanople Dungeon             | Found the MagicAxe                                           |
| `99,6d`  |  `[a6]`   | `–––––*––` |             Byzanople Dungeon             | Killed Prince Jordan                                         |
| `99,6e`  |  `[a6]`   | `––––––*–` |             Byzanople Dungeon             | Killed Princess Myrolla                                      |
| `99,6f`  |  `[a6]`   | `–––––––*` |             Byzanople Dungeon             | Found the treasure vault                                     |
| `99,70`  |  `[a7]`   | `*–––––––` |   Nisir, Phoebus,<br />Phoeban Dungeon    | Killed Mystalvision (for real)                               |
| `99,71`  |  `[a7]`   | `–*––––––` |                   Nisir                   | Killed Buck Ironhead                                         |
| `99,72`  |  `[a7]`   | `––*–––––` |             Magan Underworld              | Unlocked the Rusty Axe chest                                 |
| `99,73`  |  `[a7]`   | `–––*––––` |             Magan Underworld              | Defeated the encounter at the Rusty Axe                      |
| `99,74`  |  `[a7]`   | `––––*–––` |                 Byzanople                 | Killed the Hydra                                             |
| `99,75`  |  `[a7]`   | `–––––*––` |                 Byzanople                 | Killed the defenders at the top of the stairs                |
| `99,76`  |  `[a7]`   | `––––––*–` |                 Byzanople                 | Killed the defenders in the outer city ring                  |
| `99,77`  |  `[a7]`   | `–––––––*` |                  Dilmun                   | Defeated the Guards on Forlorn, before the first bridge      |
| `99,78`  |  `[a8]`   | `*–––––––` | Dilmun, Kingshome,<br />Kingshome Dungeon | Kingshome ambush has been triggered                          |
| `99,79`  |  `[a8]`   | `–*––––––` |              Lansk Undercity              | Have unlocked the chest under the statue                     |
| `99,7a`  |  `[a8]`   | `––*–––––` |                  Phoebus                  | Have encountered Mystalvision                                |
| `99,7b`  |  `[a8]`   | `–––*––––` |             Magan Underworld              | Unlocked the Slicer chest                                    |
| `99,7c`  |  `[a8]`   | `––––*–––` |              Guard Bridge #2              | Have defeated the guards in the Armory                       |
| `99,7d`  |  `[a8]`   | `–––––*––` |              Guard Bridge #2              | Have defeated both sets of guards in the Barracks            |
| `99,7e`  |  `[a8]`   | `––––––*–` |       Phoeban Dungeon,<br />Dilmun        | Phoebus has been destroyed                                   |
| `99,7f`  |  `[a8]`   | `–––––––*` |                  Dilmun                   | Defeated the Goblins outside Phoebus                         |
| `99,80`  |  `[a9]`   | `*–––––––` |              Phoeban Dungeon              | The dragon has been fed                                      |
| `99,81`  |  `[a9]`   | `–*––––––` |             Magan Underworld              | Have taken the 5 AP Leap of Faith                            |
| `99,82`  |  `[a9]`   | `––*–––––` |               Pilgrim Dock                | Killed the dock guards                                       |
| `99,83`  |  `[a9]`   | `–––*––––` |             Magan Underworld              | Have access to the first Namtar's Body chest                 |
| `99,84`  |  `[a9]`   | `––––*–––` |             Magan Underworld              | Dropped Namtar's Body to set up the first fight              |
| `99,85`  |  `[a9]`   | `–––––*––` |             Magan Underworld              | Defeated Namtar the first time                               |
| `99,86`  |  `[a9]`   | `––––––*–` |             Magan Underworld              | Defeated Namtar the second time<br />Have access to the second Namtar's Body chest |
| `99,87`  |  `[a9]`   | `–––––––*` |              Scorpion Bridge              | Killed the Scorpion guards                                   |
| `99,88`  |  `[aa]`   | `*–––––––` |             Tars Underground              | Have opened the Stone Arms cache                             |
| `99,89`  |  `[aa]`   | `–*––––––` |           Dragon Valley, Nisir            | Showed the Dragon Gem to the Queen                           |

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
