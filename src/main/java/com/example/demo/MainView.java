package com.example.demo;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.Key;
//import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
//import com.vaadin.flow.component.html.Image;
//import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
// import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ListSignal;
//import com.vaadin.flow.dom.Element;
//import com.vaadin.flow.server.streams.DownloadHandler;
//import java.io.IOException;
//import java.lang.Math;
import com.vaadin.flow.signals.local.ValueSignal;

@Route("")
public class MainView extends VerticalLayout {
    // ValueSignal<String> nameSignal = new ValueSignal<>("");
    ValueSignal<String> createTaskSignal = new ValueSignal<>("");
    public MainView(TodoRepo repo) {
        add(new H1("Hello, Vaadin!"));
        // TextField nameField = new TextField("Your name");
        // nameField.setValueChangeMode(ValueChangeMode.EAGER);
        // nameField.bindValue(nameSignal, nameSignal::set);
        // Span greeting = new Span(() -> "Hello, " + nameSignal.get() + "!");
        // greeting.bindVisible(() -> !nameSignal.get().isBlank());
        // add(nameField, greeting);
        ListSignal<Todo> todos = new ListSignal<>();

        repo.findAll().forEach(todos::insertLast);

        TextField createTaskField = new TextField("Task");
        createTaskField.setValueChangeMode(ValueChangeMode.EAGER);        
        createTaskField.bindValue(createTaskSignal, createTaskSignal::set);
        
        Button createTaskButton = new Button("Create Task");
        createTaskButton.addClickShortcut(Key.ENTER).listenOn(createTaskField);
        createTaskButton.addClickListener(click -> {
            Todo todo = new Todo();
            todo.setTask(createTaskSignal.peek());
            repo.save(todo);
            todos.insertLast(todo);
            createTaskSignal.set("");      
        });         
        createTaskButton.bindEnabled(() -> !createTaskSignal.get().isBlank());

        VerticalLayout todosLayout = new VerticalLayout();
        todosLayout.bindChildren(todos, todoSignal -> {
            return new Span(todoSignal.map(Todo::getTask));
        });
    
        Span summary = new Span(() -> "Remaining tasks: " + todos.getValues().filter(todo -> !todo.isDone()).count());
        add(createTaskField, createTaskButton, todosLayout, summary);
      
    }
    
}
