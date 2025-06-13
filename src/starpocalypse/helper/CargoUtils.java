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
            double randResult = rand.nextFloat();
            if (randResult > getStingyRecoveryChance(variant.getWeaponSpec(slotId).getTier()) && !variant.getWeaponSpec(slotId).hasTag("omega"))
            {
                log.info("Removing weapon from " + variant.getFullDesignationWithHullName() +" in slot " + slotId + ": " + variant.getWeaponSpec(slotId).getWeaponName() + " from " + variant.getHullSpec().getHullName() + " with rand " + randResult + " keep below " + getStingyRecoveryChance(variant.getWeaponSpec(slotId).getTier()));
                remove.add(slotId);
            }
        }
        for (String slotId : remove)
            variant.clearSlot(slotId);
        int index = 0;
        for (String id : variant.getFittedWings()) {
            if(variant.getWing(index) != null)
            {
                double randResult = rand.nextFloat();
                if (randResult > getStingyRecoveryChance(variant.getWing(index).getTier()))
                {
                    log.info("Removing wing " + variant.getWing(index).getWingName() + " from " + variant.getHullSpec().getHullName() + " with rand " + randResult + " needed to keep " + getStingyRecoveryChance(variant.getWing(index).getTier()));
                    variant.setWingId(index, null);
                }
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

    public static double getStingyRecoveryChance(int tier)
    {
        if(tier > 4)
            tier = 4;
        switch (tier){
            case 0:
                return ConfigHelper.getStingyRecoveriesWeaponT0();
            case 1:
                return ConfigHelper.getStingyRecoveriesWeaponT1();
            case 2:
                return ConfigHelper.getStingyRecoveriesWeaponT2();
            case 3:
                return ConfigHelper.getStingyRecoveriesWeaponT3();
            case 4:
                return ConfigHelper.getStingyRecoveriesWeaponT4();
            default:
                return 1.0;
        }
    }

}
