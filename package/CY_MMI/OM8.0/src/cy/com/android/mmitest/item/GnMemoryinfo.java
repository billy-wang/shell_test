/* Gionee huangjianqiang 20160125 add for CR01629117 begin */
package cy.com.android.mmitest.item;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Formatter;
import cy.com.android.mmitest.utils.DswLog;

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
    private static double mTotalSize;
    private static ArrayList<String> mList = new ArrayList<String>();
    private boolean DEBUG = false;
    double[] array = new double []{
            0.5, 1, 1.5, 2,
            3, 4, 6, 8,
            10, 12, 14, 16,
            32, 64, 128, 256};
    public String getTotalRam(Context context) {
        String str1 = "/proc/meminfo";
        String str2;
        String[] arrayOfString;
        double initial_memory = 0;

        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            str2 = localBufferedReader.readLine();

            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                if (DEBUG) {
                    //DswLog.i(str2, num + "\t");
                }
            }

            initial_memory = (double) Long.parseLong(arrayOfString[1]) / 1024 / 1024;
            localBufferedReader.close();

        } catch (IOException e) {
        }


        DswLog.d(TAG, "getTotalRam()  initial_memory= " + initial_memory);

        return translateCapacity(initial_memory);
    }


    public String getTotalRom(Context context) {
        long totalSize;
        long systemStorageSize;
        String lineStr;

        mPartitionInfor = readFile(PARTITION_FILE_PATH);

        if (DEBUG) {
            DswLog.d(TAG, "readRomSize()  size= " + mPartitionInfor.size());
        }

        for (int i = 0; i < mPartitionInfor.size(); i++) {
            lineStr = mPartitionInfor.get(i).trim();
            int nIndex = lineStr.indexOf(TOTAL_SIZE);
            int n = 0;

            if (DEBUG) {
                DswLog.d(TAG, "lineStr= " + lineStr + ",nIndex= " + nIndex);
            }
            if (nIndex > 2 && (lineStr.length() == nIndex + TOTAL_SIZE.length())) {
                String[] strSplit = lineStr.split(" ");
                for (String str : strSplit) {
                    if (!TextUtils.isEmpty(str) && (n++ == 2)) {
                        if (DEBUG) {
                            DswLog.d(TAG, "str= " + str);
                        }
                        mTotalSize = (double) Long.parseLong(str) / 1024 / 1024 ;
                    }
                }

                if (DEBUG) {
                    DswLog.d(TAG, "readRomSize()  mTotalSize= " + mTotalSize);
                }
            }

        }
        return translateCapacity(mTotalSize);
    }

    private String translateCapacity(double capacity) {
        String result = "1G";

        if (capacity <=0) {
            return "ERROR";
        }
        for (int i=0; i < array.length; i++) {
            if (capacity <= array[i]) {
                result = array[i] + "G";
                break;
            }
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
                    DswLog.d(TAG, "file in " + path + " does not exist!");
                }
                return null;
            }
            br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                if (DEBUG) {
                    //DswLog.d(TAG," read line "+line);
                }
                mList.add(line);
            }
            return mList;
        } catch (IOException io) {
            DswLog.d(TAG, "IOException");
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