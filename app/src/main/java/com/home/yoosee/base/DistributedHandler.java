package com.home.yoosee.base;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by heshengfang on 2017/4/17.
 */
public class DistributedHandler extends Handler {
    private final static String TAG = "DistributedHandler";

    private List<HandlerPart> handlerParts;

    public interface HandlerPart {
        boolean dispatchHandleMessage(Message msg);
    }

    public DistributedHandler() {
        super();
    }

    public DistributedHandler(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        try {
            for (int i = 0, l = (handlerParts != null ? handlerParts.size() : 0); i < l; i++) {
                if (handlerParts.get(i).dispatchHandleMessage(msg))
                    return;
            }
        } catch (Throwable th) {
            Log.e(TAG, "handleMessage = " + msg.what + ",", th);
        }
    }

    public void addHandlerPart(HandlerPart part) {
        if (null == handlerParts)
            handlerParts = new ArrayList<>();

        if (!handlerParts.contains(part))
            handlerParts.add(part);
    }

    public void removeHandlerPart(HandlerPart part) {
        if (null != handlerParts)
            handlerParts.remove(part);
    }

}