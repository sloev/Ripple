package sloev.ripple.adaptors;


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
    private int indexCount;

    public ContactAdaptor(Context context) {
        super(context, 0);
        dataholder = ApplicationSingleton.getDataHolder();
    }

    @Override
    public int getCount() {
        indexCount = dataholder.getIndexList().size();
        return indexCount;// + extraOptions;
    }

    @Override
    public void onClick(View v) {
       // if(v.getClass() == CheckBox.class){
            CheckBox cb = (CheckBox) v;
            UserDataStructure user = (UserDataStructure) cb.getTag();
            user.setEnabled(cb.isChecked());
        /*}else{
            System.out.println("LOL ITS ALIVE");
        }*/


    }

    private static class ViewHolder {
        TextView name;
        CheckBox checkBox;
    }

    @Override
    public int getItemViewType(int position) {
        // Define a way to determine which layout to use, here it's just evens and odds.

            return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 1; // Count of different layouts
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        System.out.println("inside adaptor");
        UserDataStructure user =  dataholder.getUserByIndex(position);


        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();

            //if (getItemViewType(position) == 1){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_list_item, parent, false);

                CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.list_contact_checkbox);
                // Populate the data into the template view using the data object
                checkBox.setChecked(user.isEnabled());
                checkBox.setOnClickListener(this);

                viewHolder.checkBox = checkBox;

                TextView name = (TextView) convertView.findViewById(R.id.list_contact_name);
                viewHolder.name = name;
            /*}else{
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.drawer_choice_item, parent, false);
                TextView name = (TextView) convertView.findViewById(R.id.menu_option_name);
                viewHolder.name = name;
            }*/

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //if (getItemViewType(position) == 1) {
    viewHolder.name.setText(user.getSnippet());
    viewHolder.checkBox.setChecked(user.isEnabled());
    viewHolder.checkBox.setTag(user);

       /* }else{
            viewHolder.name.setText("lolcat");
            //viewHolder.checkBox.setEnabled(false);
        }
        */

        return convertView;
    }
}