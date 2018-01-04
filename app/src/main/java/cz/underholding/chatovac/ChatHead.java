package cz.underholding.chatovac;

import br.com.mobiplus.simplerecylerview.annotations.ImageAdapter;
import br.com.mobiplus.simplerecylerview.annotations.LayoutAdapter;
import br.com.mobiplus.simplerecylerview.annotations.TextAdapter;

@LayoutAdapter(layoutResId = R.layout.chat_head)
public class ChatHead {

    public String name;
    public String message;
    public String img_url;
    public String time;
    public String real_chat_id;


    @TextAdapter(resId = R.id.textName)
    public String getName() {
        return name;
    }

    @ImageAdapter(resId = R.id.imageIcon)
    public String getIconUrl() {
        return img_url;
    }

    @TextAdapter(resId = R.id.textMessage)
    public String getMessage() {
        return message;
    }

    @TextAdapter(resId = R.id.textTime)
    public String getTime() {
        return time;
    }

    public ChatHead convertDbModelToChatHead(){
        return new ChatHead();
    }


}