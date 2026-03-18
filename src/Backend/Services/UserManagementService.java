package Backend.Services;

import Backend.Models.Enums.SubscriptionType;
import Backend.Models.Enums.TerminalType;
import Backend.Models.Subscriber;

public class UserManagementService {

    private static final DataBaseDummy db = new DataBaseDummy();

    public void createUser(int id, long msin, String firstName, String lastName,
                           TerminalType terminal, SubscriptionType subscription){
        Subscriber newUser = new Subscriber(id, msin, firstName, lastName, terminal, subscription);
        // db.put newUser
        return;
    }

    public Subscriber readUser(int id){
        // db.get
        return db.getUser(id);
    }


//    Read
//
//    Update
//
//    Calculate charges
//
//
//    Delete
}
