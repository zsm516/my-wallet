package com.zsm.wallet;

import android.Manifest;
import android.database.Cursor;
import android.net.Uri;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

@CapacitorPlugin(
    name = "SmsReader",
    permissions = { @Permission(alias = "sms", strings = { Manifest.permission.READ_SMS }) }
)
public class SmsReaderPlugin extends Plugin {

    @PluginMethod
    public void getMessages(PluginCall call) {
        if (getPermissionState("sms") != PermissionState.GRANTED) {
            requestPermissionForAlias("sms", call, "permCallback");
            return;
        }
        read(call);
    }

    @PermissionCallback
    private void permCallback(PluginCall call) {
        if (getPermissionState("sms") == PermissionState.GRANTED) read(call);
        else call.reject("denied");
    }

    private void read(PluginCall call) {
        int limit = call.getInt("limit", 100);
        JSArray arr = new JSArray();
        Cursor c = getContext().getContentResolver().query(
            Uri.parse("content://sms/inbox"),
            new String[] { "address", "body", "date" },
            null, null, "date DESC");
        if (c != null) {
            try {
                int i = 0;
                while (c.moveToNext() && i < limit) {
                    JSObject o = new JSObject();
                    o.put("address", c.getString(0));
                    o.put("body", c.getString(1));
                    o.put("date", c.getLong(2));
                    arr.put(o);
                    i++;
                }
            } finally {
                c.close();
            }
        }
        JSObject r = new JSObject();
        r.put("messages", arr);
        call.resolve(r);
    }
}
