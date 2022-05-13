package s0566430.songsMS.repository;

import s0566430.songsMS.model.SongList;

import javax.persistence.PersistenceException;
import java.util.List;

public interface SongListDao {

    List<SongList> findAllPublicListsByUserId(String id) throws PersistenceException;

    List<SongList> findAllListsByUserId(String id) throws PersistenceException;

    /**@param id the songlist id
     * @return the SongList object corresponding to the id from the database or <code>null</code> if the id is unassigned
     */
    SongList findListById(Integer id) throws PersistenceException;

    Void updateList(SongList songList) throws PersistenceException, IndexOutOfBoundsException;

    void deleteList(Integer id) throws PersistenceException, IndexOutOfBoundsException;

    int saveList(SongList songList) throws PersistenceException;
}
