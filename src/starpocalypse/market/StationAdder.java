package starpocalypse.market;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import lombok.extern.log4j.Log4j;
import starpocalypse.config.SimpleMap;
import starpocalypse.config.SimpleSet;

@Log4j
public class StationAdder extends MarketChanger {

    private final MarketHelper helper = new MarketHelper();
    private final SimpleSet stationDatabase = new SimpleSet("station", "stationDatabase.csv");
    private final SimpleMap factionStations = new SimpleMap("faction", "station", "stationFactionMap.csv");
    private final int sizeForOrbitalStation;
    private final int sizeForBattleStation;
    private final int sizeForStarFortress;

    public StationAdder(int sizeForOrbitalStation, int sizeForBattleStation, int sizeForStarFortress) {
        this.sizeForOrbitalStation = sizeForOrbitalStation;
        this.sizeForBattleStation = sizeForBattleStation;
        this.sizeForStarFortress = sizeForStarFortress;
    }

    @Override
    protected boolean canChange(MarketAPI market) {
        if (market.isHidden()) {
            log.debug("Skipping hidden market");
            return false;
        }
        String factionId = market.getFactionId();
        if (!factionStations.containsKey(factionId)) {
            log.warn("No station entry for " + factionId);
            return false;
        }
        return true;
    }

    @Override
    protected void changeImpl(MarketAPI market) {
        String factionId = market.getFactionId();
        String stationType = factionStations.get(factionId);
        switch (stationType) {
            case "high":
                stationType = "_high";
                break;
            case "mid":
                stationType = "_mid";
                break;
            case "low":
            default:
                stationType = "";
                break;
        }
        if (market.getSize() >= this.sizeForStarFortress) {
            helper.addMissing(market, "starfortress" + stationType, false, stationDatabase.getAll());
        } else if (market.getSize() >= this.sizeForBattleStation) {
            helper.addMissing(market, "battlestation" + stationType, false, stationDatabase.getAll());
        } else if (market.getSize() >= this.sizeForOrbitalStation) {
            helper.addMissing(market, "orbitalstation" + stationType, false, stationDatabase.getAll());
        }
    }
}
