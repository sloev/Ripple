package sloev.ripple.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import sloev.ripple.R;
import sloev.ripple.util.ApplicationSingleton;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SignInFragment.SignInListener} interface
 * to handle interaction events.
 * Use the {@link SignInFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignInFragment extends Fragment implements View.OnClickListener{
    private EditText passwordField;
    //private EditText userName;
    private TextView userNameView;
    private SharedPreferences settings;
    private String username;

    private SignInListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    // TODO: Rename and change types and number of parameters
    public static SignInFragment newInstance() {
        SignInFragment fragment = new SignInFragment();

        return fragment;
    }

    public SignInFragment() {
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
        View v = inflater.inflate(R.layout.fragment_sign_in, container, false);
/*
        View v = super.onCreateView(inflater, container, savedInstanceState);
*/
        username = settings.getString(getString(R.string.SIGNED_IN_USER), "");

        userNameView = (TextView) v.findViewById(R.id.userNameView);
        userNameView.setText(username.toString());
        Button b = (Button) v.findViewById(R.id.signinButton);
        b.setOnClickListener(this);

        passwordField = (EditText) v.findViewById(R.id.passwordField);

        return v;//inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

   /* // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.sign_in("lol", "cat");
        }
    }*/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signinButton:
                if (mListener != null) {
                    String password = passwordField.getText().toString();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(passwordField.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    mListener.sign_in(username, password );
                }
                break;
        }
    }
        @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SignInListener) activity;
            if (settings == null){
                settings = activity.getSharedPreferences(ApplicationSingleton.PREFS_NAME, 0);
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface SignInListener {
        public void sign_in(final String username, final String password);
    }

}
