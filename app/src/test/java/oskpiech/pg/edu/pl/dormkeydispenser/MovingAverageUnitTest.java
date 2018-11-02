package oskpiech.pg.edu.pl.dormkeydispenser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class MovingAverageUnitTest {
    @Test
    public void zeroFirst() {
        MovingAverage ma = new MovingAverage(5);
        assertEquals(0, ma.getAverage(), 0.01);
    }

    @Test
    public void someValuesNotFull() {
        MovingAverage ma = new MovingAverage(5);
        ma.addSample(0.5d);
        ma.addSample(0.5d);
        assertEquals(ma.getAverage(), 0.5d, 0.01);
        ma.addSample(1.0d);
        ma.addSample(1.0d);
        assertEquals(ma.getAverage(), 0.75d, 0.01);
    }

    @Test
    public void runningCircles() {
        MovingAverage ma = new MovingAverage(5);
        for (int i = 0; i < 100; i++) {
            ma.addSample(1.0d);
        }
        assertEquals(ma.getAverage(), 1.0d, 0.01);
        for (int i = 0; i < 100; i++) {
            ma.addSample(0.25d);
        }
        assertEquals(ma.getAverage(), 0.25d, 0.01);
    }
}