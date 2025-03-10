package codingwithmitch.com.tabiandating;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Objects;

import codingwithmitch.com.tabiandating.models.Message;
import codingwithmitch.com.tabiandating.models.User;
import codingwithmitch.com.tabiandating.util.PreferenceKeys;
import codingwithmitch.com.tabiandating.util.Resources;
import de.hdodenhof.circleimageview.CircleImageView;


public class ChatFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "ChatFragment";

    //widgets
    private TextView mFragmentHeading;
    private CircleImageView mProfileImage;
    private RelativeLayout mBackArrow;
    private AutoCompleteTextView mNewMessage;
    private TextView mSendMessage;
    private RelativeLayout mRelativeLayoutTop;

    //vars
    private Message mMessage;
    private ArrayList<Message> mMessages = new ArrayList<>();
    private ChatRecyclerViewAdapter mChatRecyclerViewAdapter;
    private RecyclerView mRecyclerView;
    private User mCurrentUser = new User();
    private IMainActivity mInterface;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if(bundle != null){
            mMessage = bundle.getParcelable(getString(R.string.intent_message));
            mMessages.add(mMessage);
            Log.d(TAG, "onCreate: got incoming bundle: " + mMessage.getUser().getName());
        }
        getSavedPreferences();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        Log.d(TAG, "onCreateView: started.");
        mBackArrow = view.findViewById(R.id.back_arrow);
        mFragmentHeading = view.findViewById(R.id.fragment_heading);
        mProfileImage = view.findViewById(R.id.profile_image);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mSendMessage = view.findViewById(R.id.post_message);
        mNewMessage = view.findViewById(R.id.input_message);
        mRelativeLayoutTop = view.findViewById(R.id.relLayoutTop);

        mSendMessage.setOnClickListener(this);
        mRelativeLayoutTop.setOnClickListener(this);
        mBackArrow.setOnClickListener(this);

        initToolbar();
        initRecyclerView();
        setBackgroundImage(view);
        setupAutoCompleteTextView();

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setupAutoCompleteTextView(){
        String[] messages = getResources().getStringArray(R.array.message_suggestions_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()),android.R.layout.simple_list_item_1,messages);
        mNewMessage.setAdapter(adapter);
    }


    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: init recyclerview.");
        mChatRecyclerViewAdapter = new ChatRecyclerViewAdapter(getActivity(), mMessages);
        mRecyclerView.setAdapter(mChatRecyclerViewAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void setBackgroundImage(View view){
        ImageView backgroundView = view.findViewById(R.id.background);
        Glide.with(getActivity())
                .load(Resources.BACKGROUND_HEARTS)
                .into(backgroundView);
    }


    private void getSavedPreferences(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String name = preferences.getString(PreferenceKeys.NAME, "");
        mCurrentUser.setName(name);

        String gender = preferences.getString(PreferenceKeys.GENDER, getString(R.string.gender_none));
        mCurrentUser.setGender(gender);

        String profileImageImage = preferences.getString(PreferenceKeys.PROFILE_IMAGE, "");
        mCurrentUser.setProfile_image(profileImageImage);
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick: clicked.");

        if(view.getId() == R.id.back_arrow){
            Log.d(TAG, "onClick: navigating back.");
            mInterface.onBackPressed();
        }
        if(view.getId() == R.id.post_message){
            Log.d(TAG, "onClick: posting new message.");
            mMessages.add(new Message(mCurrentUser, mNewMessage.getText().toString()));
            mChatRecyclerViewAdapter.notifyDataSetChanged();
            mNewMessage.setText("");
            mRecyclerView.smoothScrollToPosition(mMessages.size() - 1);
        }
        if(view.getId() == R.id.relLayoutTop){
            Log.d(TAG, "onClick: navigating back.");
            mInterface.inflateViewProfileFragment(mMessage.getUser());
        }

    }


    private void initToolbar(){
        Log.d(TAG, "initToolbar: initializing toolbar.");
        mBackArrow.setOnClickListener(this);
        mFragmentHeading.setText(mMessage.getUser().getName());
        Glide.with(getActivity())
                .load(mMessage.getUser().getProfile_image())
                .into(mProfileImage);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called.");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mInterface = (IMainActivity) getActivity();
    }

    public void moveFocusForward(){
        try{
            if(getActivity().getCurrentFocus().getId() == R.id.back_arrow){
                Log.d(TAG, "moveFocusForward: setting focus to top relative layout");
                mRelativeLayoutTop.getParent().requestChildFocus(mRelativeLayoutTop, mRelativeLayoutTop);
                mRelativeLayoutTop.requestFocus();
            }
            else if(getActivity().getCurrentFocus().getId() == R.id.relLayoutTop){
                Log.d(TAG, "moveFocusForward: setting focus to message input field");
                mNewMessage.requestFocus();
            }
            else if(getActivity().getCurrentFocus().getId() == R.id.input_message){
                Log.d(TAG, "moveFocusForward: setting focus to send button");
                mSendMessage.requestFocus();
            }
            else if(getActivity().getCurrentFocus().getId() == R.id.post_message){
                Log.d(TAG, "moveFocusForward: setting focus to back arrow");
                mBackArrow.getParent().requestChildFocus(mBackArrow, mBackArrow);
                mBackArrow.requestFocus();
            }
            else{
                Log.d(TAG, "moveFocusForward: setting focus to back arrow");
                mBackArrow.getParent().requestChildFocus(mBackArrow, mBackArrow);
                mBackArrow.requestFocus();
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }

}
















