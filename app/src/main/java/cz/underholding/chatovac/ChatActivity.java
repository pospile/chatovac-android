package cz.underholding.chatovac;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.rahuljanagouda.statusstories.StatusStoriesActivity;

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


        ImageView story = (ImageView)findViewById(R.id.story_btn);
        story.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //urls array that should be shown as a story
                final String[] resources = new String[]{
                        "https://firebasestorage.googleapis.com/v0/b/firebase-satya.appspot.com/o/images%2Fi00001.jpg?alt=media&token=460667e4-e084-4dc5-b873-eefa028cec32",
                        "https://firebasestorage.googleapis.com/v0/b/firebase-satya.appspot.com/o/images%2Fi00002.jpg?alt=media&token=e8e86192-eb5d-4e99-b1a8-f00debcdc016",
                        "https://firebasestorage.googleapis.com/v0/b/firebase-satya.appspot.com/o/images%2Fi00004.jpg?alt=media&token=af71cbf5-4be3-4f8a-8a2b-2994bce38377",
                        "https://firebasestorage.googleapis.com/v0/b/firebase-satya.appspot.com/o/images%2Fi00005.jpg?alt=media&token=7d179938-c419-44f4-b965-1993858d6e71",
                        "https://firebasestorage.googleapis.com/v0/b/firebase-satya.appspot.com/o/images%2Fi00006.jpg?alt=media&token=cdd14cf5-6ed0-4fb7-95f5-74618528a48b",
                        "https://firebasestorage.googleapis.com/v0/b/firebase-satya.appspot.com/o/images%2Fi00007.jpg?alt=media&token=98524820-6d7c-4fb4-89b1-65301e1d6053",
                        "https://firebasestorage.googleapis.com/v0/b/firebase-satya.appspot.com/o/images%2Fi00008.jpg?alt=media&token=7ef9ed49-3221-4d49-8fb4-2c79e5dab333",
                        "https://firebasestorage.googleapis.com/v0/b/firebase-satya.appspot.com/o/images%2Fi00009.jpg?alt=media&token=00d56a11-7a92-4998-a05a-e1dd77b02fe4",
                        "https://firebasestorage.googleapis.com/v0/b/firebase-satya.appspot.com/o/images%2Fi00010.jpg?alt=media&token=24f8f091-acb9-432a-ae0f-7e6227d18803",
                };

                //launch with presettings
                Intent a = new Intent(ChatActivity.this, StatusStoriesActivity.class);
                a.putExtra(StatusStoriesActivity.STATUS_RESOURCES_KEY, resources);
                a.putExtra(StatusStoriesActivity.STATUS_DURATION_KEY, 3000L);
                a.putExtra(StatusStoriesActivity.IS_IMMERSIVE_KEY, true);
                a.putExtra(StatusStoriesActivity.IS_CACHING_ENABLED_KEY, true);
                a.putExtra(StatusStoriesActivity.IS_TEXT_PROGRESS_ENABLED_KEY, true);
                startActivity(a);

            }
        });



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
