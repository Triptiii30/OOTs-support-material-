package com.smart.manufacturing.util;

import org.springframework.stereotype.Component;

/**
 * ArrayDemoService — Demonstrates 1D, 2D, and jagged arrays for production planning.
 */
@Component
public class ArrayDemoService {

    private static final int MONTHS = 12;
    private static final int WORKSTATIONS = 4;
    private static final int SHIFTS = 3;

    // 1D array — monthly targets
    public int[] getMonthlyProductionTargets() {
        return new int[]{100, 90, 110, 120, 130, 140, 135, 125, 115, 120, 100, 150};
    }

    // 2D array — capacity[workstation][shift]
    public int[][] getWorkstationCapacityMatrix() {
        int[][] c = new int[WORKSTATIONS][SHIFTS];
        c[0][0]=20; c[0][1]=18; c[0][2]=12; // Assembly
        c[1][0]=15; c[1][1]=15; c[1][2]=15; // Machining
        c[2][0]=10; c[2][1]=10; c[2][2]=0;  // Painting
        c[3][0]=25; c[3][1]=30; c[3][2]=20; // QC
        return c;
    }

    // Jagged array — different stages per production line
    public int[][] getProductionLineStageCapacities() {
        int[][] jagged = new int[3][];
        jagged[0] = new int[]{50, 45, 40, 38, 35}; // Line 0: 5 stages
        jagged[1] = new int[]{30, 28, 25};           // Line 1: 3 stages
        jagged[2] = new int[]{20, 18};               // Line 2: 2 stages
        return jagged;
    }

    public int computeAnnualTarget() {
        int[] m = getMonthlyProductionTargets();
        int sum = 0;
        for (int t : m) sum += t;
        return sum;
    }

    public int getPeakShiftForWorkstation(int ws) {
        int[][] matrix = getWorkstationCapacityMatrix();
        if (ws < 0 || ws >= WORKSTATIONS) throw new IllegalArgumentException("Invalid workstation: " + ws);
        int peak = 0;
        for (int s = 1; s < SHIFTS; s++) if (matrix[ws][s] > matrix[ws][peak]) peak = s;
        return peak;
    }
}
