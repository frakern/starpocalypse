# Starpocalypse

This mod makes the following changes to the campaign layer of Starsector:

* Weapons and combat ships are scarce and more expensive (2x by default). Access is regulated with a reputation system. The reputation needed to buy being affected by market stability, your best active contact in the faction (contact reputation and level), and your commission status (if possible). Buying larger hulls or high tier weapons is next to impossible without the right connections. (Thanks to @ThomasRahm for developing this system)
* There are no easy to acquire pristine ships, everything is d-modded. Including your starting fleet. Severity of the d-mods is configurable.
* Access to the black market is more restricted and only available with the transponder off. Access to higher tier weapons and larger ships is only available with an underworld contact. Additionally, you will need to pay bribes to transact your business (in lieu of tariffs).
* Markets are more secure. All core markets have Ground Defences and Patrol HQ with larger size markets all receiving stations.
* Your actions have consequences. When defeating a fleet, your reputation with other factions changes as well. Enemies of your enemy start to like you a bit, while their friends, less.
* Baseline salvage chance is reduced dependent on hull size. No ships are removed from being salvageable, but require story points.

All changes are optional, and can be disabled via `starpocalypse.json`.

_Important!_ As of Starpocalypse 2.2.0, the mod is no longer safe to disable. To remove functionality set the options in `starpocalypse.json` to the vanilla defaults.

## Implementation details

Every change can be disabled at will, see `starpocalypse.json`.
Additional configuration files can be found in `data/starpocalypse/` folder.
Mods can apply changes and merges to default values by shipping the same folder with their version of CSV files.

### Submarket changes

* Access to the military market no longer requires a commission, but combat ships, and weapons, LPCs, and modspecs on the open and military market are limited based on reputation, your contacts, its tier, their stability (`militaryRegulationsStability.csv`) and your commissioned status. See config for exact values.
* The reputation level needed to buy larger hulls or high tier weapons may initially seem impossibly high, but it can be brought down to attainable levels by factors like having a commission and a good relationship with a high importance contact. (Or find a way to reduce the market's stability)
* If you don't have high enough reputation you will not be able to buy combat ships and weapons from the open and military market. If they hate you enough they may not even sell you civilian ships or commodities (except fuel, supplies, and crew which are always available). Can be configured using the config and csv files.
* Markets with a free port allow unregulated access to the open market. (this can be turned off in the settings for extra challenge)
* Finally, all pristine ships on the open and black markets are damaged by putting a random number of d-mods on them. Military market ships are untouched but are harder to access.

* Factions have modifiers for their willingness to sell declared in the `militaryRegulationFaction.csv` file. 
* Fixed values for certain goods, weapons, and ships are configured in `militaryRegulationsSpecialReputation.csv`.
* Finally, exclusion lists can be applied to regulations - see `militaryRegulationsLegal.csv`.

Ship damager is configurable by faction and submarket, and is applied to all ships. It is controlled by `shipDamage*.csv`.

#### Black Market

* Black market mechanics are tweaked to make it less of a go-to market for everything.
* Access to the black market is only available with your transponder off, except at free ports.
* Purchasing higher tier weapons and larger ships requires an underworld contact. The higher the importance of the contact the better equipment they can give access to.
* Suspicion will be raised even when trading with the transponder off, but at half rate as it would have been with the transponder on.
* On top of that, bribes equal half of the market tariff will be required to complete any transaction.

For extra challenge, all access to the black market can be gated behind having an underworld contact by enabling that option in the settings.

### Changes to markets

* Ignore player owned markets altogether (do nothing). This also includes autonomous colonies from Nexerelin.
* Add Ground Defenses or Heavy Batteries to all non-player markets, raider bases included.
* Additionally, add Orbital Stations and Patrol HQ to all non-player, non-hidden markets that did not have them, or did not have any of their upgrades...
* Pirates optionally get megaports and heavy batteries.
* And make sure that the above are met at all times (via a transient listener).

Two files regulate station additions (`station*.csv`): faction map which points which station tech to use depending on faction, and database file that is needed to prevent stations being added multiple times.

When using mods that add new stations, it is recommended to add them all to the database even if you do not plan to use them in the faction map to prevent issues trying to add stations where one already exists.

### Hostile action repercussions

* Any non-blacklisted factions, and only player-won engagements are considered for reputation adjustment.
* Reputation adjustment is based on relationship between faction being adjusted and owner of the fleet you have beaten.
* Maximum reputation adjustment is 1 for factions that are vengeful (or -1 for factions that are cooperative) to the owner of the fleet you have beaten.
* For commissioned faction the max adjustment is +/-3.
* Stealing a colony item instantly sets your reputation to -100 (hostile).

The blacklist file `reputationBlacklist.csv` controls which factions will NOT adjust their reputation of the player.
The list of raid-protected items (special item ids) is present in `raidProtectorItem.csv`.

### Other changes

* Quantity of salvaged basic items is reduced by 25%. Special items (Blueprints, AI cores, etc) are unaffected.
* On recovery (salvage or post battle) some recoverable ships are made into story-recoverable depending on hull size (optionally even own lost ships).
* Weapon salvage rates from battles and derelict ships was reduced to 25%. Value is configurable in config.

### Optional challenge (not enabled by default)
* Remove access to the Nexerelin high-end seller.
* Remove blueprint packages from loot tables. You will have to collect blueprints one by one.
* Reduce chance to recover your own fleet's ships without using story points.
* Change number of allowed s-mods you can build in without a skill.

## Known Issues
Sometimes the derelict modifying script is too slow, and one can get a ship/weapons before it ran. I currently have no way to prevent that, though in normal gameplay this should very rarely happen. The modified setting is applied before the game is loaded, so modifying it does nothing. 
