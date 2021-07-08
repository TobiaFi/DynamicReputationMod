package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.intel.DRM_MonthlyRepDecayIntel;
import com.fs.starfarer.api.util.Misc;

import java.util.*;

public class DRM_RepDecay implements EconomyTickListener {

    public FactionAPI playerFaction;
    public LinkedHashMap<FactionAPI, Float> repMap = new LinkedHashMap<>();
    public LinkedHashMap<FactionAPI, Float> tooltipMap = new LinkedHashMap<>();

    public DRM_RepDecay() {
        TreeMap<String, FactionAPI> sortingMap = new TreeMap<>();

        //Sorts all factions based on DisplayName
        for (FactionAPI f : Global.getSector().getAllFactions()) {
            if (f.isPlayerFaction()) { playerFaction = f; }
            else { sortingMap.put(f.getDisplayName(), f); }
        }

        //Adds factions to repMap in alphabetical order
        for (String s : sortingMap.keySet()) {
            FactionAPI f = sortingMap.get(s);
            repMap.put(f, f.getRelationship(playerFaction.getId()));
        }
    }


    public void reportEconomyMonthEnd() {
        tooltipMap.clear();

        //Iterates through repMap and applies decay to visible factions only, to avoid issues with mods that add factions mid-game
        for (FactionAPI f : repMap.keySet()) {
            if (f.isShowInIntelTab()) { monthlyRepDecay(f); }
        }

        if (!tooltipMap.isEmpty()) {
            Global.getSector().getIntelManager().addIntel(new DRM_MonthlyRepDecayIntel(tooltipMap));
        }
    }

    private void monthlyRepDecay(FactionAPI f) {
        //For rep calculation
        float previousRep = repMap.get(f);
        float repLoss = 0f;
        FactionAPI commissionFaction = Misc.getCommissionFaction();

        //For tooltip purposes
        int previousRepInt = f.getRepInt(playerFaction.getId());
        int newRepInt;

        //Basic decay calculation
        if (previousRep > 0.25f) { repLoss = -previousRep * 0.10f; }
        else if (previousRep < -0.25f) { repLoss = -previousRep * 0.05f; }

        //Different calculation if player is being commissioned
        if (commissionFaction != null) {
            if (f.equals(Misc.getCommissionFaction())) {
                repLoss = -(previousRep - 0.25f) * 0.10f; //Target is commissioning faction
            } else if (f.isHostileTo(commissionFaction) && f.getRelToPlayer().getRel() != -0.50f) {
                repLoss = -(previousRep + 0.50f) * 0.05f; //Target is hostile to commissioning faction
            } else if (f.getRelationshipLevel(commissionFaction).isPositive() && f.getRelToPlayer().getRel() > 0.50f) {
                repLoss = -previousRep * 0.05f; //Target is allied to commissioning faction and has positive rep with player
            } else if (f.getRelationshipLevel(commissionFaction).isPositive() && f.getRelToPlayer().getRel() < 0f) {
                repLoss = -previousRep * 0.10f; //Target is allied to commissioning faction and has negative rep with player
            }
        }

        //Current rep doesn't drop below 25/50/75 if above those thresholds at month end (safety net to maintain relationship status)
        float floor = Math.abs(f.getRelationship(playerFaction.getId())) % 0.25f;
        if (floor != 0) { repLoss = Math.signum(repLoss) * Math.min(Math.abs(repLoss), floor); }

        //Updating rep in memory
        f.adjustRelationship(playerFaction.getId(), repLoss);
        newRepInt = f.getRepInt(playerFaction.getId());

        //Updating maps. tooltipMap is only updated if the tooltip can show a whole number greater than 0
        if (previousRepInt - newRepInt != 0) { tooltipMap.put(f, repLoss); }
        repMap.put(f, f.getRelationship(playerFaction.getId()));
    }

    //Necessary for interface implementation
    public void reportEconomyTick(int iterIndex) {}
}