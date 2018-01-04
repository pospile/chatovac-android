package cz.underholding.chatovac.dbs;

import cz.underholding.chatovac.MainActivity;
import io.objectbox.BoxStore;

/**
 * Created by pospile on 12/22/2017.
 */

public class DBS {
    private static final DBS ourInstance = new DBS();

    private BoxStore boxStore;

    public static DBS getInstance() {
        return ourInstance;
    }

    private DBS() {
    }

    public void initDB(MainActivity act) {
        boxStore = MyObjectBox.builder().androidContext(act).build();
    }
    public BoxStore getBoxStore() {
        return boxStore;
    }
    public void closeBoxStore(){
        boxStore.close();
    }
}
