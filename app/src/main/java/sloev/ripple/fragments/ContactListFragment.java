package sloev.ripple.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import sloev.ripple.R;
import sloev.ripple.adaptors.ContactAdapter;
import sloev.ripple.adaptors.ContactAdaptor;
import sloev.ripple.util.ApplicationSingleton;


public class ContactListFragment extends Fragment {
    ContactAdapter adaptor = null;
    ListView listview = null;

    // TODO: Rename and change types and number of parameters
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_contact_list, container, false);
        listview = (ListView) v.findViewById(R.id.contact_list_view);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                inflater.getContext(), android.R.layout.simple_list_item_1,
                GetlistContact());
        adaptor = new ContactAdapter(getActivity(), R.layout.contact_list_item);

        listview.setAdapter(adaptor);
        return v;
    }
    private ArrayList<String> GetlistContact(){
        ArrayList<String> contactlist = new ArrayList<String>();
        for (int i = 0;i<10;i++) {
            contactlist.add(Integer.toString(i));
        }

        return contactlist;

}
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        System.out.println(ApplicationSingleton.getDataHolder().getIndexList());




    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */


}
