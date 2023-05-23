package starpocalypse.salvage;

import exerelin.campaign.battle.NexFleetInteractionDialogPluginImpl;

public class StarpocalypseNexFleetInteractionDialogPlugin extends NexFleetInteractionDialogPluginImpl {

    public StarpocalypseNexFleetInteractionDialogPlugin() {
        this(null);
    }

    public StarpocalypseNexFleetInteractionDialogPlugin(FIDConfig params) {
        super(params);
        context = new StarpocalypseNexFleetEncounterContext();
    }
}
