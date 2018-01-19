package cz.underholding.chatovac;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.List;
import java.util.concurrent.ExecutionException;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;
import cz.underholding.chatovac.dbs.DBS;
import cz.underholding.chatovac.dbs.MetaData;
import io.objectbox.Box;
import io.objectbox.query.Query;
import io.objectbox.query.QueryBuilder;

public class ChatActivity extends AppCompatActivity {

    ListView chat_list;
    EditText msgBox;
    public int id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Box<MetaData> metaDataBox = DBS.getInstance().getBoxStore().boxFor(MetaData.class);
        QueryBuilder<MetaData> builder = metaDataBox.query();
        Query<MetaData> query = builder.build();
        final MetaData meta = query.findFirst();
        id = meta.real_id;

        Intent intent = getIntent();
        Log.e("CHAT_ID", intent.getStringExtra("chat_id"));

        TextView name = (TextView)findViewById(R.id.chatName);
        name.setText(getIntent().getStringExtra("chat_name"));

        final ChatView chatView = (ChatView) findViewById(R.id.chat_view);

        chat_list = (ListView) findViewById(R.id.chat_list);
        msgBox = (EditText)findViewById(R.id.input_edit_text);

        Button button = new Button(this);
        button.setText("Load more");
        button.setId(R.id.load_more);
        chat_list.addHeaderView(button);

        chatView.setOnSentMessageListener(new ChatView.OnSentMessageListener() {
            @Override
            public boolean sendMessage(final ChatMessage chatMessage) {
                // perform actual message sending
                try {
                    Ion.with(ChatActivity.this)
                            .load(Config.getInstance().url+"/chat/send")
                            .setBodyParameter("name", meta.name)
                            .setBodyParameter("token", meta.token)
                            .setBodyParameter("chat_id", getIntent().getStringExtra("chat_id"))
                            .setBodyParameter("message", chatMessage.getMessage())
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                    ChatMessage msg = new ChatMessage(chatMessage.getMessage(), chatMessage.getTimestamp(), ChatMessage.Type.SENT);
                                    chatView.addMessage(msg);
                                    msgBox.setText("");
                                }
                            });
                    return false;
                    //return true;
                }
                catch (Exception e) {
                    return false;
                }

            }
        });


        Ion.with(ChatActivity.this)
                .load(Config.getInstance().url+"/chat/get/id")
                .setBodyParameter("chat_id", getIntent().getStringExtra("chat_id"))
                .setBodyParameter("limit", "100")
                .setBodyParameter("offset", "0")
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        //Log.e("CHAT"+getIntent().getStringExtra("chat_id"), "RESULTS:" + result.toString());
                        for (JsonElement jsonElement : result) {
                            JsonObject obj = jsonElement.getAsJsonObject();
                            ChatMessage msg;
                            if (id == obj.get("user").getAsInt())
                            {
                                msg = new ChatMessage(obj.get("text").getAsString(), obj.get("time").getAsLong(), ChatMessage.Type.SENT);
                            }
                            else
                            {
                                msg = new ChatMessage(obj.get("text").getAsString(), obj.get("time").getAsLong(), ChatMessage.Type.RECEIVED);
                            }
                            chatView.addMessage(msg);
                        }
                    }
                });


    }

}
