package sloev.ripple.activites;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.xbill.DNS.APLRecord;

import java.util.ArrayList;

import sloev.ripple.R;
import sloev.ripple.model.UserDataStructure;
import sloev.ripple.util.ApplicationSingleton;

public class ContactAdaptor extends ArrayAdapter implements View.OnClickListener {
    ApplicationSingleton dataholder;

    public ContactAdaptor(Context context) {
        super(context, 0);
        dataholder = ApplicationSingleton.getDataHolder();
    }

    @Override
    public int getCount() {
        return dataholder.getIndexList().size();
    }

    @Override
    public void onClick(View v) {
        CheckBox cb = (CheckBox) v;
        UserDataStructure user = (UserDataStructure) cb.getTag();
        user.setEnabled(cb.isChecked());
    }

    private static class ViewHolder {
        TextView name;
        CheckBox checkBox;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        UserDataStructure user = dataholder.getUserByIndex(position);

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_list_item, parent, false);
            viewHolder = new ViewHolder();

            TextView name = (TextView) convertView.findViewById(R.id.list_contact_name);
            viewHolder.name = name;

            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.list_contact_checkbox);
            // Populate the data into the template view using the data object
            checkBox.setChecked(user.isEnabled());
            checkBox.setOnClickListener(this);

            viewHolder.checkBox = checkBox;

            convertView.setTag(viewHolder);


        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.name.setText(user.getSnippet());
        viewHolder.checkBox.setChecked(user.isEnabled());
        viewHolder.checkBox.setTag(user);

        return convertView;
    }
}