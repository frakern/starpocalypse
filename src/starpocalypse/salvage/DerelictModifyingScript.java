package starpocalypse.salvage;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import java.util.List;
import java.util.Random;
import lombok.extern.log4j.Log4j;
import starpocalypse.helper.ConfigHelper;

@Log4j
public class DerelictModifyingScript implements EveryFrameScript {

    private static final String salvageChangeAppliedTag = "$StarpocalypseReducedRecovery";

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        for (SectorEntityToken entity : getEntities(Tags.DEBRIS_FIELD)) {
            forceStoryPointRecovery(entity);
        }
        for (SectorEntityToken entity : getEntities(Tags.SALVAGEABLE)) {
            forceStoryPointRecovery(entity);
        }
    }

    private List<SectorEntityToken> getEntities(String tag) {
        return Global.getSector().getPlayerFleet().getContainingLocation().getEntitiesWithTag(tag);
    }

    private void forceStoryPointRecovery(SectorEntityToken entity) {
        MemoryAPI memory = entity.getMemoryWithoutUpdate();
        if (memory.contains(MemFlags.SALVAGE_SPECIAL_DATA)) {
            Object specialData = memory.get(MemFlags.SALVAGE_SPECIAL_DATA);
            if(!entity.hasTag(salvageChangeAppliedTag) && !memory.contains(salvageChangeAppliedTag))
            {
                if (specialData instanceof ShipRecoverySpecial.ShipRecoverySpecialData) {
                    if(!((ShipRecoverySpecial.ShipRecoverySpecialData) specialData).ships.isEmpty())
                    {
                        log.info("Found new salvageable: " +entity.getFullName()+ " in " + entity.getContainingLocation().getName() + " special Data " + specialData);
                        if(((ShipRecoverySpecial.ShipRecoverySpecialData) specialData).storyPointRecovery == null || !(((ShipRecoverySpecial.ShipRecoverySpecialData) specialData).storyPointRecovery))
                        {

                            Random rand;
                            if(entity.getId() == null || entity.getId().isEmpty())
                            {
                                rand = new Random();
                            }
                            else{
                                rand = new Random(entity.getId().hashCode());
                            }
                            double recoveryChance = 1;
                            for(ShipRecoverySpecial.PerShipData ship:((ShipRecoverySpecial.ShipRecoverySpecialData) specialData).ships)
                            {
                                ShipAPI.HullSize shipClass = ship.getVariant().getHullSize();

                                switch (shipClass){
                                    case FRIGATE:
                                        recoveryChance =  Math.min(ConfigHelper.getStingyRecoveriesChanceFrigate(),recoveryChance);
                                        break;

                                    case DESTROYER:
                                        recoveryChance = Math.min(ConfigHelper.getStingyRecoveriesChanceDestroyer(),recoveryChance);
                                        break;
                                    case CRUISER:
                                        recoveryChance = Math.min(ConfigHelper.getStingyRecoveriesChanceCruiser(),recoveryChance);
                                        break;

                                    case CAPITAL_SHIP:
                                        recoveryChance = Math.min(ConfigHelper.getStingyRecoveriesChanceCapital(),recoveryChance);
                                        break;

                                    default:
                                        recoveryChance = 0;
                                }
                                log.info("Recovery chance for " + ship.variantId + " determined  to be " + recoveryChance);
                            }
                            double randomDouble = rand.nextDouble();
                            boolean forceStoryPoint = randomDouble >= recoveryChance;
                            log.info("Salvage Chance for " + entity.getFullName() + " with ID " + entity.getId() + " Random double is " + randomDouble + " recovery chance was " + recoveryChance +" now requires story point " + forceStoryPoint);

                            ((ShipRecoverySpecial.ShipRecoverySpecialData) specialData).storyPointRecovery = forceStoryPoint;
                        }

                        entity.addTag(salvageChangeAppliedTag);
                        memory.set(salvageChangeAppliedTag, true);
                        log.info("Updated memory for entity");

                    }

                }
            }
        }
    }
}
