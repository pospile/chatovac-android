package cz.underholding.chatovac;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


import com.ajithvgiri.searchdialog.OnSearchItemSelected;
import com.ajithvgiri.searchdialog.SearchListItem;
import com.ajithvgiri.searchdialog.SearchableDialog;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.aviran.cookiebar2.CookieBar;
import org.aviran.cookiebar2.OnActionClickListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.goncalves.pugnotification.notification.PugNotification;
import br.com.mobiplus.simplerecylerview.SimpleLinearRecyclerView;
import br.com.mobiplus.simplerecylerview.adapter.OnItemClickListener;
import cz.underholding.chatovac.dbs.DBS;
import cz.underholding.chatovac.dbs.MetaData;
import io.objectbox.Box;
import io.objectbox.query.Query;
import io.objectbox.query.QueryBuilder;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;



public class MainActivity extends AppCompatActivity  {

    protected static boolean isVisible = false;

    @Override
    public void onResume() {
        super.onResume();
        setVisible(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        setVisible(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        DBS.getInstance().initDB(MainActivity.this);
        //JodaTimeAndroid.init(this);
        super.onCreate(savedInstanceState);

        final Box<MetaData> metaDataBox = DBS.getInstance().getBoxStore().boxFor(MetaData.class);
        QueryBuilder<MetaData> builder = metaDataBox.query();
        final Query<MetaData> query = builder.build();
        final MetaData meta = query.findFirst();


        if (meta == null){
            //DBS.getInstance().closeBoxStore();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        else
        {
            // (tohle odstrani login usera)long remove = query.remove();
            setContentView(R.layout.activity_main);


            Socketize(meta);
            RenderMainPage(meta);



            //-----------------


            ImageView img_v = (ImageView) findViewById(R.id.search_btn);
            img_v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Ion.with(MainActivity.this)
                            .load(Config.getInstance().url+"/users/get")
                            .setBodyParameter("token", meta.token)
                            .asJsonArray()
                            .setCallback(new FutureCallback<JsonArray>() {
                                @Override
                                public void onCompleted(Exception e, JsonArray result) {
                                    List<SearchListItem> searchListItems = new ArrayList<>();

                                    for(int i = 0; i < result.size(); i++) {
                                        JsonObject obj = result.get(i).getAsJsonObject();
                                        if (Objects.equals(meta.name, obj.get("user").getAsString()))
                                        {
                                            searchListItems.add(new SearchListItem(obj.get("id").getAsInt(), obj.get("user").getAsString()+" - váš účet"));
                                        }
                                        else
                                        {
                                            searchListItems.add(new SearchListItem(obj.get("id").getAsInt(), obj.get("user").getAsString()));
                                        }
                                    }

                                    SearchableDialog dlg = new SearchableDialog(MainActivity.this, searchListItems, "Connect with user...");
                                    dlg.setOnItemSelected(new OnSearchItemSelected() {
                                        @Override
                                        public void onClick(int position, SearchListItem searchListItem) {
                                            Log.e("CONNECTING USERS", searchListItem.getTitle() + " " + searchListItem.getId());

                                            Ion.with(MainActivity.this)
                                                    .load(Config.getInstance().url+"/users/connect")
                                                    .setBodyParameter("user1", meta.name)
                                                    .setBodyParameter("user2", searchListItem.getTitle())
                                                    .setBodyParameter("token", meta.token)
                                                    .asJsonObject()
                                                    .setCallback(new FutureCallback<JsonObject>() {
                                                        @Override
                                                        public void onCompleted(Exception e, JsonObject result) {
                                                            RenderMainPage(meta);
                                                        }
                                                    });
                                        }
                                    });
                                    dlg.show();


                                }
                            });
                }
            });
            //-----------------



            ImageView logOut = (ImageView) findViewById(R.id.logOut_btn);

            logOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long remove = query.remove();
                    DBS.getInstance().closeBoxStore();
                    MainActivity.this.finish();
                }
            });


        }

    }

    public void RenderMainPage(final MetaData meta) {
        Ion.with(MainActivity.this)
                .load(Config.getInstance().url+"/chat/get")
                .setBodyParameter("name", meta.name)
                .setBodyParameter("token", meta.token)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, final JsonArray result) {
                        // do stuff with the result or error
                        //Log.e("TAG", result.toString());
                        if (e != null)
                        {
                            Log.e("KRUCI", e.toString());
                        }
                        else
                        {
                            if (result != null)
                            {
                                final List<ChatHead> chats = new ArrayList<ChatHead>();
                                for (final JsonElement res :
                                        result) {

                                    Ion.with(MainActivity.this)
                                            .load(Config.getInstance().url+"/chat/get/last")
                                            .setBodyParameter("chat_id", res.getAsJsonObject().get("id").toString())
                                            .setBodyParameter("token", meta.token)
                                            .asJsonArray()
                                            .setCallback(new FutureCallback<JsonArray>() {
                                                @Override
                                                public void onCompleted(Exception e, JsonArray result_message) {

                                                    //Log.e("TAG", result.toString());


                                                    Log.e("TAG", res.toString());
                                                    ChatHead chat = new ChatHead();
                                                    chat.real_chat_id = res.getAsJsonObject().get("id").getAsString();
                                                    chat.img_url = "https://www.shareicon.net/data/128x128/2017/05/30/886553_user_512x512.png";
                                                    chat.name = res.getAsJsonObject().get("name").getAsString();
                                                    if (result_message != null) {
                                                        for (JsonElement res_msg : result_message) {
                                                            Log.e("TAG", res_msg.toString());

                                                            chat.time = res_msg.getAsJsonObject().get("time").getAsString();
                                                            chat.message = res_msg.getAsJsonObject().get("text").getAsString();
                                                            chats.add(chat);
                                                            SimpleLinearRecyclerView recyclerView = findViewById(R.id.recyclerView);
                                                            recyclerView.setCollection(chats, new OnItemClickListener<ChatHead>() {
                                                                @Override
                                                                public void onItemClick(ChatHead chat, @IdRes int resId) {
                                                                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                                                                    intent.putExtra("chat_id", chat.real_chat_id);
                                                                    intent.putExtra("chat_name", chat.getName());
                                                                    startActivity(intent);
                                                                    //Toast.makeText(MainActivity.this, "Clicked item: " + chat.getName(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    }
                                                    else{
                                                        chat.message = "No messages yet";
                                                        chat.time = "";
                                                        chats.add(chat);
                                                        SimpleLinearRecyclerView recyclerView = findViewById(R.id.recyclerView);
                                                        recyclerView.setCollection(chats, new OnItemClickListener<ChatHead>() {
                                                            @Override
                                                            public void onItemClick(ChatHead chat, @IdRes int resId) {
                                                                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                                                                intent.putExtra("chat_id", chat.real_chat_id);
                                                                intent.putExtra("chat_name", chat.getName());
                                                                startActivity(intent);
                                                                //Toast.makeText(MainActivity.this, "Clicked item: " + chat.getName(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }


                                                }
                                            });

                                }


                            }
                        }
                    }
                });
    }

    public void Socketize(final MetaData meta) {
        try {
            final Socket socket = IO.socket("http://78.102.46.113:2579");
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {


                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("name", meta.name);
                        obj.put("token", meta.token);
                        obj.put("device_id", "android");
                        socket.emit("login", obj);
                        Log.e("SOCKET", "connected");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                }

            }).on("notification", new Emitter.Listener() {

                @Override
                public void call(final Object... args) {

                    Log.e("SOCKET", "notification");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final JSONObject data = (JSONObject) args[0];
                            //Log.e("MSG", );


                            try {
                                Ion.with(MainActivity.this)
                                        .load(Config.getInstance().url+"/users/details")
                                        .setBodyParameter("id", data.getInt("user")+"")
                                        .asJsonArray()
                                        .setCallback(new FutureCallback<JsonArray>() {
                                            @Override
                                            public void onCompleted(Exception e, final JsonArray result) {
                                                try {
                                                    if (data.getInt("user") != meta.real_id)
                                                    {
                                                        JsonObject real_obj = result.get(0).getAsJsonObject();
                                                        if (!isVisible){
                                                            CookieBar.build(MainActivity.this)
                                                                    .setTitle("Nová zpráva od: " + real_obj.get("user"))
                                                                    .setMessage(data.getString("text"))
                                                                    .show();
                                                        }
                                                        else {
                                                            PugNotification.with(MainActivity.this)
                                                                    .load()
                                                                    .title("Nová zpráva od: " + real_obj.get("user"))
                                                                    .message(data.getString("text"))
                                                                    .smallIcon(R.drawable.ic_stat_onesignal_default)
                                                                    .largeIcon(R.drawable.ic_stat_onesignal_default)
                                                                    .flags(Notification.DEFAULT_VIBRATE)
                                                                    .simple()
                                                                    .build();
                                                        }
                                                    }
                                                    socket.emit("notification_resp", args);
                                                } catch (JSONException e1) {
                                                    e1.printStackTrace();
                                                }
                                            }
                                        });
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });


                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                }

            });
            socket.connect();

        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }
    }


}
