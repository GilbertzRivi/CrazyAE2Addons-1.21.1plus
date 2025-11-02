package net.oktawia.crazyae2addons;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static String toTitle(String id) {
        StringBuilder out = new StringBuilder();

        for (String part : id.split("_")) {
            if (part.isEmpty()) continue;

            if (part.chars().anyMatch(Character::isDigit)) {
                out.append(part.toUpperCase());
            } else {
                out.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase());
            }
            out.append(' ');
        }
        return out.toString().trim();
    }
    public static void asyncDelay(Runnable function, float delay) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        long delayInMillis = (long) (delay * 1000);
        scheduler.schedule(() -> {
            try {
                function.run();
            } finally {
                scheduler.shutdown();
            }
        }, delayInMillis, TimeUnit.MILLISECONDS);
    }
}
