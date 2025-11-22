package team.nextlevelmodding.ar2;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple manual event bus for MasterCallEvent. Listeners implement MasterCallListener and
 * register themselves via MasterCallBus.register(listener). Posting an event will invoke
 * all listeners in registration order and stop early if a listener cancels the event.
 */
public final class MasterCallBus {
    private static final CopyOnWriteArrayList<team.nextlevelmodding.ar2.MasterCallListener> LISTENERS = new CopyOnWriteArrayList<>();

    private MasterCallBus() { /* utility */ }

    public static void register(team.nextlevelmodding.ar2.MasterCallListener listener) {
        if (listener == null) return;
        if (!LISTENERS.contains(listener)) LISTENERS.add(listener);
    }

    public static void unregister(team.nextlevelmodding.ar2.MasterCallListener listener) {
        if (listener == null) return;
        LISTENERS.remove(listener);
    }

    /**
     * Post the event to all listeners. If any listener calls event.setCanceled(true) the
     * dispatch will stop and this method returns true (indicating cancellation).
     *
     * @param event the event to dispatch
     * @return true if the event was canceled by any listener
     */
    public static boolean post(MasterCallEvent event) {
        if (event == null) return false;
        for (team.nextlevelmodding.ar2.MasterCallListener l : LISTENERS) {
            try {
                l.onMasterCall(event);
            } catch (Throwable t) {
                ar2.LOGGER.error("Error while dispatching MasterCallEvent to listener {}", l.getClass().getName(), t);
            }
            if (event.isCanceled()) return true;
        }
        return event.isCanceled();
    }
}
