package oskpiech.pg.edu.pl.dormkeydispenser;

class ProgramState {
    private boolean lockerOpened;
    private boolean authenticated;
    private String authMethod;
    private double lightValue;
    private double soundValue;

    ProgramState() {
        this.lockerOpened = false;
        this.authenticated = false;
        this.authMethod = "password";
        this.lightValue = 0d;
        this.soundValue = 0d;
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
     *  Legal values are "photo", "audio" and "password"
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
}
