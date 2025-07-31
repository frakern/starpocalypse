package starpocalypse.salvage;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import starpocalypse.helper.CargoUtils;

public class SalvageReductionHullMod extends BaseHullMod {


    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getMod("individual_ship_recovery_mod").modifyMultAlways("Starpocalypse", (float) CargoUtils.getStingyRecoveryChance(hullSize), "You should not see this.");
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return "If you see this, something went wrong! index: " + index +" Hullsize: " +hullSize.name();
    }
    @Override
    public boolean affectsOPCosts() {
        return false;
    }
}
