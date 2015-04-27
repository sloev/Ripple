package sloev.ripple.adaptors;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import sloev.ripple.R;
import sloev.ripple.model.UserDataStructure;
import sloev.ripple.util.ApplicationSingleton;

/**
 * Created by johannes on 20/04/15.
 */
public class ContactAdapter extends ArrayAdapter implements View.OnClickListener {
    ApplicationSingleton dataholder;
    Context context;
    int layoutResourceId;


    public ContactAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
        dataholder = ApplicationSingleton.getDataHolder();
        layoutResourceId = resource;

    }
    @Override
    public int getCount() {
        int indexCount = dataholder.getIndexList().size();
        return indexCount;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ViewHolder();
            holder.txtTitle = (TextView)row.findViewById(R.id.list_contact_name);
            holder.checkBox = (CheckBox) row.findViewById(R.id.list_contact_checkbox);
            // Populate the data into the template view using the data object
            holder.checkBox.setOnClickListener(this);
            row.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)row.getTag();
        }
        UserDataStructure user =  dataholder.getUserByIndex(position);

        holder.txtTitle.setText(user.getSnippet());
        holder.checkBox.setChecked(user.isEnabled());
        holder.checkBox.setTag(position);
        return row;
    }

    @Override
    public void onClick(View v) {
        System.out.println("contact click");
        CheckBox cb = (CheckBox) v;
        Integer position = (Integer) cb.getTag();
        UserDataStructure user =  dataholder.getUserByIndex(position);
        boolean checked = cb.isChecked();
        System.out.println(checked);
        user.setEnabled(checked);
    }

    static class ViewHolder
    {
        TextView txtTitle;
        CheckBox checkBox;

    }
}
