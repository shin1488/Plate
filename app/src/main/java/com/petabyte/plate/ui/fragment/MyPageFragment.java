package com.petabyte.plate.ui.fragment;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.petabyte.plate.R;
import com.petabyte.plate.ui.activity.AddDiningPlanActivity;
import com.petabyte.plate.ui.activity.DiningManagementActivity;
import com.petabyte.plate.ui.activity.LoginActivity;
import com.petabyte.plate.ui.activity.ReservationCheckActivity;
import com.petabyte.plate.utils.GlideApp;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.util.List;

public class MyPageFragment extends Fragment {

    private TextView text_username;
    private TextView text_usertype;
    private TextView text_usermail;
    private ImageView image_userpics;

    private TextView btn_check_reserve;
    private TextView btn_manage_dining;
    private TextView btn_add_dining;
    private TextView btn_edit_desc;
    private TextView btn_logout;
    private TextView btn_edit_pw;
    private TextView btn_delete_acc;
    private TextView btn_set_noti;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private StorageReference storage;
    private DatabaseReference ref_g, ref_h;
    private String MEMBER_TYPE = "";
    private String UID = "";
    private Uri imageUri;
    private ProgressDialog progressDialog;
    private RequestManager mGlideRequestManager;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_mypage, container, false);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)v.findViewById(R.id.collapsing_toolbar_mypage);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        mGlideRequestManager = GlideApp.with(this);

        text_username = (TextView)v.findViewById(R.id.text_v_mypage_username);
        text_usermail = (TextView)v.findViewById(R.id.text_v_mypage_usermail);
        text_usertype = (TextView)v.findViewById(R.id.text_v_mypage_usertype);
        image_userpics = (ImageView)v.findViewById(R.id.image_v_mypage_userimg);
        btn_check_reserve = (TextView)v.findViewById(R.id.text_v_mypage_check_reservation);
        btn_manage_dining = (TextView)v.findViewById(R.id.text_v_mypage_manage_dining);
        btn_add_dining = (TextView)v.findViewById(R.id.text_v_mypage_add_dining);
        btn_edit_desc = (TextView)v.findViewById(R.id.text_v_mypage_edit_description);
        btn_edit_pw = (TextView)v.findViewById(R.id.text_v_mypage_edit_pw);
        btn_delete_acc = (TextView)v.findViewById(R.id.text_v_mypage_delete_account);
        btn_logout = (TextView)v.findViewById(R.id.text_v_mypage_logout);
        btn_set_noti = (TextView)v.findViewById(R.id.text_v_mypage_set_noti);

        setUserinfoCard();

        // set user profile image onclick method
        image_userpics.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                makeDialog("SELECT", "EDIT_PICS",
                        "프로필 사진 선택", "프로필 사진을 변경합니다", "");
            }
        });

        // set using info card
        btn_check_reserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        getActivity(), ReservationCheckActivity.class)
                        .putExtra("UID", UID)
                        .putExtra("MEMBER_TYPE", MEMBER_TYPE));
            }
        });

        btn_manage_dining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), DiningManagementActivity.class));
            }
        });

        btn_add_dining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddDiningPlanActivity.class));
            }
        });

        // set account card
        btn_edit_desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeDialog("PROMPT", "EDIT_DESC", "소개문구 변경",
                        "변경할 소개 문구를 입력해주세요.\n소개 문구는 즉시 반영돼요!", "간단한 문구 입력");
            }
        });

        btn_edit_pw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeDialog("PROMPT", "EDIT_PW", "비밀번호 변경",
                        "변경할 비밀번호를 입력해주세요.\n비밀번호를 바꾼 뒤에는 다시 로그인 해야돼요.", "여섯자리 이상 입력");
            }
        });

        btn_delete_acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeDialog("CONFIRM", "DEL_ACC", "회원 탈퇴",
                        "정말 PLATE를 떠나시겠어요?", "");
            }
        });

        btn_logout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        // set notification card
        btn_set_noti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName());
                startActivity(intent);
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == Crop.REQUEST_PICK && resultCode == Activity.RESULT_OK){
            beginCrop(data.getData());
        }else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
    }

    // get Storage reference /user/MEMBER_TYPE
    public StorageReference getStorageRef(){
        storage = FirebaseStorage.getInstance("gs://plate-f5144.appspot.com/")
                .getReference("user").child(MEMBER_TYPE.toLowerCase());
        //Log.d("result", "getStorageRef >> uri = "+storage);
        return storage;
    }

    // set user info card
    public void setUserinfoCard(){
        // set user info card
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        ref_g = FirebaseDatabase.getInstance().getReference("User").child("Guest");
        ref_h = FirebaseDatabase.getInstance().getReference("User").child("Host");
        UID = user.getUid();

        ref_g.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Object g_uid = dataSnapshot.getKey();
                    if(g_uid.equals(UID)){
                        text_username.setText(dataSnapshot.child("Profile/Name").getValue().toString());
                        text_usertype.setText("Guest 회원");
                        text_usermail.setText(user.getEmail());
                        MEMBER_TYPE = "Guest";
                        setProfileImage();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        ref_h.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Object h_uid = dataSnapshot.getKey();
                    if(h_uid.equals(UID)){
                        text_username.setText(dataSnapshot.child("Profile/Name").getValue().toString());
                        text_usertype.setText("Host 회원");
                        text_usermail.setText(user.getEmail());
                        MEMBER_TYPE = "Host";
                        setProfileImage();
                        btn_manage_dining.setVisibility(View.VISIBLE);
                        btn_add_dining.setVisibility(View.VISIBLE);
                        btn_edit_desc.setVisibility(View.VISIBLE);

                        if(dataSnapshot.child("Status").getValue().toString().equals("WAITING")){
                            btn_add_dining.setTextColor(Color.GRAY);
                            btn_add_dining.setText("다이닝 일정 추가 [심사 진행중]");
                            btn_add_dining.setEnabled(false);
                            btn_manage_dining.setTextColor(Color.GRAY);
                            btn_manage_dining.setText("다이닝 일정 관리 [심사 진행중]");
                            btn_manage_dining.setEnabled(false);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // set profile image using GlideApp
    public void setProfileImage(){
        if(MEMBER_TYPE.equals("Host")){
            ref_h.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String imageName = snapshot.child(UID).child("Profile/Image").getValue().toString();
                    if(!imageName.equals("DEFAULT")){
                        //Log.d("result", "setProfileImage >> host : "+imageName);
                        // set image with GlideApp
                        mGlideRequestManager
                                .load(getStorageRef().child(imageName))
                                .override(200)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .circleCrop()
                                .into(image_userpics);
                    }else{
                        //Log.d("result", "setProfileImage >> host, NO CUSTOM IMAGE UPLOADED!");
                        mGlideRequestManager
                                .load(getResources().getDrawable(R.drawable.ic_character))
                                .circleCrop()
                                .into(image_userpics);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }else if(MEMBER_TYPE.equals("Guest")){
            ref_g.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String imageName = snapshot.child(UID).child("Profile/Image").getValue().toString();
                    if(!imageName.equals("DEFAULT")){
                        // set image with GlideApp
                        GlideApp.with(getContext())
                                 .load(getStorageRef().child(imageName))
                                .override(200)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .circleCrop()
                                .into(image_userpics);
                    }else{
                        GlideApp.with(getContext())
                                .load(getResources().getDrawable(R.drawable.ic_character))
                                .circleCrop()
                                .into(image_userpics);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    // function for make dialog with TYPE and other PARAMS
    // TYPE : PROMPT, CONFIRM, SELECT
    public void makeDialog(String type, final String action, String title, String message, String extra){
        final AlertDialog.Builder d = new AlertDialog.Builder(getContext());

        // item selection type dialog
        if(type.equals("SELECT")){
            final CharSequence[] items = {"앨범에서 사진 선택", "기본 사진으로 변경"};
            d.setTitle(Html.fromHtml("<b>"+title+"</b>"));
            d.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which){
                        case 0:
                            PermissionListener permissionlistener = new PermissionListener(){
                                @Override
                                public void onPermissionGranted() {
                                    openGallery();
                                }
                                @Override
                                public void onPermissionDenied(List<String> deniedPermissions) {
                                    Toast.makeText(getActivity(), "권한을 거부하였습니다.\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                                }
                            };
                            TedPermission.with(getContext())
                                    .setPermissionListener(permissionlistener)
                                    .setRationaleMessage("프로필 사진을 바꾸기 위해 권한이 필요해요!")
                                    .setDeniedMessage("프로필 사진을 바꾸기 위해 이 권한이 필요합니다.\n[설정] > [권한] 에서 권한을 허용할 수 있어요.")
                                    .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    .check();
                            break;
                        case 1:
                            if(MEMBER_TYPE.equals("Host")) ref_h.child(UID).child("Profile/Image").setValue("DEFAULT");
                            else ref_g.child(UID).child("Profile/Image").setValue("DEFAULT");
                            //Log.d("result", "database updated");
                            setProfileImage();
                            break;
                    }
                }
            });
        }else{
            d.setMessage(message);
            // make layout for set view's margin
            LinearLayout container = new LinearLayout(getContext());
            container.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            int left_margin = this.dpToPx(20, getActivity().getResources());
            int top_margin = this.dpToPx(0, getActivity().getResources());
            int right_margin = this.dpToPx(20, getActivity().getResources());
            int bottom_margin = this.dpToPx(0, getActivity().getResources());
            lp.setMargins(left_margin, top_margin, right_margin, bottom_margin);

            // PROMPT BOX TYPE
            if(type.equals("PROMPT")){
                final EditText input = new EditText(getActivity());
                input.setLayoutParams(lp);
                input.setGravity(android.view.Gravity.TOP | android.view.Gravity.LEFT);
                input.setHint(extra);
                if(MEMBER_TYPE.equals("Host") && action.equals("EDIT_DESC")){
                    ref_h.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                                Object h_uid = dataSnapshot.getKey();
                                if(h_uid.equals(user.getUid())){
                                    input.setText(dataSnapshot.child("Profile/Description").getValue().toString());
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
                container.addView(input, lp);
                d.setView(container);
                d.setPositiveButton("바꾸기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // force hide keyboard on fragment
                        InputMethodManager mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        mInputMethodManager.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        String new_input = input.getText().toString().trim();
                        switch(action){
                            case "EDIT_PW":
                                if(new_input.length() < 6) return;
                                makeProgressDialog();
                                user.updatePassword(new_input).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            progressDialog.dismiss();
                                            logout();
                                        }
                                    }
                                });
                                break;
                            case "EDIT_DESC":
                                ref_h.child(UID).child("Profile/Description").setValue(new_input);
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                d.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {dialog.dismiss();}
                });
                // CONFIRM BOX TYPE
            }else if(type.equals("CONFIRM")){
                d.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        makeProgressDialog();
                        // remove user info from Authentication
                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    // remove user info from Realtime DB
                                    switch(MEMBER_TYPE){
                                        case "Host":
                                            ref_h.child(user.getUid()).removeValue();
                                        case "Guest":
                                            ref_g.child(user.getUid()).removeValue();
                                    }
                                    progressDialog.dismiss();
                                    logout();
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                });
                d.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {dialog.dismiss();}
                });
            }
        }

        // set btn color
        AlertDialog tmp = d.create();
        tmp.show();
        tmp.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
        tmp.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    // get image from album(gallery app)
    private void openGallery() {
        // crop image
        Crop.pickImage(getContext(),this);
    }

    // android-crop lib feature
    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(getContext(),this);
    }

    // android-crop lib feature
    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == Activity.RESULT_OK) {
            imageUri = Crop.getOutput(result);
            uploadImage();
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(getActivity(), Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // get file's extension
    private String getExtension(String fileName){
        String fileEx = "";
        if (fileName.contains("."))
            fileEx = fileName.substring(fileName.lastIndexOf(".")+1);
        return fileEx;
    }

    // upload image to Storage
    private void uploadImage(){
        final Uri file = Uri.fromFile(new File(imageUri.getPath()));
        //final String extension = getExtension(file.getLastPathSegment());
        final String finalFilename = UID;
        if(storage == null) getStorageRef();

        storage = storage.child(finalFilename);
        //Log.d("result", "final storage path >> "+storage);
        makeProgressDialog();
        storage.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                final Task<Uri> imageUrl = task.getResult().getStorage().getDownloadUrl();
                while (!imageUrl.isComplete());
                //Log.d("result", "upload completed");

                // set image path on database
                if(MEMBER_TYPE.equals("Host")) ref_h.child(UID).child("Profile/Image").setValue(finalFilename);
                else ref_g.child(UID).child("Profile/Image").setValue(finalFilename);
                //Log.d("result", "database updated");
                setProfileImage();
                progressDialog.dismiss();
            }
        });
    }

    // logout function
    public void logout(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    // calculate dimension
    public static int dpToPx(float dp, Resources resources) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) px;
    }

    // make progress dialog
    public void makeProgressDialog(){
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("바뀐 내용을 반영하고있어요!");
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);
        progressDialog.show();
    }
}
