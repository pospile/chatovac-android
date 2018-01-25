package cz.underholding.chatovac;

/**
 * Created by pospile on 15/01/2018.
 */
public class Config {
    private static Config ourInstance = new Config();

    //public String url = "http://78.102.46.113:3000";
    public String url = "http://10.0.2.2:3000";

    public MainActivity main = null;

    public static Config getInstance() {
        return ourInstance;
    }

    private Config() {
    }
}
