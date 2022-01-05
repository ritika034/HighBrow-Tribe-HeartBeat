package com.example.Tribes.Model;

import com.example.Tribes.Repo.QuestionsRepo;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Questions {
    @Id
    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private String question;
    public Questions() {

    }
    public Questions(int id, String question) {
        this.id = id;
        this.question = question;
    }


}
