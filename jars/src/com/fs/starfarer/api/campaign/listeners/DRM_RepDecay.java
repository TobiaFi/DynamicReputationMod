package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.intel.DRM_MonthlyRepDecayIntel;
import com.fs.starfarer.api.impl.campaign.intel.DRM_MonthlySecondDegreeRepIntel;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;

public class DRM_RepDecay implements EconomyTickListener {

    public String playerFactionID;
    public LinkedHashMap<FactionAPI, Float> repMap = new LinkedHashMap<>();
    public LinkedHashMap<FactionAPI, Float> tooltipMap = new LinkedHashMap<>();
    public LinkedHashMap<FactionAPI, LinkedHashMap<FactionAPI, Float>> secondDegreeRepMap = new LinkedHashMap<>();

    public DRM_RepDecay() {
        TreeMap<String, FactionAPI> sortingMap = new TreeMap<>();

        //Sorts all factions based on DisplayName
        for (FactionAPI f : Global.getSector().getAllFactions()) {
            if (f.isPlayerFaction()) { playerFactionID = f.getId(); }
            else { sortingMap.put(f.getDisplayName(), f); }
        }

        //Adds factions to repMap in alphabetical order
        for (String s : sortingMap.keySet()) {
            FactionAPI f = sortingMap.get(s);
            repMap.put(f, f.getRelToPlayer().getRel());
        }
    }


    public void reportEconomyMonthEnd() {
        tooltipMap.clear();
        secondDegreeRepMap.clear();

        //Iterates through repMap and applies decay to visible factions only, to avoid issues with mods that add factions mid-game
        for (FactionAPI f : repMap.keySet()) {
            if (f.isShowInIntelTab()) {
                monthlyRepDecay(f);
                secondDegreeRep(f);
            }
        }

        if (!tooltipMap.isEmpty()) {
            Global.getSector().getIntelManager().addIntel(new DRM_MonthlyRepDecayIntel(tooltipMap));
        }

        if (!secondDegreeRepMap.isEmpty()) {
            secondDegreeRepAdjust(secondDegreeRepMap);
            for (FactionAPI f : secondDegreeRepMap.keySet()) {
                Global.getSector().getIntelManager().addIntel(new DRM_MonthlySecondDegreeRepIntel(f, secondDegreeRepMap.get(f)));
            }
        }
    }

    private void monthlyRepDecay(FactionAPI f) {
        //For rep calculation
        float previousRep = repMap.get(f);
        float repLoss = 0f;
        FactionAPI commissionFaction = Misc.getCommissionFaction();

        //For tooltip purposes
        int previousRepInt = f.getRelToPlayer().getRepInt();
        int newRepInt;

        //Basic decay calculation
        if (previousRep > 0.25f) { repLoss = -previousRep * ( 0.10f - contactRep(f) ); }
        else if (previousRep < -0.25f) { repLoss = -previousRep * ( 0.05f + contactRep(f) ); }

        //Different calculation if player is being commissioned
        if (commissionFaction != null) {
            if (f.equals(Misc.getCommissionFaction())) {
                repLoss = -(previousRep - 0.25f) * ( 0.10f - contactRep(f) ); //Target is commissioning faction
            } else if (f.isHostileTo(commissionFaction) && f.getRelToPlayer().getRel() != -0.50f) {
                repLoss = -(previousRep + 0.50f) * ( 0.05f + contactRep(f) ); //Target is hostile to commissioning faction
            } else if (f.getRelationshipLevel(commissionFaction).isPositive() && f.getRelToPlayer().getRel() > 0.50f) {
                repLoss = -previousRep * ( 0.05f - contactRep(f) ); //Target is allied to commissioning faction and has positive rep with player
            } else if (f.getRelationshipLevel(commissionFaction).isPositive() && f.getRelToPlayer().getRel() < 0f) {
                repLoss = -previousRep * ( 0.10f + contactRep(f) ); //Target is allied to commissioning faction and has negative rep with player
            }
        }

        //Current rep doesn't drop below 25/50/75 if above those thresholds at month end (safety net to maintain relationship status)
        float floor = Math.abs(f.getRelToPlayer().getRel()) % 0.25f;
        if (floor != 0) { repLoss = Math.signum(repLoss) * Math.min(Math.abs(repLoss), floor); }

        //Updating rep in memory
        f.adjustRelationship(playerFactionID, repLoss);
        newRepInt = f.getRelToPlayer().getRepInt();

        //Updating maps. tooltipMap is only updated if the tooltip can show a whole number greater than 0
        if (previousRepInt - newRepInt != 0) { tooltipMap.put(f, repLoss); }
        repMap.put(f, f.getRelToPlayer().getRel());
    }

    private float contactRep(FactionAPI f) {
        //For rep calculation
        float contactLevel = 0f;
        float baseContactScaling = 0.25f;
        float highestContactRep = 0f;
        float contactRepDecay;
        ArrayList<PersonAPI> contactList = new ArrayList<>(); //Technically pointless, might be useless for tooltips in the future

        //Gets the full list of contacts of the highest importance that the player has in faction f
        for (IntelInfoPlugin c : Global.getSector().getIntelManager().getIntel(ContactIntel.class)) {
            PersonAPI p = ((ContactIntel)c).getPerson();
            if (!p.getFaction().equals(f)) { continue; }
            if (p.getImportance().getValue() > contactLevel) {
                contactList.clear();
                contactLevel = p.getImportance().getValue();
                contactList.add(p);
            }
            if (p.getImportance().getValue() == contactLevel) { contactList.add(p); }
        }

        //Finds the highest rep level between the contacts with the highest importance
        for (PersonAPI p : contactList) {
            if (p.getRelToPlayer().getRel() > highestContactRep) { highestContactRep = p.getRelToPlayer().getRel(); }
        }

        //Final calculation. Contribution varies from 0% to 2.5%
        contactRepDecay = contactLevel * baseContactScaling * highestContactRep;
        return contactRepDecay;
    }

    public void secondDegreeRep (FactionAPI f) {
        Float hostileRepAdjust = -0.05f;
        Float friendlyRepAdjust = 0.02f;
        LinkedHashMap<FactionAPI, Float> relationsMap = new LinkedHashMap<>();

        //Only gets relations with factions the player is cooperative with
        if (f.getRelToPlayer().getRepInt() < 75) { return; }
        for (FactionAPI h : Global.getSector().getAllFactions()) {
            if (h.isShowInIntelTab() && !h.getId().equals("pirates") && !h.equals(f)) {
                if (h.getRepInt(f.getId()) > 0) { relationsMap.put(h, friendlyRepAdjust); }
                else if (h.getRepInt(f.getId()) < 0) { relationsMap.put(h, hostileRepAdjust); }
            }
        }

        //Updates map
        if (!relationsMap.isEmpty()) { secondDegreeRepMap.put(f, relationsMap); }
    }

    public void secondDegreeRepAdjust (LinkedHashMap<FactionAPI, LinkedHashMap<FactionAPI, Float>> map) {
        //Applies changes to reputation AFTER monthly decay
        for (FactionAPI f : map.keySet()) {
            for (FactionAPI h : map.get(f).keySet()) {
                h.adjustRelationship(playerFactionID, map.get(f).get(h));
                repMap.put(h, h.getRelToPlayer().getRel());
            }
        }
    }

    //Necessary for interface implementation
    public void reportEconomyTick(int iterIndex) {}
}