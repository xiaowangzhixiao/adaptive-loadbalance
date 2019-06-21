package com.aliware.tianchi;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ServerStatus
 */
public class ProviderStatus {

    public String name;

    public int maxCurrent;

    public AtomicInteger current = new AtomicInteger(0);

    public String encode() {
        return name + "," + current.get() + "," + maxCurrent;
    }

    public ProviderStatus(String name, int current, int maxCurrent) {
        this.name = name;
        this.current.set(current);
        this.maxCurrent = maxCurrent;
    }

    public ProviderStatus(String status) {
        String[] strings = status.split(",");
        if (strings.length < 3) {
            this.name = null;
            this.current.set(0);
            this.maxCurrent = 0;
        } else {
            this.name = strings[0];
            this.current.set(Integer.parseInt(strings[1]));
            this.maxCurrent = Integer.parseInt(strings[2]);
        }
    }
}