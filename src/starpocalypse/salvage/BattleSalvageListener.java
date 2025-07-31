package starpocalypse.salvage;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.skills.HullRestoration;
import lombok.extern.log4j.Log4j;
import starpocalypse.helper.CargoUtils;
import starpocalypse.helper.ConfigHelper;

import java.util.List;
import java.util.Random;

@Log4j
public class BattleSalvageListener extends BaseCampaignEventListener {
    public BattleSalvageListener(boolean permaRegister) {
        super(permaRegister);
    }

    @Override
    public void reportBattleFinished(CampaignFleetAPI primaryWinner, BattleAPI battle)
    {
        for(FleetMemberAPI ship : Global.getSector().getPlayerFleet().getMembersWithFightersCopy())
        {
            ship.getVariant().removePermaMod("starpocalypeSalvage");
            ship.getVariant().removeTag("starpocalypseAppliedStingyWeapon");
            Global.getSector().getPlayerFleet().getStats().getDynamic().getMod("ship_recovery_mod").unmodifyFlat("starpocalype_ship_recovery_fix");
        }
    }

    @Override
    public void reportPlayerEngagement(EngagementResultAPI result)
    {
        if(result.didPlayerWin() && ConfigHelper.isStingyRecoveriesCombat()) // In theory just not adding the listener should be enough, but in my testing it was active anyway sometimes. So better make sure
        {
            applySalvageReduction(result.getLoserResult().getDestroyed(), false);
            applySalvageReduction(result.getLoserResult().getDisabled(), false);
            applySalvageReduction(result.getWinnerResult().getDestroyed(), true);
            applySalvageReduction(result.getWinnerResult().getDisabled(), true);
        }
        if(ConfigHelper.isStingyNerfHullRestoration())
        {
            float flatMod = 0;
            for(String flatKey : Global.getSector().getPlayerFleet().getStats().getDynamic().getMod("ship_recovery_mod").getFlatBonuses().keySet())
            {
                if(! Global.getSector().getPlayerFleet().getStats().getDynamic().getMod("ship_recovery_mod").getFlatBonus(flatKey).getSource().equals("starpocalype_ship_recovery_fix"))
                    flatMod += Global.getSector().getPlayerFleet().getStats().getDynamic().getMod("ship_recovery_mod").getFlatBonus(flatKey).getValue();
            }
            ConfigHelper.overwriteOriginalVanillaFloat("baseOwnShipRecoveryChance",  flatMod + ConfigHelper.getOriginalVanillaFloat("baseOwnShipRecoveryChance"));
            //Todo On version update check Misc.isShipRecoverable if ship_recovery_mod still applies to every ship
            Global.getSector().getPlayerFleet().getStats().getDynamic().getMod("ship_recovery_mod").modifyFlatAlways("starpocalype_ship_recovery_fix", -flatMod, "Starpocalypse ship recovery fix");
            ConfigHelper.overwriteOriginalVanillaFloat("baseOwnShipRecoveryChance",  flatMod + ConfigHelper.getOriginalVanillaFloat("baseOwnShipRecoveryChance"));
        }
    }

    private void applySalvageReduction(List<FleetMemberAPI> members, boolean isPlayer)
    {
        Random rand = new Random();
        for(FleetMemberAPI ship : members)
        {
            boolean applyMod = false;
            if (!ship.isAlly() || ConfigHelper.isStingyRecoveriesIncludePlayerShips()) {
                if (ship.isFrigate() && (!isPlayer ||  ConfigHelper.getStingyRecoveriesCombatPlayerShipsSize() > 1)) {
                    applyMod = true;
                } else if (ship.isDestroyer() && (!isPlayer  || ConfigHelper.getStingyRecoveriesCombatPlayerShipsSize() > 2)) {
                    applyMod = true;
                } else if (ship.isCruiser() && (!isPlayer  || ConfigHelper.getStingyRecoveriesCombatPlayerShipsSize() > 3)) {
                    applyMod = true;
                } else if (ship.isCapital() && (!isPlayer || ConfigHelper.isStingyRecoveriesIncludePlayerShips())) {
                    applyMod = true;
                }
            }
            if(applyMod && !ship.getVariant().hasHullMod("starpocalypeSalvage"))
            {
                ship.getVariant().addPermaMod("starpocalypeSalvage");
                ship.updateStats();
            }
            // Remove more weapons based on StingyRecoveriesChanceWeapons
            if(!ship.getVariant().hasTag("starpocalypseAppliedStingyWeapon") && !ship.isFighterWing() && (!isPlayer  || ConfigHelper.isStingyRecoveriesIncludePlayerShips()))
            {
                ship.getVariant().addTag("starpocalypseAppliedStingyWeapon");
                CargoUtils.handleStingyWeapon(ship.getVariant(), rand);
            }
        }
    }
}
