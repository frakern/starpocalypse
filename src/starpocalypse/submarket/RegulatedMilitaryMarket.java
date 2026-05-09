package starpocalypse.submarket;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.submarkets.MilitarySubmarketPlugin;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;
import lombok.extern.log4j.Log4j;
import starpocalypse.helper.ConfigHelper;
import starpocalypse.helper.SubmarketUtils;

@Log4j
public class RegulatedMilitaryMarket extends MilitarySubmarketPlugin {

    private final SubmarketDelegate shared = new SubmarketDelegate(this);

    @Override
    public void init(SubmarketAPI submarket) {
        super.init(submarket);
        shared.location = SubmarketUtils.getLocation(submarket);
    }

    @Override
    public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
        boolean vanillaIllegal = this.market.isIllegal(commodityId);
        CommodityOnMarketAPI com = this.market.getCommodityData(commodityId);
        boolean isMilitary = com.getCommodity().getTags().contains("military");
        if (!isMilitary) {
            return shared.isIllegalOnSubmarket(commodityId, action, vanillaIllegal);
        } else {
            RepLevel req = this.getRequiredLevelAssumingLegal(commodityId, action);
            RepLevel level = this.submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction("player"));
            boolean legal = level.isAtWorst(req);
            if (this.requiresCommission(req)) {
                legal &= this.hasCommission();
            }

            return shared.isIllegalOnSubmarket(commodityId, action, !legal);
        }
    }

    @Override
    public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
        if (stack.isCommodityStack()) {
            return this.isIllegalOnSubmarket((String) stack.getData(), action);
        } else {
            boolean vanillaIllegal = false;
            RepLevel req = this.getRequiredLevelAssumingLegal(stack, action);
            if (req != null && this.requiresCommission(req) && !shared.hasCommission(this.submarket.getFaction())) {
                vanillaIllegal = true;
            }
            return shared.isIllegalOnSubmarket(stack, action, vanillaIllegal);
        }
    }

    @Override
    public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
        boolean vanillaIllegal = false;
        RepLevel req = this.getRequiredLevelAssumingLegal(member, action);
        if (req != null && this.requiresCommission(req) && !shared.hasCommission(this.submarket.getFaction())) {
            vanillaIllegal = true;
        }
        return shared.isIllegalOnSubmarket(member, action, vanillaIllegal);
    }

    @Override
    public void updateCargoPrePlayerInteraction() {
        shared.calculateReputationModifiers();
        super.updateCargoPrePlayerInteraction();
    }

    @Override
    public String getIllegalTransferText(CargoStackAPI stack, SubmarketPlugin.TransferAction action) {
        RepLevel req = this.getRequiredLevelAssumingLegal(stack, action);
        if (req != null) {
            int requiredReputation = shared.getRequiredReputation(stack);
            if (this.requiresCommission(req)) {
                return (
                    "Req: " +
                    this.submarket.getFaction().getDisplayName() +
                    " - " +
                    requiredReputation +
                    " / 100, " +
                    " commission"
                );
            } else {
                return "Req: " + this.submarket.getFaction().getDisplayName() + " - " + requiredReputation + " / 100";
            }
        } else {
            return "Illegal to trade in " + stack.getDisplayName() + " here";
        }
    }

    @Override
    public Highlights getIllegalTransferTextHighlights(CargoStackAPI stack, SubmarketPlugin.TransferAction action) {
        Highlights h = new Highlights();
        h.append(getIllegalTransferText(stack, action), Misc.getNegativeHighlightColor());
        return h;
    }

    @Override
    public String getIllegalTransferText(FleetMemberAPI member, SubmarketPlugin.TransferAction action) {
        RepLevel req = this.getRequiredLevelAssumingLegal(member, action);
        if (req != null) {
            String str = "";
            int reputation = market.getFaction().getRelToPlayer().getRepInt();
            int requiredReputation = shared.getRequiredReputation(member);
            if (reputation < requiredReputation) {
                str =
                    str +
                    "Req: " +
                    this.submarket.getFaction().getDisplayName() +
                    " - " +
                    requiredReputation +
                    " / 100";
            }

            if (this.requiresCommission(req) && !shared.hasCommission(this.submarket.getFaction())) {
                if (!str.isEmpty()) {
                    str = str + "\n";
                }

                str = str + "Req: " + this.submarket.getFaction().getDisplayName() + " - " + "commission";
            }

            return str;
        } else {
            return action == TransferAction.PLAYER_BUY ? "Illegal to buy" : "Illegal to sell";
        }
    }

    @Override
    public Highlights getIllegalTransferTextHighlights(FleetMemberAPI member, SubmarketPlugin.TransferAction action) {
        Highlights h = new Highlights();
        h.append(getIllegalTransferText(member, action), Misc.getNegativeHighlightColor());
        return h;
    }

    private RepLevel getRequiredLevelAssumingLegal(String commodityId, SubmarketPlugin.TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {
            return RepLevel.VENGEFUL;
        } else {
            int requiredReputation = shared.getRequiredReputation(commodityId);
            return RepLevel.getLevelFor((float) Math.min(100, requiredReputation));
        }
    }

    private RepLevel getRequiredLevelAssumingLegal(CargoStackAPI stack, SubmarketPlugin.TransferAction action) {
        if (stack.isWeaponStack() || stack.isModSpecStack() || stack.isFighterWingStack()) {
            if (action == TransferAction.PLAYER_BUY) {
                int requiredReputation = shared.getRequiredReputation(stack);
                return RepLevel.getLevelFor((float) Math.min(100, requiredReputation));
            }

            return RepLevel.VENGEFUL;
        } else {
            return !stack.isCommodityStack()
                ? null
                : this.getRequiredLevelAssumingLegal((String) stack.getData(), action);
        }
    }

    private RepLevel getRequiredLevelAssumingLegal(FleetMemberAPI member, SubmarketPlugin.TransferAction action) {
        if (action == TransferAction.PLAYER_BUY) {
            int requiredReputation = shared.getRequiredReputation(member);
            return RepLevel.getLevelFor((float) Math.min(100, requiredReputation));
        } else {
            return null;
        }
    }

    @Override
    protected boolean requiresCommission(RepLevel req) {
        return super.requiresCommission(req) && !ConfigHelper.isMilitaryNoCommission();
    }

    @Override
    public boolean isEnabled(CoreUIAPI ui) {
        if (ConfigHelper.isMilitaryNoCommission()) {
            return ui.getTradeMode() != CampaignUIAPI.CoreUITradeMode.SNEAK;
        }
        else {
            return super.isEnabled(ui);
        }
    }

    @Override
    public String getTooltipAppendix(CoreUIAPI ui) {
        if (ConfigHelper.isMilitaryNoCommission()) {
            return ui.getTradeMode() == CampaignUIAPI.CoreUITradeMode.SNEAK ? "Requires: proper docking authorization" : null;
        }
        else {
            return super.getTooltipAppendix(ui);
        }
    }
}
