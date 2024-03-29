package com.petabyte.plate.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.petabyte.plate.R;
import com.petabyte.plate.data.DiningMasterData;
import com.petabyte.plate.data.FoodStyle;
import com.petabyte.plate.data.UserData;
import com.petabyte.plate.ui.view.DetailTimeListBottomSheet;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {

    private Context context;
    private Intent intent;
    private String uid, diningUid;
    private DiningMasterData diningMasterData;
    private HashMap<String, UserData> userDataMap =  new HashMap<>();

    private LinearLayout dishImageList;
    private LinearLayout dishList;
    private ImageButton cancelButton;
    private TextView diningTitle;
    private TextView diningSubtitle;
    private TextView diningDate;
    private TextView diningLocation;
    private TextView diningDetailLocation;
    private ChipGroup styleChipGroup;
    private TextView diningDescription;
    private TextView diningLocationMap;
    private TextView diningDetailLocationMap;
    private ImageView chefImage;
    private TextView chefName;
    private TextView chefIntroduction;
    private TextView rating;
    private SimpleRatingBar ratingBar;
    private TextView bookmarked;
    private TextView diningPrice;
    private Button purchaseButton;
    private CheckBox bookmarkBox;

    private DatabaseReference databaseReference, ref_g, ref_h;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        context = this;

        intent = getIntent();
        diningUid = intent.getStringExtra("diningUid");

        dishImageList = (LinearLayout)findViewById(R.id.linear_layout_dishImage_DetailActivity);
        dishList = (LinearLayout)findViewById(R.id.linear_layout_dishList_DetailActivity);
        cancelButton = (ImageButton)findViewById(R.id.cancel_button_DetailActivity);
        diningTitle = (TextView)findViewById(R.id.dining_title_DetailActivity);
        diningSubtitle = (TextView)findViewById(R.id.dining_subtitle_DetailActivity);
        diningDate = (TextView)findViewById(R.id.dining_date_DetailActivity);
        diningLocation = (TextView)findViewById(R.id.dining_location_DetailActivity);
        diningDetailLocation = (TextView)findViewById(R.id.dining_detail_location_DetailActivity);
        styleChipGroup = (ChipGroup)findViewById(R.id.chip_group_DetailActivity);
        diningDescription = (TextView)findViewById(R.id.dining_description_DetailActivity);
        diningLocationMap = (TextView)findViewById(R.id.dining_location_map_DetailActivity);
        diningDetailLocationMap = (TextView)findViewById(R.id.dining_detail_location_map_DetailActivity);
        chefImage = (ImageView)findViewById(R.id.chef_image_DetailActivity);
        chefName = (TextView)findViewById(R.id.chef_name_DetailActivity);
        chefIntroduction = (TextView)findViewById(R.id.chef_introduction_DetailActivity);
        rating = (TextView)findViewById(R.id.rating_text_view_DetailActivity);
        ratingBar = (SimpleRatingBar)findViewById(R.id.rating_bar_DetailActivity);
        bookmarked = (TextView)findViewById(R.id.number_of_bookmark_DetailActivity);
        diningPrice = (TextView)findViewById(R.id.price_DetailActivity);
        purchaseButton = (Button)findViewById(R.id.purchase_button_DetailActivity);
        bookmarkBox = (CheckBox)findViewById(R.id.checkbox_DetailActivity);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        getDiningMasterData();
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_DetailActivity)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                mMap = googleMap;
                databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        LatLng location = new LatLng(diningMasterData.getCoordinate().get("latitude"), diningMasterData.getCoordinate().get("longitude"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));//move camera to location
                        if (mMap != null) {
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.title(diningMasterData.getTitle());
                            markerOptions.position(location);
                            Marker marker = googleMap.addMarker(markerOptions);
                            marker.showInfoWindow();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        setCheckBoxStatus();
        getDiningImage();
        getDiningInformation();
        getChefInformation();

        diningLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//주소 textview를 누르면 지도 어플리케이션으로 연결
                String address = diningLocation.getText().toString().replace(' ', '+');
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="+address));
                v.getContext().startActivity(intent);
            }
        });

        diningLocationMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = diningLocationMap.getText().toString().replace(' ', '+');
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="+address));
                v.getContext().startActivity(intent);
            }
        });

        cancelButton.setOnClickListener(this);
        purchaseButton.setOnClickListener(this);
        bookmarkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bookmarkBox.isChecked()) {
                    Snackbar.make(v, "찜 목록에 추가하였습니다.", 3000).show();
                    bookmarkBox.setChecked(true);
                    databaseReference = FirebaseDatabase.getInstance().getReference("Dining").child(diningUid);
                    databaseReference.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            DiningMasterData data = currentData.getValue(DiningMasterData.class);
                            if(data == null)
                                return Transaction.success(currentData);
                            data.setBookmark(data.getBookmark() + 1);
                            currentData.setValue(data);
                            return Transaction.success(currentData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

                        }
                    });
                }
                else {
                    Snackbar.make(v, "찜 목록에서 삭제하였습니다.", 3000).show();
                    bookmarkBox.setChecked(false);
                    databaseReference = FirebaseDatabase.getInstance().getReference("Dining").child(diningUid);
                    databaseReference.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            DiningMasterData data = currentData.getValue(DiningMasterData.class);
                            if(data == null)
                                return Transaction.success(currentData);
                            data.setBookmark(data.getBookmark() - 1);
                            currentData.setValue(data);
                            return Transaction.success(currentData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

                        }
                    });
                }
                reviseBookmarkStatus(diningUid, bookmarkBox.isChecked());
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v == cancelButton) {
            finish();
        }
        else if(v == purchaseButton) {
            databaseReference = FirebaseDatabase.getInstance().getReference();
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Bundle bundle = new Bundle();
                    bundle.putString("date", diningMasterData.getDate());
                    bundle.putString("title", diningTitle.getText().toString());
                    bundle.putString("diningUid", diningUid);
                    DetailTimeListBottomSheet bottomSheet = new DetailTimeListBottomSheet();
                    bottomSheet.setArguments(bundle);
                    bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void getDiningImage() {
        getDiningMasterData();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storageReference = FirebaseStorage.getInstance().getReference();
                for (int i = 0; i < diningMasterData.getImages().size() - 1; i++) {
                    String imageName = diningMasterData.getImages().get(i + 1);
                    storageReference.child("dining").child(diningUid).child(imageName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            ImageView dishImage = new ImageView(context);
                            GradientDrawable drawable = (GradientDrawable) context.getDrawable(R.drawable.image_radius);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, toDp(250));
                            layoutParams.gravity = Gravity.CENTER;
                            layoutParams.bottomMargin = toDp(15);
                            dishImage.setBackground(drawable);
                            dishImage.setClipToOutline(true);
                            dishImage.setLayoutParams(layoutParams);
                            Picasso.get().load(uri).fit().centerCrop().into(dishImage);
                            dishImageList.addView(dishImage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getDiningInformation() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Dining");
        databaseReference.orderByKey().equalTo(diningUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshots) {
                for(final DataSnapshot dataSnapshot : snapshots.getChildren()){
                    diningTitle.setText(dataSnapshot.child("title").getValue().toString());
                    diningSubtitle.setText(dataSnapshot.child("subtitle").getValue().toString());//set subtitle from database
                    diningDate.setText(dataSnapshot.child("date").getValue().toString());
                    diningLocation.setText(dataSnapshot.child("location").child("location").getValue().toString());
                    diningDetailLocation.setText(dataSnapshot.child("location").child("detail").getValue().toString());
                    diningLocationMap.setText(dataSnapshot.child("location").child("location").getValue().toString());
                    diningDetailLocationMap.setText(dataSnapshot.child("location").child("detail").getValue().toString());
                    for(int i = 0; i < dataSnapshot.child("style").getChildrenCount(); i++) {
                        FoodStyle style = FoodStyle.valueOf((dataSnapshot.child("style").child(Integer.toString(i + 1)).getValue().toString()));
                        String styleLabel = "#" + style.label;
                        Chip chip = new Chip(styleChipGroup.getContext());
                        chip.setClickable(false);
                        chip.setText(styleLabel);
                        chip.setTextColor(Color.parseColor("#FFFFFF"));
                        chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)));
                        styleChipGroup.addView(chip);
                    }
                    for(int i = 1; i <= dataSnapshot.child("dishes").getChildrenCount(); i++) {//get dishes from database
                        String dish = dataSnapshot.child("dishes").child(Integer.toString(i)).getValue().toString();
                        TextView textView = new TextView(context);
                        textView.setText("- " + dish);
                        if(i != dataSnapshot.child("dishes").getChildrenCount())
                            textView.setPadding(0,0,0, toDp(3));
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                        textView.setTextColor(getResources().getColor(R.color.textDarkPrimary));
                        dishList.addView(textView);
                    }
                    bookmarked.setText(dataSnapshot.child("bookmark").getValue() + "명이 좋아합니다.");
                    diningDescription.setText("\"" + dataSnapshot.child("description").getValue().toString() + "\"");
                    diningPrice.setText(String.format(Locale.KOREA, "%,d", dataSnapshot.child("price").getValue()) + "원");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getChefInformation() {
        storageReference = FirebaseStorage.getInstance().getReference("user").child("host");
        databaseReference = FirebaseDatabase.getInstance().getReference();
        //get Host user Information
        databaseReference.child("User").child("Host").orderByKey().equalTo(diningUid.substring(0, 28)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshots) {
                for(DataSnapshot dataSnapshot : snapshots.getChildren()) {
                    String rateString = dataSnapshot.child("Profile").child("Rating").getValue().toString();
                    String profileImageName = dataSnapshot.child("Profile").child("Image").getValue().toString();
                    storageReference.child(profileImageName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            chefImage.setBackground(new ShapeDrawable(new OvalShape()));
                            chefImage.setClipToOutline(true);
                            Picasso.get().load(uri).fit().centerCrop().into(chefImage);
                        }
                    });
                    double rate = Double.parseDouble(rateString);
                    Long ratingCount = (Long) dataSnapshot.child("Profile").child("RatingCount").getValue();
                    chefName.setText(dataSnapshot.child("Profile").child("Name").getValue().toString());
                    chefIntroduction.setText("\"" + dataSnapshot.child("Profile").child("Description").getValue().toString() + "\"");
                    rating.setText(String.format("유저 평점\n" + "%.1f" + " / 5.0 (" + ratingCount + "명 참여)", rate));
                    ratingBar.setRating(((float) rate));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void reviseBookmarkStatus (final String diningUid, final boolean isChecked) {
        getUserData();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        ref_g = FirebaseDatabase.getInstance().getReference("User").child("Guest");
        ref_h = FirebaseDatabase.getInstance().getReference("User").child("Host");
        uid = user.getUid();
        ref_g.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String userUid = dataSnapshot.getKey();
                    if(userUid.equals(uid)){
                        if(isChecked) {//if not checked before listener runs, add to bookmark
                            if(userDataMap.get(uid).getBookmark() != null) {
                                if (!(userDataMap.get(uid).getBookmark().values().contains(diningUid))) {
                                    ref_g.child(uid).child("Bookmark").push().setValue(diningUid);
                                }
                            } else
                                ref_g.child(uid).child("Bookmark").push().setValue(diningUid);
                        } else {//if checked before listener runs, remove from bookmark
                            if((userDataMap.get(uid).getBookmark().values().contains(diningUid))) {
                                userDataMap.get(uid).getBookmark().remove(diningUid);
                                ref_g.child(uid).child("Bookmark").setValue(userDataMap.get(uid).getBookmark());
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ref_h.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String userUid = dataSnapshot.getKey();
                    if(userUid.equals(uid)){
                        if(isChecked) {//if not checked before listener runs, add to bookmark
                            if(userDataMap.get(uid).getBookmark() != null) {
                                if (!(userDataMap.get(uid).getBookmark().values().contains(diningUid))) {
                                    ref_h.child(uid).child("Bookmark").push().setValue(diningUid);
                                }
                            } else
                                ref_h.child(uid).child("Bookmark").push().setValue(diningUid);
                        } else {//if checked before listener runs, remove from bookmark
                            if((userDataMap.get(uid).getBookmark().values().contains(diningUid))) {
                                userDataMap.get(uid).getBookmark().values().remove(diningUid);
                                ref_h.child(uid).child("Bookmark").setValue(userDataMap.get(uid).getBookmark());
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserData(){
        ref_g = FirebaseDatabase.getInstance().getReference("User").child("Guest");
        ref_h = FirebaseDatabase.getInstance().getReference("User").child("Host");

        ref_g.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    userDataMap.put(dataSnapshot.getKey(), dataSnapshot.getValue(UserData.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ref_h.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    userDataMap.put(dataSnapshot.getKey(), dataSnapshot.getValue(UserData.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public int toDp(int size) {
        float scale = getResources().getDisplayMetrics().density;
        return (int)(size * scale + 0.5f);
    }

    private void getDiningMasterData(){
        databaseReference = FirebaseDatabase.getInstance().getReference("Dining");
        databaseReference.orderByKey().equalTo(diningUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot datasnapshot: snapshot.getChildren()){
                    diningMasterData = datasnapshot.getValue(DiningMasterData.class);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Date time = new Date();
                    String currentDate = format.format(time);
                    if(diningMasterData.getDate().compareTo(currentDate) < 0) {
                        purchaseButton.setEnabled(false);
                        purchaseButton.setText("종료된 다이닝입니다.");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setCheckBoxStatus() {
        getUserData();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        uid = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Dining");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(userDataMap.size() != 0) {
                    if (userDataMap.get(uid).getBookmark() != null) {
                        if (userDataMap.get(uid).getBookmark().values().contains(diningUid))
                            bookmarkBox.setChecked(true);
                        else
                            bookmarkBox.setChecked(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}