package com.fs.starfarer.api.impl.campaign.intel;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.LinkedHashMap;

public class DRM_MonthlyRepDecayIntel extends BaseIntelPlugin {

    public LinkedHashMap<FactionAPI, Float> tooltipMap = new LinkedHashMap<>();

    public DRM_MonthlyRepDecayIntel(LinkedHashMap<FactionAPI, Float> map) {
        endingTimeRemaining = 5f;
        tooltipMap.putAll(map); //Map of rep changes is copied to avoid unintended interactions
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        info.addPara("As your actions lose some of their impact over time, people will slowly forget or forgive what caught their attention in the past."
                + "\nYour standing with the following factions has changed:",0f);
        bullet(info);
        for (FactionAPI f : tooltipMap.keySet()) {
            CoreReputationPlugin.addAdjustmentMessage(tooltipMap.get(f), f, null,
                    null, null, info, Misc.getTextColor(), false, 10f);
        }
        unindent(info);
    }

    protected String getName() {
        return "Monthly reputation report";
    }

    //TODO: find a proper icon
    public String getIcon() {return "graphics/icons/intel/reputation.png";}
}
