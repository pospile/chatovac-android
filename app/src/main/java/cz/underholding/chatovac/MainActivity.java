package cz.underholding.chatovac;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.Toast;

import com.ajithvgiri.searchdialog.SearchListItem;
import com.ajithvgiri.searchdialog.SearchableDialog;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.mobiplus.simplerecylerview.SimpleLinearRecyclerView;
import br.com.mobiplus.simplerecylerview.adapter.OnItemClickListener;
import cz.underholding.chatovac.dbs.DBS;
import cz.underholding.chatovac.dbs.MetaData;
import io.objectbox.Box;
import io.objectbox.query.Query;
import io.objectbox.query.QueryBuilder;


public class MainActivity extends AppCompatActivity  {



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
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        else
        {
            // (tohle odstrani login usera)long remove = query.remove();
            setContentView(R.layout.activity_main);

            List<SearchListItem> searchListItems = new ArrayList<>();
            searchListItems.add(new SearchListItem(0, "Patrik"));
            searchListItems.add(new SearchListItem(0, "George"));
            final SearchableDialog searchableDialog = new SearchableDialog(this, searchListItems, "Kontakty");

            ImageView img_v = (ImageView) findViewById(R.id.search_btn);
            ImageView logOut = (ImageView) findViewById(R.id.logOut_btn);
            img_v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchableDialog.show();
                }
            });
            logOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long remove = query.remove();
                    DBS.getInstance().closeBoxStore();
                    MainActivity.this.finish();
                }
            });



            Ion.with(MainActivity.this)
                    .load("http://10.0.2.2:3000/chat/get")
                    .setBodyParameter("name", meta.name)
                    .setBodyParameter("token", meta.token)
                    .asJsonArray()
                    .setCallback(new FutureCallback<JsonArray>() {
                        @Override
                        public void onCompleted(Exception e, final JsonArray result) {
                            // do stuff with the result or error
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
                                                .load("http://10.0.2.2:3000/chat/get/last")
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
                                                });

                                    }


                                }
                            }
                        }
                    });

        }

    }


}
