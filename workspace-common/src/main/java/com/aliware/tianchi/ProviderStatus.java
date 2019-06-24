package com.aliware.tianchi;


/**
 * ServerStatus
 */
public class ProviderStatus {

    public int name;

    public int maxCurrent;

    public int current = 0;

    public String encode() {
        return name + "," + current + "," + maxCurrent;
    }

    public ProviderStatus(int name, int current, int maxCurrent) {
        this.name = name;
        this.current = current;
        this.maxCurrent = maxCurrent;
    }

    public static ProviderStatus decode(String status) {
        String[] strings = status.split(",");
        if (strings.length < 3) {
            return null;
        } else {
            return new ProviderStatus(Integer.parseInt(strings[0]), Integer.parseInt(strings[1]), Integer.parseInt(strings[2]));
        }
    }
}