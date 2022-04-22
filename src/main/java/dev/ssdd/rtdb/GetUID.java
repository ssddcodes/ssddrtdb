package dev.ssdd.rtdb;

import java.util.Random;

public class GetUID {

    private static final int[] lastRandChars = new int[12];
    private static final Random randGen = new Random();
    private static final String PUSH_CHARS = "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz";
    private static long lastPushTime;

    public static synchronized String generateUID(long now) {
        boolean duplicateTime = (now == lastPushTime);
        lastPushTime = now;

        char[] timeStampChars = new char[8];
        StringBuilder result = new StringBuilder(20);
        for (int i = 7; i >= 0; i--) {
            timeStampChars[i] = PUSH_CHARS.charAt((int) (now % 64));
            //  Log.d("MainAc", "generatePushChildName: "+ PUSH_CHARS.charAt((int) (now % 64)));
            now = now / 64;
        }
        hardAssert(now == 0);

        result.append(timeStampChars);

        if (!duplicateTime) {
            for (int i = 0; i < 12; i++) {
                lastRandChars[i] = randGen.nextInt(64);
            }
        } else {
            incrementArray();
        }
        for (int i = 0; i < 12; i++) {
            result.append(PUSH_CHARS.charAt(lastRandChars[i]));
        }
        hardAssert(result.length() == 20);
        return result.toString();
    }

    public static void hardAssert(boolean condition) {
        hardAssert(condition, "");
    }

    private static void incrementArray() {
        for (int i = 11; i >= 0; i--) {
            if (lastRandChars[i] != 63) {
                lastRandChars[i] = lastRandChars[i] + 1;
                return;
            }
            lastRandChars[i] = 0;
        }
    }

    public static void hardAssert(boolean condition, String message) {
        if (!condition) {
            System.out.println("Assertion failed " + message);
        }
    }
}
