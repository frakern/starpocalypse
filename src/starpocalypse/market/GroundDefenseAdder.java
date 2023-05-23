package starpocalypse.market;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import lombok.extern.log4j.Log4j;

@Log4j
public class GroundDefenseAdder extends MarketChanger {

    private final MarketHelper helper = new MarketHelper();
    private final int sizeHeavyBatteries;
    private final String[] blockingIndustries;

    public GroundDefenseAdder(int sizeHeavyBatteries) {
        this.sizeHeavyBatteries = sizeHeavyBatteries;
        this.blockingIndustries = new String[] {Industries.GROUNDDEFENSES, Industries.HEAVYBATTERIES};
    }

    @Override
    protected boolean canChange(MarketAPI market) {
        return true;
    }

    @Override
    protected void changeImpl(MarketAPI market) {
        if (market.getSize() >= this.sizeHeavyBatteries) {
            helper.addMissing(market, Industries.HEAVYBATTERIES, true, blockingIndustries);
        }
        helper.addMissing(market, Industries.GROUNDDEFENSES, false, blockingIndustries);
    }
}
