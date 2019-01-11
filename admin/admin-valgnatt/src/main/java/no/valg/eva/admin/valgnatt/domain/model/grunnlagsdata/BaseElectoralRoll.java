package no.valg.eva.admin.valgnatt.domain.model.grunnlagsdata;

import java.io.Serializable;

import no.valg.eva.admin.configuration.domain.model.valgnatt.ElectoralRollCount;

public abstract class BaseElectoralRoll implements Serializable {

    private String areaId;
    private String areaName;
    
    protected int voterTotal;

    public BaseElectoralRoll(String id, String name) {
        this.areaId = id;
        this.areaName = name;
    }

    protected void incrementNoOfVoters(ElectoralRollCount electoralRollCount) {
        voterTotal += electoralRollCount.getVoterTotal();
    }

    public int getVoterTotal() {
        return voterTotal;
    }

    public String getAreaId() {
        return areaId;
    }

    public String getAreaName() {
        return areaName;
    }
}
