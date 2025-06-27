package starpocalypse.submarket;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.submarkets.BlackMarketPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import java.util.Objects;
import starpocalypse.config.SimpleMap;
import starpocalypse.helper.CargoUtils;
import starpocalypse.helper.ConfigHelper;
import starpocalypse.helper.SubmarketUtils;

public class RegulatedBlackMarket extends BlackMarketPlugin {

    private String location;
    private int bestContactLevel;

    @Override
    public void init(SubmarketAPI submarket) {
        super.init(submarket);
        location = SubmarketUtils.getLocation(submarket);
    }

    @Override
    public void createTooltip(CoreUIAPI ui, TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltip(ui, tooltip, expanded);
        if (doesWantShyBlackMarket()) {
            tooltip.addPara(
                "Due to the heavy military presence, trading on Black Market " +
                "is only possible with the transponder turned off.",
                10
            );
        }
        if(ConfigHelper.isBlackMarketRequiresContact())
        {
            tooltip.addPara(
                    "You do not have any contacts that enable you to trade on the Black Market.",
                    10
            );
        }
    }

    @Override
    public float getTariff() {
        return ConfigHelper.getBlackMarketFenceCut() * market.getTariff().getModifiedValue();
    }

    @Override
    public boolean isEnabled(CoreUIAPI ui) {

        boolean result = true;

        if (doesWantShyBlackMarket()) {
            result &= !getTransponderState();
        }

        if (ConfigHelper.isBlackMarketRequiresContact())
        {
            boolean hasContactHere = false;
            for(PersonAPI person:submarket.getMarket().getPeopleCopy())
            {
                if(person.getFaction().getDisplayName().toLowerCase().contains("pirate"))
                {
                    hasContactHere |= ContactIntel.playerHasContact(person,false);
                }
            }
            result &= hasContactHere ;
        }

        if(ConfigHelper.hasNexerelin())
        {
            result |= market.hasCondition("nex_rebellion_condition");
        }

        return result;
    }

    private boolean doesWantShyBlackMarket() {
        if (!ConfigHelper.isShyBlackMarket()) {
            return false;
        }
        String faction = market.getFactionId();
        return ConfigHelper.getShyBlackMarketFaction().has(faction);
    }

    private boolean getTransponderState() {
        return Global.getSector().getPlayerFleet().isTransponderOn();
    }

    @Override
    public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {

        if (ConfigHelper.wantsRegulation(market.getFactionId())) {
            CommoditySpecAPI commodity =  market.getCommodityData(commodityId).getCommodity();
            if (isAlwaysLegal(commodity.getName())) {
                return false;
            }
        }
        return super.isIllegalOnSubmarket(commodityId, action);
    }

    public String getIllegalTransferText(CargoStackAPI stack, SubmarketPlugin.TransferAction action)
    {
        int bestContactLevelBefore = bestContactLevel;
        bestContactLevel = Integer.MAX_VALUE;
        if(super.isIllegalOnSubmarket(stack, action))
        {
            bestContactLevel = bestContactLevelBefore;
            return super.getIllegalTransferText(stack, action);
        }
        else
        {
            bestContactLevel = bestContactLevelBefore;
            return "Underworld Contact: " + getContactLevelText(bestContactLevel) + " Required: " + getContactLevelText(getContactLevelFor(stack));
        }
    }

    @Override
    public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {

        boolean vanillaIllegal = super.isIllegalOnSubmarket(stack, action);
        if (!ConfigHelper.wantsRegulation(market.getFactionId())) {
            return vanillaIllegal;
        }

        String stackName = stack.getDisplayName();
        if (isAlwaysLegal(stackName)) {
            return false;
        }

        if(vanillaIllegal)
            return true;

        if (isInsignificant(stack)) {
            return false;
        }

        if(ConfigHelper.isBlackMarketGoodStuffRequiresContact())
        {
            return bestContactLevel < getContactLevelFor(stack);
        }

        return false;
    }

    private int getContactLevelFor(CargoStackAPI stack)
    {
        int tier = CargoUtils.getTier(stack);
        return switch (tier) {
            case (0) -> ConfigHelper.getBlackMarketWeaponT0();
            case (1) -> ConfigHelper.getBlackMarketWeaponT1();
            case (2) -> ConfigHelper.getBlackMarketWeaponT2();
            case (3) -> ConfigHelper.getBlackMarketWeaponT3();
            case (4) -> ConfigHelper.getBlackMarketWeaponT4();
            default -> 0;
        };
    }

    public String getIllegalTransferText(FleetMemberAPI member, SubmarketPlugin.TransferAction action)
    {
        int bestContactLevelBefore = bestContactLevel;
        bestContactLevel = Integer.MAX_VALUE;
        if(super.isIllegalOnSubmarket(member, action))
        {
            bestContactLevel = bestContactLevelBefore;
            return super.getIllegalTransferText(member, action);
        }
        else
        {
            bestContactLevel = bestContactLevelBefore;
            return "Underworld Contact: " + getContactLevelText(bestContactLevel) + " Required: " + getContactLevelText(getContactLevelFor(member));
        }
    }

    @Override
    public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
        boolean vanillaIllegal = super.isIllegalOnSubmarket(member, action);

        if (!ConfigHelper.wantsRegulation(market.getFactionId())) {
            return super.isIllegalOnSubmarket(member, action);
        }

        String hullName = StandingMarketRegulation.getHullName(member);
        if (isAlwaysLegal(hullName)) {
            return false;
        }

        if(vanillaIllegal)
            return true;


        if (isInsignificant(member)) {
            return false;
        }

        if(ConfigHelper.isBlackMarketGoodStuffRequiresContact())
        {
            return bestContactLevel < getContactLevelFor(member);
        }
        return false;
    }

    private int getContactLevelFor(FleetMemberAPI member)
    {
        if(StandingMarketRegulation.isCivilian(member.getVariant()))
        {
            return ConfigHelper.getBlackMarketShipCivilian();
        }
        else if(member.isFrigate())
        {
            return ConfigHelper.getBlackMarketShipFrigate();
        }
        else if(member.isDestroyer())
        {
            return ConfigHelper.getBlackMarketShipDestroyer();
        }
        else if(member.isCruiser())
        {
            return ConfigHelper.getBlackMarketShipCruiser();
        }
        else if(member.isCapital())
        {
            return ConfigHelper.getBlackMarketShipCapital();
        }
        return 0;
    }

    @Override
    public void updateCargoPrePlayerInteraction()
    {
        super.updateCargoPrePlayerInteraction();
        if(ConfigHelper.isBlackMarketGoodStuffRequiresContact())
        {
            getBestContact();
        }
    }


    private void getBestContact()
    {
        bestContactLevel = 0;
        for(PersonAPI person:submarket.getMarket().getPeopleCopy())
        {
            if(person.hasTag("underworld"))
            {
                if(ContactIntel.playerHasContact(person,false)) {

                    switch (person.getImportance().getDisplayName())
                    {
                        case("Very Low"):
                            bestContactLevel = Math.max(1, bestContactLevel);
                            break;
                        case("Low"):
                            bestContactLevel = Math.max(2, bestContactLevel);
                            break;
                        case("Medium"):
                            bestContactLevel = Math.max(3, bestContactLevel);
                            break;
                        case("High"):
                            bestContactLevel = Math.max(4, bestContactLevel);
                            break;
                        case("Very High"):
                            bestContactLevel = Math.max(5, bestContactLevel);
                            break;
                        default:
                            bestContactLevel = Math.max(0, bestContactLevel);
                            break;
                    }
                }
            }
        }
    }

    private String getContactLevelText(int level)
    {
        return switch (level) {
            case (0) -> "None";
            case (1) -> "Very Low";
            case (2) -> "Low";
            case (3) -> "Medium";
            case (4) -> "High";
            case (5) -> "Very High";
            default -> "Boss";
        };
    }


    private boolean isAlwaysLegal(String name) {
        return ConfigHelper.getRegulationLegal().has(name);
    }

    private boolean isInsignificant(CargoStackAPI stack) {
        return CargoUtils.getTier(stack) <= ConfigHelper.getRegulationMaxTier();
    }

    private boolean isInsignificant(FleetMemberAPI member) {
        return member.getFleetPointCost() <= ConfigHelper.getRegulationMaxFP();
    }

    public String getTariffTextOverride() {
        return "Bribes";
    }
}
