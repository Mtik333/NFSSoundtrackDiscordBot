package com.nfssoundtrack.newapproach.others;

import com.nfssoundtrack.newapproach.model2.Songs;
import org.hibernate.annotations.processing.HQL;
import org.hibernate.query.Page;

import java.util.List;

public interface Queries2 {

    @HQL("where upper(title) like :title order by title")
    List<Songs> findSongsByTitleWithPagination(String title, Page page);

    @HQL("where upper(title) like :title and upper(band) like :band")
    List<Songs> findSongsByTitleAndBandWithPagination(String title, String band, Page page);

}
