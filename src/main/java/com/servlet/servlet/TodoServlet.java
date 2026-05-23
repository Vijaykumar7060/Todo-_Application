package com.servlet.servlet;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.servlet.Entity.Todo;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/todos", "/todo/*"})
public class TodoServlet extends HttpServlet {

    private static final String TODOS_ATTR = "todos";
    private static final String ID_GEN_ATTR = "idGenerator";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        List<Todo> todos = getTodos();
        
        System.out.println("=== doGet called | Todos count: " + todos.size() + " ===");

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Todo App</title>");
        out.println("<style>");
        out.println("body{font-family:Arial;margin:40px;}");
        out.println("input, textarea, button{padding:8px;margin:5px;}");
        out.println("div.todo {border:1px solid #ddd;padding:12px;margin:10px 0;border-radius:5px;}");
        out.println("</style>");
        out.println("</head><body>");
        
        out.println("<h1>📋 My Todos</h1>");
        out.println("<p><a href='todos'>🏠 Home</a> | <a href='todos?action=addForm'>➕ Add New Todo</a></p><hr>");

        if ("addForm".equals(req.getParameter("action"))) {
            showAddForm(out);
        } else {
            showTodoList(out, todos);
        }
        
        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String action = req.getParameter("action");
        System.out.println("=== doPost called with action: " + action + " ===");

        if ("add".equals(action)) {
            String title = req.getParameter("title");
            String desc = req.getParameter("description");
            
            System.out.println("Title received: '" + title + "'");
            System.out.println("Description received: '" + desc + "'");

            if (title != null && !title.trim().isEmpty()) {
                int newId = getNextId();
                Todo todo = new Todo(newId, title.trim(), desc);
                getTodos().add(todo);
                
                System.out.println("✅ SUCCESS: Todo added with ID " + newId + " | Total todos: " + getTodos().size());
            } else {
                System.out.println("❌ FAILED: Title is empty");
            }
        } 
        else if ("toggle".equals(action)) {
            int id = Integer.parseInt(req.getParameter("id"));
            for (Todo t : getTodos()) {
                if (t.getId() == id) {
                    t.setCompleted(!t.isCompleted());
                    System.out.println("Toggled todo ID: " + id);
                    break;
                }
            }
        } 
        else if ("delete".equals(action)) {
            int id = Integer.parseInt(req.getParameter("id"));
            getTodos().removeIf(t -> t.getId() == id);
            System.out.println("Deleted todo ID: " + id);
        }

        // === Critical Fix: Use full URL for redirect ===
        String redirectURL = req.getContextPath() + "/todos";
        System.out.println("Redirecting to: " + redirectURL);
        resp.sendRedirect(redirectURL);
    }

    private List<Todo> getTodos() {
        ServletContext context = getServletContext();
        List<Todo> todos = (List<Todo>) context.getAttribute(TODOS_ATTR);
        if (todos == null) {
            todos = new ArrayList<>();
            context.setAttribute(TODOS_ATTR, todos);
            System.out.println("Created new todos list in ServletContext");
        }
        return todos;
    }

    private int getNextId() {
        ServletContext context = getServletContext();
        AtomicInteger idGen = (AtomicInteger) context.getAttribute(ID_GEN_ATTR);
        if (idGen == null) {
            idGen = new AtomicInteger(1);
            context.setAttribute(ID_GEN_ATTR, idGen);
        }
        return idGen.getAndIncrement();
    }

    private void showTodoList(PrintWriter out, List<Todo> todos) {
        out.println("<h2>All Todos (" + todos.size() + ")</h2>");
        
        if (todos.isEmpty()) {
            out.println("<p><i>No todos yet. Add some above!</i></p>");
            return;
        }
        
        for (Todo todo : todos) {
            out.println("<div class='todo'>");
            out.println("<h3>" + todo.getTitle() + 
                       (todo.isCompleted() ? " <span style='color:green'>(✓ Completed)</span>" : "") + "</h3>");
            
            if (todo.getDescription() != null && !todo.getDescription().isEmpty()) {
                out.println("<p>" + todo.getDescription() + "</p>");
            }

            out.println("<form style='display:inline;' method='post'>");
            out.println("<input type='hidden' name='id' value='" + todo.getId() + "'>");
            out.println("<input type='hidden' name='action' value='toggle'>");
            out.println("<button type='submit'>" + 
                       (todo.isCompleted() ? "Mark Incomplete" : "Mark Complete") + "</button>");
            out.println("</form> ");

            out.println("<form style='display:inline;' method='post'>");
            out.println("<input type='hidden' name='id' value='" + todo.getId() + "'>");
            out.println("<input type='hidden' name='action' value='delete'>");
            out.println("<button type='submit' style='color:red'>🗑 Delete</button>");
            out.println("</form>");
            out.println("</div>");
        }
    }

    private void showAddForm(PrintWriter out) {
        out.println("<h2>➕ Add New Todo</h2>");
        out.println("<form method='post' action='todos'>");   // Explicit action added
        out.println("Title: <br><input type='text' name='title' required style='width:400px'><br><br>");
        out.println("Description: <br><textarea name='description' rows='4' cols='50'></textarea><br><br>");
        out.println("<input type='hidden' name='action' value='add'>");
        out.println("<button type='submit' style='padding:10px 20px'>Add Todo</button>");
        out.println("</form>");
        out.println("<br><a href='todos'>← Back to List</a>");
    }
}
