# Starpocalypse

This mod makes the following changes to the campaign layer of Starsector:

1. Weapons and combat ships are scarce and highly regulated. As such, only military markets (and those pesky black markets) will sell higher tier weapons, LPCs, modspecs, and combat ships. Open markets still sell civilian grade ships and low tier combat ships and items.
    1. Lawless factions (e.g. pirates and pathers) and independents do not submit to this rule of law. You can still find high tier weapons and combat ships at their bases (both core and raider).
    2. Contraband can happen, and when stability is low, some weapons and ships from Military Market can become "legal".
2. There are no pristine ships any more, everything is d-modded. Including your starting fleet.
3. Access to most black markets is impossible while legally docked at the station. Additionally, you will need to pay bribes to transact your business (in lieu of tariffs).
4. Factions are armed to the teeth. Any non-hidden market has at least an orbiting station, ground defences, and patrol HQ. Hidden bases (pirates and pathers raider bases) only get ground defences.
5. Your actions have consequences. When defeating a fleet, your reputation with seemingly unrelated factions changes as well. Enemies of your enemy start to like you a bit, while their friends, less.
    1. Similarly, targeting any colony item will be deemed as an act of war.
6. Blueprint packages are no longer lootable. You will have to collect blueprints one by one.
7. And speaking of salvage, everything needs a story point.
8. Finally, a skill is needed to use s-mods at all (no skill = 0 s-mods, with skill = 1 s-mod).

All changes are optional, and can be disabled via `starpocalypse.json`.

_Important!_ As of Starpocalypse 2.2.0, the mod is no longer safe to disable. In order to remove Starpocalypse from a save game, delete `starpocalypse/data` folder, load, and finally save the game.

## Implementation details

Every change can be disabled at will, see `starpocalypse.json`.
Additional configuration files can be found in `data/starpocalypse/` folder.
Mods can apply changes and merges to default values by shipping the same folder with their version of CSV files.

### Changes to markets

1. Ignore player owned markets altogether (do nothing). This also means autonomous colonies from Nexerelin.
2. Add Ground Defenses or Heavy Batteries to all non-player markets, raider bases included.
3. Additionally, add orbital stations and Patrol HQ to all non-player, non-hidden markets that did not have them, or did not have any of their upgrades...
4. And make sure that the above two are met at all times (via a transient listener).

Two files regulate station additions (`station*.csv`): faction map which points which station tech to use depending on faction, and database file that is needed to prevent stations being added multiple times.

When using mods that add new stations, it is generally a good idea to add them all to the database even if you do not plan to use them in the faction map.

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

1. Remove combat ships, and high tier weapons, LPCs, and modspecs from open markets except for independent, pirate or Luddic Path markets.
2. When stability is low, some of the initially illegal items and ships on military market will become legal.
3. When stability is high, higher value weapons and ships disappear from the black market.
4. Finally, damage all pristine ships by putting a random number of d-mods on them.

Which factions have their Open Markets regulated is declared in the `militaryRegulationFaction.csv` file.
Same submarkets and factions can additionally have contraband applied to their Military Market in `militaryRegulationsStability.csv`.
Finally, exclusion lists can be applied to regulations - see `militaryRegulationsLegal.csv`.

Ship damager accepts faction and submarket, and is applied to all ships. It is controlled by `shipDamage*.csv`.

Both faction and submarket files work as whitelist and accept: faction or submarket (allow) id, negated faction or submarket id (!disallow), "all" keyword (allow all except negated).

#### Black Market

Black market mechanics are slightly tweaked to make it less of a go-to market for everything.
Factions that regulate their open markets will block access to the black market.
As such, you will have to illegally dock at those stations (transponder off).
Market stability will also affect what items are available for trade on the black market using values mapped in `militaryRegulationsStability.csv`.
On top of that, bribes equal half of the market tariff will be required to complete any transactions.

### Other changes

On game load, all blueprint packages are given "no drop" tag.
Quantity of salvaged basic items is reduced by 50%. Special items (weapons, AI cores, etc) are unaffected.
On recovery (salvage or post battle) all recoverable ships are made into story-recoverable (even own lost ships).
Finally, all other findable "items" are removed from salvageable debris (cryopod officers or survey data).
