package starpocalypse.helper;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SpecialItemPlugin;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.impl.items.MultiBlueprintItemPlugin;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import lombok.extern.log4j.Log4j;
import starpocalypse.salvage.DerelictModifyingScript;
import starpocalypse.salvage.StarpocalypseCampaignPlugin;

@Log4j
public class DropTableUtils {

    public static void removeBlueprintPackages() {
        for (SpecialItemSpecAPI specialItemSpec : Global.getSettings().getAllSpecialItemSpecs()) {
            SpecialItemPlugin plugin = specialItemSpec.getNewPluginInstance(null);
            if (plugin instanceof MultiBlueprintItemPlugin) {
                log.debug("Removing " + specialItemSpec.getName() + " from drop table");
                specialItemSpec.getTags().add(Tags.NO_DROP);
            }
        }
    }

    public static void makeRecoveryRequireStoryPoint() {
        Global.getSector().registerPlugin(new StarpocalypseCampaignPlugin());
        Global.getSector().addTransientScript(new DerelictModifyingScript());
    }

    public static void applySalvageMultiplier(float multiplier) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        MutableFleetStatsAPI stats = fleet.getStats();
        String desc = "Leakage";
        String id = "starpocalypse";
        stats.getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_NOT_RARE).modifyFlat(id, multiplier, desc);
    }
}
