package s0566430.songsMS.repository;

import s0566430.songsMS.model.Song;

import javax.persistence.PersistenceException;
import java.util.List;

public interface SongDao {
    int saveSong(Song song) throws PersistenceException;

    /**@param id the song id
     * @return the Song object corresponding to the id from the database or <code>null</code> if the id is unassigned
     */
    Song findSong(int id) throws PersistenceException;
    List<Song> findAllSongs() throws PersistenceException;

    /**@param song the Song object with the information that is to be updated in the existing one
     */
    void updateSong(Song song) throws PersistenceException, IndexOutOfBoundsException;

    /**@param id the given id of the Song that is to be deleted
     */
    void deleteSong(int id) throws PersistenceException, IndexOutOfBoundsException;
}
