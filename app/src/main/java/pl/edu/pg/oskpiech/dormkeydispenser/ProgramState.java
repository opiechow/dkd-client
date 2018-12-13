package pl.edu.pg.oskpiech.dormkeydispenser;

import org.json.JSONException;
import org.json.JSONObject;

class ProgramState {
    private boolean lockerOpened;
    private boolean authenticated;
    private String authMethod;
    private String authMethodToSend;
    private double lightValue;
    private double soundValue;
    private String dataToSend;
    private String userId;

    ProgramState() {
        this.lockerOpened = false;
        this.authenticated = false;
        this.authMethod = "password";
        this.authMethodToSend = "passwdAuth";
        this.lightValue = 0d;
        this.soundValue = 0d;
        this.dataToSend = "";
        this.userId = "0";
    }

    boolean isAuthenticated() {
        return this.authenticated;
    }

    void setAuthenticated(boolean setVal) {
        this.authenticated = setVal;
    }

    String getAuthMethod() {
        return this.authMethod;
    }

    /*
     *  Legal values are "photoAuth", "audioAuth" and "passwdAuth"
     */
    void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    double getLightValue() {
        return lightValue;
    }

    void setLightValue(double lightValue) {
        this.lightValue = lightValue;
    }

    double getSoundValue() {
        return soundValue;
    }

    void setSoundValue(double soundValue) {
        this.soundValue = soundValue;
    }

    boolean isLockerOpened() {
        return lockerOpened;
    }

    void setLockerOpened(boolean lockerOpened) {
        this.lockerOpened = lockerOpened;
    }

    public String getDataToSend() {
        return dataToSend;
    }

    public void setDataToSend(String dataToSend) {
        this.dataToSend = dataToSend;
    }

    JSONObject getAuthJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("userId", 144526);
        json.put("msgType", getAuthMethodToSend());
        json.put("authData", getDataToSend());
        return json;
    }

    public String getAuthMethodToSend() {
        return authMethodToSend;
    }

    public void setAuthMethodToSend(String authMethodToSend) {
        this.authMethodToSend = authMethodToSend;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
