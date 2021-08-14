package com.e.rhino;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Random;

public class Tools {

    // format the seconds to look like: 13:10
    public static String getTimeFromSeconds(long seconds) {
        Date dt = new Date(seconds * 1000);
        String pattern = (seconds > 3600) ? "hh:mm:ss" : "mm:ss";
        String time = new SimpleDateFormat(pattern).format(dt);
        return time;
    }

    public static int keepInRange(int val, int min, int max)
    {
        // check min
        int rc = val;

        if (val < min) // check the min
            rc = min;
        else if (val > max) // check the max
            rc = max;

        return rc;
    }

    public static int min(int val1, int val2)
    {
        int rc = (val1 < val2) ? val1 : val2;

        return rc;
    }

    public static int max(int val1, int val2)
    {
        int rc = (val1 > val2) ? val1 : val2;

        return rc;
    }

    public static String getRandomString(String ... msgs)
    {
        return msgs[new Random().nextInt(msgs.length)];
    }

    public static Bitmap getThumbnail(Resources res, int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
