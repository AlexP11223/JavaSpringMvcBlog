package alexp.blog.utils;

import com.github.springtestdbunit.operation.DatabaseOperationLookup;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.operation.DefaultDatabaseOperationLookup;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;

import java.sql.SQLException;
import java.sql.Statement;

public class HsqldbSequenceResetter implements DatabaseOperationLookup {

    public static final org.dbunit.operation.DatabaseOperation SEQUENCE_RESETTER = new org.dbunit.operation.DatabaseOperation() {
        @Override
        public void execute(IDatabaseConnection connection, IDataSet dataSet) throws DatabaseUnitException, SQLException {
            String[] tables = dataSet.getTableNames();
            Statement statement = connection.getConnection().createStatement();
            for (String table : tables) {
                statement.execute("TRUNCATE TABLE " + table + " RESTART IDENTITY AND COMMIT NO CHECK");

            }
        }
    };

    @Override
    public org.dbunit.operation.DatabaseOperation get(DatabaseOperation databaseOperation) {
        if (databaseOperation == DatabaseOperation.TRUNCATE_TABLE) {
            return SEQUENCE_RESETTER;
        }

        return new DefaultDatabaseOperationLookup().get(databaseOperation);
    }
}
