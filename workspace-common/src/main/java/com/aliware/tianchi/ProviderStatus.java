package com.aliware.tianchi;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ServerStatus
 */
public class ProviderStatus {

    public int name;

    public AtomicInteger current = new AtomicInteger(0);

    public String encode() {
        return name + "," + current;
    }

    public ProviderStatus(int name, int current) {
        this.name = name;
        this.current.set(current);
        
    }

    public static ProviderStatus decode(String status) {
        String[] strings = status.split(",");
        if (strings.length < 2) {
            return null;
        } else {
            return new ProviderStatus(Integer.parseInt(strings[0]), Integer.parseInt(strings[1]));
        }
    }
}