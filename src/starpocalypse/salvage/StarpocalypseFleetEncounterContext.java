package starpocalypse.salvage;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import java.util.LinkedList;
import java.util.List;
import starpocalypse.helper.ConfigHelper;

public class StarpocalypseFleetEncounterContext extends FleetEncounterContext {

    private final List<FleetMemberAPI> recoverableShips = new LinkedList<>();
    private final List<FleetMemberAPI> playerShips = new LinkedList<>();
    private final List<FleetMemberAPI> storyRecoverableShips = new LinkedList<>();

    @Override
    public List<FleetMemberAPI> getRecoverableShips(
        BattleAPI battle,
        CampaignFleetAPI winningFleet,
        CampaignFleetAPI otherFleet
    ) {
        // Get game-generated list of recoverable ships. Will be combined in getStoryRecoverableShips.
        recoverableShips.clear();
        recoverableShips.addAll(super.getRecoverableShips(battle, winningFleet, otherFleet));
        storyRecoverableShips.clear();
        storyRecoverableShips.addAll(super.getStoryRecoverableShips());

        // Empty list to hold player ships.
        playerShips.clear();

        // Fleet snapshot before battle.
        List<FleetMemberAPI> playerFleet = Global.getSector().getPlayerFleet().getFleetData().getSnapshot();

        // Loop through recoverable ships and pull out player ships according to settings.
        for (FleetMemberAPI ship : recoverableShips) {
            if (playerFleet.contains(ship)) {
                if (!ConfigHelper.isStingyRecoveriesIncludePlayerShips()) {
                    playerShips.add(ship);
                }
                else if (ship.isFrigate() && ConfigHelper.getStingyRecoveriesCombatPlayerShipsSize() > 1) {
                    playerShips.add(ship);
                }
                else if (ship.isDestroyer() && ConfigHelper.getStingyRecoveriesCombatPlayerShipsSize() > 2) {
                    playerShips.add(ship);
                }
                else if (ship.isCruiser() && ConfigHelper.getStingyRecoveriesCombatPlayerShipsSize() > 3) {
                    playerShips.add(ship);
                }
            }
        }

        // Remove any selected player ships from original recoverable ships list.
        recoverableShips.removeAll(playerShips);
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
