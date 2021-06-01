package com.fs.starfarer.api.impl.campaign.intel;

import com.fs.starfarer.api.campaign.FactionAPI;

import java.util.Map;

public class DRO_MonthlyRepDecayIntel extends BaseIntelPlugin {

    public Map<FactionAPI, Float> repMap;

    public DRO_MonthlyRepDecayIntel(Map<FactionAPI, Float> m) {repMap = m;}
}
