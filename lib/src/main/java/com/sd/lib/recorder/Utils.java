package com.sd.lib.recorder;

import java.io.File;

class Utils
{
    public static File createDefaultFileUnderDir(File dir, String ext)
    {
        if (dir == null)
            return null;

        if (ext == null)
            ext = "";

        try
        {
            long current = System.currentTimeMillis();
            File file = new File(dir, current + ext);
            while (file.exists())
            {
                current++;
                file = new File(dir, current + ext);
            }
            return file;
        } catch (Exception e)
        {
            return null;
        }
    }

    public static boolean deleteFileOrDir(File path)
    {
        if (path == null || !path.exists())
            return true;

        if (path.isFile())
            return path.delete();

        final File[] files = path.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                deleteFileOrDir(file);
            }
        }
        return path.delete();
    }
}
