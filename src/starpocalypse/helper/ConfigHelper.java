package starpocalypse.helper;

import com.fs.starfarer.api.Global;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import starpocalypse.config.SimpleMap;
import starpocalypse.config.SimpleSet;

public class ConfigHelper {

    private static Map<String, Object> originalVanillaSetting = new HashMap<>();

    @Getter
    private static float blackMarketFenceCut = 0.5f;

    @Getter
    private static int minDmods = 2;

    @Getter
    private static int maxDmods = 4;

    private static boolean regulation = true;

    @Getter
    private static boolean freePortOpenMarketRegulations = true;

    @Getter
    private static float regulationMaxTier = 0;

    @Getter
    private static float regulationMaxFP = 0;

    private static final SimpleMap regulationFaction = new SimpleMap(
        "faction",
        "reputationModifier",
        "militaryRegulationFaction.csv"
    );

    @Getter
    private static final SimpleSet regulationLegal = new SimpleSet("name", "militaryRegulationLegal.csv");

    @Getter
    private static final SimpleMap reputationStability = new SimpleMap(
        "stability",
        "reputationModifier",
        "militaryRegulationStability.csv"
    );

    @Getter
    private static final SimpleMap reputationIndividual = new SimpleMap(
        "item_or_hull_name",
        "reputation",
        "militaryRegulationSpecialReputation.csv"
    );

    @Getter
    private static boolean shyBlackMarket = false;

    @Getter
    private static boolean blackMarketRequiresContact = false;

    @Getter
    private static boolean blackMarketGoodStuffRequiresContact = false;

    @Getter
    private static boolean freePortBlackMarketRequiresContact = false;

    @Getter
    private static int blackMarketWeaponT0 = 0;

    @Getter
    private static int blackMarketWeaponT1 = 0;

    @Getter
    private static int blackMarketWeaponT2 = 0;

    @Getter
    private static int blackMarketWeaponT3 = 0;

    @Getter
    private static int blackMarketWeaponT4 = 0;

    @Getter
    private static int blackMarketShipCivilian = 0;

    @Getter
    private static int blackMarketShipFrigate = 0;

    @Getter
    private static int blackMarketShipDestroyer = 0;

    @Getter
    private static int blackMarketShipCruiser = 0;

    @Getter
    private static int blackMarketShipCapital = 0;

    @Getter
    private static boolean reputationBonusAtLowStability = true;

    @Getter
    private static int reputationMinimumSelling = -100;

    @Getter
    private static int reputationBonusSurplus = 0;

    @Getter
    private static int reputationBonusShortage = 0;

    @Getter
    private static double reputationContactFactor = 1f;

    @Getter
    private static int reputationContactBonusNoContact = 0;

    @Getter
    private static int reputationContactBonusVeryLow = 0;

    @Getter
    private static int reputationContactBonusLow = 0;

    @Getter
    private static int reputationContactBonusMedium = 0;

    @Getter
    private static int reputationContactBonusHigh = 0;

    @Getter
    private static int reputationContactBonusVeryHigh = 0;

    @Getter
    private static int reputationCommissionBonus = 0;

    @Getter
    private static int reputationWeaponT0 = 0;

    @Getter
    private static int reputationWeaponT1 = 0;

    @Getter
    private static int reputationWeaponT2 = 0;

    @Getter
    private static int reputationWeaponT3 = 0;

    @Getter
    private static int reputationWeaponT4 = 0;

    @Getter
    private static boolean reputationShipLogarthmic = true;

    @Getter
    private static int reputationShipCivilian = 0;

    @Getter
    private static int reputationShipFrigate = 0;

    @Getter
    private static int reputationShipDestroyer = 0;

    @Getter
    private static int reputationShipCruiser = 150;

    @Getter
    private static int reputationShipCapital = 200;

    @Getter
    private static boolean stingyRecoveriesDerelicts = true;

    @Getter
    private static boolean stingyRecoveriesCombat = true;

    @Getter
    private static boolean stingyRecoveriesIncludePlayerShips = true;

    @Getter
    private static int stingyRecoveriesCombatPlayerShipsSize = 1;

    @Getter
    private static double stingyRecoveriesChanceFrigate = 1;

    @Getter
    private static double stingyRecoveriesChanceDestroyer = 1;

    @Getter
    private static double stingyRecoveriesChanceCruiser = 1;

    @Getter
    private static double stingyRecoveriesChanceCapital = 1;

    @Getter
    private static double stingyRecoveriesWeaponT0 = 1f;

    @Getter
    private static double stingyRecoveriesWeaponT1 = 1f;

    @Getter
    private static double stingyRecoveriesWeaponT2 = 1f;

    @Getter
    private static double stingyRecoveriesWeaponT3 = 1f;

    @Getter
    private static double stingyRecoveriesWeaponT4 = 1f;

    @Getter
    static boolean applyBuySellCostMultToQuest = true;

    @Getter
    static boolean stingyNerfHullRestoration = true;

    @Getter
    private static float costMultiplierWeapon = 1;

    @Getter
    private static float costMultiplierShips = 1;

    @Getter
    private static float costMultiplierSellerProfitMargin = 0;

    @Getter
    private static float costMultiplierOverrideDmods = 0.3f;

    @Getter
    private static boolean disablePrismFreeport = false;

    @Getter
    private static final SimpleSet shyBlackMarketFaction = new SimpleSet("faction", "shyBlackMarketFaction.csv");

    @Getter
    private static final SimpleSet shipDamageFaction = new SimpleSet("faction", "shipDamageFaction.csv");

    @Getter
    private static final SimpleSet shipDamageSubmarket = new SimpleSet("submarket", "shipDamageSubmarket.csv");

    public static boolean hasNexerelin() {
        return Global.getSettings().getModManager().isModEnabled("nexerelin");
    }

    public static void init(JSONObject settings, Logger log) {
        loadConfig(settings);
        transparentMarket(settings, log);
    }

    public static boolean isUninstall() {
        JSONObject settings = Global.getSettings().getSettingsJSON();
        return !settings.optBoolean("hasStarpocalypse", false);
    }

    public static boolean wantsRegulation(String factionId) {
        return (
            regulation &&
            (
                regulationFaction.containsKey(factionId) ||
                (regulationFaction.containsKey("all") && !regulationFaction.containsKey("!" + factionId))
            )
        );
    }

    public static int getFactionReputationModifier(String factionId) {
        if (regulationFaction.containsKey(factionId)) {
            return Integer.parseInt(regulationFaction.get(factionId));
        } else if (regulationFaction.containsKey("all") && !regulationFaction.containsKey("!" + factionId)) {
            return Integer.parseInt(regulationFaction.get("all"));
        } else {
            return 0;
        }
    }

    @SuppressWarnings("PMD.AvoidReassigningParameters")
    private static int clamp(int value, int min, int max) {
        value = Math.max(value, min);
        value = Math.min(value, max);
        return value;
    }

    private static void loadConfig(JSONObject settings) {
        blackMarketFenceCut = (float) settings.optDouble("blackMarketFenceCut", 0.5);
        minDmods = clamp(settings.optInt("minimumDmods", 2), 1, 5);
        maxDmods = clamp(settings.optInt("maximumDmods", 4), minDmods, 5);
        regulation = settings.optBoolean("marketRegulations", true);
        freePortOpenMarketRegulations = settings.optBoolean("freePortOpenMarketRegulations", true);
        regulationMaxFP = settings.optInt("regulationMaxLegalFP", 0);
        regulationMaxTier = settings.optInt("regulationMaxLegalTier", 0);
        shyBlackMarket = settings.optBoolean("shyBlackMarket", true);
        blackMarketRequiresContact = settings.optBoolean("blackMarketRequiresContact", true);
        blackMarketGoodStuffRequiresContact = settings.optBoolean("blackMarketGoodStuffRequiresContact", true);
        freePortBlackMarketRequiresContact = settings.optBoolean("freePortBlackMarketRequiresContact", true);

        blackMarketWeaponT0 = settings.optInt("blackMarketWeaponT0", 0);
        blackMarketWeaponT1 = settings.optInt("blackMarketWeaponT1", 0);
        blackMarketWeaponT2 = settings.optInt("blackMarketWeaponT2", 0);
        blackMarketWeaponT3 = settings.optInt("blackMarketWeaponT3", 0);
        blackMarketWeaponT4 = settings.optInt("blackMarketWeaponT4", 0);

        blackMarketShipCivilian = settings.optInt("blackMarketShipCivilian", 0);
        blackMarketShipFrigate = settings.optInt("blackMarketShipFrigate", 0);
        blackMarketShipDestroyer = settings.optInt("blackMarketShipDestroyer", 0);
        blackMarketShipCruiser = settings.optInt("blackMarketShipCruiser", 0);
        blackMarketShipCapital = settings.optInt("blackMarketShipCapital", 0);

        reputationBonusAtLowStability = settings.optBoolean("reputationBonusAtLowStability", true);

        reputationMinimumSelling = settings.optInt("reputationMinimumSelling", -100);
        reputationBonusSurplus = settings.optInt("reputationBonusSurplus", 0);
        reputationBonusShortage = settings.optInt("reputationBonusShortage", 0);
        reputationContactFactor = settings.optDouble("reputationContactFactor", 1.0);

        reputationContactBonusNoContact = settings.optInt("reputationContactBonusNoContact", 0);
        reputationContactBonusVeryLow = settings.optInt("reputationContactBonusVeryLow", 0);
        reputationContactBonusLow = settings.optInt("reputationContactBonusLow", 0);
        reputationContactBonusMedium = settings.optInt("reputationContactBonusMedium", 0);
        reputationContactBonusHigh = settings.optInt("reputationContactBonusHigh", 0);
        reputationContactBonusVeryHigh = settings.optInt("reputationContactBonusVeryHigh", 0);
        reputationCommissionBonus = settings.optInt("reputationCommissionBonus", 0);

        reputationWeaponT0 = settings.optInt("reputationWeaponT0", 0);
        reputationWeaponT1 = settings.optInt("reputationWeaponT1", 0);
        reputationWeaponT2 = settings.optInt("reputationWeaponT2", 0);
        reputationWeaponT3 = settings.optInt("reputationWeaponT3", 0);
        reputationWeaponT4 = settings.optInt("reputationWeaponT4", 0);

        reputationShipLogarthmic = settings.optBoolean("reputationShipLogarthmic", true);
        reputationShipCivilian = settings.optInt("reputationShipCivilian", 0);
        reputationShipFrigate = settings.optInt("reputationShipFrigate", 0);
        reputationShipDestroyer = settings.optInt("reputationShipDestroyer", 0);
        reputationShipCruiser = settings.optInt("reputationShipCruiser", 0);
        reputationShipCapital = settings.optInt("reputationShipCapital", 0);

        applyBuySellCostMultToQuest = settings.optBoolean("applyBuySellCostMultToQuest", true);
        stingyNerfHullRestoration = settings.optBoolean("stingyNerfHullRestoration", true);

        stingyRecoveriesDerelicts = settings.optBoolean("stingyRecoveriesDerelicts", true);
        stingyRecoveriesCombat = settings.optBoolean("stingyRecoveriesCombat", true);
        stingyRecoveriesIncludePlayerShips = settings.optBoolean("stingyRecoveriesCombatIncludePlayerShips", true);
        stingyRecoveriesCombatPlayerShipsSize = settings.optInt("stingyRecoveriesCombatPlayerShipsSize", 1);

        stingyRecoveriesChanceFrigate = settings.optDouble("stingyRecoveriesChanceFrigate", 1.0);
        stingyRecoveriesChanceDestroyer = settings.optDouble("stingyRecoveriesChanceDestroyer", 1.0);
        stingyRecoveriesChanceCruiser = settings.optDouble("stingyRecoveriesChanceCruiser", 1.0);
        stingyRecoveriesChanceCapital = settings.optDouble("stingyRecoveriesChanceCapital", 1.0);

        stingyRecoveriesWeaponT0 = settings.optDouble("stingyWeaponWeaponT0", 1f);
        stingyRecoveriesWeaponT1 = settings.optDouble("stingyWeaponWeaponT1", 1f);
        stingyRecoveriesWeaponT2 = settings.optDouble("stingyWeaponWeaponT2", 1f);
        stingyRecoveriesWeaponT3 = settings.optDouble("stingyWeaponWeaponT3", 1f);
        stingyRecoveriesWeaponT4 = settings.optDouble("stingyWeaponWeaponT4", 1f);

        costMultiplierWeapon = (float) settings.optDouble("costMultiplierWeapon", 1.0);
        costMultiplierShips = (float) settings.optDouble("costMultiplierShips", 1.0);
        costMultiplierSellerProfitMargin = (float) settings.optDouble("costMultiplierSellerProfitMargin", 1.0);
        costMultiplierOverrideDmods = (float) settings.optDouble("costMultiplierOverrideDmods", 1.0);

        disablePrismFreeport = settings.optBoolean("removeHighEndSeller", false);
    }

    public static void overwriteOriginalVanillaFloat(String setting, Float value) {
        if (!originalVanillaSetting.containsKey(setting)) {
            originalVanillaSetting.put(setting, Global.getSettings().getFloat(setting));
        }
        Global.getSettings().setFloat(setting, value);
    }

    public static float getOriginalVanillaFloat(String setting) {
        if (!originalVanillaSetting.containsKey(setting)) {
            originalVanillaSetting.put(setting, Global.getSettings().getFloat(setting));
        }
        return (float) originalVanillaSetting.get(setting);
    }

    private static void transparentMarket(JSONObject settings, Logger log) {
        if (settings.optBoolean("transparentMarket", true)) {
            float mult = (float) settings.optDouble("transparentMarketMult", 0.5);
            log.info("Setting transponder off market awareness mult to " + mult);
            Global.getSettings().setFloat("transponderOffMarketAwarenessMult", mult);
        }
    }
}
