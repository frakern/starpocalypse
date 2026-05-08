package starpocalypse.submarket;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.submarkets.OpenMarketPlugin;
import lombok.extern.log4j.Log4j;
import starpocalypse.helper.SubmarketUtils;

@Log4j
public class RegulatedOpenMarket extends OpenMarketPlugin {

    private final SubmarketDelegate shared = new SubmarketDelegate(this);

    @Override
    public void init(SubmarketAPI submarket) {
        super.init(submarket);
        shared.location = SubmarketUtils.getLocation(submarket);
    }

    @Override
    public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
        boolean vanillaIllegal = super.isIllegalOnSubmarket(commodityId, action);
        return shared.isIllegalOnSubmarket(commodityId, action, vanillaIllegal);
    }

    @Override
    public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
        boolean vanillaIllegal = super.isIllegalOnSubmarket(stack, action);
        return shared.isIllegalOnSubmarket(stack, action, vanillaIllegal);
    }

    @Override
    public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
        boolean vanillaIllegal = super.isIllegalOnSubmarket(member, action);
        return shared.isIllegalOnSubmarket(member, action, vanillaIllegal);
    }

    @Override
    public void updateCargoPrePlayerInteraction() {
        shared.calculateReputationModifiers();
        super.updateCargoPrePlayerInteraction();
    }

    @Override
    public String getIllegalTransferText(CargoStackAPI stack, SubmarketPlugin.TransferAction action) {
        if (stack.isCommodityStack() && this.market.isIllegal((String) stack.getData())) {
            return super.getIllegalTransferText(stack, action);
        } else {
            int requiredReputation = shared.getRequiredReputation(stack);
            return "Req: " + this.submarket.getFaction().getDisplayName() + " - " + requiredReputation + " / 100";
        }
    }

    @Override
    public String getIllegalTransferText(FleetMemberAPI member, SubmarketPlugin.TransferAction action) {
        int requiredReputation = shared.getRequiredReputation(member);
        if (super.isIllegalOnSubmarket(member, action)) {
            return super.getIllegalTransferText(member, action);
        } else {
            return "Req: " + this.submarket.getFaction().getDisplayName() + " - " + requiredReputation + " / 100";
        }
    }
}
