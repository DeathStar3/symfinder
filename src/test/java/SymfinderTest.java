import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SymfinderTest {

    @Test
    public void dateFormatterTest() {
        long oneMillisecond = 1;
        long oneSecond = 1000 * oneMillisecond;
        long oneMinute = 60 * oneSecond;
        long oneHour = 60 * oneMinute;
        long oneDay = 24 * oneHour;
        assertEquals("00:00:00.001", Symfinder.formatExecutionTime(oneMillisecond));
        assertEquals("00:00:01.000", Symfinder.formatExecutionTime(oneSecond));
        assertEquals("00:00:10.000", Symfinder.formatExecutionTime(10 * oneSecond));
        assertEquals("00:01:00.000", Symfinder.formatExecutionTime(oneMinute));
        assertEquals("00:01:05.000", Symfinder.formatExecutionTime(oneMinute + 5 * oneSecond));
        assertEquals("01:00:00.000", Symfinder.formatExecutionTime(oneHour));
        assertEquals("24:00:00.000", Symfinder.formatExecutionTime(oneDay));
        assertEquals("01:01:01.001", Symfinder.formatExecutionTime(oneHour + oneMinute + oneSecond + oneMillisecond));
        assertEquals("25:01:01.001", Symfinder.formatExecutionTime(oneDay + oneHour + oneMinute + oneSecond + oneMillisecond));
    }

}