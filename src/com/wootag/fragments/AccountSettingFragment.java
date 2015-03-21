/*
 * Copyright (C) 2014 - present : TagFu Pte Ltd - All Rights Reserved. Unauthorized copying of this file, via any
 * medium is strictly prohibited - Proprietary and confidential
 */
package com.wTagFufragments;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import com.woTagFuhangePasswordActivity;
import com.wooTagFunstant;
import com.wootTagFuimport com.wootaTagFuter.CropOptionAdapter;
import com.wootagTagFurrorResponse;
import com.wootag.TagFuer;
import com.wootag.fTagFuk.Session;
import com.wootag.moTagFuckend;
import com.wootag.pulTagFuresh.PullToRefreshBase;
import com.wootag.pullTagFuesh.PullToRefreshBase.OnRefreshListener;
import com.wootag.pulltTagFush.PullToRefreshScrollView;
import com.wootag.ui.ImaTagFuport com.wootag.util.AlTagFuimport com.wootag.util.ConTagFumport com.wootag.util.CropTagFu;
import com.wootag.util.MainMTagFu;
import com.wootag.util.TwitteTagFu;

public class AccountSettingFragment extends FriendsListFragment {

    private static final String USER_ID = "user_id";
    private static final String DATA2 = "data";
    private static final String OK = "ok";
    private static final String PICK_A_GENDER = "Pick a Gender";
    private static final String NOT_SPECIFIED = "Not Specified";
    private static final String FEMALE = "Female";
    private static final String MALE = "Male";
    private static final String PLEASE_TRY_AGAIN = "Please try again.";
    private static final String USER = "user";
    private static final String PHOTO2 = "photo";
    private static final String BANNER2 = "banner";
    private static final String COUNTRY2 = "country";
    private static final String GENDER = "gender";
    private static final String PHONE = "phone";
    private static final String PROFESSION = "profession";
    private static final String BIO = "bio";
    private static final String COMPLETE_ACTION_USING = "Complete action using";
    private static final String TMP_AVATAR = "tmp_avatar_";
    private static final String SELECT_IMAGE = "Select Image";
    private static final String SELECT_FROM_GALLERY = "Select from gallery";
    private static final String TAKE_FROM_CAMERA = "Take from camera";
    private static final String CHOOSE_CROP_APP = "Choose Crop App";
    private static final String COM_ANDROID_CAMERA_ACTION_CROP = "com.android.camera.action.CROP";
    private static final String CAN_NOT_FIND_IMAGE_CROP_APP = "Can not find image crop app";
    private static final String RETURN_DATA = "return-data";
    private static final String SCALE = "scale";
    private static final String ASPECT_Y = "aspectY";
    private static final String ASPECT_X = "aspectX";
    private static final String OUTPUT_Y = "outputY";
    private static final String OUTPUT_X = "outputX";
    private static final String IMAGE = "image/*";
    private static final String USERID = "userid";
    private static final String EMPTY = "";
    private static final String JPG = ".jpg";
    private static final String IMG_TEMP = "IMG_TEMP";
    private static final String MOVIES = "/Movies/";
    private static final String IMG = "IMG_";
    private static final String PIC = "pic_";
    private static final String PNG = ".png";
    private static final String WOOTAG = "/Wootag";TagFupublicTagFuc AccountSettingFragment accountSettingActivity;

    private static final int CROP_FROM_CAMERA = 2;

    protected static final Logger LOG = LoggerManager.getLogger();
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_FILE = 3;

    private Object banner;
    private EditText bio;
    private ImageButton cancel;
    private String capturedImagePath;
    private LinearLayout changePassword;
    protected Context context;
    private EditText country;
    private File croppedImageFile;
    private EditText designation;
    private TextView edit;
    private Button fbButton;
    private ImageButton fbConnect;
    private TextView fbMail;
    private ToggleButton fbToggle;
    protected TextView genderText;
    private LinearLayout genderView;
    private Button gPlusButton;
    private ImageButton gPlusconnect;
    private TextView gPlusMail;
    private ToggleButton gplusToggle;
    protected LayoutInflater inflater;
    private boolean editProfile;
    private boolean profileBannerEdit;
    private TextView mail;
    protected Uri imageCaptureUri;
    private EditText mobileNumber;
    protected PullToRefreshScrollView pullToRefreshScrollView;
    private Object photo;
    private TextView profileName;
    private ImageView profilePic;
    private ImageButton save;
    private TextView setCoverBackground;
    private Button twitterButton;
    private ImageButton twitterConnect;
    private TextView twitterMail;
    private ToggleButton twitterToggle;
    private View view;
    private EditText website;

    public JSONObject getJSONRequest() throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(USER_ID, Config.getUserId());
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        request.put(USER, obj);

        return request;

    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        this.getActivity();
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case PICK_FROM_CAMERA:
            this.doCrop();

            break;

        case PICK_FROM_FILE:
            this.imageCaptureUri = data.getData();

            this.doCrop();

            break;

        case CROP_FROM_CAMERA:
            final Bundle extras = data.getExtras();

            if (extras != null) {
                final Bitmap photo = extras.getParcelable(DATA2);
                if (this.profileBannerEdit) {
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, baos); // bm is the
                    final byte[] byteArrayImage = baos.toByteArray();
                    this.banner = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
                } else if (this.editProfile) {
                    this.setImage(photo);
                }

            }

            final File file = new File(this.imageCaptureUri.getPath());

            if (file.exists()) {
                file.delete();
            }

            break;
        default:
            break;

        }
    }

    @Override
    public void onClick(final View view) {

        switch (view.getId()) {
        case R.id.changepassword:
            this.onChangePasswordClick();
            break;
        case R.id.profileImageView:
            this.onEditClick();
            break;
        case R.id.edit:
            this.onEditClick();
            break;
        case R.id.save:
            this.updateMyAccount();
            break;
        case R.id.cancel:
            BaseFragment.tabActivity.removeFromBackStack();
            break;
        case R.id.fbconnect:
        case R.id.twitterconnect:
        case R.id.gpluesconnect:
            super.onClick(view);
            break;
        case R.id.fbtoggle:
            this.onFacebookToggle();
            break;
        case R.id.twittertoggle:
            this.onTwitterToggleClick();
            break;
        case R.id.gplustoggle:
            this.onGPlusClick();
            break;
        case R.id.coverpage:
            this.onCoverPageClick();
            break;
        default:
            break;
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        this.view = inflater.inflate(R.layout.account_settings, container, false);
        this.inflater = inflater;
        this.context = this.getActivity();
        accountSettingActivity = this;
        this.loadViews();
        MainManager.getInstance().setProfileUpdateFlag(0);
        new LoadMyAccountDetails(true).execute();
        return this.view;
    }

    public void pickImage() {

        final Intent intent = new Intent();
        intent.setType(IMAGE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        this.getActivity().startActivityForResult(intent, 100);

    }

    public void showDialog() {

        final CharSequence[] items = { MALE, FEMALE, NOT_SPECIFIED };
        final AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle(PICK_A_GENDER);
        builder.setPositiveButton(OK, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int which) {

            }
        });

        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int which) {

                if (MALE.equals(items[which])) {
                    AccountSettingFragment.this.genderText.setText(MALE);
                } else if (FEMALE.equals(items[which])) {
                    AccountSettingFragment.this.genderText.setText(FEMALE);
                } else if (NOT_SPECIFIED.equals(items[which])) {
                    AccountSettingFragment.this.genderText.setText(NOT_SPECIFIED);
                }

            }
        });
        builder.show();

    }

    @Override
    public void userDetailsFinished(final User userDetails, final String socialSite) {

        super.userDetailsFinished(userDetails, socialSite);
        if (Constant.FACEBOOK.equalsIgnoreCase(socialSite)) {
            if (userDetails.getEmailId() != null) {
                MainManager.getInstance().setFacebookEmail(userDetails.getEmailId());
                this.fbMail.setText(Config.getFacebookLoggedUserId());
                this.fbToggle.setBackgroundResource(R.drawable.on);
                this.fbToggle.setVisibility(View.VISIBLE);
                this.fbConnect.setVisibility(View.GONE);
            }
        } else if (Constant.TWITTER.equalsIgnoreCase(socialSite)) {
            MainManager.getInstance().setTwitterEmail(userDetails.getEmailId());
            this.twitterToggle.setBackgroundResource(R.drawable.on);
            this.twitterToggle.setVisibility(View.VISIBLE);
            this.twitterConnect.setVisibility(View.GONE);
            this.twitterMail.setText(userDetails.getEmailId());
        } else if (Constant.GOOGLE_PLUS.equalsIgnoreCase(socialSite)) {
            MainManager.getInstance().setGPlusEmail(userDetails.getEmailId());
            this.gPlusMail.setText(userDetails.getEmailId());
            this.gplusToggle.setBackgroundResource(R.drawable.on);
            this.gplusToggle.setVisibility(View.VISIBLE);
            this.gPlusconnect.setVisibility(View.GONE);
        }
    }

    private void createImageFile() {

        final File effortFolder = new File(Environment.getExternalStorageDirectory() + WOOTAG);
        if (!effortFolder.exists()) {
            effortFolder.mkdirs();
        }
        final String imageFileName = PIC + System.currentTimeMillis() + PNG;
        final File file = new File(effortFolder.getAbsolutePath(), imageFileName);
        this.capturedImagePath = file.getAbsolutePath();
        LOG.i("path  is " + file.getAbsolutePath());
        if (this.capturedImagePath != null) {
            final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // currentTime=System.currentTimeMillis();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            this.startActivityForResult(takePictureIntent, 200);
        }
    }

    private File createNewFile(String prefix) {

        if (Strings.isNullOrEmpty(prefix)) {
            prefix = IMG;
        }
        final File newDirectory = new File(Environment.getExternalStorageDirectory() + MOVIES);
        if (!newDirectory.exists() && newDirectory.mkdir()) {
            LOG.d(this.context.getClass().getName(), newDirectory.getAbsolutePath() + " directory created");
        }
        this.croppedImageFile = new File(newDirectory, (IMG_TEMP + JPG));// prefix+System.currentTimeMillis()
        if (this.croppedImageFile.exists()) {
            // this wont be executed
            this.croppedImageFile.delete();
            try {
                this.croppedImageFile.createNewFile();
            } catch (final IOException e) {
                LOG.e(e);
            }
        }

        return this.croppedImageFile;
    }

    private void doCrop() {

        final List<CropOption> cropOptions = new ArrayList<CropOption>();

        final Intent intent = new Intent(COM_ANDROID_CAMERA_ACTION_CROP);
        intent.setType(IMAGE);

        final List<ResolveInfo> list = this.getActivity().getPackageManager().queryIntentActivities(intent, 0);

        final int size = list.size();

        if (size == 0) {
            Toast.makeText(this.getActivity(), CAN_NOT_FIND_IMAGE_CROP_APP, Toast.LENGTH_SHORT).show();

            return;
        }

        intent.setData(this.imageCaptureUri);
        if (this.profileBannerEdit) {
            intent.putExtra(OUTPUT_X, 1000);
            intent.putExtra(OUTPUT_Y, 600);
            intent.putExtra(ASPECT_X, 0);
            intent.putExtra(ASPECT_Y, 1);
            intent.putExtra(SCALE, true);
            intent.putExtra(RETURN_DATA, true);
        } else {
            intent.putExtra(OUTPUT_X, 200);
            intent.putExtra(OUTPUT_Y, 200);
            intent.putExtra(ASPECT_X, 0);
            intent.putExtra(ASPECT_Y, 1);
            intent.putExtra(SCALE, true);
            intent.putExtra(RETURN_DATA, true);
        }

        if (size == 1) {
            final Intent i = new Intent(intent);
            final ResolveInfo res = list.get(0);

            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            this.getActivity().startActivityForResult(i, CROP_FROM_CAMERA);
        } else {
            for (final ResolveInfo res : list) {
                final CropOption cropOption = new CropOption();

                cropOption.title = this.getActivity().getPackageManager()
                        .getApplicationLabel(res.activityInfo.applicationInfo);
                cropOption.icon = this.getActivity().getPackageManager()
                        .getApplicationIcon(res.activityInfo.applicationInfo);
                cropOption.appIntent = new Intent(intent);

                cropOption.appIntent
                        .setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

                cropOptions.add(cropOption);
            }

            final CropOptionAdapter adapter = new CropOptionAdapter(this.getActivity(), cropOptions);

            final AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
            builder.setTitle(CHOOSE_CROP_APP);
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(final DialogInterface dialog, final int item) {

                    AccountSettingFragment.this.getActivity().startActivityForResult(cropOptions.get(item).appIntent,
                            CROP_FROM_CAMERA);
                }
            });

            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(final DialogInterface dialog) {

                    if (AccountSettingFragment.this.imageCaptureUri != null) {
                        AccountSettingFragment.this.getActivity().getContentResolver()
                                .delete(AccountSettingFragment.this.imageCaptureUri, null, null);
                        AccountSettingFragment.this.imageCaptureUri = null;
                    }
                }
            });

            final AlertDialog alert = builder.create();

            alert.show();
        }
    }

    private void editProfilePicture() {

        final String[] items = new String[] { TAKE_FROM_CAMERA, SELECT_FROM_GALLERY };
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.select_dialog_item, items);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());

        builder.setTitle(SELECT_IMAGE);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int item) { // pick from camera

                if (item == 0) {
                    final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    AccountSettingFragment.this.imageCaptureUri = Uri.fromFile(new File(Environment
                            .getExternalStorageDirectory(), TMP_AVATAR + System.currentTimeMillis() + JPG));

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, AccountSettingFragment.this.imageCaptureUri);

                    try {
                        intent.putExtra(RETURN_DATA, true);
                        AccountSettingFragment.this.getActivity().startActivityForResult(intent, PICK_FROM_CAMERA);
                    } catch (final ActivityNotFoundException e) {
                        LOG.e(e);
                    }
                } else { // pick from file
                    final Intent intent = new Intent();
                    intent.setType(IMAGE);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    AccountSettingFragment.this.getActivity().startActivityForResult(
                            Intent.createChooser(intent, COMPLETE_ACTION_USING), PICK_FROM_FILE);
                }
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void loadViews() {

        this.profilePic = (ImageView) this.view.findViewById(R.id.profileImageView);
        this.profileName = (TextView) this.view.findViewById(R.id.profileNameTextView);
        this.designation = (EditText) this.view.findViewById(R.id.designation);
        this.website = (EditText) this.view.findViewById(R.id.website);
        this.country = (EditText) this.view.findViewById(R.id.country);
        this.mail = (TextView) this.view.findViewById(R.id.mailTextView);
        this.mobileNumber = (EditText) this.view.findViewById(R.id.phnumber);
        this.bio = (EditText) this.view.findViewById(R.id.bio);
        this.genderText = (TextView) this.view.findViewById(R.id.gender);
        this.genderView = (LinearLayout) this.view.findViewById(R.id.genderView);
        this.changePassword = (LinearLayout) this.view.findViewById(R.id.changepassword);
        this.edit = (TextView) this.view.findViewById(R.id.edit);
        this.setCoverBackground = (TextView) this.view.findViewById(R.id.coverpage);
        this.fbButton = (Button) this.view.findViewById(R.id.fb);
        this.twitterButton = (Button) this.view.findViewById(R.id.twitter);
        this.gPlusButton = (Button) this.view.findViewById(R.id.gplusshare);
        this.fbMail = (TextView) this.view.findViewById(R.id.fbmail);
        this.twitterMail = (TextView) this.view.findViewById(R.id.twittermail);
        this.gPlusMail = (TextView) this.view.findViewById(R.id.gplusmail);
        this.gPlusconnect = (ImageButton) this.view.findViewById(R.id.gpluesconnect);
        this.twitterToggle = (ToggleButton) this.view.findViewById(R.id.twittertoggle);
        this.fbToggle = (ToggleButton) this.view.findViewById(R.id.fbtoggle);
        this.fbConnect = (ImageButton) this.view.findViewById(R.id.fbconnect);
        this.twitterConnect = (ImageButton) this.view.findViewById(R.id.twitterconnect);
        this.gplusToggle = (ToggleButton) this.view.findViewById(R.id.gplustoggle);
        this.save = (ImageButton) this.view.findViewById(R.id.save);
        this.cancel = (ImageButton) this.view.findViewById(R.id.cancel);
        this.changePassword.setOnClickListener(this);
        this.gPlusconnect.setOnClickListener(this);
        this.setCoverBackground.setOnClickListener(this);
        this.twitterToggle.setOnClickListener(this);
        this.fbToggle.setOnClickListener(this);
        this.edit.setOnClickListener(this);
        this.profilePic.setOnClickListener(this);
        this.save.setOnClickListener(this);
        this.cancel.setOnClickListener(this);
        this.fbConnect.setOnClickListener(this);
        this.twitterConnect.setOnClickListener(this);
        this.gplusToggle.setOnClickListener(this);

        if (MainManager.getInstance().getFbEmail() != null) {
            this.fbMail.setText(MainManager.getInstance().getFbEmail());
            this.fbToggle.setBackgroundResource(R.drawable.on);
            this.fbToggle.setVisibility(View.VISIBLE);
            this.fbConnect.setVisibility(View.GONE);
        }

        if (MainManager.getInstance().getGplusEmail() != null) {
            this.gPlusMail.setText(MainManager.getInstance().getGplusEmail());
            this.gplusToggle.setBackgroundResource(R.drawable.on);
            this.gplusToggle.setVisibility(View.VISIBLE);
            this.gPlusconnect.setVisibility(View.GONE);
        }

        if (TwitterUtils.isAuthenticated(this.context)) {
            this.twitterToggle.setBackgroundResource(R.drawable.on);
            this.twitterToggle.setVisibility(View.VISIBLE);
            this.twitterConnect.setVisibility(View.GONE);
            this.twitterMail.setText(MainManager.getInstance().getTwitterEmail());
        }
        final float scale = this.getResources().getDisplayMetrics().density;
        this.pullToRefreshScrollView = (PullToRefreshScrollView) this.view.findViewById(R.id.accountScrollview);
        this.pullToRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

            @Override
            public void onRefresh(final PullToRefreshBase<ScrollView> refreshView) {

                new LoadMyAccountDetails(false).execute();
            }
        });
        this.genderView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View view) {

                AccountSettingFragment.this.showDialog();

            }
        });

    }

    private void logoutFB() {

        Session session = null;
        session = Session.getActiveSession();
        if ((session != null) && !session.isClosed()) {
            session.closeAndClearTokenInformation();
            session.close();
            Session.setActiveSession(null);
        }
    }

    private void logoutGplus() {

        this.gplusSignout();

    }

    private void logoutTwitter() {

        MainManager.getInstance().setTwitterOAuthtoken(null);
        MainManager.getInstance().setTwitterSecretKey(null);
    }

    /**
     *
     */
    private void onChangePasswordClick() {

        if (MainManager.getInstance().getLoginType() == 0) {
            final Intent intent = new Intent(this.context, ChangePasswordActivity.class);
            this.startActivity(intent);
        }
    }

    /**
     *
     */
    private void onCoverPageClick() {

        this.profileBannerEdit = true;
        this.editProfile = false;
        this.editProfilePicture();
    }

    /**
     *
     */
    private void onEditClick() {

        this.editProfile = true;
        this.profileBannerEdit = false;
        this.editProfilePicture();
    }

    /**
     *
     */
    private void onFacebookToggle() {

        this.fbToggle.setVisibility(View.GONE);
        this.fbConnect.setVisibility(View.VISIBLE);
        MainManager.getInstance().setFacebookEmail(null);
        this.fbMail.setText(EMPTY);
        this.logoutFB();
    }

    /**
     *
     */
    private void onGPlusClick() {

        MainManager.getInstance().setGPlusEmail(null);
        this.gPlusMail.setText(EMPTY);
        this.gplusToggle.setVisibility(View.GONE);
        this.gPlusconnect.setVisibility(View.VISIBLE);
        this.logoutGplus();
    }

    /**
     *
     */
    private void onTwitterToggleClick() {

        MainManager.getInstance().setTwitterEmail(null);
        this.twitterToggle.setVisibility(View.GONE);
        this.twitterConnect.setVisibility(View.VISIBLE);
        this.logoutTwitter();
    }

    private void updateMyAccount() {

        try {
            final String updteMyAccountReq = this.getUpdateAccountRequest().toString();
        } catch (final JSONException exception) {
            LOG.e(exception);
        }
        new updateAccountAsyne(true).execute();
    }

    protected JSONObject getUpdateAccountRequest() throws JSONException {

        final JSONObject request = new JSONObject();
        final JSONObject obj = new JSONObject();
        obj.put(USERID, Config.getUserId());
        if (this.website.getText() != null) {
            obj.put(this.getActivity().getString(R.string.website), this.website.getText().toString());
        }
        if (this.profileName.getText() != null) {
            obj.put(NAME, this.profileName.getText().toString());
        }
        if (this.bio.getText() != null) {
            obj.put(BIO, this.bio.getText().toString());
        }
        if (this.designation.getText() != null) {
            obj.put(PROFESSION, this.designation.getText().toString());
        }
        if (this.mobileNumber.getText() != null) {
            obj.put(PHONE, this.mobileNumber.getText().toString());
        }
        if (this.genderText.getText() != null) {
            obj.put(GENDER, this.genderText.getText());
        }

        if (this.country.getText() != null) {
            obj.put(COUNTRY2, this.country.getText().toString());
        }
        if (this.banner != null) {
            obj.put(BANNER2, this.banner);
        }
        if (this.photo != null) {
            obj.put(PHOTO2, this.photo);
        }
        obj.put(Constant.DEVICE_TYPE, Constant.DEVICE_MODEL);
        obj.put(Constant.RESOLUTION, Config.getDeviceResolutionValue());
        request.put(USER, obj);
        return request;
    }

    void loadAccountDetails(final User profileData) {

        if (profileData.getPhotoPath() != null) {
            Image.displayImage(profileData.getPhotoPath(), this.getActivity(), this.profilePic, 0);

        } else {
            this.profilePic.setImageResource(R.drawable.member);
        }

        if (profileData.getUserName() != null) {
            this.profileName.setText(profileData.getUserName());

        } else {
            this.profileName.setText(EMPTY);
        }

        if (profileData.getWebsite() != null) {
            this.website.setText(profileData.getWebsite());

        } else {
            this.website.setText(EMPTY);
        }

        if (profileData.getCountry() != null) {
            this.country.setText(profileData.getCountry());

        } else {
            this.country.setText(EMPTY);
        }

        if (profileData.getProfession() != null) {
            this.designation.setText(profileData.getProfession());

        } else {
            this.designation.setText(EMPTY);
        }

        if (profileData.getEmailId() != null) {
            this.mail.setText(profileData.getEmailId());

        } else {
            this.mail.setText(EMPTY);
        }

        if (profileData.getBio() != null) {
            this.bio.setText(profileData.getBio());

        } else {
            this.bio.setText(EMPTY);
        }

        if (profileData.getPhone() != null) {
            this.mobileNumber.setText(profileData.getPhone());

        } else {
            this.mobileNumber.setText(EMPTY);
        }

        this.profileName.requestFocus();

        if (profileData.getGender() != null) {
            final String genderString = profileData.getGender();
            this.genderText.setText(genderString);
        }
        this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    void setImage(final Bitmap bitmap) {

        if (bitmap != null) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // bm is the
            final byte[] byteArrayImage = baos.toByteArray();
            this.photo = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
            if (this.editProfile) {
                this.profilePic.setImageBitmap(bitmap);
            }
        } else {
            Alerts.showInfoOnly(PLEASE_TRY_AGAIN, this.getActivity());
        }
    }

    public class LoadMyAccountDetails extends AsyncTask<Void, Void, Void> {

        private static final String NO_RESPONSE_AVAILABLE = "No Response available";
        private Object accountDetails;
        private final boolean progressVisible;
        private ProgressDialog progressDialog;

        public LoadMyAccountDetails(final boolean progressVisible) {

            this.progressVisible = progressVisible;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                this.accountDetails = Backend.accountDetails(AccountSettingFragment.this.context,
                        AccountSettingFragment.this.getJSONRequest().toString());
            } catch (final JSONException exception) {
                LOG.e(exception);
            }

            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            if (this.progressVisible) {
                this.progressDialog.dismiss();
            }

            AccountSettingFragment.this.pullToRefreshScrollView.onRefreshComplete();
            if (this.accountDetails != null) {
                if (this.accountDetails instanceof User) {
                    final User myAccountInfo = (User) this.accountDetails;
                    AccountSettingFragment.this.loadAccountDetails(myAccountInfo);
                } else if (this.accountDetails instanceof ErrorResponse) {
                    final ErrorResponse error = (ErrorResponse) this.accountDetails;
                    Alerts.showInfoOnly(error.getMessage(), AccountSettingFragment.this.getActivity());
                }
            } else {
                Alerts.showInfoOnly(NO_RESPONSE_AVAILABLE, AccountSettingFragment.this.getActivity());
            }
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if (this.progressVisible) {
                this.progressDialog = ProgressDialog.show(AccountSettingFragment.this.context, EMPTY, EMPTY, true);
                final View view = AccountSettingFragment.this.inflater.inflate(R.layout.progress_bar, null, false);
                this.progressDialog.setContentView(view);
                this.progressDialog.setCancelable(false);
                this.progressDialog.setCanceledOnTouchOutside(false);
                this.progressDialog.show();
            }
        }

    }

    public class updateAccountAsyne extends AsyncTask<Void, Void, Void> {

        private static final String NO_RESPONSE_AVAILABLE = "No Response available";
        private final boolean progressVisible;
        private ProgressDialog progressDialog;
        private Object returnObj;

        public updateAccountAsyne(final boolean progressVisible) {

            this.progressVisible = progressVisible;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            try {
                this.returnObj = Backend.updateAccount(AccountSettingFragment.this.context, AccountSettingFragment.this
                        .getUpdateAccountRequest().toString());
            } catch (final JSONException exception) {
                LOG.e(exception);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {

            super.onPostExecute(result);
            if (this.progressVisible) {
                this.progressDialog.dismiss();
            }

            if (this.returnObj != null) {
                if (this.returnObj instanceof Boolean) {
                    MainManager.getInstance().setProfileUpdateFlag(1);
                    AccountSettingFragment.this.getActivity().getFragmentManager().popBackStackImmediate();
                    // finish();
                } else if (this.returnObj instanceof ErrorResponse) {
                    final ErrorResponse error = (ErrorResponse) this.returnObj;
                    Alerts.showInfoOnly(error.getMessage(), AccountSettingFragment.this.context);
                }
            } else {
                Alerts.showInfoOnly(NO_RESPONSE_AVAILABLE, AccountSettingFragment.this.context);
            }
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            if (this.progressVisible) {
                this.progressDialog = ProgressDialog.show(AccountSettingFragment.this.context, EMPTY, EMPTY, true);
                this.progressDialog
                        .setContentView(((LayoutInflater) AccountSettingFragment.this.context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.progress_bar,
                                null, false));
                this.progressDialog.setCancelable(false);
                this.progressDialog.setCanceledOnTouchOutside(false);
                this.progressDialog.show();
            }
        }
    }
}
