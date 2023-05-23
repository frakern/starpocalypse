package starpocalypse.salvage;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;

import java.util.List;

public class DerelictModifyingScript implements EveryFrameScript {

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        // Check for nearby salvageable derelicts
        for (SectorEntityToken entity : getEntities(Tags.DEBRIS_FIELD)) {
            clearSpecialData(entity);
        }
        for (SectorEntityToken entity : getEntities(Tags.SALVAGEABLE)) {
            clearSpecialData(entity);
        }
    }

    private List<SectorEntityToken> getEntities(String tag) {
        return Global.getSector().getPlayerFleet().getContainingLocation().getEntitiesWithTag(tag);
    }

    private void clearSpecialData(SectorEntityToken entity) {
        MemoryAPI memory = entity.getMemoryWithoutUpdate();
        if (memory.contains(MemFlags.SALVAGE_SPECIAL_DATA)) {
            Object specialData = memory.get(MemFlags.SALVAGE_SPECIAL_DATA);
            if (specialData instanceof ShipRecoverySpecial.ShipRecoverySpecialData) {
                // Salvageable derelicts: always require a story point
                ((ShipRecoverySpecial.ShipRecoverySpecialData) specialData).storyPointRecovery = true;
            }
        }
    }
}
