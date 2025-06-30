package starpocalypse.submarket;

import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import exerelin.campaign.submarkets.PrismMarket;
import lombok.extern.log4j.Log4j;
import starpocalypse.helper.ConfigHelper;
import starpocalypse.helper.SubmarketUtils;

@Log4j
public class RegulatedPrismMarket extends PrismMarket {

    private String location;

    @Override
    public void init(SubmarketAPI submarket) {
        super.init(submarket);
        location = SubmarketUtils.getLocation(submarket);
    }

    @Override
    public String getTooltipAppendix(CoreUIAPI ui)
    {
        if (!isEnabled(ui))
        {
            String msg = "Due to the recent punitive sanctions against its operators, " +
                    "the fabled rare ship and weapons market has been closed.";
            return msg;
        }
        return null;
    }

    @Override
    public boolean isEnabled(CoreUIAPI ui) {
        if (ConfigHelper.hasNexerelin() && ConfigHelper.isDisablePrismFreeport()) {
            return false;

        } else {
            return true;
        }
    }
}
