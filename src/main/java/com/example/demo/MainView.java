package com.example.demo;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.Key;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.flow.signals.shared.SharedListSignal;
import com.vaadin.flow.component.icon.VaadinIcon;

@Route("")
public class MainView extends VerticalLayout {
    ValueSignal<String> createTaskSignal = new ValueSignal<>("");
    
    static SharedListSignal<Todo> todos = new SharedListSignal<>(Todo.class);
    
    public MainView(TodoRepo repo) {
        add(new H1("Hello, Vaadin!"));
        if (todos.peek().isEmpty()) 
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
            Signal <Boolean> doneSignal = todoSignal.map(Todo::isDone);           
            Checkbox doneBox = new Checkbox();
            doneBox.bindValue(doneSignal, done->{
               todoSignal.update(todo -> {
                    todo.setDone(done);                   
                    return repo.save(todo);
                });
            });    
            Span taskSpan = new Span(todoSignal.map(Todo::getTask));
            taskSpan.getStyle().bind("text-decoration", () -> doneSignal.get() ? "line-through" : "none");
            Button removeButton = new Button(VaadinIcon.TRASH.create());
            removeButton.addClickListener(click -> {
                repo.delete(todoSignal.peek());
                todos.remove(todoSignal);
            });
            removeButton.addThemeVariants(ButtonVariant.SMALL, ButtonVariant.TERTIARY);
            HorizontalLayout layout = new HorizontalLayout(doneBox, taskSpan, removeButton);
            layout.setAlignItems(Alignment.BASELINE);
            return layout;
              });
    
        Span summary = new Span(() -> "Remaining tasks: " + todos.getValues().filter(todo -> !todo.isDone()).count());
        add(createTaskField, createTaskButton, todosLayout, summary);
      
    }
    
}
