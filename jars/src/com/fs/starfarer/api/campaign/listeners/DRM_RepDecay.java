package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.intel.DRM_MonthlyRepDecayIntel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class DRM_RepDecay implements EconomyTickListener {

    public FactionAPI playerFaction;
    public HashMap<FactionAPI, Float> repMap = new HashMap<FactionAPI, Float>();
    public HashMap<FactionAPI, Integer> tooltipMap = new HashMap<FactionAPI, Integer>();


    public DRM_RepDecay() {
        for (FactionAPI f : Global.getSector().getAllFactions()) {
            if (f.isPlayerFaction()) {playerFaction = f;}
            else {repMap.put(f, null);}
        }
        for (FactionAPI f : repMap.keySet()) {repMap.put(f, f.getRelationship(playerFaction.getId()));}
    }

    public void reportEconomyMonthEnd() {
        for (FactionAPI f : repMap.keySet()) {
            if (f.isShowInIntelTab()) { monthlyRepDecay(f); }
        }
        Global.getSector().getIntelManager().addIntel(new DRM_MonthlyRepDecayIntel(tooltipMap));
    }

    private void monthlyRepDecay(FactionAPI f) {
        //Creating variables
        float previousRep = repMap.get(f);
        int previousRepInt = f.getRepInt(playerFaction.getId());
        int newRepInt;
        float repLoss = 0f;

        //Decay calculation
        if (previousRep > 0.25) { repLoss = -  previousRep * 0.10f; }
        else if (previousRep < -0.25) { repLoss = - previousRep * 0.05f; }


        //Making it so current rep can't drop below 25/50/75 if above those thresholds
        float floor = Math.abs(f.getRelationship(playerFaction.getId())) % 0.25f;
        if (floor != 0) { repLoss = Math.signum(repLoss) * Math.min(Math.abs(repLoss), floor); }

        //Updating rep
        f.adjustRelationship(playerFaction.getId(), repLoss);
        newRepInt = f.getRepInt(playerFaction.getId());

        //Updating maps
        if (previousRepInt - newRepInt != 0) { tooltipMap.put(f, newRepInt - previousRepInt); }
        repMap.put(f, f.getRelationship(playerFaction.getId()));
    }

    public void reportEconomyTick(int iterIndex) {}
}