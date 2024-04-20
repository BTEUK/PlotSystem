package net.bteuk.plotsystem.exceptions;

/**
 * Exception that gets throws when a region can not be found.
 */
public class RegionNotFoundException extends Exception {
    public RegionNotFoundException(String error) {
        super(error);
    }
}
