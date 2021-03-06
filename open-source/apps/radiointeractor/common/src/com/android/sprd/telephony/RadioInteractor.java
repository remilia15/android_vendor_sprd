
package com.android.sprd.telephony;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.IccOpenLogicalChannelResponse;

import static com.android.sprd.telephony.RIConstants.RADIOINTERACTOR_SERVER;

public class RadioInteractor {

    IRadioInteractor mRadioInteractorProxy;
    private static RadioInteractorService sRadioInteractorService;
    private static final String TAG = "RadioInteractor";

    public RadioInteractor(Context context) {
        mRadioInteractorProxy = IRadioInteractor.Stub
                .asInterface(ServiceManager.getService(RADIOINTERACTOR_SERVER));
    }

    static void setService(RadioInteractorService service) {
        UtilLog.logd(TAG, " setService  RadioInteractorService class  "
                + RadioInteractor.class.hashCode());
        sRadioInteractorService = service;
    }

    public void listen(RadioInteractorCallbackListener radioInteractorCallbackListener,
            int events) {
        this.listen(radioInteractorCallbackListener, events, true);
    }

    public void listen(RadioInteractorCallbackListener radioInteractorCallbackListener,
            int events, boolean notifyNow) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(radioInteractorCallbackListener.mSlotId);
            if (rih != null) {
                if (events == RadioInteractorCallbackListener.LISTEN_NONE) {
                    rih.unregisterForUnsolRadioInteractor(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForRiConnected(radioInteractorCallbackListener.mHandler);
                    rih.unregisterForRadioInteractorEmbms(radioInteractorCallbackListener.mHandler);
                    return;
                }
                if ((events & RadioInteractorCallbackListener.LISTEN_RADIOINTERACTOR_EVENT) != 0) {
                    rih.unsolicitedRegisters(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_RADIOINTERACTOR_EVENT);
                }
                if ((events & RadioInteractorCallbackListener.LISTEN_RI_CONNECTED_EVENT) != 0) {
                    rih.registerForRiConnected(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_RI_CONNECTED_EVENT);
                }
                if ((events & RadioInteractorCallbackListener.LISTEN_RADIOINTERACTOR_EMBMS_EVENT) != 0) {
                    rih.registerForRadioInteractorEmbms(radioInteractorCallbackListener.mHandler,
                            RadioInteractorCallbackListener.LISTEN_RADIOINTERACTOR_EMBMS_EVENT);
                }
                return;
            }
            mRadioInteractorProxy.listenForSlot(radioInteractorCallbackListener.mSlotId,
                    radioInteractorCallbackListener.mCallback,
                    events, notifyNow);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private RadioInteractorHandler getRadioInteractorHandler(int slotId) {
        UtilLog.logd(TAG, "RadioInteractorService:  " + sRadioInteractorService
                + "  RadioInteractorService class " + RadioInteractor.class.hashCode());
        if (sRadioInteractorService != null) {
            return sRadioInteractorService.getRadioInteractorHandler(slotId);
        }
        return null;
    }

    // This interface will be eliminated
    public int sendAtCmd(String[] oemReq, String[] oemResp, int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.invokeOemRILRequestStrings(oemReq, oemResp);
            }
            return mRadioInteractorProxy.sendAtCmd(oemReq, oemResp, slotId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getSimCapacity(int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.getSimCapacity();
            }
            return mRadioInteractorProxy.getSimCapacity(slotId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getRequestRadioInteractor(int type, int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.getRequestRadioInteractor(type);
            }
            return mRadioInteractorProxy.getRequestRadioInteractor(type, slotId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void enableRauNotify(int slotId){
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                rih.enableRauNotify();
                return;
            }
            mRadioInteractorProxy.enableRauNotify(slotId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /* SPRD: Bug#525009 Add support for Open Mobile API @{*/

    /**
     * Opens a logical channel to the ICC card.
     *
     * @param AID Application id. See ETSI 102.221 and 101.220.
     * @param slotId int
     * @return an IccOpenLogicalChannelResponse object.
     */
    public IccOpenLogicalChannelResponse iccOpenLogicalChannel(String AID, int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.iccOpenLogicalChannel(AID);
            }
            return mRadioInteractorProxy.iccOpenLogicalChannel(AID, slotId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Opens a logical channel to the ICC card.
     *
     * @param AID Application id. See ETSI 102.221 and 101.220.
     * @param p2 byte.
     * @param slotId int
     * @return an IccOpenLogicalChannelResponse object.
     */
    public IccOpenLogicalChannelResponse iccOpenLogicalChannel(String AID, byte p2, int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.iccOpenLogicalChannelP2(AID, p2);
            }
            return mRadioInteractorProxy.iccOpenLogicalChannelP2(AID, p2, slotId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Closes a previously opened logical channel to the ICC card.
     *
     * Input parameters equivalent to TS 27.007 AT+CCHC command.
     * @param channel is the channel id to be closed as retruned by a successful
     *            iccOpenLogicalChannel.
     * @param slotId int
     * @return true if the channel was closed successfully.
     */
    public boolean iccCloseLogicalChannel(int channel, int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.iccCloseLogicalChannel(channel);
            }
            return mRadioInteractorProxy.iccCloseLogicalChannel(channel, slotId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Transmit an APDU to the ICC card over a logical channel.
     *
     * Input parameters equivalent to TS 27.007 AT+CGLA command.
     * @param channel is the channel id to be closed as returned by a successful
     *            iccOpenLogicalChannel.
     * @param cla Class of the APDU command.
     * @param instruction Instruction of the APDU command.
     * @param p1 P1 value of the APDU command.
     * @param p2 P2 value of the APDU command.
     * @param p3 P3 value of the APDU command. If p3 is negative a 4 byte APDU
     *            is sent to the SIM.
     * @param data Data to be sent with the APDU.
     * @param slotId int
     * @return The APDU response from the ICC card with the status appended at
     *            the end.
     */
    public String iccTransmitApduLogicalChannel(int channel, int cla,
            int instruction, int p1, int p2, int p3, String data, int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.iccTransmitApduLogicalChannel(channel, cla, instruction, p1, p2, p3, data);
            }
            return mRadioInteractorProxy.iccTransmitApduLogicalChannel(channel, cla,
                    instruction, p1, p2, p3, data, slotId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Transmit an APDU to the ICC card over the basic channel.
     *
     * Input parameters equivalent to TS 27.007 AT+CSIM command.
     * @param cla Class of the APDU command.
     * @param instruction Instruction of the APDU command.
     * @param p1 P1 value of the APDU command.
     * @param p2 P2 value of the APDU command.
     * @param p3 P3 value of the APDU command. If p3 is negative a 4 byte APDU
     *            is sent to the SIM.
     * @param data Data to be sent with the APDU.
     * @param slotId int
     * @return The APDU response from the ICC card with the status appended at
     *            the end.
     */
    public String iccTransmitApduBasicChannel(int cla,
            int instruction, int p1, int p2, int p3, String data, int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.iccTransmitApduBasicChannel(cla, instruction, p1, p2, p3, data);
            }
            return mRadioInteractorProxy.iccTransmitApduBasicChannel(cla,
                instruction, p1, p2, p3, data, slotId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the ATR of the UICC if available.
     * @param slotId int
     * @return The ATR of the UICC if available.
     */
    public String iccGetAtr(int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.iccGetAtr();
            }
            return mRadioInteractorProxy.iccGetAtr(slotId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
    /* @} */

    public boolean queryHdVoiceState(int slotId){
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.queryHdVoiceState();
            }
            return mRadioInteractorProxy.queryHdVoiceState(slotId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Send request to set call forward number whether shown.
     *
     * @param slotId int
     * @param enabled CMCC is true,other is false
     */
    public void setCallingNumberShownEnabled(int slotId, boolean enabled) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                rih.setCallingNumberShownEnabled(enabled);
                return;
            }
            mRadioInteractorProxy.setCallingNumberShownEnabled(slotId, enabled);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Store Sms To Sim
     *
     * @param enable True is store SMS to SIM card,false is store to phone.
     * @param slotId int
     * @return whether successful store Sms To Sim
     */
    public boolean storeSmsToSim(boolean enable, int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.storeSmsToSim(enable);
            }
            return mRadioInteractorProxy.storeSmsToSim(enable, slotId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Return SMS Storage Mode.
     *
     * @param slotId int
     * @return SMS Storage Mode
     */
    public String querySmsStorageMode(int slotId) {
        try {
            RadioInteractorHandler rih = getRadioInteractorHandler(slotId);
            if (rih != null) {
                return rih.querySmsStorageMode();
            }
            return mRadioInteractorProxy.querySmsStorageMode(slotId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
}
