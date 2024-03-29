package com.petabyte.plate.ui.view;

import android.content.Context;
import android.content.Intent;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.petabyte.plate.R;
import com.petabyte.plate.adapter.HomeHorizontalListAdapter;
import com.petabyte.plate.data.HomeCardData;
import com.petabyte.plate.ui.activity.MoreListActivity;
import com.petabyte.plate.utils.CustomAlignSnapHelper;
import com.petabyte.plate.utils.LogTags;

import java.util.ArrayList;
import java.util.List;

/**
 * Create custom horizontal recycler view
 * inside of the Nested Scroll View in Home Fragment
 */

public class HomeHorizontalList extends ConstraintLayout {

    // RecyclerView 관련 변수
    private RecyclerView recyclerView;
    private HomeHorizontalListAdapter recyclerAdapter;
    private RecyclerView.LayoutManager layoutManager;

    // View 변수
    private TextView titleView;
    private ConstraintLayout moreButton;

    // Firebase 관련 변수
    private StorageReference mStorage;

    public HomeHorizontalList(@NonNull Context context) {
        super(context);
        initViews();
    }

    public HomeHorizontalList(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public HomeHorizontalList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    public HomeHorizontalList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.view_homehorizontallist, this);

        mStorage = FirebaseStorage.getInstance("gs://plate-f5144.appspot.com/").getReference();

        recyclerView = (RecyclerView)this.findViewById(R.id.recycler_view_v_homehorizontal);
        titleView = (TextView)this.findViewById(R.id.title_tv_v_homehorizontal);
        moreButton = (ConstraintLayout) this.findViewById(R.id.title_layout_v_homehorizontal);


        // 부드러운 스크롤이 아닌 item 단위로 스크롤을 하기 위해 적용
        SnapHelper helper = new CustomAlignSnapHelper(2);
        helper.attachToRecyclerView(recyclerView);

        // 해당 RecyclerView를 터치한 뒤 위/아래로 스크롤하면 Collapsing Toolbar가 제대로 닫히지 않는 문제 해결
        recyclerView.setNestedScrollingEnabled(false);

        // RecyclerView에 LayoutManager 연결
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // RecyclerView에 Adapter연결
        recyclerAdapter = new HomeHorizontalListAdapter();
        recyclerAdapter.setReference(mStorage);
        recyclerView.setAdapter(recyclerAdapter);

        // 더보기 버튼을 누르면 리스트 뷰로 이동
        moreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MoreListActivity.class);
                intent.putParcelableArrayListExtra("data", recyclerAdapter.getItems());
                getContext().startActivity(intent);
            }
        });
    }

    public void setTitle(String title) {
        titleView.setText(title);
    }

    public void setTitle(Spanned fromHtml) {
        titleView.setText(fromHtml);
    }

    public void addData(HomeCardData data) {
        recyclerAdapter.addItem(data);
        recyclerAdapter.notifyDataSetChanged();
    }

    public void removeAllData() {
        recyclerAdapter.removeAllItem();
        recyclerAdapter.notifyDataSetChanged();
    }
}
