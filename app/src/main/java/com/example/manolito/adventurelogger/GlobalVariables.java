package com.example.manolito.adventurelogger;

/**
 * Created by Manolito on 2016-04-05.
 */
public class GlobalVariables {
    //the state of the bluetooth adapter
    public static BTStatus status = BTStatus.NOT_PAIRED;

    public enum BTStatus {
        NOT_PAIRED,
        ATTEMPTING,
        PAIRING,
        PAIRED
    }
}
