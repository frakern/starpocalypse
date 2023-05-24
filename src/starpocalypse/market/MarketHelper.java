package starpocalypse.market;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import java.util.Objects;
import lombok.extern.log4j.Log4j;

@Log4j
public class MarketHelper {

    public void addMissing(MarketAPI market, String industryId, Boolean removeBlocking, String... blockingIndustries) {
        if (removeBlocking) {
            removeBlocking(market, industryId, blockingIndustries);
        }
        if (!hasIndustry(market, blockingIndustries)) {
            log.info("Adding industry " + industryId);
            market.addIndustry(industryId);
        }
    }

    public void removeBlocking(MarketAPI market, String industryId, String... blockingIndustries) {
        for (String blocker : blockingIndustries) {
            if (!Objects.equals(blocker, industryId)) {
                removeIndustry(market, blocker);
            }
        }
    }

    public boolean hasIndustry(MarketAPI market, String... blockingIndustries) {
        for (String blocker : blockingIndustries) {
            if (market.hasIndustry(blocker)) {
                return true;
            }
        }
        return false;
    }

    public void removeIndustry(MarketAPI market, String industryId) {
        if (market.hasIndustry(industryId)) {
            market.removeIndustry(industryId, null, false);
        }
    }
}
