package net.bteuk.plotsystem.events;

import java.util.Arrays;

import static net.bteuk.plotsystem.PlotSystem.LOGGER;

public class EventManager {

    public static void event(String uuid, String[] event) {

        LOGGER.info("Event: " + Arrays.toString(event));

        //Start the execution process by looking at the event message structure.
        switch (event[0]) {
            case "teleport" -> TeleportEvent.event(uuid, event);
            case "submit" -> SubmitEvent.event(uuid, event);
            case "retract" -> RetractEvent.event(uuid, event);
            case "delete" -> DeleteEvent.event(uuid, event);
            case "leave" -> LeaveEvent.event(uuid, event);
            case "claim" -> ClaimEvent.event(uuid, event);
            case "review" -> ReviewEvent.event(uuid, event);
            case "join" -> JoinEvent.event(uuid, event);
            case "kick" -> KickEvent.event(uuid, event);
            case "close" -> CloseEvent.event(uuid, event);
            case "outlines" -> OutlinesEvent.event(uuid, event);
        }
    }
}
