package starpocalypse.submarket;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import starpocalypse.helper.ConfigHelper;
import starpocalypse.helper.SubmarketUtils;

public class SubmarketSwapper implements ColonyInteractionListener {

    public static void register() {
        SubmarketSwapper swapper = new SubmarketSwapper();
        Global.getSector().getListenerManager().addListener(swapper, true);
    }

    public static void uninstallLegacy() {
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            SubmarketUtils.replaceSubmarket(market, "regulated_open_market", Submarkets.SUBMARKET_OPEN, true);
            SubmarketUtils.replaceSubmarket(market, "regulated_generic_military", Submarkets.GENERIC_MILITARY, true);
            SubmarketUtils.replaceSubmarket(market, "regulated_black_market", Submarkets.SUBMARKET_BLACK, true);
            if (ConfigHelper.hasNexerelin()) {
                SubmarketUtils.replaceSubmarket(market, "regulated_exerelin_prismMarket", "exerelin_prismMarket", true);
            }
        }
    }

    public static void reinstall() {
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            SubmarketUtils.replaceSubmarkets(market, ConfigHelper.isUninstall());
        }
    }

    @Override
    public void reportPlayerOpenedMarket(MarketAPI market) {
        reportPlayerOpenedMarketAndCargoUpdated(market);
    }

    @Override
    public void reportPlayerClosedMarket(MarketAPI market) {}

    @Override
    public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {
        SubmarketUtils.replaceSubmarkets(market, false);
        SubmarketUtils.updateSubmarkets(market);
    }

    @Override
    public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {}
}
