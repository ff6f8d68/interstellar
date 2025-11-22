package team.nextlevelmodding.ar2;

public interface MasterCallListener {
    /**
     * Called when a MasterCallEvent is posted on the MasterCallBus.
     */
    void onMasterCall(MasterCallEvent event);
}

