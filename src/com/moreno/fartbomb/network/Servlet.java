package com.moreno.fartbomb.network;

public enum Servlet {

    SIGNUP("AddUser"), //
    AUTHENTICATE("Authentication"), //
    FORGOTTEN("ForgotUser"), //
    CHANGE_ACCOUNT("ChangeAccount"), //
    FART_HALL("FartHall"), //
    USER_INFO("UserInfo"), //
    FIND_FRIENDS("FindFriends"), //
    FRIEND_REQUEST("Friendship"), //
    SAVE_BOMB("SaveBomb"), //
    FETCH_AUDIO("FetchAudio"), //
    SPLASH("Splash"), //
    RATE_BOMB("RateBomb")//
    ;

    public static Servlet getEnum(String value) {
        for (Servlet pt : values()) {
            if (pt.toString().equals(value)) {
                return pt;
            }
        }
        return null;
    }

    private final String sValue;

    private Servlet(String value) {
        sValue = value;
    }

    @Override
    public String toString() {
        return sValue;
    }
}
