/* Gionee huangjianqiang 20160125 add for CR01629117 begin */
package gn.com.android.mmitest.item;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class GnMemoryinfo {

    private static String TAG = "GnMemoryinfo";

    private ArrayList<String> mPartitionInfor;
    private static final String PARTITION_FILE_PATH = "/proc/partitions";
    private static final String TOTAL_SIZE = "mmcblk0";
    private static final String SYSTEM_STORAGE_SIZE = "mmcblk0p6";
    private static long mTotalSize;
    private static long mSystemStorageSize;
    private static ArrayList<String> mList = new ArrayList<String>();
    private boolean DEBUG = false;

    public String getTotalRam(Context context) {
        String str1 = "/proc/meminfo";
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;

        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            str2 = localBufferedReader.readLine();

            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                if (DEBUG) {
                    //Log.i(str2, num + "\t");
                }
            }

            initial_memory = Long.parseLong(arrayOfString[1]) * 1024;
            localBufferedReader.close();

        } catch (IOException e) {
        }

        if (DEBUG) {
            Log.d(TAG, "getTotalRam()  initial_memory= " + initial_memory);
        }
        return translateCapacity(initial_memory);
    }


    public String getTotalRom(Context context) {
        long totalSize;
        long systemStorageSize;
        String lineStr;

        mPartitionInfor = readFile(PARTITION_FILE_PATH);

        if (DEBUG) {
            Log.d(TAG, "readRomSize()  size= " + mPartitionInfor.size());
        }

        for (int i = 0; i < mPartitionInfor.size(); i++) {
            lineStr = mPartitionInfor.get(i).trim();
            int nIndex = lineStr.indexOf(TOTAL_SIZE);
            int n = 0;

            if (DEBUG) {
                Log.d(TAG, "lineStr= " + lineStr + ",nIndex= " + nIndex);
            }
            if (nIndex > 2 && (lineStr.length() == nIndex + TOTAL_SIZE.length())) {
                String[] strSplit = lineStr.split(" ");
                for (String str : strSplit) {
                    if (!TextUtils.isEmpty(str) && (n++ == 2)) {
                        if (DEBUG) {
                            Log.d(TAG, "str= " + str);
                        }
                        mTotalSize = Long.parseLong(str) * 1024;
                    }
                }
                //mTotalSize = translateCapacity(mTotalSize);

                if (DEBUG) {
                    Log.d(TAG, "readRomSize()  mTotalSize= " + mTotalSize);
                }
            }

        }
        return translateCapacity(mTotalSize);
    }

    private String translateCapacity(long capacity) {
        String result = "1G";

        if (DEBUG) {
            Log.d(TAG, "translateCapacity()  capacity= " + capacity);
        }

        if (capacity <= 536870912L) {
            result = "512M";/*512M*/
        } else if (capacity <= 1073741824L) {
            result = "1.0G";/*1G*/
        } else if (capacity <= 1610612736L) {
            result = "1.5G";/*1.5G*/
        } else if (capacity <= 2147483648L) {
            result = "2.0G";/*2G*/
        } else if (capacity <= 3221225472L) {
            result = "3.0G";/*3G*/
        } else if (capacity <= 4294967296L) {
            result = "4.0G";/*4G*/
        } else if (capacity <= 8589934592L) {
            result = "8.0G";/*8G*/
        } else if (capacity <= 17179869184L) {
            result = "16.0G";/*16G*/
        } else if (capacity <= 34359738368L) {
            result = "32.0G";/*32G*/
        } else if (capacity <= 68719476736L) {
            result = "64.0G";/*64G*/
        } else if (capacity <= 137438953472L) {
            result = "128.0G";/*128G*/
        }
        return result;
    }

    private ArrayList<String> readFile(String path) {
        mList.clear();
        File file = new File(path);
        FileReader fr = null;
        BufferedReader br = null;
        try {
            if (file.exists()) {
                fr = new FileReader(file);
            } else {
                if (DEBUG) {
                    Log.d(TAG, "file in " + path + " does not exist!");
                }
                return null;
            }
            br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                if (DEBUG) {
                    //Log.d(TAG," read line "+line);
                }
                mList.add(line);
            }
            return mList;
        } catch (IOException io) {
            Log.d(TAG, "IOException");
            io.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
        return null;
    }

}
/* Gionee huangjianqiang 20160125 add for CR01629117 end */