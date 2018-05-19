package com.zyt.jni.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

/**
 * 
 * 
 * @author 舒建华
 * @function
 * @date 2016年3月19日
 */
public class ZytJniInterface {

	private static final int ALL_CRYPT_VERSION = 200;

	private static final String TAG = "veb_a3";
	private static final String PRE = "VEBA3 process code:";

	public static final int UPDATE_COS_NOT_EXIST_SUCCESS = 101;
	public static final int UPDATE_COS_SUCCESS = 102;
	public static final int UPDATE_COS_COMPUTING = 103;

	public static final int UPDATE_COS_FAILURE = 104;

	private static final int CHIP_IS_BOOT_CANT_TEST = 105;

	public static final int CHIP_IS_NORMAL = 106;
	public static final int CHIP_IS_ABNORMAL = 107;

	// 正常流程代码
	private static final int COS_FILE_DELETE_SUCCESS = 108;
	private static final int COS_FILE_DELETE_FAILURE = 109;

	private static final int CHIP_TEST_AES_CRYPT_SUCCESS = 301;
	private static final int CHIP_TEST_SM2_CRYPT_SUCCESS = 302;
	private static final int CHIP_TEST_SM3_CRYPT_SUCCESS = 303;
	private static final int CHIP_TEST_SM4_CRYPT_SUCCESS = 304;

	private static final int CHIP_TEST_AES_CRYPT_FAILURE = 311;
	private static final int CHIP_TEST_SM2_CRYPT_FAILURE = 312;
	private static final int CHIP_TEST_SM3_CRYPT_FAILURE = 313;
	private static final int CHIP_TEST_SM4_CRYPT_FAILURE = 314;
	private static final int CHIP_TEST_SUCCESS = 320;
	private static final int CHIP_TEST_FAILURE = 321;

	private static final String AES_INSTANCE = "ZYT_HARD_AES.ECB.Normal.FileCrypt";
	private static final String SM4_INSTANCE = "ZYT_HARD_SM4.ECB.Normal.FileCrypt";
	private static final String SM3_INSTANCE = "ZYT_HARD_SM3.FileCrypt";
	private static final String SM2_INSTANCE = "ZYT_HARD_SM2.ECB";

	private static final String AES = "AES";
	private static final String SM4 = "SM4";
	private static final String SM3 = "SM3";
	private static final String SM2 = "SM2";
	private static final String Key = "0123456789ABCDEF";
	// private static final int ERROR_UPDATAING = -35;
	private static final int ERROR_COMPUTING = -36;

	/**
	 * 请更换MD5的时候使用大写MD5值
	 */
	@SuppressWarnings("unused")
	private static final String cosMd5 = "C4CEAC372D0186E9DA5BCCFBF4891C0B";

	/**
	 * 升级固件并且测试芯片，将升级固件和测试芯片放在一起。
	 * 
	 * @return true-检测成功 false-请到对应动作中查看对应错误信息
	 */
	public static synchronized int updateAndTestDriver(Context ctx, File cosFile) {

		int flag = CHIP_IS_ABNORMAL;

		String updateFlag = getUpdateFlag();
		if (!TextUtils.isEmpty(updateFlag) && updateFlag.equals("yes")) {
			flag = updateDriver(ctx, cosFile);
			// 考虑极端情况，同时有后台进行的加解密服务，那么直接返回，让用户再等待。
			if (flag == UPDATE_COS_COMPUTING) {
				return flag;
			}
		}

		if (!storeKey(ctx)) {
			flag = CHIP_IS_ABNORMAL;
			return flag;
		}

		// 其它情况，升级失败直接芯片挂掉，升级失败进入boot模式，升级成功，都继续测试。
		if (testDriver(ctx)) {
			flag = CHIP_IS_NORMAL;
			if (cosFile.delete()) {
				Log.w(TAG, PRE + COS_FILE_DELETE_SUCCESS);
			} else {
				Log.w(TAG, PRE + COS_FILE_DELETE_FAILURE);
			}
		} else {
			flag = CHIP_IS_ABNORMAL;
		}

		return flag;
	}

	/**
	 * 测试芯片加解密，根据对应版本检测相关加解密性能
	 * 
	 * @return true-加解密成功 false-加解密失败
	 */
	public static synchronized boolean testDriver(Context ctx) {
		boolean flag = false;
		int version = getChipFormatVersion(ctx);

		Log.w(TAG, "VEB_A3_current test version:" + version);
		if (version < 0) {
			// 说明芯片没有正常工作或者是芯片进入了boot模式。
			Log.w(TAG, PRE + CHIP_IS_BOOT_CANT_TEST);
			return flag;
		}

		if (version >= ALL_CRYPT_VERSION) {
			flag = true;
			boolean temp = false;

			temp = ZytJniInterface.aesCryptTest();
			if (temp) {
				Log.w(TAG, PRE + CHIP_TEST_AES_CRYPT_SUCCESS);
			} else {
				Log.w(TAG, PRE + CHIP_TEST_AES_CRYPT_FAILURE);
			}
			flag &= temp;

			temp = ZytJniInterface.sm2CryptTest(ctx);
			if (temp) {
				Log.w(TAG, PRE + CHIP_TEST_SM2_CRYPT_SUCCESS);
			} else {
				Log.w(TAG, PRE + CHIP_TEST_SM2_CRYPT_FAILURE);
			}
			flag &= temp;

			temp = ZytJniInterface.sm3CryptTest();
			if (temp) {
				Log.w(TAG, PRE + CHIP_TEST_SM3_CRYPT_SUCCESS);
			} else {
				Log.w(TAG, PRE + CHIP_TEST_SM3_CRYPT_FAILURE);
			}
			flag &= temp;

			temp = ZytJniInterface.sm4CryptTest();
			if (temp) {
				Log.w(TAG, PRE + CHIP_TEST_SM4_CRYPT_SUCCESS);
			} else {
				Log.w(TAG, PRE + CHIP_TEST_SM4_CRYPT_FAILURE);
			}
			flag &= temp;

		} else {
			boolean ret1 = ZytJniInterface.aesCryptTest();
			if (ret1) {
				flag = true;
				Log.w(TAG, PRE + CHIP_TEST_AES_CRYPT_SUCCESS);
			} else {
				Log.w(TAG, PRE + CHIP_TEST_AES_CRYPT_FAILURE);
			}
		}

		if (flag) {
			Log.w(TAG, PRE + CHIP_TEST_SUCCESS);
		} else {
			Log.w(TAG, PRE + CHIP_TEST_FAILURE);
		}
		return flag;
	}

	private static String getUpdateFlag() {

		String isUpdate = "no";
		Class<?> SystemProperties = null;
		try {
			SystemProperties = Class.forName("android.os.SystemProperties");
			Method get = SystemProperties.getMethod("get", String.class,
					String.class);
			isUpdate = (String) get.invoke(SystemProperties,
					"ro.gn.zyt.a3.upgrade.support", "yes");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		Log.w(TAG, "VEB_A3 ,the updateFlag is " + isUpdate);
		return isUpdate;
	}

	public static String getChipVersion(Context ctx) {
		Object object = ctx.getSystemService("zytcrypt");
		String version = null;

		byte[] outBuffer = new byte[64];

		byte[] out = null;
		try {
			Constructor<?> iZConstructor = IZytCryptBody.getConstructor(
					byte[].class, byte[].class, int.class, int.class);
			Object iZytCryptBody = iZConstructor.newInstance("".getBytes(),
					outBuffer, 16, 64);

			Method method = ZytCryptManager.getMethod("getChipVersion",
					IZytCryptBody);
			int status = (Integer) method.invoke(object, iZytCryptBody);
			if (status != 0) {
				Log.w(TAG, "the get chip version status is not 0,is " + status);
				return null;
			}
			Method getOutBuffer = IZytCryptBody.getMethod("getOutbuff");
			out = (byte[]) getOutBuffer.invoke(iZytCryptBody);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		if (out != null) {
			version = new String(out);
		}

		return version;
	}

	public static Class<?> ZytCryptManager = null;
	public static Class<?> IZytCryptBody = null;
	public static Class<?> IZytAsymParam = null;
	public static Class<?> IZytCryptParam = null;

	static {
		try {
			ZytCryptManager = Class
					.forName("android.app.zyitong.ZytCryptManager");
			IZytCryptBody = Class.forName("android.app.zyitong.IZytCryptBody");
			IZytAsymParam = Class.forName("android.app.zyitong.IZytAsymParam");
			IZytCryptParam = Class
					.forName("android.app.zyitong.IZytCryptParam");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static String getChipSN(Context ctx) {
		String sn = null;
		Object object = ctx.getSystemService("zytcrypt");
		try {
			Constructor<?> IZytCryptBodyCons[] = IZytCryptBody
					.getConstructors();
			byte[] outBuffer = new byte[64];
			Object iZytCryptBody = IZytCryptBodyCons[1].newInstance(
					"".getBytes(), outBuffer, 16, 64);
			Method method = ZytCryptManager.getMethod("getChipSN",
					IZytCryptBody);
			int status = (Integer) method.invoke(object, iZytCryptBody);
			if (status != 0) {
				Log.w(TAG, "the get chip sn status is not 0,is " + status);
				return null;
			}

			Method getOutBuffer = IZytCryptBody.getMethod("getOutbuff");
			byte[] out = (byte[]) getOutBuffer.invoke(iZytCryptBody);

			if (out != null) {
				sn = byte2Hex(out);
			}

		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}

		return sn;
	}

	private static boolean storeKey(Context ctx) {
		boolean ret = false;
		Object zytCryptManager = ctx.getSystemService("zytcrypt");
		Object iZytCryptParam = null;
		try {
			Constructor<?> IZytCryptParamCon = IZytCryptParam.getConstructor(
					int.class, int.class, int.class, int.class, byte[].class);
			iZytCryptParam = IZytCryptParamCon.newInstance(0, 1, 0,
					Key.length(), Key.getBytes());

			Method storeKey = ZytCryptManager.getMethod("storeKey",
					IZytCryptParam);
			int storeKeyState = (Integer) storeKey.invoke(zytCryptManager,
					iZytCryptParam);
			if (storeKeyState == 0) {
				ret = true;
				Log.w(TAG, "VEB_A3 store key success");
				return ret;
			}
			Log.w(TAG, "veb_a3 store key fail,the status is " + storeKeyState);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return ret;
	}

	@SuppressWarnings("unused")
	public static int updateDriver(Context ctx, File f) {

		if (!(f.exists() && f.length() > 0 && f.isFile())) {
			Log.w(TAG,
					"VEB_A3 cosfile not found or not valid file,return success");
			return UPDATE_COS_NOT_EXIST_SUCCESS;
		}

		/*
		 * // 增加MD5校验支持 if
		 * (!(checkMd5(f).equals(cosMd5.toUpperCase(Locale.ENGLISH)))) {
		 * Log.w(TAG, "cosfile exists but not valid file,return success");
		 * return UPDATE_COS_NOT_EXIST_SUCCESS; }
		 */

		FileInputStream fis = null;

		Object zytCryptManager = ctx.getSystemService("zytcrypt");
		try {
			fis = new FileInputStream(f);
			byte[] cosByte = new byte[(int) f.length()];
			fis.read(cosByte);
			Constructor<?> iZConstructor = IZytCryptBody.getConstructor(
					byte[].class, byte[].class, int.class, int.class);

			byte[] in = new byte[16];
			Object iZytCryptBody = iZConstructor.newInstance(cosByte,
					"".getBytes(), cosByte.length, 16);

			Method method = ZytCryptManager.getMethod("updateChipCos",
					IZytCryptBody);
			int status = (Integer) method
					.invoke(zytCryptManager, iZytCryptBody);
			if (status == 0) {
				Log.w(TAG, "VEB_A3 update success");

				return UPDATE_COS_SUCCESS;
			} else if (status == ERROR_COMPUTING) {
				Log.w(TAG,
						"veb_a3 the chip is computing ,update fail,the status is "
								+ status);
				return UPDATE_COS_COMPUTING;
			}
			Log.w(TAG, "VEB_A3 update driver error ,the status is " + status);

		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
					fis = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return UPDATE_COS_FAILURE;
	}

	/**
	 * 获取格式化的芯片版本号码
	 * 
	 * @return 可能返回如1072，1202或者2002等。如果返回-1，则获取失败，提示芯片进入boot模式
	 */
	public static int getChipFormatVersion(Context ctx) {
		int flag = -1;
		String version = getChipVersion(ctx);
		Log.w(TAG, "VEB_A3 getChipFormatVersion:" + version);
		if (version != null) {
			flag = extractChipNumberVersion(version);
		}
		return flag;
	}

	public static int extractChipNumberVersion(String data) {
		String newData = data.substring(9, 15).replace(".", "")
				.replace(" ", "");
		Log.w(TAG, "VEB_A3 extractChipNumberVersion" + newData);
		// 最后一位不为数字的就是调试状态
		if (Character.isDigit(newData.substring(newData.length() - 1)
				.toCharArray()[0])) {
			return Integer.valueOf(newData);
		}
		return -1;
	}

	public static String checkMd5(File f) {
		if (!f.exists() || !f.isFile()) {
			return null;
		}
		FileInputStream fis = null;
		byte[] rb = null;
		DigestInputStream digestInputStream = null;
		try {
			fis = new FileInputStream(f);
			MessageDigest md5 = MessageDigest.getInstance("md5");
			digestInputStream = new DigestInputStream(fis, md5);
			byte[] buffer = new byte[4096];
			while (digestInputStream.read(buffer) > 0)
				;
			md5 = digestInputStream.getMessageDigest();
			rb = md5.digest();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < rb.length; i++) {
			String a = Integer.toHexString(0XFF & rb[i]);
			if (a.length() < 2) {
				a = '0' + a;
			}
			sb.append(a);
		}
		return sb.toString().toUpperCase(Locale.ENGLISH);
	}

	public static final boolean aesCryptTest() {
		int size = 1024;
		byte[] originByte = new byte[size];
		Random random = new Random(System.currentTimeMillis());
		random.nextBytes(originByte);
		Log.w(TAG, "VEB_A3 the AES length is " + originByte.length + ","
				+ originByte[523] + "," + originByte[1023] + ","
				+ originByte[0]);
		byte[] encryptContent = symmetryEncryptByteArrayContent(originByte,
				AES_INSTANCE, AES);
		if (encryptContent == null) {
			Log.w(TAG, "the aes encrypt result is null");
			return false;
		}

		if (compareArrayContent(originByte, encryptContent)) {
			Log.w(TAG, "the compareArrayContent is false! ");
			return false;
		}
		byte[] decryptContent = symmetryDecryptByteArrayContent(encryptContent,
				AES_INSTANCE, AES);
		if (decryptContent == null) {
			Log.w(TAG, "the aes decrypt result is null");
			return false;
		}

		if (compareArrayContent(encryptContent, decryptContent)) {
			Log.w(TAG,
					"the aes encrypt result equal decrypt result ,so decrypt is fail");
			return false;
		}

		if (!compareArrayContent(originByte, decryptContent)) {
			Log.w(TAG,
					"the aes origin byte not equal decrypt byte,so decrypt or encrypt is fail");
			return false;
		}

		Log.w(TAG, "aes test success");

		return true;
	}

	/**
	 * 判断两个数组是否相等
	 * 
	 * @param a
	 * @param b
	 * @return 若相等，则返回true,否则返回false.当a和b都为null时，也返回false.
	 */
	private static boolean compareArrayContent(byte[] a, byte[] b) {
                if (a == null || b == null) {
                        return false;
                }
                if (a.length != b.length) { 
                    return false;
                } else {
                        for (int i = 0; i < a.length; i++) {
                            if (a[i] != b[i]) {                 
                                    return false;
                            }
                        }
                    return true;
                }
	}

	@SuppressLint("TrulyRandom")
	private static final boolean sm2CryptTest(Context ctx) {

		byte[] publicKey = new byte[64];
		byte[] privateKey = new byte[32];

		Object zytCryptManager = ctx.getSystemService("zytcrypt");
		int size = 128;
		byte[] originByte = new byte[size];
		Random random = new Random(System.currentTimeMillis());
		random.nextBytes(originByte);
		Object iZytAsymParam = null;
		try {
			Constructor<?> IZytAsymParamCon = IZytAsymParam.getConstructor(
					int.class, int.class, int.class, int.class, byte[].class,
					byte[].class);
			iZytAsymParam = IZytAsymParamCon.newInstance(102, 0, 1,
					publicKey.length, publicKey, privateKey);  // 只导出公钥。
			Method asymOutputKey = ZytCryptManager.getMethod("asymOutputKey",
					IZytAsymParam);
			int outKeyState = (Integer) asymOutputKey.invoke(zytCryptManager,
					iZytAsymParam);
			if (outKeyState != 0) {
				Log.w(TAG,
						"the sm2 crypt test asymOutputkey is fail,the out key status is "
								+ outKeyState);
				return false;
			}

			Method getPublicKey = IZytAsymParam.getMethod("getPublicKey");
			publicKey = (byte[]) getPublicKey.invoke(iZytAsymParam);

			SecretKey k1 = new SecretKeySpec(publicKey, SM2);
			SecretKey k2 = new SecretKeySpec(privateKey, SM2);

			Cipher mCipher = Cipher.getInstance(SM2_INSTANCE);
			mCipher.init(Cipher.ENCRYPT_MODE, k1);
			byte[] encryptContent = mCipher.doFinal(originByte);
			if (compareArrayContent(originByte, encryptContent)) {
				Log.w(TAG,
						"the sm2 compareArrayContent originByte equal encryptContent,sm2 fail!");
				return false;
			}

			mCipher.init(Cipher.DECRYPT_MODE, k2);
			byte[] decryptContent = mCipher.doFinal(encryptContent);
			if (!compareArrayContent(originByte, decryptContent)) {
				Log.w(TAG,
						"VEB_A3 sm2 compareArrayContent origin byte not equal decryptContent,sm2 fail");
				return false;
			}
			Log.w(TAG, "the sm2 test success");
			return true;

		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}

		return false;
	}

	private static final boolean sm3CryptTest() {
		int size = 512;
		byte[] originByte = new byte[size];
		Random random = new Random(System.currentTimeMillis());
		random.nextBytes(originByte);
		Log.w(TAG, "VEB_A3 the SM3 length is " + originByte.length + ","
				+ originByte[511] + "," + originByte[100] + "," + originByte[0]);
		byte[] encryptContent = symmetryEncryptByteArrayContent(originByte,
				SM3_INSTANCE, SM3);
		if (compareArrayContent(originByte, encryptContent)) {
			Log.w(TAG,
					"the sm3 cryptTest compareArrayContent originByte equal encryptContent ,sm3 fail");
			return false;
		}
		return true;
	}

	private static final boolean sm4CryptTest() {
		int size = 1024;
		byte[] originByte = new byte[size];
		Random random = new Random(System.currentTimeMillis());
		random.nextBytes(originByte);
		Log.w(TAG, "VEB_A3 the SM4 length is " + originByte.length + ","
				+ originByte[523] + "," + originByte[1023] + ","
				+ originByte[0]);
		byte[] encryptContent = symmetryEncryptByteArrayContent(originByte,
				SM4_INSTANCE, SM4);
		if (encryptContent == null) {
			Log.w(TAG, "the sm4 encrypt result is null");
			return false;
		}
		if (compareArrayContent(originByte, encryptContent)) {
			Log.w(TAG,
					"the sm4 compareArrayContent originByte equal encryptContent,sm4 test fail");
			return false;
		}
		byte[] decryptContent = symmetryDecryptByteArrayContent(encryptContent,
				SM4_INSTANCE, SM4);
        
		if (decryptContent == null) {
			Log.w(TAG, "the sm4 decrypt result is null");
			return false;
		}

		if (compareArrayContent(encryptContent, decryptContent)) {
			Log.w(TAG,
					"the sm4 encryptContent equal decryptContent,sm4 test fail");
			return false;
		}

		if (!compareArrayContent(originByte, decryptContent)) {
			Log.w(TAG,
					"the sm4 origin byte not equal decryptContent,sm4 test fail");
			return false;
		}

		return true;
	}

        private static byte[] symmetryEncryptByteArrayContent(byte[] originContent,
        String cipherInstance, String encryptWay) {
        SecretKey k = new SecretKeySpec(Key.getBytes(), encryptWay);
        byte[] cryptContentByte = null;
        try {
                Cipher cipher = Cipher.getInstance(cipherInstance); // 在普通的没有合框架的手机会崩溃
                cipher.init(Cipher.ENCRYPT_MODE, k);
                cryptContentByte = cipher.doFinal(originContent);
        } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
        } catch (NoSuchPaddingException e) {
                e.printStackTrace();
        } catch (InvalidKeyException e) {
                e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return cryptContentByte;
	}

	private static byte[] symmetryDecryptByteArrayContent(
			byte[] encryptContent, String cipherInstance, String encryptWay) {
		SecretKey k = new SecretKeySpec(Key.getBytes(), encryptWay);
		byte[] decryptContentByte = null;
		try {
			Cipher cipher = Cipher.getInstance(cipherInstance);
			cipher.init(Cipher.DECRYPT_MODE, k);
			decryptContentByte = cipher.doFinal(encryptContent);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();

		}
		return decryptContentByte;
	}

	private static String byte2Hex(byte[] b) {
		StringBuilder sb = new StringBuilder();
		for (byte a : b) {
			String s = Integer.toHexString(a & 0xff);
			while (s.length() < 2) {
				s = "0" + s;
			}
			sb.append(s);
		}
		return sb.toString();
	}
}
