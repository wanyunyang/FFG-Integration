package helpers;

/**
 * Singleton class, that contains helpers, related to data formatting for presentation.
 */
public abstract class FormatHelper {
    /**
     * Takes number of seconds, and formats them as a mm:ss string
     * @param seconds Duration in seconds.
     * @return Duration, formatted as 'mm:ss'
     */
    public static String SecToMMSS(int seconds) {
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }
}
