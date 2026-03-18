package Backend.Models;

import Backend.Models.Enums.ServiceType;

public class UserSession {

    private int id;

    private int subscriber_id;

    /** type of service that is being used */
    private ServiceType service;

    // TODO check if sessions are measured in seconds or minutes
    /** for how long the service is being used in seconds */
    private int duration;

    public UserSession(ServiceType service, int duration){
        this.service = service;
        this.duration = duration;

    }

    public ServiceType getService(){
        return this.service;
    }

    public int getDuration(){
        return this.duration;
    }

}
