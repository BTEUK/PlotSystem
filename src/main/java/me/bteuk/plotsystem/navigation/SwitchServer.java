package me.bteuk.plotsystem.navigation;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.utils.User;

public class SwitchServer {

    public static void toServer(User u, String server) {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("Connect");
        out.writeUTF(server);

        u.player.sendPluginMessage(PlotSystem.getInstance(), "BungeeCord", out.toByteArray());

    }
}
