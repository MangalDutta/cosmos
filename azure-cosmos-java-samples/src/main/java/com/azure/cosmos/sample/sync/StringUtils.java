package com.azure.cosmos.sample.sync;

public class StringUtils {

    public static String printStackTrace(final StackTraceElement[] stackTraceElements) {
        StringBuilder builder = new StringBuilder();
        for (final StackTraceElement ste : stackTraceElements) {
            builder.append(System.lineSeparator());
            builder.append(ste.toString());
        }
        //System.out.println(builder.toString());
        return builder.toString();
    }

}
