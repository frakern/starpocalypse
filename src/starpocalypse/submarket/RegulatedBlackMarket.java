package starpocalypse.submarket;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.submarkets.BlackMarketPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import java.util.Objects;
import starpocalypse.config.SimpleMap;
import starpocalypse.helper.CargoUtils;
import starpocalypse.helper.ConfigHelper;
import starpocalypse.helper.SubmarketUtils;

public class RegulatedBlackMarket extends BlackMarketPlugin {

    private String location;

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
    }

    @Override
    public float getTariff() {
        return ConfigHelper.getBlackMarketFenceCut() * market.getTariff().getModifiedValue();
    }

    @Override
    public boolean isEnabled(CoreUIAPI ui) {
        if (doesWantShyBlackMarket()) {
            return !getTransponderState();
        }
        return true;
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
        if (!ConfigHelper.wantsRegulation(market.getFactionId())) {
            return super.isIllegalOnSubmarket(commodityId, action);
        }
        if (isAlwaysLegal(commodityId)) {
            return false;
        }
        if (isSignificant(commodityId) && isStabilityIllegal(commodityId)) {
            return true;
        }
        return super.isIllegalOnSubmarket(commodityId, action);
    }

    @Override
    public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
        if (!ConfigHelper.wantsRegulation(market.getFactionId())) {
            return super.isIllegalOnSubmarket(stack, action);
        }
        String stackName = stack.getDisplayName();
        if (isAlwaysLegal(stackName)) {
            return false;
        }
        if (stack.isCommodityStack()) {
            return isIllegalOnSubmarket((String) stack.getData(), action);
        }
        if (isInsignificant(stack)) {
            return false;
        }
        if (isStabilityIllegal(ConfigHelper.getRegulationStabilityItem(), stack.getBaseValuePerUnit())) {
            return true;
        }
        return super.isIllegalOnSubmarket(stack, action);
    }

    @Override
    public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
        if (!ConfigHelper.wantsRegulation(market.getFactionId())) {
            return super.isIllegalOnSubmarket(member, action);
        }
        String hullName = getHullName(member);
        if (isAlwaysLegal(hullName)) {
            return false;
        }
        if (isCivilian(member.getVariant())) {
            return false;
        }
        if (isInsignificant(member)) {
            return false;
        }
        if (isStabilityIllegal(ConfigHelper.getRegulationStabilityShip(), member.getBaseValue())) {
            return true;
        }
        return super.isIllegalOnSubmarket(member, action);
    }

    public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
        return "High stability prevents sale on black market.";
    }

    public String getIllegalTransferText(FleetMemberAPI member, TransferAction action) {
        if (action == TransferAction.PLAYER_BUY) {
            return "Illegal to buy";
        } else {
            return this.isFreeTransfer() ? "Illegal to store" : "Cannot sell";
        }
    }

    @Override
    public void updateCargoPrePlayerInteraction() {
        super.updateCargoPrePlayerInteraction();
        if (ConfigHelper.wantsRegulation(market.getFactionId())) {
            removeItems(submarket.getCargo());
            removeShips(submarket.getCargo().getMothballedShips());
        }
    }

    private String getHullName(FleetMemberAPI ship) {
        ShipHullSpecAPI hullSpec = ship.getHullSpec().getBaseHull();
        if (hullSpec == null) {
            hullSpec = ship.getHullSpec();
        }
        return hullSpec.getHullName();
    }

    private boolean isAlwaysLegal(String name) {
        return ConfigHelper.getRegulationLegal().has(name);
    }

    private boolean isCivilian(ShipVariantAPI variant) {
        return (
            variant.hasHullMod(HullMods.CIVGRADE) || variant.getHints().contains(ShipHullSpecAPI.ShipTypeHints.CIVILIAN)
        );
    }

    private boolean isSignificant(String commodityId) {
        CommodityOnMarketAPI com = market.getCommodityData(commodityId);
        return com.getCommodity().getTags().contains(Commodities.TAG_MILITARY);
    }

    private boolean isInsignificant(CargoStackAPI stack) {
        return CargoUtils.getTier(stack) <= ConfigHelper.getRegulationMaxTier();
    }

    private boolean isInsignificant(FleetMemberAPI member) {
        return member.getFleetPointCost() <= ConfigHelper.getRegulationMaxFP();
    }

    private boolean isStabilityIllegal(String name) {
        if (!ConfigHelper.wantsRegulation(market.getFactionId())) {
            return false;
        }
        float stability = submarket.getMarket().getStabilityValue();
        if (Objects.equals(name, "marines") && stability >= 7) {
            return true;
        }
        if (Objects.equals(name, "hand_weapons") && stability >= 5) {
            return true;
        }
        return false;
    }

    private boolean isStabilityIllegal(SimpleMap stabilityMap, float baseValue) {
        if (!ConfigHelper.wantsRegulation(market.getFactionId())) {
            return false;
        }
        float stability = submarket.getMarket().getStabilityValue();
        if (stability <= 0) {
            return false;
        }
        if (stability >= 10) {
            return true;
        }
        String stabilityKey = String.format("%.0f", stability);
        if (!stabilityMap.containsKey(stabilityKey)) {
            log.error("Missing stability mapping for key " + stabilityKey);
            return false;
        }
        float stabilityValue = Float.parseFloat(stabilityMap.get(stabilityKey));
        return baseValue > stabilityValue;
    }

    private void removeItems(CargoAPI cargo) {
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            if (isIllegalOnSubmarket(stack, TransferAction.PLAYER_BUY)) {
                log.info(location + ": Removing from black market due to high stability " + stack.getDisplayName());
                cargo.removeStack(stack);
            }
        }
        cargo.sort();
    }

    private void removeShips(FleetDataAPI ships) {
        for (FleetMemberAPI member : ships.getMembersListCopy()) {
            if (isIllegalOnSubmarket(member, TransferAction.PLAYER_BUY)) {
                log.info(
                    location +
                    ": Removing from black market due to high stability " +
                    member.getHullSpec().getHullName()
                );
                ships.removeFleetMember(member);
            }
        }
        ships.sort();
    }

    public String getTariffTextOverride() {
        return "Bribes";
    }
}
