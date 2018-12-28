package com.ofs.training.jsp.demo.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ofs.demo.util.BeanFactory;
import com.ofs.demo.util.ConnectionManager;
import com.ofs.demo.util.JsonUtil;
import com.ofs.training.demo.model.Person;
import com.ofs.training.jsp.demo.service.PersonService;

public class PersonServlet extends HttpServlet {

        boolean flag;
        PersonService personService = BeanFactory.getPersonService();

        @Override
        protected void doGet(HttpServletRequest request,
                             HttpServletResponse response) throws IOException, ServletException {

            PrintWriter out = response.getWriter();
            response.setContentType("text/html");

            String personId = request.getParameter("id");
            String includeAddress = request.getParameter("addr");
            Connection connection = ConnectionManager.getConnection();

            if (personId != null) {

                Person person = personService.read(Long.parseLong(personId), connection);
            } else {
                List<Person> personList = personService.readAll(connection);
                StringBuilder personTable = new StringBuilder();
                for(Person everyPerson : personList) {

                    personTable.append("<tr>")
                               .append("<td>")
                               .append(everyPerson.getId())
                               .append("</td>")
                               .append("<td>")
                               .append(everyPerson.getFirstName())
                               .append("</td>")
                               .append("<td>")
                               .append(everyPerson.getLastName())
                               .append("</td>")
                               .append("<td>")
                               .append(everyPerson.getEmail())
                               .append("</td>")
                               .append("<td>")
                               .append(everyPerson.getBirthDate())
                               .append("</td>")
                               .append("</tr>");
                }
                out.write(personTable.toString());
            }
            out.close();
        }

        @Override
        protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

            PrintWriter out = response.getWriter();
            response.setContentType("application/json");

            Connection connection = ConnectionManager.getConnection();

            BufferedReader reader = request.getReader();
            List<String> jsonLines = reader.lines()
                                           .collect(Collectors.toList());

            String personJson = String.join("", jsonLines);
            String.format("INPUT JSON >> %s", personJson);

            Person input = JsonUtil.toObject(personJson, Person.class);
            Person person = personService.create(input, connection);
            out.close();
        }

        @Override
        protected void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException {


            response.setContentType("application/json");
            PrintWriter out = response.getWriter();

            String operation = request.getParameter("op");
            Connection connection = ConnectionManager.getConnection();

            if (operation.equals("update")) {

                BufferedReader reader = request.getReader();
                List<String> jsonLines = reader.lines()
                                               .collect(Collectors.toList());

                String personJson = String.join("", jsonLines);
                System.out.format("Input JSON >> %s", personJson);

                Person input = JsonUtil.toObject(personJson, Person.class);
                Person person = personService.update(input, connection);
                out.write(JsonUtil.toJson(person));

            } else if (operation.equals("delete")) {

                BufferedReader reader = request.getReader();
                List<String> jsonLines = reader.lines()
                                               .collect(Collectors.toList());

                String personJson = String.join("", jsonLines);
                System.out.format("Input JSON >> %s", personJson);

                Person input = JsonUtil.toObject(personJson, Person.class);
                personService.delete(input.getId(), connection);
            }
            out.close();
        }
}
