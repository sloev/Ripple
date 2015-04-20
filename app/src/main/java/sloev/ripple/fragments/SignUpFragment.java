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
 * to handle interaction events.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment implements View.OnClickListener {
    private SharedPreferences settings;
    private EditText passwordField;
    private EditText userName;

    private SignUpListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    // TODO: Rename and change types and number of parameters
    public static SignUpFragment newInstance() {
        SignUpFragment fragment = new SignUpFragment();
        return fragment;
    }

    public SignUpFragment() {
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
        View v =  inflater.inflate(R.layout.fragment_sign_up, container, false);

        userName = (EditText) v.findViewById(R.id.userNameField);
        userName.setText("");
        Button b = (Button) v.findViewById(R.id.signupButton);
        b.setOnClickListener(this);

        passwordField = (EditText) v.findViewById(R.id.passwordField);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SignUpListener) activity;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signupButton:
                if (mListener != null) {
                    String username = userName.getText().toString();
                    String password = passwordField.getText().toString();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(passwordField.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    imm.hideSoftInputFromWindow(userName.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    mListener.sign_up(username, password);
                }
                break;
        }
    }

    public interface SignUpListener {
        public void sign_up(String username, String password);
    }

}
