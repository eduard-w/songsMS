package s0566430.songsMS;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import s0566430.songsMS.model.User;

import javax.persistence.PersistenceException;

@Repository
@Transactional
public class UserDaoImpl implements UserDao{

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public User findUser(String userId) throws PersistenceException {
        try {
            return sessionFactory.getCurrentSession().get(User.class, userId);
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }
}
