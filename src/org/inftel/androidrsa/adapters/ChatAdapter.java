
package org.inftel.androidrsa.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.inftel.androidrsa.R;
import org.inftel.androidrsa.xmpp.AvatarsCache;
import org.inftel.androidrsa.xmpp.Conexion;
import org.jivesoftware.smack.packet.Message;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChatAdapter extends ArrayAdapter<Message> {
    private Context context;
    private ArrayList<Message> list;
    private String TAG = "ChatAdapter";
    private HashMap<String, Bitmap> avatarMap;
    private String myJid;

    static class ViewHolder {
        public TextView body;
        public ImageView avatar;
        public LinearLayout layout;
        public LinearLayout layoutInterno;
        public TextView time;
    }

    public ChatAdapter(Context context, ArrayList<Message> listMessages) {
        super(context, R.layout.chat_row, listMessages);
        this.context = context;
        this.list = listMessages;
        myJid = Conexion.getInstance().getUser();
        this.avatarMap = AvatarsCache.getInstance();
        this.avatarMap.put(myJid, AvatarsCache.getMyAvatar());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.chat_row, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.body = (TextView) rowView.findViewById(R.id.body);
            viewHolder.avatar = (ImageView) rowView.findViewById(R.id.chatAvatar);
            viewHolder.layout = (LinearLayout) rowView.findViewById(R.id.chatrow_layout);
            viewHolder.layoutInterno = (LinearLayout) rowView
                    .findViewById(R.id.chat_imgtext_layout);
            viewHolder.time = (TextView) rowView.findViewById(R.id.time);
            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();
        Message m = list.get(position);
        if (m.getBody() != null) {
            holder.body.setText(m.getBody());
        }

        if ((m.getSubject() != null) && (m.getSubject() != "")) {
            holder.time.setText(m.getSubject());
        }
        else {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            m.setSubject(sdf.format(new Date()));
            list.set(position, m);
            holder.time.setText(m.getSubject());
        }

        if (m.getFrom().equals(myJid)) {
            holder.layout.setBackgroundResource(R.drawable.a9p_09_11_00089);
            holder.avatar.setImageBitmap(avatarMap.get(myJid));
        }

        else {
            holder.avatar.setImageBitmap(avatarMap.get(m.getFrom()));
            holder.layout.setBackgroundResource(R.drawable.a9p_09_11_00087);
        }

        return rowView;
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    public HashMap<String, Bitmap> getAvatarMap() {
        return avatarMap;
    }

    public void setAvatarMap(HashMap<String, Bitmap> avatarMap) {
        this.avatarMap = avatarMap;
    }

}
