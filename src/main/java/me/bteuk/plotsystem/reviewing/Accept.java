package me.bteuk.plotsystem.reviewing;

import me.bteuk.plotsystem.PlotSystem;

public class Accept {

    //Reviewing values.
    public int accuracy;
    public int quality;

    public Accept() {

        accuracy = 1;
        quality = 1;

    }

    public double accuracyMultiplier() {

        return (1 + (accuracy-3) * PlotSystem.getInstance().getConfig().getDouble("accuracy_multiplier"));

    }

    public double qualityMultiplier() {

        return (1 + (quality-3) * PlotSystem.getInstance().getConfig().getDouble("quality_multiplier"));

    }
}
