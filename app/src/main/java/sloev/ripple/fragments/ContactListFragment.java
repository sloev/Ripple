package sloev.ripple.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


import sloev.ripple.R;
import sloev.ripple.adaptors.ContactAdapter;
import sloev.ripple.model.UserDataStructure;
import sloev.ripple.util.ApplicationSingleton;


public class ContactListFragment extends Fragment implements View.OnClickListener {
    ApplicationSingleton dataholder = null;
    ContactAdapter adapter = null;
    ListView listview = null;
    EditText new_contact_name = null;
    Button new_contact_button = null;
    Button delete_contact_button = null;

    private ContactListListener listener = null;


    public static ContactListFragment newInstance() {
        ContactListFragment fragment = new ContactListFragment();
        return fragment;
    }

    public ContactListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataholder = ApplicationSingleton.getDataHolder();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_contact_list, container, false);
        listview = (ListView) v.findViewById(R.id.contact_list_view);

        adapter = new ContactAdapter(getActivity(), R.layout.contact_list_item);
        listview.setAdapter(adapter);

        new_contact_name = (EditText) v.findViewById(R.id.new_contact_name);
        new_contact_button = (Button) v.findViewById(R.id.new_contact_button);
        new_contact_button.setOnClickListener(this);
        delete_contact_button = (Button) v.findViewById(R.id.delete_contact_button);
        delete_contact_button.setOnClickListener(this);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (ContactListListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ContactListListener");
        }
    }

        @Override
        public void onDetach () {
            super.onDetach();
            listener.save_contacts();
            listener = null;

        }

        @Override
        public void onClick (View v) {
            String snippet = new_contact_name.getText().toString();
            int userId = Integer.parseInt(snippet);
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(new_contact_name.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            switch(v.getId()) {
                case R.id.new_contact_button:
                    add_contact(userId, snippet);
                    break;
                case R.id.delete_contact_button:
                    delete_contact(userId, snippet);
                    break;
            }
        }
    public void add_contact(int userId, String snippet){

            boolean enabled = false;
            UserDataStructure userdata = new UserDataStructure(userId, enabled, snippet);
            dataholder.addUserToContacts(userId, userdata);
            System.out.println("added contact");
            if (listener!=null){
                listener.save_contacts();
            }
            adapter.notifyDataSetChanged();

        }
    public void delete_contact(int userId, String snippet){
        dataholder.removeUserToContacts(userId);
        if(listener!=null){
            listener.save_contacts();
        }
        System.out.println( "delete user");
    }
        public interface ContactListListener {
            public void save_contacts();
        }
    }



