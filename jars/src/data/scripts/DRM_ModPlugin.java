package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.listeners.DRM_RepDecay;
import com.fs.starfarer.api.impl.campaign.intel.DRM_MonthlyRepDecayIntel;

public class DRM_ModPlugin extends BaseModPlugin {

    @Override
    public void onGameLoad(boolean newGame) {
        //DRO_RepDecay repDecay = new DRM_RepDecay();
        //DRO_MonthlyRepDecayIntel monthlyRepDecayIntel = new DRM_MonthlyRepDecayIntel();

        Global.getSector().getListenerManager().addListener(new DRM_RepDecay(), false);
    }

}
