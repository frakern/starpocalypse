package starpocalypse.market;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import lombok.extern.log4j.Log4j;
@Log4j

public class PirateUpgrader extends MarketChanger{
    private final MarketHelper helper = new MarketHelper();

    private final boolean maxHeavyBatteries;
    private final boolean upgradeSpaceport;

    public PirateUpgrader(boolean maxHeavyBatteries, boolean upgradeSpaceport) {
        this.maxHeavyBatteries = maxHeavyBatteries;
        this.upgradeSpaceport = upgradeSpaceport;
    }

    @Override
    protected boolean canChange(MarketAPI market) {

        boolean required = upgradeSpaceport || maxHeavyBatteries;
        boolean done = (!upgradeSpaceport || market.hasIndustry(Industries.MEGAPORT)) && (!maxHeavyBatteries || (market.hasIndustry(Industries.HEAVYBATTERIES) && market.getIndustry(Industries.HEAVYBATTERIES).isImproved()));

        return required && ! done;
    }

    @Override
    protected void changeImpl(MarketAPI market) {
        if(market.getFactionId().equals("pirates") || market.getFactionId().equals("luddic_path"))
        {
            if(upgradeSpaceport)
            {
                helper.addMissing(market, Industries.MEGAPORT, true, Industries.SPACEPORT);
            }
            if(maxHeavyBatteries)
            {
                helper.addMissing(market, Industries.HEAVYBATTERIES, true, Industries.GROUNDDEFENSES);
                helper.improveIndustry(market, Industries.HEAVYBATTERIES);
            }
        }
    }
}

