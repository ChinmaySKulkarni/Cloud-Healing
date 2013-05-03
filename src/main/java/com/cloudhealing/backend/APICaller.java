package com.cloudhealing.backend;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.json.JSONArray;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class APICaller {

    private static String sessionKey = null;
    private static final String HOST_IP = "192.168.1.101";
    private static final String consumerSecret = "";
    private static final String consumerKey = "";
    private static OAuthService service = null;
    private static Token accessToken = null;
    private static List<NewCookie> cookies;

    public void APICaller() {
    }

    public static String getHostID(String hostName){
        try {
            HashMap<String, String> arguments = new HashMap<String, String>();
            arguments.put("name", hostName);
            JSONObject response = APICaller.call("listHosts", arguments);
            JSONObject host = response.getJSONArray("host").getJSONObject(0);
            String id = host.getString("id");
            return id;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static void login() {
        String username = "admin";
        String password = "Shaatir1!";

        try {
            password = new String(Hex.encodeHex(MessageDigest.getInstance("MD5").digest(password.getBytes())));
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("username", username);
            params.put("password", password);
            JSONObject response = call("login", params);

            sessionKey = response.getString("sessionkey");
            System.out.println("SessiomKey:" + sessionKey);
        } catch (JSONException e) {
            e.printStackTrace();
        }  catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    public static JSONObject call(String APIName, HashMap<String, String> arguments) {
        try {

            if (sessionKey == null && !APIName.equals("login")) {
                login();
            }

            Client client = Client.create();

            WebResource webResource = client.resource("http://" + HOST_IP +":8080/client/api");
            MultivaluedMap formData = new MultivaluedMapImpl();
            for (String argument : arguments.keySet()){
                formData.add(argument, arguments.get(argument));
            }
            formData.add("command", APIName);
            formData.add("response", "json");
            formData.add("sessionkey", sessionKey);

            WebResource.Builder builder = webResource.queryParams(formData).getRequestBuilder();
            if (!APIName.equals("login")) {
                for (NewCookie c : cookies) {
                    builder = builder.cookie(c);
                }
            }

            ClientResponse response = builder.get(ClientResponse.class);
            //ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            if (APIName.equals("login")) {
                cookies = response.getCookies();
                //System.out.println("Cookies Set");
            }
            if (response.getStatus() != HttpServletResponse.SC_OK) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus() + response.getEntity(String.class));
            }

            String output = response.getEntity(String.class);
            System.out.println(output);
            JSONObject jsonObj = new JSONObject(output);
            jsonObj = jsonObj.getJSONObject(APIName.toLowerCase() + "response");
            return jsonObj;

        } catch (Exception e) {

            e.printStackTrace();

        }
        return null;
    }

    public static void main(String... args) {
        /*String password = "Shaatir1!";
        try {
            password = new String(Hex.encodeHex(MessageDigest.getInstance("MD5").digest(password.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        System.out.println(password);*/
        //APICaller.call("sdf", null);
        //System.out.println(APICaller.getHostID("abhishek-Inspiron-1564"));
        //System.out.println(APICaller.getVMsOnHost("bfb6febb-66b6-492a-9565-94dc0ef62e2e"));
        migrateAllVMS("abhishek-Inspiron-1564");

    }

    public static void migrateAllVMS(String node) {
        String hostId = getHostID(node);
        List<String> VMIds = getVMsOnHost(hostId);
        for(String vmId : VMIds) {
            migrate(vmId);
        }
    }

    private static JSONObject handleAsync(String jobid) {
        HashMap<String, String> arguments = new HashMap<String, String>();
        arguments.put("jobid", jobid);
        JSONObject response;
        do {
            response = APICaller.call("queryAsyncJobResult", arguments);
        } while (!response.has("jobresult"));
        return response;

    }
    private static boolean migrate(String vmId) {
        try {
            HashMap<String, String> arguments = new HashMap<String, String>();
            arguments.put("virtualmachineid", vmId);
            JSONObject response = APICaller.call("migrateVirtualMachine", arguments);
            String jobId = response.getString("jobid");
            handleAsync(jobId);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static List<String> getVMsOnHost(String hostId) {
        try {
            HashMap<String, String> arguments = new HashMap<String, String>();
            //arguments.put("hostid", hostId);
            List<String> vmIds = new ArrayList<String>();
            JSONObject response = APICaller.call("listVirtualMachines", arguments);
            JSONArray virtualMachines = response.getJSONArray("virtualmachine");
            for (int i = 0; i < virtualMachines.length(); i++) {
                JSONObject virtualMachine = virtualMachines.getJSONObject(i);
                vmIds.add(virtualMachine.getString("id"));
            }
            return vmIds;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
