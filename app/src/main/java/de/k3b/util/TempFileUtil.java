package de.k3b.util;

import java.io.File;

public class TempFileUtil {
    public static final String TEMP_FILE_SUFFIX = "_llcrop.jpg";

    // temp file will be deleted after 2 hours
    private static final long TEMP_FILE_LIFETIME_IN_MILLI_SECS = 1000 * 60 * 60 * 2;

    /**
     * #11: remove unused temporary crops from send/get_content after some time.
     * */
    public static void removeOldTempFiles(File dir, long nowInMilliSecs) {
        for (File candidate : dir.listFiles()) {
            if (candidate.isFile() &&  shouldDeleteTempFile(candidate, nowInMilliSecs)) {
                candidate.delete();
            }
        }
    }

    private static boolean shouldDeleteTempFile(File candidate, long nowInMilliSecs) {
        if (candidate == null) return false;
        return shouldDeleteTempFile(candidate.getName(), candidate.lastModified(), nowInMilliSecs);
        //
    }

    // static package to allow unit testing
    static boolean shouldDeleteTempFile(String fileName, long lastModifiedInMilliSecs, long nowInMilliSecs) {
        if ((fileName == null) || (lastModifiedInMilliSecs == 0)) return false;

        final long candidateAgeInMilliSecs = nowInMilliSecs - lastModifiedInMilliSecs;
        return (candidateAgeInMilliSecs > TEMP_FILE_LIFETIME_IN_MILLI_SECS)
                && fileName.endsWith(TEMP_FILE_SUFFIX);
    }

    public static String getLastPath(String originalFileName) {
        if (originalFileName != null) {
            int splitPos =  originalFileName.lastIndexOf(File.separatorChar);
            if (splitPos == -1) splitPos =  originalFileName.lastIndexOf('/');
            if (splitPos >= 0) originalFileName = originalFileName.substring(splitPos + 1);
        }
        return originalFileName;
    }


}
