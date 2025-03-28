package starpocalypse.helper;

import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.extern.log4j.Log4j;

/**
 * Cargo as defined by Starsector - cargo stacks and mothballed fleet
 */
@Log4j
public class CargoUtils {

    public static void damageShip(String location, FleetMemberAPI ship, int minDmods, int maxDmods) {
        String hullName = ship.getHullSpec().getHullName();
        ShipVariantAPI variant = ship.getVariant();
        Random random = new Random();
        if (DModManager.setDHull(variant)) {
            log.info(location + ": Damaging " + hullName);
            int numberOfDmods = getNumberOfDmods(random, minDmods, maxDmods);
            DModManager.addDMods(variant, true, numberOfDmods, random);
        }
    }

    public static int getTier(CargoStackAPI stack) {
        int tier = -1;
        if (stack.isWeaponStack()) {
            WeaponSpecAPI spec = stack.getWeaponSpecIfWeapon();
            tier = spec.getTier();
        } else if (stack.isSpecialStack() && stack.getSpecialDataIfSpecial().getId().equals(Items.TAG_MODSPEC) ) {
            HullModSpecAPI spec = stack.getHullModSpecIfHullMod();
            tier = spec.getTier();
        } else if (stack.isFighterWingStack()) {
            FighterWingSpecAPI spec = stack.getFighterWingSpecIfWing();
            tier = spec.getTier();
        }
        return tier;
    }

    private static int getNumberOfDmods(Random random, int minDmods, int maxDmods) {
        int numDmods = minDmods;
        if (maxDmods > minDmods) {
            numDmods += random.nextInt(maxDmods);
        }
        return numDmods;
    }

    public static void handleStingyWeapon(ShipVariantAPI variant, Random rand)
    {
        if(variant.hasTag("consistent_weapon_drops"))
            return;

        // Remove more weapons based on StingyRecoveriesChanceWeapons
        List<String> remove = new ArrayList<String>();

        for (String slotId : variant.getNonBuiltInWeaponSlots()) {
            if (rand.nextFloat() > ConfigHelper.getStingyRecoveriesChanceWeapons() && !variant.getWeaponSpec(slotId).hasTag("omega"))
            {
                log.info("Removing weapon " + variant.getWeaponSpec(slotId).getWeaponName() + " from " + variant.getHullVariantId());
                remove.add(slotId);
            }
        }
        for (String slotId : remove)
            variant.clearSlot(slotId);
        int index = 0;
        for (String id : variant.getFittedWings()) {
            if (rand.nextFloat() > ConfigHelper.getStingyRecoveriesChanceWeapons())
            {
                log.info("Removing wing " + variant.getWing(index).getWingName() + " from " + variant.getHullVariantId());
                variant.setWingId(index, null);
            }
            index++;
        }

    }

    public static double getStingyRecoveryChance(ShipAPI.HullSize shipClass)
    {
        switch (shipClass){
            case FRIGATE:
                return ConfigHelper.getStingyRecoveriesChanceFrigate();
            case DESTROYER:
                return ConfigHelper.getStingyRecoveriesChanceDestroyer();
            case CRUISER:
                return ConfigHelper.getStingyRecoveriesChanceCruiser();
            case CAPITAL_SHIP:
                return ConfigHelper.getStingyRecoveriesChanceCapital();
            default:
                return 0;
        }
    }
}
