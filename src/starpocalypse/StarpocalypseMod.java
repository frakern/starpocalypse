package starpocalypse;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.missions.HandMeDownFreighter;
import com.fs.starfarer.api.impl.campaign.missions.HijackingMission;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exerelin.campaign.intel.missions.BuyShip;
import lombok.extern.log4j.Log4j;
import org.json.JSONObject;
import starpocalypse.helper.ConfigHelper;
import starpocalypse.helper.DropTableUtils;
import starpocalypse.market.*;
import starpocalypse.reputation.EngagementListener;
import starpocalypse.reputation.RaidListener;
import starpocalypse.submarket.ShipDamager;
import starpocalypse.submarket.SubmarketSwapper;

import com.fs.starfarer.api.impl.campaign.missions.SurplusShipHull;

@Log4j
public class StarpocalypseMod extends BaseModPlugin {

    private static JSONObject settings;
    private static Map<String,Object> original_vanilla_setting = new HashMap<>();
    private boolean cached_orgiginal_settings = false;
    @Override
    public void onApplicationLoad() throws Exception {
        settings = Global.getSettings().loadJSON("starpocalypse.json");
        ConfigHelper.init(settings, log);
        disableBlueprintDrop();
        setMaxPermaMods();
    }

    @Override
    public void onNewGameAfterTimePass() {
        addDmodsToStartingFleet();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        SubmarketSwapper.uninstallLegacy();
        SubmarketSwapper.reinstall();
        addDmodsToShipsInSubmarkets();
        militaryRegulations();
        industryChanges();
        combatAdjustedReputation();
        hostilityForSpecialItemRaid();
        stingyDerelictRecoveries();
        stingyCombatRecoveries();
        salvageMultiplier();
        applyCostModifiers();
        applyCostModifierToVanillaQuests();
    }

    @Override
    public void beforeGameSave() {
        DropTableUtils.removeSalvageMultiplier();
    }

    @Override
    public void afterGameSave() {
        if (ConfigHelper.isUninstall()) {
            SharedData.getData().getPlayerActivityTracker().advance(0);
            showUninstalledDialog();
        } else {
            salvageMultiplier();
        }
    }

    private void industryChanges() {
        MarketListener listener = new MarketListener();
        addGroundDefenses(listener);
        addPatrolHqs(listener);
        addStations(listener);
        upgradePirateSpaceport(listener);
        listener.register();
    }

    private void addDmodsToShipsInSubmarkets() {
        if (settings.optBoolean("addDmodsToShipsInSubmarkets", true)) {
            log.info("Enabling ship damager in submarkets");
            ShipDamager.register();
        }
    }

    private void addDmodsToStartingFleet() {
        if (settings.optBoolean("addDmodsToStartingFleet", true)) {
            log.info("Damaging starting fleet");
            List<FleetMemberAPI> members = Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy();
            ShipDamager.apply("player fleet", members);
        }
    }

    private void addGroundDefenses(MarketListener listener) {
        if (settings.optBoolean("addGroundDefenses", true)) {
            log.info("Enabling ground defenses adder");
            listener.add(new GroundDefenseAdder(settings.optInt("sizeHeavyBatteries", 5)));
        }
    }

    private void addPatrolHqs(MarketListener listener) {
        if (settings.optBoolean("addPatrolHqs", true)) {
            log.info("Enabling patrol hq adder");
            listener.add(
                new IndustryAdder(
                    Industries.PATROLHQ,
                    false,
                    Industries.PATROLHQ,
                    Industries.MILITARYBASE,
                    Industries.HIGHCOMMAND
                )
            );
        }
    }

    private void addStations(MarketListener listener) {
        if (settings.optBoolean("addStations", true)) {
            log.info("Enabling station adder");
            listener.add(
                new StationAdder(
                    settings.optInt("sizeForOrbitalStation", 5),
                    settings.optInt("sizeForBattleStation", 6),
                    settings.optInt("sizeForStarFortress", 7)
                )
            );
        }
    }
    private void upgradePirateSpaceport(MarketListener listener) {
        listener.add(
                new PirateUpgrader(
                        settings.optBoolean("pirateHeavyBatteries", true),
                        settings.optBoolean("pirateMegaport", true)
                )
        );
    }



        private void combatAdjustedReputation() {
        if (settings.optBoolean("combatAdjustedReputation", true)) {
            log.info("Enabling combat adjusted reputation");
            EngagementListener.register();
        }
    }

    private void disableBlueprintDrop() {
        if (settings.optBoolean("blueprintPackageNoDrop", true)) {
            log.info("Removing blueprint packages from drop lists");
            DropTableUtils.removeBlueprintPackages();
        }
    }

    private void hostilityForSpecialItemRaid() {
        if (settings.optBoolean("hostilityForSpecialItemRaid", true)) {
            log.info("Enabling hostility for special item raid");
            RaidListener.register();
        }
    }

    private void militaryRegulations() {
        if (settings.optBoolean("militaryRegulations", true)) {
            log.info("Enabling military regulations");
            SubmarketSwapper.register();
        }
    }

    private void setMaxPermaMods() {
        int maxPermaMods = settings.optInt("maxPermaMods", 0);
        Misc.MAX_PERMA_MODS = maxPermaMods;
    }

    private void stingyDerelictRecoveries() {
        if (settings.optBoolean("stingyRecoveriesDerelicts", true)) {
            log.info("Enabling stingy derelict recoveries");
            DropTableUtils.makeDerelictRecoveryRequireStoryPoint();
        }
    }

    private void stingyCombatRecoveries() {
        if (settings.optBoolean("stingyRecoveriesCombat", true)) {
            log.info("Enabling stingy combat recoveries");
            DropTableUtils.makeCombatRecoveryRequireStoryPoint();
        }
    }

    private void salvageMultiplier() {
        DropTableUtils.applySalvageMultiplier((float) settings.optDouble("salvageMultiplier", -0.5));
    }

    private void showUninstalledDialog() {
        CampaignUIAPI campaignUi = Global.getSector().getCampaignUI();
        if (campaignUi == null) {
            return;
        }
        campaignUi.showMessageDialog(
            "Starpocalypse has been removed from this save. You can now quit the game and disable this mod." +
            "\n\nThank you for playing with Starpocalypse. I hope you had a bad day." +
            "\n\nYours, Jaghaimo."
        );
    }

    private void applyCostModifiers() {

        if(!cached_orgiginal_settings)
        {
            original_vanilla_setting.put("shipSellPriceMult", Global.getSettings().getFloat("shipSellPriceMult"));
            original_vanilla_setting.put("shipBuyPriceMult", Global.getSettings().getFloat("shipBuyPriceMult"));
            original_vanilla_setting.put("shipWeaponBuyPriceMult", Global.getSettings().getFloat("shipWeaponBuyPriceMult"));
            original_vanilla_setting.put("shipWeaponSellPriceMult", Global.getSettings().getFloat("shipWeaponSellPriceMult"));
            original_vanilla_setting.put("productionCostMult", Global.getSettings().getFloat("productionCostMult"));
            original_vanilla_setting.put("productionCapacityPerSWUnit", Global.getSettings().getFloat("productionCapacityPerSWUnit"));
            cached_orgiginal_settings = true;
        }
        Global.getSettings().setFloat("shipBuyPriceMult", ConfigHelper.getCostMultiplierShips() * (float)original_vanilla_setting.get("shipSellPriceMult"));
        Global.getSettings().setFloat("shipWeaponBuyPriceMult", ConfigHelper.getCostMultiplierWeapon() * (float)original_vanilla_setting.get("shipWeaponBuyPriceMult"));
        Global.getSettings().setFloat("productionCostMult", ConfigHelper.getCostMultiplierProduction() * (float)original_vanilla_setting.get("productionCostMult"));
        Global.getSettings().setFloat("productionCapacityPerSWUnit", ConfigHelper.getCostMultiplierProduction() * (float)original_vanilla_setting.get("productionCapacityPerSWUnit"));

        if(ConfigHelper.getCostMultiplierSellerProfitMargin() == 0)
        {
            Global.getSettings().setFloat("shipSellPriceMult", ConfigHelper.getCostMultiplierShips() * (float)original_vanilla_setting.get("shipSellPriceMult"));
            Global.getSettings().setFloat("shipWeaponSellPriceMult", ConfigHelper.getCostMultiplierWeapon() * (float)original_vanilla_setting.get("shipWeaponSellPriceMult"));
        }
        else
        {
            Global.getSettings().setFloat("shipSellPriceMult", (1f - ConfigHelper.getCostMultiplierSellerProfitMargin()) * ConfigHelper.getCostMultiplierShips() * (float)original_vanilla_setting.get("shipBuyPriceMult"));
            Global.getSettings().setFloat("shipWeaponSellPriceMult", (1f - ConfigHelper.getCostMultiplierSellerProfitMargin()) * ConfigHelper.getCostMultiplierWeapon() * (float)original_vanilla_setting.get("shipWeaponBuyPriceMult"));
        }
        Global.getSettings().setFloat("hullWithDModsSellPriceMult", ConfigHelper.getCostMultiplierOverrideDmods());
    }

    private void applyCostModifierToVanillaQuests() {

        if(ConfigHelper.isApplyBuySellCostMultToQuest())
        {
            SurplusShipHull.BASE_PRICE_MULT = 0.5f * (Global.getSettings().getFloat("shipSellPriceMult") + Global.getSettings().getFloat("shipBuyPriceMult"));
            HijackingMission.BASE_PRICE_MULT = Global.getSettings().getFloat("shipSellPriceMult") / 2f;
            HandMeDownFreighter.BASE_PRICE_MULT = Global.getSettings().getFloat("shipSellPriceMult");
        }
    }
}
