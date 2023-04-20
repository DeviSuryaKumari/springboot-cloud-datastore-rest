package com.example.springbootclouddatastorerest;

import com.google.cloud.spring.data.datastore.core.mapping.Entity;
import org.springframework.data.annotation.Id;

@Entity(name = "Plan")
public class Plan {
    @Id
    Long id;
    String title;
    Double budget;

    public Plan(Long id, String title, Double budget) {
        this.id = id;
        this.title = title;
        this.budget = budget;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

}
