package cz.underholding.chatovac.dbs;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class MetaData {


    @Id
    public long id;

    public String name;
    public String token;
    public int real_id;
    public String url;

    public MetaData(){}

    public MetaData(String _name, String _token, int _real_id, String _url)
    {
        name = _name;
        token = _token;
        real_id = _real_id;
        url = _url;
    }
}