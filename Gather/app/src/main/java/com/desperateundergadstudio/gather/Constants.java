package com.desperateundergadstudio.gather;

/**
 * Created by Ryan on 11/10/2014.
 */
public class Constants {
    static String api_base = "http://rthill91.synology.me:8081";
    static String login = "/user/login";
    static String logout = "/user/logout";
    static String register = "/user/register";
    static String getAttendingEvents = "/events/user/attending";
    static String getCreatedEvents = "/events/user/created";
    static String getAllEvents = "/events/all";
    static String createEvent = "/events/create";
    static String getEventComments = "/events/comments";
    static String addComment = "/events/addComment";
    static String attendEvent = "/events/setAttend";
    static String unattendEvent = "/events/unsetAttend";
    static String deleteEvent = "/events/delete";
    static String session_prefs = "Session";
}
