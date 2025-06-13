package starpocalypse.helper;

import com.fs.starfarer.api.Global;
import lombok.Getter;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import starpocalypse.config.SimpleMap;
import starpocalypse.config.SimpleSet;
import java.util.HashMap;
import java.util.Map;

public class ConfigHelper {

    private static Map<String,Object> originalVanillaSetting = new HashMap<>();

    @Getter
    private static float blackMarketFenceCut = 0.5f;

    @Getter
    private static int minDmods = 2;

    @Getter
    private static int maxDmods = 4;

    private static boolean regulation = true;

    @Getter
    private static boolean militaryNoCommission = false;

    @Getter
    private static float regulationMaxTier = 0;

    @Getter
    private static float regulationMaxFP = 0;

    private static final SimpleMap regulationFaction = new SimpleMap("faction", "standing", "militaryRegulationFaction.csv");

    @Getter
    private static final SimpleSet regulationLegal = new SimpleSet("name", "militaryRegulationLegal.csv");

    @Getter
    private static final SimpleMap standingStability = new SimpleMap(
        "stability",
        "standing",
        "militaryRegulationStability.csv"
    );

    @Getter
    private static final SimpleMap standingIndividual = new SimpleMap(
        "item_or_hull_name",
        "standing",
        "militaryRegulationSpecialStanding.csv"
    );

    @Getter
    private static boolean shyBlackMarket = false;

    @Getter
    private static boolean blackMarketRequiresContact = false;

    @Getter
    private static boolean blackMarketGoodStuffRequiresContact = false;

    @Getter
    private static int blackMarketWeaponT0 = 0;

    @Getter
    private static int blackMarketWeaponT1 = 0;

    @Getter
    private static int blackMarketWeaponT2= 0;

    @Getter
    private static int blackMarketWeaponT3= 0;

    @Getter
    private static int blackMarketWeaponT4= 0;

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
    private static boolean standingBonusAtLowStability = true;

    @Getter
    private static int standingMinimumSelling = -100;

    @Getter
    private static int standingBonusSurplus = 0;

    @Getter
    private static int standingBonusShortage = 0;

    @Getter
    private static double standingContactFactor = 1f;

    @Getter
    private static double standingFactionFactor = 1f;

    @Getter
    private static int standingContactBonusNoContact = 0;

    @Getter
    private static int standingContactBonusVeryLow = 0;

    @Getter
    private static int standingContactBonusLow = 0;

    @Getter
    private static int standingContactBonusMedium = 0;

    @Getter
    private static int standingContactBonusHigh = 0;

    @Getter
    private static int standingContactBonusVeryHigh = 0;

    @Getter
    private static int standingCommissionBonus = 0;

    @Getter
    private static int standingWeaponT0 = 0;

    @Getter
    private static int standingWeaponT1 = 0;

    @Getter
    private static int standingWeaponT2= 0;

    @Getter
    private static int standingWeaponT3= 0;

    @Getter
    private static int standingWeaponT4= 0;

    @Getter
    private static int standingShipCivilian = 0;

    @Getter
    private static int standingShipFrigate = 0;

    @Getter
    private static int standingShipDestroyer = 0;

    @Getter
    private static int standingShipCruiser = 150;

    @Getter
    private static int standingShipCapital = 200;

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
        return regulation &&
                (regulationFaction.containsKey(factionId) || (regulationFaction.containsKey("all") && ! regulationFaction.containsKey("!"+factionId)))  ;
    }

    public static int getFactionStandingBonus(String factionId)
    {
        if(regulationFaction.containsKey(factionId))
        {
            return Integer.parseInt(regulationFaction.get(factionId));
        }
        else if(regulationFaction.containsKey("all") && !regulationFaction.containsKey("!"+factionId))
        {
            return Integer.parseInt(regulationFaction.get("all"));
        }
        else
        {
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
        regulation = settings.optBoolean("militaryRegulations", true);
        militaryNoCommission = settings.optBoolean("militaryNoCommission", false);
        regulationMaxFP = settings.optInt("regulationMaxLegalFP", 0);
        regulationMaxTier = settings.optInt("regulationMaxLegalTier", 0);
        shyBlackMarket = settings.optBoolean("shyBlackMarket", true);
        blackMarketRequiresContact = settings.optBoolean("blackMarketRequiresContact", true);
        blackMarketGoodStuffRequiresContact = settings.optBoolean("blackMarketGoodStuffRequiresContact", true);

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


        standingBonusAtLowStability = settings.optBoolean("standingBonusAtLowStability", true);

        standingMinimumSelling = settings.optInt("standingMinimumSelling", -100);
        standingBonusSurplus = settings.optInt("standingBonusSurplus", 0);
        standingBonusShortage = settings.optInt("standingBonusShortage", 0);
        standingContactFactor = settings.optDouble("standingContactFactor",1.0);
        standingFactionFactor = settings.optDouble("standingFactionFactor",1.0);

        standingContactBonusNoContact = settings.optInt("standingContactBonusNoContact", 0);
        standingContactBonusVeryLow = settings.optInt("standingContactBonusVeryLow", 0);
        standingContactBonusLow = settings.optInt("standingContactBonusLow", 0);
        standingContactBonusMedium = settings.optInt("standingContactBonusMedium", 0);
        standingContactBonusHigh = settings.optInt("standingContactBonusHigh", 0);
        standingContactBonusVeryHigh = settings.optInt("standingContactBonusVeryHigh", 0);
        standingCommissionBonus = settings.optInt("standingCommissionBonus", 0);

        standingWeaponT0 = settings.optInt("standingWeaponT0", 0);
        standingWeaponT1 = settings.optInt("standingWeaponT1", 0);
        standingWeaponT2 = settings.optInt("standingWeaponT2", 0);
        standingWeaponT3 = settings.optInt("standingWeaponT3", 0);
        standingWeaponT4 = settings.optInt("standingWeaponT4", 0);

        standingShipCivilian = settings.optInt("standingShipCivilian", 0);
        standingShipFrigate = settings.optInt("standingShipFrigate", 0);
        standingShipDestroyer = settings.optInt("standingShipDestroyer", 0);
        standingShipCruiser = settings.optInt("standingShipCruiser", 0);
        standingShipCapital = settings.optInt("standingShipCapital", 0);

        applyBuySellCostMultToQuest = settings.optBoolean("applyBuySellCostMultToQuest", true);
        stingyNerfHullRestoration = settings.optBoolean("stingyNerfHullRestoration", true);

        stingyRecoveriesDerelicts = settings.optBoolean("stingyRecoveriesDerelicts", true);
        stingyRecoveriesCombat = settings.optBoolean("stingyRecoveriesCombat", true);
        stingyRecoveriesIncludePlayerShips = settings.optBoolean("stingyRecoveriesCombatIncludePlayerShips", true);
        stingyRecoveriesCombatPlayerShipsSize = settings.optInt("stingyRecoveriesCombatPlayerShipsSize", 1);

        stingyRecoveriesChanceFrigate = settings.optDouble("stingyRecoveriesChanceFrigate",1.0);
        stingyRecoveriesChanceDestroyer = settings.optDouble("stingyRecoveriesChanceDestroyer",1.0);
        stingyRecoveriesChanceCruiser = settings.optDouble("stingyRecoveriesChanceCruiser",1.0);
        stingyRecoveriesChanceCapital = settings.optDouble("stingyRecoveriesChanceCapital",1.0);


        stingyRecoveriesWeaponT0 = settings.optDouble("stingyWeaponWeaponT0", 1f);
        stingyRecoveriesWeaponT1 = settings.optDouble("stingyWeaponWeaponT1", 1f);
        stingyRecoveriesWeaponT2 = settings.optDouble("stingyWeaponWeaponT2", 1f);
        stingyRecoveriesWeaponT3 = settings.optDouble("stingyWeaponWeaponT3", 1f);
        stingyRecoveriesWeaponT4 = settings.optDouble("stingyWeaponWeaponT4", 1f);

        costMultiplierWeapon = (float) settings.optDouble("costMultiplierWeapon",1.0);
        costMultiplierShips = (float) settings.optDouble("costMultiplierShips",1.0);
        costMultiplierSellerProfitMargin = (float) settings.optDouble("costMultiplierSellerProfitMargin",1.0);
        costMultiplierOverrideDmods = (float) settings.optDouble("costMultiplierOverrideDmods",1.0);
    }


    public static void overwriteOriginalVanillaFloat(String setting, Float value)
    {
        if(!originalVanillaSetting.containsKey(setting))
        {
            originalVanillaSetting.put(setting, Global.getSettings().getFloat(setting));
        }
        Global.getSettings().setFloat(setting, value);
    }

    public static float getOriginalVanillaFloat(String setting)
    {
        if(!originalVanillaSetting.containsKey(setting))
        {
            originalVanillaSetting.put(setting, Global.getSettings().getFloat(setting));
        }
        return (float)originalVanillaSetting.get(setting);
    }


    private static void transparentMarket(JSONObject settings, Logger log) {
        if (settings.optBoolean("transparentMarket", true)) {
            float mult = (float) settings.optDouble("transparentMarketMult", 0.5);
            log.info("Setting transponder off market awareness mult to " + mult);
            Global.getSettings().setFloat("transponderOffMarketAwarenessMult", mult);
        }
    }
}
