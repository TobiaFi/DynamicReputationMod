package com.fs.starfarer.api.impl.campaign.intel;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.characters.RelationshipAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.HashMap;

public class DRM_MonthlyRepDecayIntel extends BaseIntelPlugin {

    public HashMap<FactionAPI, Integer> tooltipMap;

    public DRM_MonthlyRepDecayIntel(HashMap<FactionAPI, Integer> map) {

        endingTimeRemaining = 5f;
        tooltipMap = map;
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        info.addPara("LALALILALA", Misc.getNegativeHighlightColor(), 0f);
        for (FactionAPI f : tooltipMap.keySet()) {
            String article;
            String name = f.getDisplayName();
            String rep1 = "bababa";
            Color good = new Color(147, 242, 0);
            Color bad = new Color(231, 91, 0);
            Color[] hl1 = new Color[] {Color.RED, Color.GREEN};

            if (f.getDisplayNameWithArticle().startsWith("the")) { article = "the "; }
            else { article = ""; }
/*
            if (tooltipMap.get(f) > 0) {
                rep = " improved by " + tooltipMap.get(f).toString();
                //hl = new Color[] {f.getColor(), good};
            }
            else {
                rep = " decreased by " + tooltipMap.get(f).toString();
                //hl = new Color[] {f.getColor(), bad};
            }
*/
            //info.addPara(f.getDisplayName() + tooltipMap.get(f).toString(), f.getColor(), 0f);
            info.addPara("Relationship with " + article + name + rep1, 0f, hl1, name, rep1);

        }
    }

    protected String getName() {
        return "Monthly reputation report";
    }

    public String getIcon() {return "graphics/icons/intel/reputation.png";}
}
