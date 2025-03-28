package starpocalypse.salvage;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import lombok.extern.log4j.Log4j;
import starpocalypse.helper.CargoUtils;
import starpocalypse.helper.ConfigHelper;
import starpocalypse.submarket.StandingMarketRegulation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@Log4j
public class FleetEncounterModifyingScript
{
    private final List<FleetMemberAPI> recoverableShips = new LinkedList<>();
    private final List<FleetMemberAPI> storyRecoverableShips = new LinkedList<>();

    public List<FleetMemberAPI> getRecoverableShips(
            BattleAPI battle,
            CampaignFleetAPI winningFleet,
            CampaignFleetAPI otherFleet,
            List<FleetMemberAPI> recoverableShipsSuper,
            List<FleetMemberAPI> storyRecoverableShipsSuper
    ) {
        List<FleetMemberAPI> removeShips = new LinkedList<>();
        // Get game-generated list of recoverable ships. Will be combined in getStoryRecoverableShips.
        recoverableShips.clear();
        recoverableShips.addAll(recoverableShipsSuper);
        storyRecoverableShips.clear();
        storyRecoverableShips.addAll(storyRecoverableShipsSuper);

        // Fleet snapshot before battle.
        List<FleetMemberAPI> playerFleet = Global.getSector().getPlayerFleet().getFleetData().getSnapshot();

        for (FleetMemberAPI ship : storyRecoverableShips)
        {
            Random rand = new Random();
            // Remove more weapons based on StingyRecoveriesChanceWeapons
            if(!ship.isFighterWing() && (ship.getOwner() != 1  || ConfigHelper.isStingyRecoveriesIncludePlayerShips()))
            {
                CargoUtils.handleStingyWeapon(ship.getVariant(), rand);
            }
        }

        // Loop through recoverable ships and pull out player ships according to settings.
        for (FleetMemberAPI ship : recoverableShips) {

            Random rand = new Random();
            // Remove more weapons based on StingyRecoveriesChanceWeapons
            if(!ship.isFighterWing() && (ship.getOwner() != 1  || ConfigHelper.isStingyRecoveriesIncludePlayerShips()))
            {
                CargoUtils.handleStingyWeapon(ship.getVariant(), rand);
            }

            double recoveryChance = CargoUtils.getStingyRecoveryChance(ship.getHullSpec().getHullSize());
            ShipAPI.HullSize shipClass = ship.getHullSpec().getHullSize();
            double randomDouble = rand.nextDouble();
            boolean forceStoryPoint = randomDouble >= recoveryChance;

            log.info("Combat-recovery chance for " + StandingMarketRegulation.getHullName(ship) + " determined  to be " + recoveryChance + " with resulting roll of " + randomDouble);


            if(forceStoryPoint){
                if (!ship.isAlly() || ConfigHelper.isStingyRecoveriesIncludePlayerShips()) {
                    if (ship.isFrigate() && (ship.getOwner() == 1 ||  ConfigHelper.getStingyRecoveriesCombatPlayerShipsSize() > 1)) {
                        log.info(StandingMarketRegulation.getHullName(ship) + " named " + ship.getShipName() + " recovery now needs story point");
                        removeShips.add(ship);
                    } else if (ship.isDestroyer() && (ship.getOwner() == 1  || ConfigHelper.getStingyRecoveriesCombatPlayerShipsSize() > 2)) {
                        log.info(StandingMarketRegulation.getHullName(ship) + " recovery now needs story point");
                        removeShips.add(ship);
                    } else if (ship.isCruiser() && (ship.getOwner() == 1  || ConfigHelper.getStingyRecoveriesCombatPlayerShipsSize() > 3)) {
                        log.info(StandingMarketRegulation.getHullName(ship) + " recovery now needs story point");
                        removeShips.add(ship);
                    } else if (ship.isCapital() && (ship.getOwner() == 1  || ConfigHelper.isStingyRecoveriesIncludePlayerShips())) {
                        log.info(StandingMarketRegulation.getHullName(ship) + " recovery now needs story point");
                        removeShips.add(ship);
                    }
                }
            }
        }
        recoverableShips.removeAll(removeShips);
        storyRecoverableShips.addAll(removeShips);
        return recoverableShips;
    }

    public List<FleetMemberAPI> getStoryRecoverableShips() {
        List<FleetMemberAPI> allShips = new LinkedList<>(storyRecoverableShips);
        int cutOff = Math.min(23, allShips.size());
        return allShips.subList(0, cutOff);
    }

    public boolean isRecoverableShip(FleetMemberAPI member)
    {
        return recoverableShips.contains(member) || storyRecoverableShips.contains(member);
    }

}
