# Version 4.1.0
* Fixed contact level not providing standing
* Fixed stringy weapon recovery mistakenly applying to players
* Changed costMultiplierProduction to costMultiplierBuildings and corrected the description
* Reimplemented battle stingy recovery as BaseCampaignEventListener. That should make it more resilient and also apply to fights originating from Domain probes
* Disabled salvage stingy recovery in the tutorial to prevent soft-locks.
* Changed stingy weapon recovery to support different values based on weapon tier.
* Improved messaging illegality of items caused by low standing

# Version 4.0.0
* Reimagined submarket changes. Now willingness to sell is based on multiple factors.
* Made black markets require a pirate contact.
* Improved pirate defences.
* Added stingy weapon recovery. Weapons are now harder to acquire.
* Increased prices of ships and weapons
* Updated to 0.98a

# Version 3.0.2
* Add adjustable setting for class of player ships subject to stingy recoveries.
* Adjust black market to allow some items at higher stability levels.
* Fix ShipDamager always applying 1 less dmod than max setting.
* Fix for Heavy Weapons being marked as illegal in markets of factions that do not regulate their sale.

# Version 3.0.1

* Added setting for salvage multiplier
* Added removal of items from black markets based on stability
* Renamed black market tariffs to bribes
* Added more granular control for settings related to market defences
* Added compatibility with Nexerelin to stingy recoveries
* Split stingy recoveries settings to separate derelicts and combat recoveries
* Added option so combat stingy recoveries does not apply to player ships

# Version 3.0.0

* Starsector 0.96a compatibility update
