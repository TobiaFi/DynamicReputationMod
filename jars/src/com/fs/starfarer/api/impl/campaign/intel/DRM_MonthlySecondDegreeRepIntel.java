package com.fs.starfarer.api.impl.campaign.intel;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.LinkedHashMap;

public class DRM_MonthlySecondDegreeRepIntel extends BaseIntelPlugin{

    public FactionAPI faction;
    public LinkedHashMap<FactionAPI, Float> relationsMap = new LinkedHashMap<>();

    public DRM_MonthlySecondDegreeRepIntel (FactionAPI f, LinkedHashMap<FactionAPI, Float> map) {
        endingTimeRemaining = 5f;
        faction = f;
        relationsMap.putAll(map); //Map of rep changes is copied to avoid unintended interactions
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        info.addPara("Your good standing with " + faction.getDisplayNameWithArticle() + " has been recognised by their allies. Worryingly, their enemies have also taken notice.", 0f, faction.getColor(), faction.getDisplayName());
        bullet(info);
        for (FactionAPI f : relationsMap.keySet()) {
            CoreReputationPlugin.addAdjustmentMessage(relationsMap.get(f), f, null,
                    null, null, info, Misc.getTextColor(), false, 10f);
        }
        unindent(info);
    }

    protected String getName() {
        return "High profile relations with " + faction.getDisplayNameWithArticle();
    }

    //TODO: find a proper icon
    public String getIcon() {return "graphics/icons/intel/reputation.png";}
}
