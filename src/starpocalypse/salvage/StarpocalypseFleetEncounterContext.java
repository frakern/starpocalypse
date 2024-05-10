package starpocalypse.salvage;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;

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
}
