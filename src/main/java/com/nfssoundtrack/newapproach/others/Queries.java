package com.nfssoundtrack.newapproach.others;

import com.nfssoundtrack.newapproach.model2.Songs;
import org.hibernate.query.SelectionQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.nfssoundtrack.newapproach.others.MiscHelper.sessionFactory;

public class Queries {

    public static List<Songs> getSongsByTitle(String title){
        var songs = sessionFactory.fromTransaction(session -> {
            try {
                return session
                    .createSelectionQuery("from Songs where upper(title) like :title", Songs.class)
                    .setParameter("title", title.toUpperCase())
                    .setMaxResults(20)
                    .getResultList();
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        });
        return songs;
    }

    public static List<Songs> getSongsByGame(String game){
        var songs = sessionFactory.fromTransaction(session -> {
            try {
                return session
                        .createSelectionQuery("from Songs s INNER JOIN s.games " +
                                "where trim(concat(upper(prefix),' ', upper(gametitle))) like :game", Songs.class)
                        .setParameter("game", game.toUpperCase())
                        .setMaxResults(20)
                        .getResultList();
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        });
        return songs;
    }
    //title:cos
    public static List<Songs> getSongsByConditions(String... myArgs){
        StringBuilder appendableQuery = new StringBuilder("from Songs");
        Optional<String> bandCondition = Arrays.stream(myArgs).filter(s -> s.startsWith("band")).findFirst();
        Optional<String> titleCondition = Arrays.stream(myArgs).filter(s -> s.startsWith("title")).findFirst();
        Optional<String> gameCondition = Arrays.stream(myArgs).filter(s -> s.startsWith("game")).findFirst();
        var songs = sessionFactory.fromTransaction(session -> {
            try {
                String band=null;
                String title=null;
                String game=null;
                boolean whereAppended=false;
                if (gameCondition.isPresent()){
                    game = gameCondition.get().split(":")[1];
                    appendableQuery.append(" INNER JOIN games where trim(concat(upper(prefix),' ', upper(gametitle))) like :game");
                    whereAppended=true;
                }
                if (bandCondition.isPresent()){
                    band = bandCondition.get().split(":")[1];
                    if (!whereAppended){
                        appendableQuery.append(" where upper(band) like :band");
                    } else {
                        appendableQuery.append(" and upper(band) like :band");
                    }
                    whereAppended=true;
                }
                if (titleCondition.isPresent()){
                    title = titleCondition.get().split(":")[1];
                    if (!whereAppended){
                        appendableQuery.append(" where upper(title) like :title");
                    } else {
                        appendableQuery.append(" and upper(title) like :title");
                    }
                    whereAppended=true;
                }
                if (!whereAppended){
                    appendableQuery.append(" where src_id != '0'");
                } else {
                    appendableQuery.append(" and src_id != '0'");
                }
                SelectionQuery<Songs> query = session.createSelectionQuery(appendableQuery.toString(), Songs.class);
                if (bandCondition.isPresent()){
                    query.setParameter("band",band);
                }
                if (titleCondition.isPresent()){
                    query.setParameter("title",title);
                }
                if (gameCondition.isPresent()){
                    query.setParameter("game",game);
                }
                return query.getResultList();
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        });
        return songs;
    }

    public static List<Songs> getRandomSong(){
        var songs = sessionFactory.fromTransaction(session -> {
            try {
                return session
                        .createSelectionQuery("from Songs s where src_id != '0' order by rand() limit 1", Songs.class)
                        .getResultList();
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        });
        return songs;
    }

    public static Songs getSongsBySrcId(String src_id){
        var songs = sessionFactory.fromTransaction(session -> {
            try {
                return session
                        .createSelectionQuery("from Songs where upper(src_id) like :src_id", Songs.class)
                        .setParameter("src_id", src_id.toUpperCase())
                        .getSingleResult();
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        });
        return songs;
    }

    public static List<Songs> getSongsById(Integer id){
        var songs = sessionFactory.fromTransaction(session -> {
            try {
                return session
                        .createSelectionQuery("from Songs where id = :id", Songs.class)
                        .setParameter("id", id)
                        .getResultList();
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        });
        return songs;
    }

    //select * from songs join games on songs.gameid=games.id join series on series.id=games.series
    //where games.id in (1,2,3) and songs.id in (100,2000,3);
    public static List<Songs> getSongsByFilter(String... myArgs){
        StringBuilder appendableQuery = new StringBuilder("from Songs as s");
        Optional<String> seriesCondition = Arrays.stream(myArgs).filter(s -> s!=null && s.startsWith("series.ids")).findFirst();
        Optional<String> gamesCondition = Arrays.stream(myArgs).filter(s -> s!=null && s.startsWith("games.ids")).findFirst();
        Optional<String> songsCondition = Arrays.stream(myArgs).filter(s -> s!=null && s.startsWith("songs.ids")).findFirst();
        var songs = sessionFactory.fromTransaction(session -> {
            try {
                boolean whereAppended=false;
                if (seriesCondition.isPresent()){
                    appendableQuery.append(" INNER JOIN games");
                }
                if (gamesCondition.isPresent()){
                    appendableQuery.append(" INNER JOIN games.series");
                }
                if (seriesCondition.isPresent()){
                    appendableQuery.append(" where series.id in (:series)");
                    whereAppended=true;
                }
                if (gamesCondition.isPresent()){
                    if (whereAppended){
                        appendableQuery.append(" and games.id in (:games)");
                    }
                    else {
                        appendableQuery.append(" where games.id in (:games)");
                    }
                    whereAppended=true;
                }
                if (songsCondition.isPresent()){
                    if (whereAppended){
                        appendableQuery.append(" and s.id in (:songs)");
                    } else {
                        appendableQuery.append(" where s.id in (:songs)");
                    }
                }
                SelectionQuery query = session.createSelectionQuery(appendableQuery.toString(), Songs.class);
                if (seriesCondition.isPresent()){
                    String[] ids = (seriesCondition.get().split(":")[1].split(","));
                    List<Integer> ints = new ArrayList<>();
                    for (String str : ids){
                        ints.add(Integer.parseInt(str));
                    }
                    query.setParameterList("series",ints);
                }
                if (gamesCondition.isPresent()){
                    String[] ids = (gamesCondition.get().split(":")[1].split(","));
                    List<Long> ints = new ArrayList<>();
                    for (String str : ids){
                        ints.add(Long.parseLong(str));
                    }
                    query.setParameterList("games",ints);
                }
                if (songsCondition.isPresent()){
                    String[] ids = (songsCondition.get().split(":")[1].split(","));
                    List<Long> ints = new ArrayList<>();
                    for (String str : ids){
                        ints.add(Long.parseLong(str));
                    }
                    query.setParameterList("songs",ints);
                }
                return query.getResultList();
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        });
        return songs;
    }

}
