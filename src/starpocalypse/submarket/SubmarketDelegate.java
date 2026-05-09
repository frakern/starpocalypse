package starpocalypse.submarket;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.util.Misc;
import exerelin.campaign.AllianceManager;
import exerelin.campaign.PlayerFactionStore;
import lombok.extern.log4j.Log4j;
import starpocalypse.helper.CargoUtils;
import starpocalypse.helper.ConfigHelper;

@Log4j
public class SubmarketDelegate {

    private final BaseSubmarketPlugin submarketPlugin;
    String location;
    int marketModifer = 0;
    int factionModifier = 0;
    int contactModifier = 0;

    public SubmarketDelegate(BaseSubmarketPlugin submarketPlugin) {
        this.submarketPlugin = submarketPlugin;
    }

    public void calculateReputationModifiers() {
        marketModifer = getMarketModifier(submarketPlugin.getSubmarket());
        factionModifier = getFactionModifier(submarketPlugin.getSubmarket().getFaction());
        contactModifier = getContactModifier(getBestContactOfFaction(submarketPlugin.getSubmarket().getFaction()));
    }

    private int getMarketModifier(SubmarketAPI market) {
        double marketModifier = 0;

        String stabilityKey = String.format("%.0f", market.getMarket().getStabilityValue());
        if (
            ConfigHelper.isReputationBonusAtLowStability() &&
            ConfigHelper.getReputationStability().containsKey(stabilityKey) &&
            !submarketPlugin.isBlackMarket()
        ) {
            marketModifier += Integer.parseInt(ConfigHelper.getReputationStability().get(stabilityKey));
        }

        return (int) marketModifier;
    }

    private int getFactionModifier(FactionAPI faction) {
        double factionModifier = 0;

        if (hasCommission(faction)) {
            factionModifier += ConfigHelper.getReputationCommissionBonus();
        }

        factionModifier += ConfigHelper.getFactionReputationModifier(faction.getId());

        return (int) factionModifier;
    }

    private int getContactModifier(PersonAPI contact) {
        double contactModifier = 0;
        if (contact != null) {
            contactModifier += ConfigHelper.getReputationContactFactor() * contact.getRelToPlayer().getRepInt();
            switch (contact.getImportance().getDisplayName()) {
                case ("Very Low"):
                    contactModifier += ConfigHelper.getReputationContactBonusVeryLow();
                    break;
                case ("Low"):
                    contactModifier += ConfigHelper.getReputationContactBonusLow();
                    break;
                case ("Medium"):
                    contactModifier += ConfigHelper.getReputationContactBonusMedium();
                    break;
                case ("High"):
                    contactModifier += ConfigHelper.getReputationContactBonusHigh();
                    break;
                case ("Very High"):
                    contactModifier += ConfigHelper.getReputationContactBonusVeryHigh();
                    break;
                default:
                    break;
            }
        }
        return (int) contactModifier;
    }

    private PersonAPI getBestContactOfFaction(FactionAPI faction) {
        PersonAPI candidate = null;
        int best_bonus = -25000;
        for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(ContactIntel.class)) {
            if (
                !intel.isEnding() &&
                !intel.isEnded() &&
                ((ContactIntel) intel).getState() != ContactIntel.ContactState.POTENTIAL &&
                ((ContactIntel) intel).getState() != ContactIntel.ContactState.SUSPENDED &&
                ((ContactIntel) intel).getState() != ContactIntel.ContactState.LOST_CONTACT_DECIV &&
                ((ContactIntel) intel).getState() != ContactIntel.ContactState.LOST_CONTACT &&
                ((ContactIntel) intel).getPerson().getFaction().equals(faction)
            ) {
                int contactReputation = getContactModifier(((ContactIntel) intel).getPerson());
                if (candidate == null || best_bonus < contactReputation) {
                    candidate = ((ContactIntel) intel).getPerson();
                    best_bonus = contactReputation;
                }
            }
        }
        return candidate;
    }

    public boolean isIllegalOnSubmarket(
        String commodityId,
        SubmarketPlugin.TransferAction action,
        boolean vanillaIllegal
    ) {
        CommoditySpecAPI commodity = submarketPlugin.getMarket().getCommodityData(commodityId).getCommodity();
        if (!ConfigHelper.wantsRegulation(submarketPlugin.getMarket().getFactionId()) || commodity.isMeta()) {
            return vanillaIllegal;
        }

        if (vanillaIllegal || action == SubmarketPlugin.TransferAction.PLAYER_SELL) {
            return vanillaIllegal;
        }

        if (
            submarketPlugin.isOpenMarket() &&
            !ConfigHelper.isFreePortOpenMarketRegulations() &&
            submarketPlugin.getMarket().isFreePort()
        ) {
            return false;
        }

        if (isAlwaysLegal(commodity.getName())) {
            return false;
        }
        if (isAlwaysIllegal(commodity.getName())) {
            return true;
        }

        int reputation = submarketPlugin.getMarket().getFaction().getRelToPlayer().getRepInt();

        return reputation < getRequiredReputation(commodity.getName());
    }

    public boolean isIllegalOnSubmarket(
        CargoStackAPI stack,
        SubmarketPlugin.TransferAction action,
        boolean vanillaIllegal
    ) {
        if (!ConfigHelper.wantsRegulation(submarketPlugin.getMarket().getFactionId())) {
            return vanillaIllegal;
        }

        if (vanillaIllegal || action == SubmarketPlugin.TransferAction.PLAYER_SELL) {
            return vanillaIllegal;
        }

        if (
            submarketPlugin.isOpenMarket() &&
            !ConfigHelper.isFreePortOpenMarketRegulations() &&
            submarketPlugin.getMarket().isFreePort()
        ) {
            return false;
        }

        String stackName = stack.getDisplayName();
        if (isAlwaysLegal(stackName)) {
            return false;
        }
        if (isAlwaysIllegal(stackName)) {
            return true;
        }
        if (!isSignificant(stack)) {
            return false;
        }

        int reputation = submarketPlugin.getMarket().getFaction().getRelToPlayer().getRepInt();

        return reputation < getRequiredReputation(stack);
    }

    public boolean isIllegalOnSubmarket(
        FleetMemberAPI member,
        SubmarketPlugin.TransferAction action,
        boolean vanillaIllegal
    ) {
        if (!ConfigHelper.wantsRegulation(submarketPlugin.getMarket().getFactionId())) {
            return vanillaIllegal;
        }

        if (vanillaIllegal || action == SubmarketPlugin.TransferAction.PLAYER_SELL) {
            return vanillaIllegal;
        }

        if (
            submarketPlugin.isOpenMarket() &&
            !ConfigHelper.isFreePortOpenMarketRegulations() &&
            submarketPlugin.getMarket().isFreePort()
        ) {
            return false;
        }

        String hullName = getHullName(member);
        if (isAlwaysLegal(hullName)) {
            return false;
        }
        if (isAlwaysIllegal(hullName)) {
            return true;
        }
        if (!isSignificant(member)) {
            return false;
        }

        int reputation = submarketPlugin.getMarket().getFaction().getRelToPlayer().getRepInt();

        return reputation < getRequiredReputation(member);
    }

    public int getRequiredReputation(CargoStackAPI stack) {
        int requiredReputation = 0;
        String itemKey = stack.getDisplayName();
        if (ConfigHelper.getReputationIndividual().containsKey(itemKey)) {
            requiredReputation += Integer.parseInt(ConfigHelper.getReputationIndividual().get(itemKey));
        } else {
            int tier = CargoUtils.getTier(stack);

            switch (tier) {
                case (0):
                    requiredReputation += ConfigHelper.getReputationWeaponT0();
                    break;
                case (1):
                    requiredReputation += ConfigHelper.getReputationWeaponT1();
                    break;
                case (2):
                    requiredReputation += ConfigHelper.getReputationWeaponT2();
                    break;
                case (3):
                    requiredReputation += ConfigHelper.getReputationWeaponT3();
                    break;
                case (4):
                    requiredReputation += ConfigHelper.getReputationWeaponT4();
                    break;
                default:
                    break;
            }
        }

        requiredReputation += ConfigHelper.getReputationMinimumSelling();
        requiredReputation -= marketModifer;
        requiredReputation -= factionModifier;
        requiredReputation -= contactModifier;
        requiredReputation -= (stack.isCommodityStack() ? getCommodityShortageModifier(stack.getCommodityId()) : 0);

        return requiredReputation;
    }

    public int getRequiredReputation(FleetMemberAPI member) {
        int requiredReputation = 0;
        String itemKey = getHullName(member);
        if (ConfigHelper.getReputationIndividual().containsKey(itemKey)) {
            requiredReputation += Integer.parseInt(ConfigHelper.getReputationIndividual().get(itemKey));
        } else if (ConfigHelper.isReputationShipLogarithmic()) {
            if (isCivilian(member.getVariant())) {
                requiredReputation += ConfigHelper.getReputationShipCivilian();
            } else {
                int fleetPoints = Math.max(1, member.getFleetPointCost());
                float scale = 60f;
                float minimum = 0f;
                requiredReputation += Math.round(scale * (float) Math.log(fleetPoints) + minimum);
            }
        } else {
            if (isCivilian(member.getVariant())) {
                requiredReputation += ConfigHelper.getReputationShipCivilian();
            } else if (member.isFrigate()) {
                requiredReputation += ConfigHelper.getReputationShipFrigate();
            } else if (member.isDestroyer()) {
                requiredReputation += ConfigHelper.getReputationShipDestroyer();
            } else if (member.isCruiser()) {
                requiredReputation += ConfigHelper.getReputationShipCruiser();
            } else if (member.isCapital()) {
                requiredReputation += ConfigHelper.getReputationShipCapital();
            }
        }

        switch (DModManager.getNumDMods(member.getVariant())) {
            case 0:
                requiredReputation += (int) (requiredReputation * 0.25f);
                break;
            case 1:
                requiredReputation += (int) (requiredReputation * 0.1f);
                break;
            case 2:
                requiredReputation += 0;
                break;
            case 3:
                requiredReputation -= (int) (requiredReputation * 0.1f);
                break;
            case 4:
                requiredReputation -= (int) (requiredReputation * 0.2f);
                break;
            case 5:
                requiredReputation -= (int) (requiredReputation * 0.4f);
                break;
        }

        requiredReputation += ConfigHelper.getReputationMinimumSelling();
        requiredReputation -= marketModifer;
        requiredReputation -= factionModifier;
        requiredReputation -= contactModifier;

        return requiredReputation;
    }

    public int getRequiredReputation(String commodityName) {
        int requiredReputation = 0;
        if (ConfigHelper.getReputationIndividual().containsKey(commodityName)) {
            requiredReputation += Integer.parseInt(ConfigHelper.getReputationIndividual().get(commodityName));
        }

        requiredReputation += ConfigHelper.getReputationMinimumSelling();
        requiredReputation -= marketModifer;
        requiredReputation -= factionModifier;
        requiredReputation -= contactModifier;

        return requiredReputation;
    }

    public String getHullName(FleetMemberAPI member) {
        ShipHullSpecAPI hullSpec = member.getHullSpec().getBaseHull();
        if (hullSpec == null) {
            hullSpec = member.getHullSpec();
        }
        return hullSpec.getHullName();
    }

    public boolean isCivilian(ShipVariantAPI variant) {
        return (
            variant.hasHullMod(HullMods.CIVGRADE) || variant.getHints().contains(ShipHullSpecAPI.ShipTypeHints.CIVILIAN)
        );
    }

    public boolean hasCommission(FactionAPI faction) {
        if (ConfigHelper.hasNexerelin()) {
            return hasCommissionNex(faction);
        }
        return faction.getId().equals(Misc.getCommissionFactionId());
    }

    private boolean hasCommissionNex(FactionAPI faction) {
        String commissionFaction = Misc.getCommissionFactionId();
        if (hasCommissionNex(commissionFaction, faction)) {
            return true;
        }
        if (hasCommissionNex(PlayerFactionStore.getPlayerFactionId(), faction)) {
            return true;
        }
        return faction.getId().equals(commissionFaction);
    }

    private boolean hasCommissionNex(String factionId, FactionAPI faction) {
        if (factionId == null) {
            return false;
        }
        return AllianceManager.areFactionsAllied(factionId, faction.getId());
    }

    public int getCommodityShortageModifier(String commodityID) {
        // Cant really get how much shortage there is relative to market size. So now it is just if there is a shortage/excess
        int excess = Math.min(submarketPlugin.getMarket().getCommodityData(commodityID).getExcessQuantity(), 1);
        int shortage = Math.min(submarketPlugin.getMarket().getCommodityData(commodityID).getDeficitQuantity(), 1);
        return excess * ConfigHelper.getReputationBonusSurplus() + shortage * ConfigHelper.getReputationBonusShortage();
    }

    public boolean isAlwaysIllegal(String name) {
        return ConfigHelper.getRegulationLegal().hasNot(name);
    }

    public boolean isAlwaysLegal(String name) {
        return ConfigHelper.getRegulationLegal().has(name);
    }

    public boolean isSignificant(CargoStackAPI stack) {
        return CargoUtils.getTier(stack) > ConfigHelper.getRegulationMaxTier();
    }

    public boolean isSignificant(FleetMemberAPI member) {
        return member.getFleetPointCost() > ConfigHelper.getRegulationMaxFP();
    }
}
