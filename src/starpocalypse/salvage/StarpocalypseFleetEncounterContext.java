package starpocalypse.salvage;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.util.Misc;
import starpocalypse.helper.CargoUtils;

import java.util.List;

public class StarpocalypseFleetEncounterContext extends FleetEncounterContext {

    private final FleetEncounterModifyingScript salvageModifier = new FleetEncounterModifyingScript();
    @Override
    public List<FleetMemberAPI> getRecoverableShips(
            BattleAPI battle,
            CampaignFleetAPI winningFleet,
            CampaignFleetAPI otherFleet
    ) {
        return salvageModifier.getRecoverableShips(battle,winningFleet,otherFleet,super.getRecoverableShips(battle,winningFleet,otherFleet),super.getStoryRecoverableShips());
    }

    @Override
    public List<FleetMemberAPI> getStoryRecoverableShips() {
        return salvageModifier.getStoryRecoverableShips();
    }

    @Override
    protected void lootWeapons(FleetMemberAPI member, ShipVariantAPI variant, boolean own, float mult, boolean lootingModule)
    {
        if(!salvageModifier.isRecoverableShip(member))
        {
            CargoUtils.handleStingyWeapon(variant, Misc.random);
        }
        super.lootWeapons(member, variant, own, mult, lootingModule);
    }
}
