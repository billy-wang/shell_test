package cy.com.android.mmitest.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import cy.com.android.mmitest.utils.DswLog;
import cy.com.android.mmitest.TestResult;
import java.util.ArrayList;
import cy.com.android.mmitest.service.INvRamService;
import vendor.mediatek.hardware.nvram.V1_0.INvram;
import com.android.internal.util.HexDump;
import cy.com.android.mmitest.utils.ProinfoUtil;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import java.io.File;
import java.util.Arrays;
import cy.com.android.mmitest.CyMMITest;
import android.os.SystemProperties;
import java.util.List;
import android.content.Context;
import android.os.Environment;

import android.telephony.TelephonyManager;
/**
 * Created by qiang on 12/19/17.
 */
public class NvRamService extends Service {
    private final IBinder mBinder = new ServiceBinder();
    private final String TAG = "NvRamService";
    private volatile INvram mNvram = null;

    private List<String> keepList = Arrays.asList(CyMMITest.keepArray);
    private String SDPATH = null;

    @Override
    public IBinder onBind(Intent intent) {
        if (intent.getAction() != null &&
                intent.getAction().equals("cy.com.android.mmitest.NvRamService")) {
            return mAIDLBinder;
        }

        return null;
    }

    /**
     * class use to return service instance
     */
    public class ServiceBinder extends Binder {
        /**
         * get FM service instance
         *
         * @return service instance
         */
        NvRamService getService() {
            return NvRamService.this;
        }
    }

    public int EraseSD() {

        SDPATH = "/mnt/sdcard";
        File sd = new File(SDPATH);
        if (sd.canWrite())
            dFile(new File(SDPATH));

        SDPATH = "/mnt/sdcard2";
        sd = new File(SDPATH);
        if (sd.canWrite())
            dFile(new File(SDPATH));


        SDPATH = getDefaultExternalSdPath();
        if (SDPATH == null)
            return 0;
        sd = new File(SDPATH);
        if (sd.canWrite())
            dFile(new File(SDPATH));
        return 0;
    }

    public String getDefaultExternalSdPath() {
        DswLog.i(TAG, "getDefaultExternalSdPath()");
        String externalPath = null;
        StorageManager storageManager =
                (StorageManager) this.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] volumes = storageManager.getVolumeList();
        for (StorageVolume volume : volumes) {
            String volumePathStr = volume.getPath();
            if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(volume.getState())) {
                DswLog.i(TAG + "/Utils", volumePathStr + " is mounted!");
                VolumeInfo volumeInfo = storageManager.findVolumeById(volume.getId());
                if (isUSBOTG(volumeInfo)) {
                    continue;
                }
                if (volume.isEmulated()) {
                    String viId = volumeInfo.getId();
                    DswLog.i(TAG + "/Utils", "Is emulated and volumeInfo.getId() : " + viId);
                    // If external sd card, the viId will be like
                    // "emulated:179,130"
                    if (!viId.equalsIgnoreCase("emulated")) {
                        externalPath = volumePathStr;
                        break;
                    }
                } else {
                    DiskInfo diskInfo = volumeInfo.getDisk();
                    if (diskInfo == null) {
                        continue;
                    }
                    String diId = diskInfo.getId();
                    String emmcSupport =  SystemProperties.get("ro.mtk_emmc_support", "");
                    DswLog.i(TAG + "/Utils", "Is not emulated and diskInfo.getId() : " + diId);
                    // If is emmcSupport and is internal sd card, the diId will be like "disk:179,0"
                    // if is not emmcSupport and is internal sd card, the diId will be like "disk:7,1"
                    if ((emmcSupport.equals("1") && !diId.equalsIgnoreCase("disk:179,0"))
                            || (!emmcSupport.equals("1") && !diId.equalsIgnoreCase("disk:7,1"))) {
                        externalPath = volumePathStr;
                        break;
                    }
                }
            } else {
                DswLog.i(TAG, volumePathStr + " is not mounted!");
            }
        }
        DswLog.i(TAG, "getDefaultExternalSdPath() = " + externalPath);
        return externalPath;
    }

    private void dFile(File file) {
        for (String item : keepList) {
            if ((SDPATH + "/" + item).equalsIgnoreCase(file.toString()))
                return;
        }

        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
                return;
            } else if (file.isDirectory()) {
                DswLog.e(TAG, "dir :" + file.toString());
                File files[] = file.listFiles();
                if (files == null) {
                    DswLog.e(TAG, file + " listFiles()" + " return null");
                    return;
                }
                for (int i = 0; i < files.length; i++) {
                    dFile(files[i]);
                }
            }

            if (!SDPATH.equals(file.toString())) {
                file.delete();
            }

        } else {
            DswLog.e(TAG, "delete file is not exist");
        }
    }

    private boolean isUSBOTG(VolumeInfo volumeInfo) {
        DiskInfo diskInfo = volumeInfo.getDisk();
        if (diskInfo == null) {
            return false;
        }

        String diskID = diskInfo.getId();
        if (diskID != null) {
            // for usb otg, the disk id same as disk:8:x
            String[] idSplit = diskID.split(":");
            if (idSplit != null && idSplit.length == 2) {
                if (idSplit[1].startsWith("8,")) {
                    DswLog.i(TAG, "this is a usb otg");
                    return true;
                }
            }
        }
        return false;
    }

    private INvRamService.Stub mAIDLBinder = new INvRamService.Stub() {

        //**************** Android O read & write INvramInfo begin ****************//
        public byte[] readINvramInfo(int length) {
            String readInfo = null;
            byte[] productInfoBuff = null;
            try {
                mNvram = INvram.getService();
                if (null == mNvram) {
                    DswLog.e(TAG, "getPadioProxy: mnvram == null");
                }else{
                    DswLog.e(TAG, "readINvramInfo: mNvram is OK");
                    readInfo = mNvram.readFileByName(ProinfoUtil.getProinfoPath(),length);
                    productInfoBuff = HexDump.hexStringToByteArray(readInfo.substring(0, readInfo.length() - 1));
                    DswLog.i(TAG, "getINvramInfo mNvram  read ="+readInfo);
                }
            } catch (RemoteException ex) {
                DswLog.e(TAG, ex.toString());
            }
            return productInfoBuff;
        }

        public void writeToNvramInfo(byte[] sn_buff, int length) {
            int wflag = 0;
            try {
                mNvram = INvram.getService();

                if (null == mNvram) {
                    DswLog.e(TAG, "getPadioProxy: mnvram == null");
                }else{
                    DswLog.i(TAG, "mNvram begin write");
                    String buff = null;
                    buff = mNvram.readFileByName(ProinfoUtil.getProinfoPath(), length);
                    ArrayList<Byte> dataArray = new ArrayList<Byte>(length);
                    for (int i = 0; i < length; i++) {
                        dataArray.add(i, new Byte(sn_buff[i]));
                    }
                    wflag = mNvram.writeFileByNamevec(ProinfoUtil.getProinfoPath(),length, dataArray);
                    String readInfo = mNvram.readFileByName(ProinfoUtil.getProinfoPath(),length);
                    DswLog.i(TAG, "writeFileByNamevec flag="+wflag);
                    DswLog.i(TAG, "getINvramInfo mNvram write & read ="+readInfo);
                }
                DswLog.e(TAG, "nvram writeToNvramInfo flag="+wflag);
            } catch (Exception e) {
                e.printStackTrace();
                DswLog.e(TAG, "nvram write Error:"+ e.getMessage() + ":" + e.getCause());
                return;
            }
        }

        public int eraseSdCard() {
            return EraseSD();
        }

        public int getSimStatus(int id) {
            DswLog.d(TAG, "getSimStatus start");
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            int simState = telephonyManager.getSimState(id);
            DswLog.d(TAG, "SimCard id="+id + " simState="+simState);
            return simState;
        }
    };
}
