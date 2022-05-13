package s0566430.songsMS;

import s0566430.songsMS.model.User;

import javax.persistence.PersistenceException;

public interface UserDao {

    /**@param userId the user id
     * @return the s0566430.songsMS.model.User object corresponding to the user id from the database or <code>null</code> if the given id is unassigned
     */
    User findUser(String userId) throws PersistenceException;
}
