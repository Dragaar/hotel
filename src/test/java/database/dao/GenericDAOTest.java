package database.dao;

import com.rosivanyshyn.db.dao.GenericDAO;
import com.rosivanyshyn.db.manager.DBManager;
import com.rosivanyshyn.db.transaction.TransactionManager;

import org.apache.log4j.lf5.DefaultLF5Configurator;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/** Implementation of base DAO test functionality
 *  <p>Methods should be used in logical data operation order
 *  <br>insert -> get -> update -> delete
 * @param <T> Type of Entity
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class GenericDAOTest<T> {
    Connection connection;
    T entity = setEntity();
    GenericDAO<T> genericDAO = setDAO();
    //Are we need to activate the DB cleaning mechanism after each test
    protected boolean cleanDB = true;

    /** Set DAO implementation with the right type
     * @return DAO implementation
     */
    abstract protected GenericDAO<T> setDAO();

    /** Set entity with the right type
     * @return Empty entity
     */
    abstract protected T setEntity();

    /** Get Entity ID**/
    abstract protected Long getEntityId(T entity);

    @BeforeAll
    protected void globalSetUp() throws IOException {
        DefaultLF5Configurator.configure();
        connection = DBManager.getConnection();
    }

    /** Logic for insert test
     * @param insertNewEntityBuilder Entity pattern, which will be using during all next DAO tests cases
     */
    protected void insertTestLogic(BuildEntity<T> insertNewEntityBuilder){
        connection = DBManager.getConnection();
        entity = insertNewEntityBuilder.buildEntity();

        boolean result = (Boolean) TransactionManager.execute(connection,
                ()-> genericDAO.insert(connection, entity)
        );

        assertTrue(result);
    }

    /** Logic for get test */
    protected void getTestLogic() {
        connection = DBManager.getConnection();

        @SuppressWarnings("unchecked")
        T getEntity = (T) TransactionManager.execute(connection,
                ()-> genericDAO.get(connection, getEntityId(entity)));

        assertEquals(entity, getEntity);
    }

    protected void getByFieldTestLogic(String fieldName, Object value) {
        connection = DBManager.getConnection();

        @SuppressWarnings("unchecked")
        T getEntity = (T) TransactionManager.execute(connection,
                ()-> genericDAO.getByField(connection, fieldName, value));


        assertEquals(entity, getEntity);
    }

    /** Logic for getAll test */
    protected void getAllTestLogic() {
        connection = DBManager.getConnection();
        boolean result = true;

        @SuppressWarnings("unchecked")
        ArrayList<T> getEntities = (ArrayList<T>) TransactionManager.execute(connection,
                ()-> genericDAO.getAll(connection));

        for(T e : getEntities){
            if (e == null) {
                result = false;
                break;
            }
        }

        assertTrue(result);
    }

    /** Logic for update test
     * @param updateEntityBuilder Entity pattern, which test and update general DAO tests entity
     */
    protected void updateTestLogic(BuildEntity<T> updateEntityBuilder) {
        T entityUpdated = updateEntityBuilder.buildEntity();
        connection = DBManager.getConnection();

        boolean result = (Boolean) TransactionManager.execute(connection,
                ()-> genericDAO.update(connection, entityUpdated));

        if(result) entity = entityUpdated;

        assertTrue(result);
    }

    /** Logic for delete test */
    protected void deleteTestLogic(){
        connection = DBManager.getConnection();
        boolean result = (Boolean) TransactionManager.execute(connection,
                ()-> genericDAO.delete(connection, getEntityId(entity))
        );
        assertTrue(result);
    }

    @AfterEach
    protected void clear(){
        if(cleanDB) {
            connection = DBManager.getConnection();
            boolean result = (Boolean) TransactionManager.execute(connection,
                    () -> {
                        if (genericDAO.get(connection, getEntityId(entity)) != null) {
                            return genericDAO.delete(connection, getEntityId(entity));
                        } else return true;
                    });
            assertTrue(result);
        }
    }

    /** Responsible for filling a specific entity with data
     * @param <T> Type of Entity
     */
    @FunctionalInterface
    protected interface BuildEntity <T> {
        /** Example -
         * <code>
         * <br> Account.builder()
         * <br> .id(entity.getId())
         * <br> .role(entity.getRole())
         * <br> .firstName(entity.getFirstName())
         * <br> .build();
         *</code>
         * @return T - Filled Entity
         */
        T buildEntity();
    }
}
