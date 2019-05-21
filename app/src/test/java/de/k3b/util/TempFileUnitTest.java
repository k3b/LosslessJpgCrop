package de.k3b.util;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class TempFileUnitTest {
    private static final String FILE_NAME = "hello" + TempFileUtil.TEMP_FILE_SUFFIX;

    @Test
    public void shouldDeleteOldTempFile() {
        long fileDate = getTime(13, 1);
        long now = getTime(15,2); // delta: 2'01
        assertEquals(true,
                TempFileUtil.shouldDeleteTempFile(FILE_NAME, fileDate, now));
    }

    @Test
    public void shouldNotDeleteOldTempFile() {
        long fileDate = getTime(13, 1);
        long now = getTime(14,59); // delta: 1'58
        assertEquals(false,
                TempFileUtil.shouldDeleteTempFile(FILE_NAME, fileDate, now));
    }

    private long getTime(int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, hour);
        c.set(Calendar.MINUTE, hour);
        return c.getTimeInMillis();
    }
}