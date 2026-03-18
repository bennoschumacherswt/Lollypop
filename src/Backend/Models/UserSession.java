package Backend.Models;

import Backend.Models.Enums.ServiceType;

public class UserSession {

    /** type of service that is being used */
    private ServiceType service;

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
