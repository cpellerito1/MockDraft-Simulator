package com.example.fantasy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class Roster {
    // Catcher
    private long c;
    // First base
    private long b1;
    // Second base
    private long b2;
    // Short stop
    private long ss;
    // Third base
    private long b3;
    // Middle infielder (ss/2B)
    private long middle;
    // Corner infielder (1B/3B)
    private long corner;
    // Outfielders
    private long[] of = new long[4];
    // Util player (any batter)
    private long util;
    // Pitchers (both relief and starting)
    private long[] pitchers = new long[9];
    //Bench
    private ArrayList<Long> bench = new ArrayList<>();

    // Getters and Setters
    public long getC() {
        return c;
    }

    public void setC(long c) {
        this.c = c;
    }

    public long getB1() {
        return b1;
    }

    public void setB1(long b1) {
        this.b1 = b1;
    }

    public long getB2() {
        return b2;
    }

    public void setB2(long b2) {
        this.b2 = b2;
    }

    public long getSs() {
        return ss;
    }

    public void setSs(long ss) {
        this.ss = ss;
    }

    public long getB3() {
        return b3;
    }

    public void setB3(long b3) {
        this.b3 = b3;
    }

    public long getMiddle() {
        return middle;
    }

    public void setMiddle(long middle) {
        this.middle = middle;
    }

    public long getCorner() {
        return corner;
    }

    public void setCorner(long corner) {
        this.corner = corner;
    }

    public long[] getOf() {
        return of;
    }

    public void setOf(long[] of) {
        this.of = of;
    }

    public void addOf(long id) {
        for (int i = 0; i < 4; i++)
            if (of[i] == 0)
                of[i] = id;
    }

    public int checkOf() {
        int count = 0;
        for (int i = 0; i < 4; i++)
            if (of[i] > 0)
                count++;
        return count;
    }

    public long getUtil() {
        return util;
    }

    public void setUtil(long util) {
        this.util = util;
    }

    public long[] getPitchers() {
        return pitchers;
    }

    public void setPitchers(long[] pitchers) {
        this.pitchers = pitchers;
    }

    public int checkP() {
        int count = 0;
        for (int i = 0; i < 9; i++)
            if (pitchers[i] > 0)
                count++;
        return count;
    }

    public void addP(long id) {
        for (int i = 0; i < 9; i++)
            if (pitchers[i] == 0)
                pitchers[i] = id;
    }

    public ArrayList<Long> getBench() {
        return bench;
    }

    public void setBench(ArrayList<Long> bench) {
        this.bench = bench;
    }

    public void addBench(long id) {
        this.bench.add(id);
    }

    public void setPosition(String position, long id) {
        switch (position) {
            case "1B" -> setB1(id);
            case "2B" -> setB2(id);
            case "3B" -> setB3(id);
            case "SS" -> setSs(id);
            case "C" -> setC(id);
            case "OF" -> addOf(id);
            case "SP", "RP" -> addP(id);
            case "util" -> setUtil(id);
            case "mid" -> setMiddle(id);
            case "corner" -> setCorner(id);
        }
    }

    public List<String> needs() {
        HashSet<String> set = new HashSet<>();
        if (getB1() == 0)
            set.add("1B");
        if (getB2() == 0)
            set.add("2B");
        if (getB3() == 0)
            set.add("3B");
        if (getSs() == 0)
            set.add("SS");
        if (getC() == 0)
            set.add("C");
        if (checkOf() < 4)
            set.add("OF");
        if (checkP() < 9)
            set.addAll(List.of("SP", "RP"));
        if (getMiddle() == 0)
            set.add("mid");
        if (getCorner() == 0)
            set.add("corner");
        if (getUtil() == 0)
            set.add("util");

        return new ArrayList<>(set)  ;
    }
}