# Starpocalypse - Economic apocalypse

This mod makes the following changes to the campaign layer of Starsector:

1. Weapons and combat ships are scarce and highly regulated and more expensive. Access to these is regulated with a standing system, with the standing affected by your reputation, your best contact (contact reputation and quality) and your commission status (if possible). Buying larger hulls or high tier weapons is next to impossible without the right connections.
    1. If you dont have a high enough standing you wont be getting combat ships and weapons equipment from them. If they hate you enough they may even not sell you civilian ships. Can be configured using the config and csv files mentioned in it.
    2. You can only buy combat ships and weapons on the black market if your contact has a high enough importance. Can be configured in the config.

2. There are no pristine ships, everything is d-modded. Including your starting fleet.
3. Access to most Black Markets is impossible while not having a pirate contact at that location. Additionally, you will need to pay bribes to transact your business (in lieu of tariffs).
4. Factions are armed to the teeth. All core markets have Ground Defences and Patrol HQ with larger size markets all receiving stations. Pirates get megaports and heavy batteries.
5. Your actions have consequences. When defeating a fleet, your reputation with other factions changes as well. Enemies of your enemy start to like you a bit, while their friends, less.
    1. Similarly, targeting any colony item will be deemed as an act of war.
6. Blueprint packages are no longer lootable. You will have to collect blueprints one by one.
7. Baseline salvage chance is reduced dependent on hull size. No ships are removed from being salvageable, but require a story-point.

All changes are optional, and can be disabled via `starpocalypse.json`.

_Important!_ As of Starpocalypse 2.2.0, the mod is no longer safe to disable. In order to remove Starpocalypse from a save game, delete `starpocalypse/data` folder, load, and finally save the game.

## Implementation details

Every change can be disabled at will, see `starpocalypse.json`.
Additional configuration files can be found in `data/starpocalypse/` folder.
Mods can apply changes and merges to default values by shipping the same folder with their version of CSV files.

### Changes to markets

1. Ignore player owned markets altogether (do nothing). This also means autonomous colonies from Nexerelin.
2. Add Ground Defenses or Heavy Batteries to all non-player markets, raider bases included.
3. Additionally, add Orbital Stations and Patrol HQ to all non-player, non-hidden markets that did not have them, or did not have any of their upgrades...
4. Pirates get megaports and heavy batteries.
5. And make sure that the above three are met at all times (via a transient listener).

Two files regulate station additions (`station*.csv`): faction map which points which station tech to use depending on faction, and database file that is needed to prevent stations being added multiple times.

When using mods that add new stations, it is recommended to add them all to the database even if you do not plan to use them in the faction map to prevent issues trying to add stations where one already exists.

### Hostile action repercussions

1. Any non-blacklisted factions, and only player-won engagements are considered for reputation adjustment.
2. Reputation adjustment is based on relationship between faction being adjusted and owner of the fleet you have beaten.
3. Maximum reputation adjustment is 1 for factions that are vengeful (or -1 for factions that are cooperative) to the
   owner of the fleet you have beaten.
4. For commissioned faction the max adjustment is +/-3.
5. Stealing a colony item instantly sets your reputation to -1 (hostile).

The blacklist file `reputationBlacklist.csv` controls which factions will NOT adjust their reputation of the player.
The list of raid-protected items (special item ids) is present in `raidProtectorItem.csv`.

### Submarket changes

1. Combat ships, and weapons, LPCs, and modspecs are limited based on standing, your contacts, its tier, their stability (`militaryRegulationsStability.csv`) and your commissioned status. See config for exact values
2. Finally, all pristine ships are damaged by putting a random number of d-mods on them.

Factions have modifiers for their willingness to sell weapons declared in the `militaryRegulationFaction.csv` file. 
The willingness to sell given good can be modified in `militaryRegulationsSpecialStanding.csv`.
Finally, exclusion lists can be applied to regulations - see `militaryRegulationsLegal.csv`.

Ship damager is configurable by faction and submarket, and is applied to all ships. It is controlled by `shipDamage*.csv`.

#### Black Market

Black Market mechanics are tweaked to make it less of a go-to market for everything.
One needs a pirate contact at the location to access the black market. Even then not every contact enables you to get everything. The higher the importance of the contact the more you can get.
Suspicion will be raised even when trading with the transponder off, but at half rate as with the transponder on.
On top of that, bribes equal half of the market tariff will be required to complete any transaction.

### Other changes

On game load, all blueprint packages are given "no drop" tag.
Quantity of salvaged basic items is reduced by 25%. Special items (weapons, AI cores, etc) are unaffected.
On recovery (salvage or post battle) some recoverable ships are made into story-recoverable depending on hull size (optionally even own lost ships).
Weapon salvage rates from battles and derelict ships was reduced to 25%. Value is configurable in config.

## Known Issues
Sometimes the derelict modifying script is too slow, and one can get a ship/weapons before it ran. I currently have no way to prevent that, though in normal gameplay this should very rarely happen.