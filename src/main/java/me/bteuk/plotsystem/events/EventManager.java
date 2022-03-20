package me.bteuk.plotsystem.events;

public class EventManager {

    public static void event(String uuid, String[] event) {

        //Start the execution process by looking at the event message structure.
        if (event[0].equals("teleport")) {

            TeleportEvent.event(uuid, event);

        }
        else if (event[0].equals("submit")) {

            SubmitEvent.event(uuid, event);

        }
        else if (event[0].equals("retract")) {

            RetractEvent.event(uuid, event);

        }
        else if (event[0].equals("delete")) {



        }
        else if (event[0].equals("leave")) {



        }

    }

}
