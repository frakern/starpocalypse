package starpocalypse.submarket;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommodityMarketDataAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.submarkets.OpenMarketPlugin;
import lombok.extern.log4j.Log4j;
import starpocalypse.helper.CargoUtils;
import starpocalypse.helper.ConfigHelper;
import starpocalypse.helper.SubmarketUtils;

@Log4j
public class RegulatedOpenMarket extends OpenMarketPlugin {

    private String location;
    private int econStanding = 0;
    private int contactStanding = 0;

    @Override
    public void init(SubmarketAPI submarket) {
        super.init(submarket);
        location = SubmarketUtils.getLocation(submarket);
    }

    @Override
    public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
        boolean vanillaIllegal = super.isIllegalOnSubmarket(commodityId, action);

        if (!ConfigHelper.isFreePortOpenMarketRegulations() && market.isFreePort()) {
            return false;
        }

        CommoditySpecAPI commodity =  market.getCommodityData(commodityId).getCommodity();
        if (!ConfigHelper.wantsRegulation(market.getFactionId()) || commodity.isMeta()) {
            return vanillaIllegal;
        }

        if(vanillaIllegal || action == TransferAction.PLAYER_SELL)
        {
            return vanillaIllegal;
        }

        if (isAlwaysLegal(commodity.getName())) {
            return false;
        }
        if (isAlwaysIllegal(commodity.getName())) {
            return true;
        }

        return !StandingMarketRegulation.legalWithStanding(commodity.getName(), econStanding + contactStanding + getCommodityStandingModifier(commodityId));
    }

    @Override
    public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
        if (!ConfigHelper.wantsRegulation(market.getFactionId())) {
            return super.isIllegalOnSubmarket(stack, action);
        }
        boolean vanillaIllegal = super.isIllegalOnSubmarket(stack, action);

        if (!ConfigHelper.isFreePortOpenMarketRegulations() && market.isFreePort()) {
            return false;
        }

        if(vanillaIllegal || action == TransferAction.PLAYER_SELL)
        {
            return vanillaIllegal;
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

        return !StandingMarketRegulation.legalWithStanding(stack, econStanding + contactStanding + (stack.isCommodityStack() ? getCommodityStandingModifier(stack.getCommodityId()) : 0));
    }

    @Override
    public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
        if (!ConfigHelper.wantsRegulation(market.getFactionId())) {
            return super.isIllegalOnSubmarket(member, action);
        }

        boolean vanillaIllegal = super.isIllegalOnSubmarket(member, action);

        if (!ConfigHelper.isFreePortOpenMarketRegulations() && market.isFreePort()) {
            return false;
        }

        if(vanillaIllegal || action == TransferAction.PLAYER_SELL)
        {
            return vanillaIllegal;
        }

        String hullName = StandingMarketRegulation.getHullName(member);
        if (isAlwaysLegal(hullName)) {
            return false;
        }
        if (isAlwaysIllegal(hullName)) {
            return true;
        }
        if (!isSignificant(member)) {
            return false;
        }
        return !StandingMarketRegulation.legalWithStanding(member, econStanding + contactStanding);
    }

    @Override
    public void updateCargoPrePlayerInteraction()
    {
        calculateStandings();
        super.updateCargoPrePlayerInteraction();
    }

    @Override
    public String getIllegalTransferText(CargoStackAPI stack, SubmarketPlugin.TransferAction action) {
        int econStandingBefore = econStanding;
        int requiredStanding = StandingMarketRegulation.getRequiredStanding(stack);
        econStanding = requiredStanding + Math.abs(contactStanding);
        if(super.isIllegalOnSubmarket(stack, action)) // This super call is not working out the way I want so lets make sure standing doesn't cause issues
        {
            econStanding = econStandingBefore;
            return super.getIllegalTransferText(stack, action);
        }
        else
        {
            econStanding = econStandingBefore;
            int standing = (econStanding + contactStanding);
            return "Standing: " + standing + " Required: " + (requiredStanding - (stack.isCommodityStack() ? getCommodityStandingModifier(stack.getCommodityId()) : 0));
        }
    }

    @Override
    public String getIllegalTransferText(FleetMemberAPI ship, SubmarketPlugin.TransferAction action) {
        int econStandingBefore = econStanding;
        int requiredStanding = StandingMarketRegulation.getRequiredStanding(ship);
        econStanding = requiredStanding + Math.abs(contactStanding);
        if(super.isIllegalOnSubmarket(ship, action))
        {
            econStanding = econStandingBefore;
            return super.getIllegalTransferText(ship, action);
        }
        else
        {
            econStanding = econStandingBefore;
            int standing = (econStanding + contactStanding);
            return "Standing: " + standing + " Required: " + requiredStanding;
        }
    }

    private int getCommodityStandingModifier(String commodityID)
    {
        // Cant really get how much shortage there is relative to market size. So now it is just if there is a shortage/excess
        int excess = Math.min(market.getCommodityData(commodityID).getExcessQuantity(), 1);
        int shortage = Math.min(market.getCommodityData(commodityID).getDeficitQuantity(), 1);
        return excess * ConfigHelper.getStandingBonusSurplus() + shortage * ConfigHelper.getStandingBonusShortage();

    }

    private void calculateStandings()
    {
        econStanding = StandingMarketRegulation.getEconStanding(submarket) + ConfigHelper.getFactionStandingBonus(submarket.getFaction().getId());
        contactStanding = StandingMarketRegulation.getContactStanding(StandingMarketRegulation.getBestContactOfFaction(submarket.getFaction()));
    }

    private boolean isAlwaysIllegal(String name) {
        return ConfigHelper.getRegulationLegal().hasNot(name);
    }

    private boolean isAlwaysLegal(String name) {
        return ConfigHelper.getRegulationLegal().has(name);
    }


    private boolean isSignificant(CargoStackAPI stack) {
        return CargoUtils.getTier(stack) > ConfigHelper.getRegulationMaxTier();
    }

    private boolean isSignificant(FleetMemberAPI member) {
        return member.getFleetPointCost() > ConfigHelper.getRegulationMaxFP();
    }

}
