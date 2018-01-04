package cz.underholding.chatovac.dbs;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by pospile on 12/22/2017.
 */

@Entity
public class ChatMessage {
    @Id
    public long id;

    public int db_id;
    public String name;
    public String time;
    public String message;
    public String img_url;
}
