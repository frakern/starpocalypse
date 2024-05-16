package starpocalypse.submarket;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.util.Misc;
import exerelin.campaign.AllianceManager;
import exerelin.campaign.PlayerFactionStore;
import lombok.extern.log4j.Log4j;
import starpocalypse.helper.CargoUtils;
import starpocalypse.helper.ConfigHelper;
@Log4j
public class StandingMarketRegulation
{

    public static int getEconStanding(SubmarketAPI market)
    {
        double econStanding = 0;
        boolean hasCommission = hasCommission(market.getPlugin().isBlackMarket() ? Global.getSector().getFaction("pirates") : market.getFaction());

        econStanding += ConfigHelper.getStandingFactionFactor() * (market.getPlugin().isBlackMarket() ? Global.getSector().getFaction("pirates").getRelToPlayer().getRepInt() : market.getFaction().getRelToPlayer().getRepInt());

        if(hasCommission)
        {
            econStanding += ConfigHelper.getStandingCommissionBonus();

        }

        String stabilityKey = String.format("%.0f", market.getMarket().getStabilityValue());
        if(ConfigHelper.isStandingBonusAtLowStability() && ConfigHelper.getStandingStability().containsKey(stabilityKey) && !market.getPlugin().isBlackMarket())
        {
            econStanding += Integer.parseInt(ConfigHelper.getStandingStability().get(stabilityKey));;
        }

        return (int) econStanding;
    }

    public static int getContactStanding(PersonAPI contact)
    {
        double contactStanding = 0;
        if(contact != null)
        {
            contactStanding += ConfigHelper.getStandingContactFactor() * contact.getRelToPlayer().getRepInt();
            int contactLevelBonus = 0;
            switch (contact.getImportance().getDisplayName())
            {
                case("Very Low"):
                    contactLevelBonus = ConfigHelper.getStandingContactBonusVeryLow();
                    break;
                case("Low"):
                    contactLevelBonus = ConfigHelper.getStandingContactBonusLow();
                    break;
                case("Medium"):
                    contactLevelBonus = ConfigHelper.getStandingContactBonusMedium();
                    break;
                case("High"):
                    contactLevelBonus = ConfigHelper.getStandingContactBonusHigh();
                    break;
                case("Very High"):
                    contactLevelBonus = ConfigHelper.getStandingContactBonusVeryHigh();
                    break;
                default:
                    break;
            }
        }
        return (int) contactStanding;
    }

    public static PersonAPI getBestContactOfFaction(FactionAPI faction)
    {
        PersonAPI candidate = null;
        int best_bonus = -25000;
        for(IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(ContactIntel.class))
        {
            if (!intel.isEnding() &&
                    !intel.isEnded() &&
                    ((ContactIntel)intel).getState() != ContactIntel.ContactState.POTENTIAL &&
                    ((ContactIntel)intel).getState() != ContactIntel.ContactState.SUSPENDED &&
                    ((ContactIntel)intel).getState() != ContactIntel.ContactState.LOST_CONTACT_DECIV &&
                    ((ContactIntel)intel).getState() != ContactIntel.ContactState.LOST_CONTACT &&
                    ((ContactIntel)intel).getPerson().getFaction().equals(faction)
            )
            {
                int contactStanding = getContactStanding(((ContactIntel)intel).getPerson());
                if(candidate == null || best_bonus < contactStanding)
                {
                    candidate = ((ContactIntel)intel).getPerson();
                    best_bonus = contactStanding;
                }
            }
        }
        return candidate;
    }

    public static int getRequiredStanding(CargoStackAPI stack)
    {
        int requiredStanding = 0;
        String itemKey = stack.getDisplayName();
        if(ConfigHelper.getStandingIndividual().containsKey(itemKey))
        {
            requiredStanding -= Integer.parseInt(ConfigHelper.getStandingIndividual().get(itemKey));
        }

        int tier = CargoUtils.getTier(stack);

        switch (tier)
        {
            case(0):
                requiredStanding += ConfigHelper.getStandingWeaponT0();
                break;
            case(1):
                requiredStanding += ConfigHelper.getStandingWeaponT1();
                break;
            case(2):
                requiredStanding += ConfigHelper.getStandingWeaponT2();
                break;
            case(3):
                requiredStanding += ConfigHelper.getStandingWeaponT3();
                break;
            case(4):
                requiredStanding += ConfigHelper.getStandingWeaponT4();
                break;
            default:
                break;
        }
        requiredStanding += ConfigHelper.getStandingMinimumSelling();

        return requiredStanding;
    }

    public static boolean legalWithStanding(CargoStackAPI stack, int standing) {
        return standing >= getRequiredStanding(stack);
    }


    public static int getRequiredStanding(FleetMemberAPI ship)
    {
        int requiredStanding = 0;
        String itemKey = getHullName(ship);
        if(ConfigHelper.getStandingIndividual().containsKey(itemKey))
        {
            requiredStanding -= Integer.parseInt(ConfigHelper.getStandingIndividual().get(itemKey));
        }

        if(isCivilian(ship.getVariant()))
        {
            requiredStanding += ConfigHelper.getStandingShipCivilian();
        }
        else if(ship.isFrigate())
        {
            requiredStanding += ConfigHelper.getStandingShipFrigate();
        }
        else if(ship.isDestroyer())
        {
            requiredStanding += ConfigHelper.getStandingShipDestroyer();
        }
        else if(ship.isCruiser())
        {
            requiredStanding += ConfigHelper.getStandingShipCruiser();
        }
        else if(ship.isCapital())
        {
            requiredStanding += ConfigHelper.getStandingShipCapital();
        }

        requiredStanding += ConfigHelper.getStandingMinimumSelling();

        return requiredStanding;

    }

    public static boolean legalWithStanding(FleetMemberAPI ship, int standing) {
        return standing >= getRequiredStanding(ship);
    }

    public static int getRequiredStanding(String commodityName)
    {
        int requiredStanding = 0;
        if(ConfigHelper.getStandingIndividual().containsKey(commodityName))
        {
            requiredStanding -= Integer.parseInt(ConfigHelper.getStandingIndividual().get(commodityName));
        }
        requiredStanding += ConfigHelper.getStandingMinimumSelling();

        return requiredStanding;
    }

    public static boolean legalWithStanding(String commodityName, int standing) {
        return standing >= getRequiredStanding(commodityName);
    }

    public static String getHullName(FleetMemberAPI ship) {
        ShipHullSpecAPI hullSpec = ship.getHullSpec().getBaseHull();
        if (hullSpec == null) {
            hullSpec = ship.getHullSpec();
        }
        return hullSpec.getHullName();
    }

    public static boolean isCivilian(ShipVariantAPI variant) {
        return variant.hasHullMod(HullMods.CIVGRADE) || variant.getHints().contains(ShipHullSpecAPI.ShipTypeHints.CIVILIAN);
    }


    public static boolean hasCommission(FactionAPI faction) {
        if (ConfigHelper.hasNexerelin())
        {
            return hasCommissionNex(faction);
        }
        return faction.getId().equals(Misc.getCommissionFactionId());
    }

    private static boolean hasCommissionNex(FactionAPI faction) {
        String commissionFaction = Misc.getCommissionFactionId();
        if (hasCommissionNex(commissionFaction, faction)) {
            return true;
        }
        if (hasCommissionNex(PlayerFactionStore.getPlayerFactionId(), faction)) {
            return true;
        }
        return faction.getId().equals(commissionFaction);
    }

    private static boolean hasCommissionNex(String factionId, FactionAPI faction) {
        if (factionId == null) {
            return false;
        }
        return AllianceManager.areFactionsAllied(factionId, faction.getId());
    }

}
