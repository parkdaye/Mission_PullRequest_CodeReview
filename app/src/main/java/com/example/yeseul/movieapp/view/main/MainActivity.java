package com.example.yeseul.movieapp.view.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;

import com.example.yeseul.movieapp.R;
import com.example.yeseul.movieapp.data.source.movie.MovieRepository;
import com.example.yeseul.movieapp.databinding.ActivityMovieBinding;
import com.example.yeseul.movieapp.pojo.Movie;
import com.example.yeseul.movieapp.utils.KeyboardUtil;
import com.example.yeseul.movieapp.view.BaseActivity;
import com.example.yeseul.movieapp.view.custom.ReviewDialog;
import com.example.yeseul.movieapp.view.review.ReviewActivity;

public class MainActivity extends BaseActivity<ActivityMovieBinding, MainPresenter> implements MainContract.View {

    private MovieListAdapter adapter;
    private static Context context;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_movie;
    }

    @Override
    protected MainPresenter getPresenter() {
        return new MainPresenter(this, MovieRepository.getInstance());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        adapter = new MovieListAdapter(this);
        presenter.setAdapterView(adapter);
        presenter.setAdapterModel(adapter);

        binding.setPresenter(presenter);

        initView();
        presenter.onViewCreated();
    }

    private void initView() {

        // recyclerView 생성
        binding.recyclerMovie.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.recyclerMovie.setAdapter(adapter);
        binding.recyclerMovie.setEmptyView(binding.emptyView);
        binding.recyclerMovie.setNestedScrollingEnabled(false);
        binding.recyclerMovie.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // 최하단 스크롤 감지
        binding.recyclerMovie.setOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!binding.recyclerMovie.canScrollVertically(1)){
                    presenter.loadItems(false);
                }
            }
        });

        // 키보드 검색 버튼 리스너 등록
        binding.searchBox.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_SEARCH){
                onSearchButtonClicked();
                return true;
            }
            return false;
        });


        // 검색 버튼 리스너 등록
        binding.searchBox.btnSubmit.setOnClickListener(v -> onSearchButtonClicked());

        //플로팅 리뷰버튼 리스너
        binding.buttonReview.setOnClickListener(v -> onReviewButtonClicked());
    }

    /**
     * 키보드 검색 혹은 검색 버튼 눌렀을 때 호출 */
    private void onSearchButtonClicked(){

        // 입력값이 존재 하는지 체크
        String searchKey = binding.searchBox.etSearch.getText().toString();

        if(!TextUtils.isEmpty(searchKey)) {
            presenter.onSearchButtonClicked(searchKey);
            binding.recyclerMovie.scrollToPosition(0);
            binding.emptyView.setText("");
            KeyboardUtil.closeKeyboard(this, binding.searchBox.etSearch);

        } else {
            makeToast(getString(R.string.search_hint));
        }
    }

    /**
     *  리뷰 목록 버튼을 눌렀을 때
     */
    public void onReviewButtonClicked() {
        //리뷰리스트 화면으로 이동
        Intent intent = new Intent(MainActivity.this, ReviewActivity.class);
        startActivity(intent);
    }

    /**
     * 검색 결과가 없는 경우 presenter 에 의해 호출됨 */
    @Override
    public void onSearchResultEmpty(String searchKey) {

        String displayString = "<b>'" + searchKey + "'</b>" + getString(R.string.search_result_empty);

        binding.emptyView.setText(Html.fromHtml(displayString));
    }

    /**
     * 영화 상세 정보 URL 로 연결 */
    @Override
    public void startMovieDetailPage(String linkUrl) {

        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                .setToolbarColor(getResources().getColor(R.color.colorPrimary))
                .build();

        customTabsIntent.launchUrl(this, Uri.parse(linkUrl));
    }

    /**
     * 영화 리뷰 남기는 다이얼로그 설정
     */
    @Override
    public void startReviewPage(Movie movie) {
        Bundle bundle = new Bundle();
        bundle.putString("link", movie.getLinkUrl());
        bundle.putString("imgUrl", movie.getImageUrl());
        bundle.putString("title", movie.getTitle());

        ReviewDialog reviewDialog = new ReviewDialog();
        reviewDialog.setArguments(bundle);
        reviewDialog.setListener(presenter);
        reviewDialog.show(getSupportFragmentManager(), "review");
    }

    public static Context getContext() {
        return context;
    }
}
