package com.aliware.tianchi;


/**
 * ServerStatus
 */
public class ProviderStatus {

    public String name;

    public int maxCurrent;

    public int current = 0;

    public String encode() {
        return name + "," + current + "," + maxCurrent;
    }

    public ProviderStatus(String name, int current, int maxCurrent) {
        this.name = name;
        this.current = current;
        this.maxCurrent = maxCurrent;
    }

    public ProviderStatus(String status) {
        String[] strings = status.split(",");
        if (strings.length < 3) {
            this.name = null;
            this.current = 0;
            this.maxCurrent = 0;
        } else {
            this.name = strings[0];
            this.current = Integer.parseInt(strings[1]);
            this.maxCurrent = Integer.parseInt(strings[2]);
        }
    }
}