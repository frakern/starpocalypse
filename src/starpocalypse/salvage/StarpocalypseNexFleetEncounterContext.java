package starpocalypse.salvage;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import exerelin.campaign.battle.NexFleetEncounterContext;
import java.util.LinkedList;
import java.util.List;
import starpocalypse.helper.ConfigHelper;

public class StarpocalypseNexFleetEncounterContext extends NexFleetEncounterContext {

    private final List<FleetMemberAPI> recoverableShips = new LinkedList<>();
    private final List<FleetMemberAPI> playerShips = new LinkedList<>();
    private final List<FleetMemberAPI> storyRecoverableShips = new LinkedList<>();

    @Override
    public List<FleetMemberAPI> getRecoverableShips(
        BattleAPI battle,
        CampaignFleetAPI winningFleet,
        CampaignFleetAPI otherFleet
    ) {
        recoverableShips.clear();
        recoverableShips.addAll(super.getRecoverableShips(battle, winningFleet, otherFleet));
        storyRecoverableShips.clear();
        storyRecoverableShips.addAll(super.getStoryRecoverableShips());
        // Ignore player ships.
        playerShips.clear();
        if (!ConfigHelper.isStingyRecoveriesIncludePlayerShips()) {
            // Fleet snapshot before battle.
            List<FleetMemberAPI> playerFleet = Global.getSector().getPlayerFleet().getFleetData().getSnapshot();
            for (FleetMemberAPI ship : recoverableShips) {
                if (playerFleet.contains(ship)) {
                    playerShips.add(ship);
                    recoverableShips.remove(ship);
                }
            }
        }
        return playerShips;
    }

    @Override
    public List<FleetMemberAPI> getStoryRecoverableShips() {
        List<FleetMemberAPI> allShips = new LinkedList<>();
        allShips.addAll(recoverableShips);
        allShips.addAll(storyRecoverableShips);
        int cutOff = Math.min(23, allShips.size());
        return allShips.subList(0, cutOff);
    }
}
