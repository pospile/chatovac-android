package cz.underholding.chatovac;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ajithvgiri.searchdialog.OnSearchItemSelected;
import com.ajithvgiri.searchdialog.SearchListItem;
import com.ajithvgiri.searchdialog.SearchableDialog;
import com.geniusforapp.fancydialog.FancyAlertDialog;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.rahuljanagouda.statusstories.StatusStoriesActivity;

import org.michaelbel.bottomsheetdialog.BottomSheet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;
import cz.underholding.chatovac.dbs.DBS;
import cz.underholding.chatovac.dbs.MetaData;
import io.objectbox.Box;
import io.objectbox.query.Query;
import io.objectbox.query.QueryBuilder;
import siclo.com.ezphotopicker.api.EZPhotoPick;
import siclo.com.ezphotopicker.api.EZPhotoPickStorage;
import siclo.com.ezphotopicker.api.models.EZPhotoPickConfig;
import siclo.com.ezphotopicker.api.models.PhotoSource;

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



        ImageView settings = (ImageView)findViewById(R.id.settings_btn);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int[] items1 = new int[]{
                        R.string.add_user,
                        R.string.rename
                };

                new BottomSheet.Builder(ChatActivity.this)
                .setTitle("Settings for chat: " + getIntent().getStringExtra("chat_name"))
                .setDarkTheme(false)
                .setItems(items1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                //region add_user_to_chat
                                Ion.with(ChatActivity.this)
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

                                                SearchableDialog dlg = new SearchableDialog(ChatActivity.this, searchListItems, "Add user to chat...");
                                                dlg.setOnItemSelected(new OnSearchItemSelected() {
                                                    @Override
                                                    public void onClick(int position, SearchListItem searchListItem) {
                                                        Log.e("CONNECTING USERS", searchListItem.getTitle() + " " + searchListItem.getId());
                                                        Ion.with(ChatActivity.this)
                                                                .load(Config.getInstance().url+"/chat/invite")
                                                                .setBodyParameter("user_id", searchListItem.getId()+"")
                                                                .setBodyParameter("chat_id", getIntent().getStringExtra("chat_id"))
                                                                .asJsonObject()
                                                                .setCallback(new FutureCallback<JsonObject>() {
                                                                    @Override
                                                                    public void onCompleted(Exception e, JsonObject result) {
                                                                        if (e != null) {
                                                                            Log.e("RETURN", e.toString());
                                                                        }
                                                                        else {
                                                                            Log.e("RETURN", result.toString());
                                                                            Config.getInstance().main.RenderMainPage(meta);
                                                                            ChatActivity.this.finish();
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });
                                                dlg.show();


                                            }
                                        });
                                //endregion
                                break;
                            case 1:
                                //region rename_chat
                                new MaterialDialog.Builder(ChatActivity.this)
                                        .title("Change chat name")
                                        .content("Change chat name from: " + getIntent().getStringExtra("chat_name") + " to: ")
                                        .inputType(InputType.TYPE_CLASS_TEXT)
                                        .input("New name", "", new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(MaterialDialog dialog, final CharSequence input) {
                                                // Do something
                                                Log.e("New name", input.toString());
                                                Ion.with(ChatActivity.this)
                                                        .load(Config.getInstance().url+"/chat/rename")
                                                        .setBodyParameter("chat_id", getIntent().getStringExtra("chat_id"))
                                                        .setBodyParameter("name",  input.toString())
                                                        .asJsonObject()
                                                        .setCallback(new FutureCallback<JsonObject>() {
                                                            @Override
                                                            public void onCompleted(Exception e, JsonObject result) {
                                                                if (e != null){
                                                                    Log.e("RESP", e.toString());
                                                                }else {
                                                                    Log.e("RESP", result.toString());
                                                                    TextView name = (TextView)findViewById(R.id.chatName);
                                                                    name.setText(input.toString());
                                                                    Config.getInstance().main.RenderMainPage(meta);
                                                                }
                                                            }
                                                        });
                                            }
                                        }).show();
                                //endregion
                                break;
                        }
                    }
                })
                .show();

            }
        });


        ImageView story_add = (ImageView)findViewById(R.id.story_add_btn);
        story_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EZPhotoPickConfig config = new EZPhotoPickConfig();
                config.photoSource = PhotoSource.GALLERY; // or PhotoSource.CAMERA
                config.isAllowMultipleSelect = false; // only for GALLERY pick and API >18
                EZPhotoPick.startPhotoPickActivity(ChatActivity.this, config);
            }
        });


        ImageView story = (ImageView)findViewById(R.id.story_btn);
        story.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //urls array that should be shown as a story

                Ion.with(ChatActivity.this)
                        .load(Config.getInstance().url+"/stories/get")
                        .setBodyParameter("chat_id", getIntent().getStringExtra("chat_id"))
                        .setBodyParameter("token", meta.token)
                        .asJsonArray()
                        .setCallback(new FutureCallback<JsonArray>() {
                            @Override
                            public void onCompleted(Exception e, JsonArray result) {


                                Log.e("ERR", result.toString());

                                if (result.size() != 0){
                                    final String[] resources = new String[result.size()];

                                    for (int i = 0; i < result.size(); i++) {
                                        resources[i] = Config.getInstance().url+result.get(i).getAsJsonObject().get("url").getAsString();
                                    }

                                    //launch with presettings
                                    Intent a = new Intent(ChatActivity.this, StatusStoriesActivity.class);
                                    a.putExtra(StatusStoriesActivity.STATUS_RESOURCES_KEY, resources);
                                    a.putExtra(StatusStoriesActivity.STATUS_DURATION_KEY, 3000L);
                                    a.putExtra(StatusStoriesActivity.IS_IMMERSIVE_KEY, true);
                                    a.putExtra(StatusStoriesActivity.IS_CACHING_ENABLED_KEY, true);
                                    a.putExtra(StatusStoriesActivity.IS_TEXT_PROGRESS_ENABLED_KEY, true);
                                    startActivity(a);
                                }
                                else {

                                    FancyAlertDialog.Builder alert = new FancyAlertDialog.Builder(ChatActivity.this)
                                            .setimageResource(R.drawable.ic_face_black_24dp)
                                            .setTextTitle("NEW STORY")
                                            .setBody("There is no stories yet, upload one?")
                                            .setNegativeColor(R.color.colorAccent)
                                            .setNegativeButtonText("No, thank you")
                                            .setOnNegativeClicked(new FancyAlertDialog.OnNegativeClicked() {
                                                @Override
                                                public void OnClick(View view, Dialog dialog) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .setPositiveButtonText("Yes please")
                                            .setPositiveColor(R.color.colorPrimary)
                                            .setOnPositiveClicked(new FancyAlertDialog.OnPositiveClicked() {
                                                @Override
                                                public void OnClick(View view, Dialog dialog) {
                                                    EZPhotoPickConfig config = new EZPhotoPickConfig();
                                                    config.photoSource = PhotoSource.GALLERY; // or PhotoSource.CAMERA
                                                    config.isAllowMultipleSelect = false; // only for GALLERY pick and API >18
                                                    EZPhotoPick.startPhotoPickActivity(ChatActivity.this, config);
                                                }
                                            })
                                            .setBodyGravity(FancyAlertDialog.TextGravity.LEFT)
                                            .setTitleGravity(FancyAlertDialog.TextGravity.CENTER)
                                            .setCancelable(false)
                                            .build();
                                    alert.show();

                                }


                            }
                        });



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
                                    Config.getInstance().main.RenderMainPage(meta);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == EZPhotoPick.PHOTO_PICK_GALLERY_REQUEST_CODE || requestCode == EZPhotoPick.PHOTO_PICK_CAMERA_REQUEST_CODE) {
            EZPhotoPickStorage ezPhotoPickStorage = new EZPhotoPickStorage(this);
            String photoName = data.getStringExtra(EZPhotoPick.PICKED_PHOTO_NAME_KEY);
            String photoPath = ezPhotoPickStorage.getAbsolutePathOfStoredPhoto("", photoName);
            Log.e("photo", photoPath);

            Ion.with(ChatActivity.this)
                    .load(Config.getInstance().url+"/stories/upload")
                    .setMultipartParameter("chat_id", getIntent().getStringExtra("chat_id"))
                    .setMultipartFile("photo", "application/image", new File(photoPath))
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            Log.e("resp", result+"");
                        }
                    });
        }
    }

}
