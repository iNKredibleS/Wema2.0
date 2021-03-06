package com.inkredibles.wema20;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.inkredibles.wema20.models.Post;
import com.inkredibles.wema20.models.Rak;
import com.inkredibles.wema20.models.User;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseRole;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.ContextCompat.checkSelfPermission;

//import android.app.Fragment;

/*This Fragment handles the functionality to create posts. A reflection is composed of the title, body, location and an image. A user can
 *  also set if the reflection is for an act of kindness given or received. In addition, they can set it to be private or public. If it
 *  is private, only they can see it in their archive. The create Post Fragment is also used for creating a post
 *  after a successful rak completed and for group posts. The set up is slightly different for each case which is why the
 *  booleans isGroup, isRak, and isReflection are checked on viewCreated and OnResume. On successful post created the fragment will
 *  go back to feed fragment*/
public class CreatePostFragment extends Fragment implements DialogueListener {

    @BindView(R.id.Title)
    EditText et_title;
    @BindView(R.id.et_message)
    EditText et_message;
    @BindView(R.id.pictureHolder)
    ImageView pictureHolder;
    @BindView(R.id.pictureTaken)
    ImageView pictureTaken;


    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_GALLERY_IMAGE = 2;
    private static View view;

    private static File filesDir;
    public String type;
    public String privacy;
    private onItemSelectedListener listener;
    private ParseGeoPoint geoPoint;
    private String placeName;
    Rak rak;
    private ParseRole currentRole;
    private Bundle bundle;
    private PlaceAutocompleteFragment autocompleteFragment;
    private CreatePostFragment createPostFragment;
    private Boolean isGroup;
    private Boolean isRak;
    private ParseQuery<Rak> query;
    private Button postButton;
    private ProgressBar pb;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private ParseFile parseFile;
    private File file;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (autocompleteFragment != null) autocompleteFragment.setText("");
        // Defines the xml file for the fragment
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_create_post, container, false);
        } catch (InflateException e) {
            /* map is already there, just return view as it is */
            Log.d("INfaltion Error", e.toString());
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        createPostFragment = this;
        //butterknife bind
        ButterKnife.bind(this, view);
        query = ParseQuery.getQuery(Rak.class);
        setUpView();
        setupAutoComplete();
    }

    @Override
    public void onResume() {
        super.onResume();

        setUpView();

    }

    //set up the create post based on the circumstance (group, rak or reflection)
    public void setUpView() {
        //default type and privacy values
        type = "Give";
        privacy = "Public";
        filesDir = getContext().getFilesDir();
        bundle = this.getArguments();
        isGroup = false;
        Boolean isReflection = false;
        isRak = false;
        if (bundle != null) {
            isGroup = bundle.getBoolean("isGroup");
            isReflection = bundle.getBoolean("isReflection");
            isRak = bundle.getBoolean("isRak");
        }
        if (isGroup) {
            currentRole = Singleton.getInstance().getRole();
            privacy = "Private";
        } else if (isRak) {
            rak = bundle.getParcelable("RAK");
            if (rak != null) {
                // et_title.setText(rak.getTitle());
                User user = (User) ParseUser.getCurrentUser();
                try {
                    et_title.setText(rak.fetchIfNeeded().getString("title"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                System.out.println(user.getRak().getTitle());
                //set the cursor position to end of input title
                int position = et_title.length();
                Editable etext = et_title.getText();
                Selection.setSelection(etext, position);
            }

        }else if (isReflection){
            et_title.setText("");
        }
    }

    /*Sets up the location autocomplete*/
    private void setupAutoComplete() {
        autocompleteFragment = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setHint("");
        (getView().findViewById(R.id.place_autocomplete_search_button)).setVisibility(View.GONE);
        if (autocompleteFragment != null) {
            autocompleteFragment.onResume();
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    LatLng latLong = place.getLatLng(); //get a lat long
                    geoPoint = new ParseGeoPoint(latLong.latitude, latLong.longitude); //use a latlong to get a parsegeopoint
                    placeName = place.getName().toString();
                    autocompleteFragment.setText("");
                }

                @Override
                public void onError(Status status) {
                    Log.i("CreatePost: ", "An error occurred: " + status);
                }

            });
        }
    }

    //launch activity to choose a photo from gallery
    @OnClick(R.id.btn_gallery)
    protected void gallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY_IMAGE);
    }

    //launch activity to take a picture for the post
    @OnClick(R.id.btn_camera)
    protected void camera() {
        if (isStoragePermissionGranted()) {
            dispatchTakePictureIntent();
        }
    }

    @OnClick(R.id.reflectionTagBtn)
    protected void showTagDialog() {
        FragmentManager fm = getFragmentManager();
        TagsDialog tagsDialog = TagsDialog.newInstance(type);
        // SETS the target fragment for use later when sending results
        tagsDialog.setTargetFragment(CreatePostFragment.this, 300);
        tagsDialog.show(fm, "fragment_tag");
    }

    @OnClick(R.id.privacyBtn)
    protected void showPrivacyDialog() {
        FragmentManager fm = getFragmentManager();
        PrivacyDialog privacyDialog = PrivacyDialog.newInstance(privacy);
        // SETS the target fragment for use later when sending results
        privacyDialog.setTargetFragment(CreatePostFragment.this, 300);
        privacyDialog.show(fm, "fragment_tag");
    }

    @Override
    public void onFinishTagDialog(String mType) {
        if (mType != null) {
            type = mType;
        }
    }

    @Override
    public void onFinishPrivacyDialog(String mPrivacy) {
        if (mPrivacy != null) privacy = mPrivacy;
    }


    //on post button clicked
    @OnClick(R.id.btn_post)
    protected void postButtonClicked() {
        final String title = et_title.getText().toString();
        final String message = et_message.getText().toString();
        final User user = (User) ParseUser.getCurrentUser();
        final String finalPrivacy = privacy;
        final String finalType = type;
        final ParseRole role = currentRole;
        postButton = (Button) getView().findViewById(R.id.btn_post);
        pb = (ProgressBar) getView().findViewById(R.id.pbLoading);
        pb.setVisibility(ProgressBar.VISIBLE);
        postButton.setVisibility(Button.INVISIBLE);
        if (file != null) {
            parseFile = new ParseFile(file);
            parseFile.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    // If successful add file to user and signUpInBackground
                    if (null == e) {
                        createPost(title, message, user, parseFile, finalPrivacy, finalType, role);
                    } else {
                        e.printStackTrace();
                    }
                    // run a background job and once complete
                    pb.setVisibility(ProgressBar.INVISIBLE);
                    postButton.setVisibility(Button.VISIBLE);
                }
            });

        } else {
            createPost(title, message, user, parseFile, finalPrivacy, finalType, role);
        }

    }


    //create post and store to parse server
    //set the title, message, user, image, privacy, give, receive, location
    private void createPost(String title, String message, ParseUser user, ParseFile parseFile, String privacy, String type, ParseRole role) {
        if (title.trim().equals("")) {
            Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (message.trim().equals("")) {
            Toast.makeText(getContext(), "A reflection is required", Toast.LENGTH_SHORT).show();
            return;
        }
        final Post newPost = new Post();
        newPost.setTitle(title);
        newPost.setMessage(message);
        newPost.setUser(user);
        if (parseFile != null) newPost.setImage(parseFile);
        newPost.setPrivacy(privacy);
        newPost.setType(type);
        if (geoPoint != null) newPost.setLocation(geoPoint);
        if (placeName != null) newPost.setPlaceName(placeName);
        if (role != null) newPost.setRole(role);
        ParseACL parseACL = new ParseACL(ParseUser.getCurrentUser());
        parseACL.setPublicReadAccess(true);

        ParseUser.getCurrentUser().setACL(parseACL);
        newPost.saveInBackground(
                new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(getActivity(), "Post Created", Toast.LENGTH_SHORT).show();
                            resetCreatePost();
                            if (isGroup) {
                                listener.toCurrentGroup(Singleton.getInstance().getRole());
                            } else {
                                listener.toFeed();
                            }
                        } else {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //clear any information from a previous post to reuse fragment
    private void resetCreatePost() {
        et_message.setText("");
        et_title.setText("");
        bundle = null;
        currentRole = null;
        file = null;
        parseFile = null;
        pictureTaken.setImageResource(android.R.color.transparent);
        isRak = false;
        pb.setVisibility(ProgressBar.INVISIBLE);
        postButton.setVisibility(Button.VISIBLE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            pictureTaken.setImageBitmap(imageBitmap);
            file = persistImage(imageBitmap, "pic1");
        } else if (requestCode == REQUEST_GALLERY_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            pictureTaken.setImageURI(imageUri);
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                file = persistImage(imageBitmap, "pic2");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("Create post", "Error");
        }
    }

    private static File persistImage(Bitmap bitmap, String name) {
        File imageFile = new File(filesDir, name + ".jpg");
        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.d("Create Post Fragment", "Error writing bitmap", e);
        }
        return imageFile;
    }


    /*Requests permission to read external storage*/
    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v("Create Post Fragment", "Permission is granted");
                return true;
            } else {
                Log.v("Create Post Fragment", "Permission is revoked");
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            Log.v("Create Post Fragment", "Permission is granted");
            return true;
        }
    }

    /*dispatches an intent to take a picture*/
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        resetCreatePost();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        resetCreatePost();
    }

    // Initializes the listener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onItemSelectedListener) {
            listener = (onItemSelectedListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement OnItemSelectedListener");
        }
    }

}

