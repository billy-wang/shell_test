/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: NvRAMBackupAgent.aidl
 */

package gn.com.android.mmitest.item;

import android.os.IBinder;

public interface NvRAMBackupAgent extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements NvRAMBackupAgent {
        private static final java.lang.String DESCRIPTOR = "NvRAMBackupAgent";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an NvRAMBackupAgent interface, generating a
         * proxy if needed.
         */
        public static NvRAMBackupAgent asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = (android.os.IInterface) obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof NvRAMBackupAgent))) {
                return ((NvRAMBackupAgent) iin);
            }
            return new NvRAMBackupAgent.Stub.Proxy(obj);
        }

        public android.os.IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply,
                                  int flags) throws android.os.RemoteException {
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                case TRANSACTION_readFile: {
                    data.enforceInterface(DESCRIPTOR);
                    this.readFile();
                    reply.writeNoException();

                    return true;
                }

                case TRANSACTION_backupFile: {
                    data.enforceInterface(DESCRIPTOR);
                    int _result = this.backupFile();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                }

                case TRANSACTION_writeFile: {
                    data.enforceInterface(DESCRIPTOR);
                    int[] _arg0 = new int[3];
                    _arg0[0] = data.readInt();
                    byte[] _arg1;
                    _arg1 = data.createByteArray();
                    this.writeFile(_arg0);
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements NvRAMBackupAgent {
            private android.os.IBinder mRemote;

            Proxy(android.os.IBinder remote) {
                mRemote = remote;
            }

            public android.os.IBinder asBinder() {
                return mRemote;
            }

            public java.lang.String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            public int[] readFile() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int[] _result = new int[3];
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_readFile, _data, _reply, 0);
                    _result[0] = _reply.readInt();
                    _result[1] = _reply.readInt();
                    _result[2] = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            public void writeFile(int[] buff) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(buff[0]);
                    _data.writeInt(buff[1]);
                    _data.writeInt(buff[2]);
                    mRemote.transact(Stub.TRANSACTION_writeFile, _data, _reply, 0);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void writeUIcolor(int[] buff) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(buff[0]);
                    mRemote.transact(Stub.TRANSACTION_writeUIcolor, _data, _reply, 0);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }

            }

            public int backupFile() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_backupFile, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }

                return _result;
            }

        }

        static final int TRANSACTION_readFile = (IBinder.FIRST_CALL_TRANSACTION + 0);

        static final int TRANSACTION_writeFile = (IBinder.FIRST_CALL_TRANSACTION + 1);

        static final int TRANSACTION_backupFile = (IBinder.FIRST_CALL_TRANSACTION + 2);

        static final int TRANSACTION_writeUIcolor = (IBinder.FIRST_CALL_TRANSACTION + 3);
    }

    public int[] readFile() throws android.os.RemoteException;

    public void writeFile(int[] buff) throws android.os.RemoteException;

    public void writeUIcolor(int[] buff) throws android.os.RemoteException;

    public int backupFile() throws android.os.RemoteException;
}
