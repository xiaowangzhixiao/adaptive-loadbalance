package com.aliware.tianchi;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ServerStatus
 */
public class ProviderStatus {

    public int name;

    public int maxCurrent;

    public AtomicInteger current = new AtomicInteger(0);

    public String encode() {
        return name + "," + current + "," + maxCurrent;
    }

    public ProviderStatus(int name, int current, int maxCurrent) {
        this.name = name;
        this.current.set(current);
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