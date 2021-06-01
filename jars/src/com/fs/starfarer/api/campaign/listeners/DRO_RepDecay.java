package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;

import java.util.Map;

public class DRO_RepDecay implements EconomyTickListener {

    public FactionAPI playerFaction;
    public Map<FactionAPI, Float> repMap;
    public Map<FactionAPI, Float> tooltipMap;


    public DRO_RepDecay() {
        for (FactionAPI f : Global.getSector().getAllFactions()) {
            if (f.isPlayerFaction()) {playerFaction = f;}
            else {repMap.put(f, null);}
        }
        for (FactionAPI f : repMap.keySet()) {repMap.put(f, f.getRelationship(playerFaction.getId()));}
    }

    public void reportEconomyMonthEnd() {
        for (FactionAPI f : repMap.keySet()) {monthlyRepDecay(f);}
        //DRO_MonthlyRepDecayIntel monthlyRepDecayIntel = new DRO_MonthlyRepDecayIntel(tooltipMap);

    }

    private void monthlyRepDecay(FactionAPI f){
        //Creating variables
        Float repLoss = 0f;

        //Decay calculation goes here

        //Updating rep
        f.adjustRelationship(playerFaction.getId(), repLoss);

        //Updating maps
        tooltipMap.put(f, repLoss);
        repMap.put(f, f.getRelationship(playerFaction.getId()));
    }

    public void reportEconomyTick(int iterIndex) {}
}