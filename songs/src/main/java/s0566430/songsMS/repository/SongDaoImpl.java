package s0566430.songsMS.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import s0566430.songsMS.model.Song;

import javax.persistence.PersistenceException;
import java.util.List;

@Repository
@Transactional
public class SongDaoImpl implements SongDao {

    @Autowired
    private SessionFactory sessionFactory;

    public SongDaoImpl() {
    }

    @Override
    public int saveSong(Song song) throws PersistenceException {
        try {
            return (int) sessionFactory.getCurrentSession().save(song);
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Song findSong(int id) throws PersistenceException {
        try {
            return sessionFactory.getCurrentSession().get(Song.class, id);
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<Song> findAllSongs() throws PersistenceException {
        try {
            return sessionFactory.getCurrentSession().createQuery("SELECT u FROM Song u").getResultList();
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void updateSong(Song song) throws PersistenceException, IndexOutOfBoundsException {

        try {
            if (findSong(song.getId()) == null)
                throw new IndexOutOfBoundsException("song to be updated does not exist (invalid id: "+song.getId()+")");
            sessionFactory.getCurrentSession().clear();
            sessionFactory.getCurrentSession().update(song);
        } catch (IndexOutOfBoundsException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void deleteSong(int id) throws PersistenceException, IndexOutOfBoundsException {
        try {
            if (findSong(id) == null)
                throw new IndexOutOfBoundsException("song to be deleted does not exist (invalid id: "+id+")");
            Session s = sessionFactory.getCurrentSession();
            s.clear();
            s.delete(s.get(Song.class, id));
        } catch (IndexOutOfBoundsException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }
}
