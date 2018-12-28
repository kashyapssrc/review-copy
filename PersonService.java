package com.ofs.training.jsp.demo.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ofs.demo.util.AppException;
import com.ofs.demo.util.BeanFactory;
import com.ofs.demo.util.ErrorCodes;
import com.ofs.demo.util.QueryManager;
import com.ofs.training.demo.model.Person;

public class PersonService implements QueryManager {

    AddressService addressService = new AddressService();

    private void validate(Person person, Connection conn) {

        List<ErrorCodes> errors = new ArrayList<>();

        if (isEmpty(person.getFirstName())) {
            errors.add(ErrorCodes.INVALID_FIRST_NAME);
        }

        if (isEmpty(person.getLastName())) {
            errors.add(ErrorCodes.INVALID_LAST_NAME);
        }

        if (isEmpty(person.getEmail())) {
            errors.add(ErrorCodes.INVALID_EMAIL);
        }

        if (person.getBirthDate() == null) {
            errors.add(ErrorCodes.INVALID_BIRTH_DATE);
        }

        try {
            validateName(person, conn);
        } catch (AppException e) {
            errors.addAll(e.getErrorCodes());
        }

        try {
            validateEmail(person, conn);
        } catch (AppException e) {
            errors.addAll(e.getErrorCodes());
        }

        if (errors.size() > 0) {
            throw new AppException(errors);
        }
    }

    private boolean isEmpty(String value) {
        return Objects.isNull(value) || "".equals(value);
    }

    private void validateName(Person person, Connection conn) {

        try {
            String query = QueryManager.COUNT_PERSON_BY_NAME;
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, person.getFirstName());
            statement.setString(2, person.getLastName());
            ResultSet executeQuery = statement.executeQuery();
            executeQuery.next();
            int count = executeQuery.getInt(1);
            if (count > 0) {
                List<ErrorCodes> errors = new ArrayList<>();
                errors.add(ErrorCodes.INVALID_NAME);
                throw new AppException(errors);
            }
        } catch (SQLException exception) {
             throw new AppException(ErrorCodes.DATABASE_ERROR, exception.getCause());
        }
    }

    private void validateEmail(Person person, Connection conn) {

        try {
            String query = QueryManager.COUNT_PERSON_BY_EMAIL;
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, person.getEmail());
            ResultSet executeQuery = statement.executeQuery();
            executeQuery.next();
            int count = executeQuery.getInt(1);
            if (count > 0) {
                List<ErrorCodes> errors = new ArrayList<>();
                errors.add(ErrorCodes.DUPLICATE_EMAIL);
                throw new AppException(errors);
            }
        } catch (SQLException e) {
            throw new AppException(ErrorCodes.DATABASE_ERROR, e.getCause());
        }
    }

    private void validateId(long id, Connection conn) {

        if (id == 0) {
            List<ErrorCodes> errors = new ArrayList<>();
            errors.add(ErrorCodes.INVALID_PERSON_ID);
            throw new AppException(errors);
        }
    }

    private void constructPerson(Person person, ResultSet result) {

        try {
            person.setId         (result.getLong("id"));
            person.setFirstName  (result.getString("first_name"));
            person.setLastName   (result.getString("last_name"));
            person.setEmail      (result.getString("email"));
            person.setBirthDate  (result.getDate("birth_date"));
            person.setAdmin       (result.getBoolean("is_admin"));
        } catch (SQLException e) {
            throw new AppException(ErrorCodes.DATABASE_ERROR, e);
        }
    }


    private void setValue(Person person, PreparedStatement statement) {

        try {
            statement.setString(1, person.getFirstName());
            statement.setString(2, person.getLastName());
            statement.setString(3, person.getEmail());
            statement.setDate(4, person.getBirthDate());
            statement.setString(5, person.getPassword());
            statement.setBoolean(6, person.isAdmin());
        } catch (Exception e) {
            throw new AppException(ErrorCodes.DATABASE_ERROR, e.getCause());
        }
    }

    public Person create(Person person, Connection conn) {

        long generatedKey = 0;

        String insertQuery = QueryManager.CREATE_PERSON;
        try {
            validate(person, conn);

            PreparedStatement statement = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            setValue(person, statement);
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if ((generatedKeys != null) && (generatedKeys.next())) {
                generatedKey = generatedKeys.getLong(1);
            }
            person.setId(generatedKey);
        } catch (SQLException e) {
            throw new AppException(ErrorCodes.DATABASE_ERROR, e.getCause());
        }
        return person;
    }

    public Person update(Person person, Connection conn) {

        String query = QueryManager.UPDATE_PERSON;

        try {
            validateId(person.getId(), conn);

            PreparedStatement statement = conn.prepareStatement(query);
            setValue(person, statement);
            statement.setLong  (8, person.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new AppException(ErrorCodes.DATABASE_ERROR, e.getCause());
        }
        return person;
    }

    public Person read(long id, Connection connection) {

        String readQuery = QueryManager.READ_PERSON;
        Person person = BeanFactory.getPerson();
        try {
            validateId(id, connection);
            PreparedStatement statement = connection.prepareStatement(readQuery);
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                constructPerson(person, rs);
            }
            return person;
        } catch (SQLException e) {
            throw new AppException(ErrorCodes.DATABASE_ERROR, e);
        }
    }

    public List<Person> readAll(Connection conn) {

        String readAllQuery = QueryManager.READ_ALL_PERSON;
        List<Person> resultRecord = new ArrayList<>();
        Person person;

        try {
            PreparedStatement statement = conn.prepareStatement(readAllQuery);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                person = BeanFactory.getPerson();
                constructPerson(person, result);
                resultRecord.add(person);
            }
        } catch (SQLException e) {
            throw new AppException(ErrorCodes.DATABASE_ERROR, e.getCause());
        }
        return resultRecord;
    }

    public Person delete(long id, Connection conn) {

        String deleteQuery = QueryManager.DELETE_PERSON;

        try {
            validateId(id,conn);
            Person person = read(id, conn);
            PreparedStatement statement = conn.prepareStatement(deleteQuery.toString());
            statement.setLong(1, id);
            statement.executeUpdate();
            return person;
        } catch (SQLException e) {
            throw new AppException(ErrorCodes.DATABASE_ERROR, e.getCause());
        }
    }
}

