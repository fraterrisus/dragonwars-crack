Skills
===

Attributes and Skills are referenced in a few different places; what's listed here is the byte offset into [character](Characters) data, since that's the value that gets used more often. The other main place these are referenced is in [equipment](Equipment.md) data, when an Item has a skill or attribute requirement (i.e. "minimum STR 10"). In that case, the value in byte `[01]` starts at `0x00` for Strength, so add `0x0c` in order to reference this table.

| Offset      | Skill/Attribute |
| ----------- | --------------- |
| `0x0c-0x0d` | Strength        |
| `0x0e-0x0f` | Dexterity       |
| `0x10-0x11` | Intelligence    |
| `0x12-0x13` | Spirit          |
| `0x14`      | Health          |
| `0x18`      | Stun            |
| `0x1c`      | Power           |
| `0x20`      | Arcane Lore     |
| `0x21`      | Cave Lore       |
| `0x22`      | Forest Lore     |
| `0x23`      | Mountain Lore   |
| `0x24`      | Town Lore       |
| `0x25`      | Bandage         |
| `0x26`      | Climb           |
| `0x27`      | Fistfighting    |
| `0x28`      | Hiding          |
| `0x29`      | Lockpick        |
| `0x2a`      | Pickpocket      |
| `0x2b`      | Swim            |
| `0x2c`      | Tracker         |
| `0x2d`      | Bureaucracy     |
| `0x2e`      | Druid Magic     |
| `0x2f`      | High Magic      |
| `0x30`      | Low Magic       |
| `0x31`      | Merchant (?!)   |
| `0x32`      | Sun Magic       |
| `0x33`      | Axes            |
| `0x34`      | Flails          |
| `0x35`      | Maces           |
| `0x36`      | Swords          |
| `0x37`      | Two-handers     |
| `0x38`      | Bows            |
| `0x39`      | Crossbows       |
| `0x3a`      | Thrown Weapons  |

