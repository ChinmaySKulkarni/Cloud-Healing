package com.cloudhealing.backend;

import org.eclipse.jetty.util.ajax.JSON;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: new-cloud-host
 * Date: 3/5/13
 * Time: 6:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class Healer {
    public void heal(String node) {
        APICaller.migrateAllVMS(node);
    }
}
